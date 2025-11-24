package fi.laalo.fueltracker.service;

import fi.laalo.fueltracker.model.FuelEntry;
import fi.laalo.fueltracker.model.User;
import fi.laalo.fueltracker.model.Vehicle;
import fi.laalo.fueltracker.repository.FuelEntryRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class FuelAnalyticsService {

    private final FuelEntryRepository fuelEntryRepository;

    public FuelAnalyticsService(FuelEntryRepository fuelEntryRepository) {
        this.fuelEntryRepository = fuelEntryRepository;
    }

    /**
     * Filter and validate fuel entries:
     * 1. Sort by date ascending
     * 2. Remove entries where odometer < previous odometer (invalid data)
     * 3. Skip entries with missing odometers
     */
    private List<FuelEntry> getValidEntries(List<FuelEntry> entries) {
        // 1. Sort all fuel entries by date ascending
        List<FuelEntry> sortedEntries = entries.stream()
                .filter(e -> e.getOdometer() != null) // Skip entries with missing odometers
                .sorted(Comparator.comparing(FuelEntry::getDateTime))
                .collect(Collectors.toList());

        // 2. Remove any entry where odometer < previous odometer
        List<FuelEntry> validEntries = new ArrayList<>();
        for (FuelEntry entry : sortedEntries) {
            if (validEntries.isEmpty()) {
                validEntries.add(entry);
            } else {
                FuelEntry lastEntry = validEntries.get(validEntries.size() - 1);
                if (entry.getOdometer() >= lastEntry.getOdometer()) {
                    validEntries.add(entry);
                }
                // Skip entries with decreasing odometer
            }
        }
        return validEntries;
    }

    /**
     * Calculate consumption cycles using tank capacity logic
     * Core rule: Tank cannot physically contain more than tankCapacityLiters
     * remainingFuel must NEVER exceed tankCapacity
     */
    private List<ConsumptionCycle> calculateConsumptionCycles(List<FuelEntry> validEntries, Vehicle vehicle) {
        if (vehicle.getTankCapacityLiters() == null || vehicle.getTankCapacityLiters() <= 0) {
            return new ArrayList<>(); // Cannot calculate without tank capacity
        }

        List<ConsumptionCycle> cycles = new ArrayList<>();
        double remainingFuel = 0.0; // amount of fuel estimated currently in tank
        Double previousOdometer = null; // odometer at last full tank
        LocalDateTime previousFullDate = null; // date at last full tank
        double accumulatedFuel = 0.0; // fuel accumulated since last full tank
        double tankCapacity = vehicle.getTankCapacityLiters();

        for (FuelEntry entry : validEntries) {
            double liters = entry.getLitres();
            double odo = entry.getOdometer();

            // Accumulate fuel added since last full tank
            accumulatedFuel += liters;

            // If tank is NOT full yet
            if (remainingFuel + liters < tankCapacity) {
                remainingFuel += liters;
                continue; // Continue to next entry
            }

            // Tank GETS FULL now
            // consumedSinceLastFull = fuel required to reach full capacity = tankCapacity - remainingFuel
            double consumedFuel = tankCapacity - remainingFuel;
            
            // If consumedFuel is 0 or negative, it means tank was already full
            // In this case, use the accumulated fuel since the last full tank
            if (consumedFuel <= 0 && previousOdometer != null) {
                // Tank was already full, so use accumulated fuel as consumed fuel
                consumedFuel = accumulatedFuel;
            }

            // Distance driven since last full tank
            if (previousOdometer != null && previousFullDate != null && consumedFuel > 0) {
                double distance = odo - previousOdometer;
                if (distance > 0) {
                    double consumption = (consumedFuel / distance) * 100; // L/100km
                    cycles.add(new ConsumptionCycle(
                        previousOdometer,
                        odo,
                        previousFullDate,
                        entry.getDateTime(),
                        distance,
                        consumedFuel,
                        consumption
                    ));
                }
            }

            // Update state
            previousOdometer = odo;
            previousFullDate = entry.getDateTime();
            remainingFuel = tankCapacity; // Tank is now full
            accumulatedFuel = 0.0; // Reset accumulated fuel for next cycle

            // After tank is full, do NOT add extra liters beyond capacity
            // Extra liters from the same refuel (overfill situation) must NOT be counted
            // They simply indicate the real previous remainingFuel was lower
            // and the consumedFuel calculation already covered the correct amount
        }

        return cycles;
    }

    /**
     * Calculate average fuel consumption per 100km for a specific vehicle
     * Uses tank capacity logic
     */
    public double getConsumptionPerVehicle(Vehicle vehicle) {
        List<FuelEntry> entries = fuelEntryRepository.findByVehicleOrderByDateTimeAsc(vehicle);
        
        if (entries.size() < 2) {
            return 0.0; // Not enough data
        }

        // Get valid entries (sorted and filtered)
        List<FuelEntry> validEntries = getValidEntries(entries);
        
        if (validEntries.size() < 2) {
            return 0.0;
        }

        // Calculate consumption cycles using tank capacity
        List<ConsumptionCycle> cycles = calculateConsumptionCycles(validEntries, vehicle);
        
        if (cycles.isEmpty()) {
            return 0.0;
        }

        // Calculate average from valid cycles
        double totalConsumption = cycles.stream()
                .mapToDouble(ConsumptionCycle::consumption)
                .sum();
        
        return totalConsumption / cycles.size();
    }

    /**
     * Get consumption history for a vehicle
     * Uses tank capacity logic to detect full tanks automatically
     */
    public List<ConsumptionData> getConsumptionHistory(Vehicle vehicle) {
        List<FuelEntry> entries = fuelEntryRepository.findByVehicleOrderByDateTimeAsc(vehicle);
        
        if (entries.size() < 2) {
            return new ArrayList<>();
        }

        // Get valid entries (sorted and filtered)
        List<FuelEntry> validEntries = getValidEntries(entries);
        
        if (validEntries.size() < 2) {
            return new ArrayList<>();
        }

        // Calculate consumption cycles using tank capacity
        List<ConsumptionCycle> cycles = calculateConsumptionCycles(validEntries, vehicle);
        
        // Convert cycles to ConsumptionData
        List<ConsumptionData> history = cycles.stream()
                .map(cycle -> new ConsumptionData(
                    cycle.fromDate,
                    cycle.toDate,
                    cycle.distance,
                    cycle.fuelConsumed,
                    cycle.consumption
                ))
                .collect(Collectors.toList());

        return history;
    }

    /**
     * Get monthly statistics for a user
     * Uses only valid consumption cycles
     */
    public MonthlyStatistics getMonthlyStatistics(User user, YearMonth yearMonth) {
        LocalDateTime startDate = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime endDate = yearMonth.plusMonths(1).atDay(1).atStartOfDay();

        List<FuelEntry> entries = fuelEntryRepository.findByUserIdAndDateRange(
            user.getId(), startDate, endDate);

        if (entries.isEmpty()) {
            return new MonthlyStatistics(yearMonth, 0, 0.0, 0.0, 0.0, 0.0);
        }

        // Get valid entries (sorted and filtered)
        List<FuelEntry> validEntries = getValidEntries(entries);
        
        if (validEntries.isEmpty()) {
            return new MonthlyStatistics(yearMonth, 0, 0.0, 0.0, 0.0, 0.0);
        }

        // Monthly totals: Liters and Costs (sum of all fuel added in that month)
        double totalLitres = validEntries.stream()
                .mapToDouble(FuelEntry::getLitres)
                .sum();
        double totalCost = validEntries.stream()
                .mapToDouble(FuelEntry::getTotalPrice)
                .sum();
        double avgPricePerLitre = validEntries.stream()
                .mapToDouble(FuelEntry::getPricePerLitre)
                .average()
                .orElse(0.0);

        // Group entries by vehicle to calculate consumption per vehicle
        Map<Vehicle, List<FuelEntry>> entriesByVehicle = validEntries.stream()
                .collect(Collectors.groupingBy(FuelEntry::getVehicle));

        // Calculate consumption from valid cycles across all vehicles in this month
        List<ConsumptionCycle> allCycles = new ArrayList<>();
        for (Map.Entry<Vehicle, List<FuelEntry>> vehicleEntry : entriesByVehicle.entrySet()) {
            Vehicle vehicle = vehicleEntry.getKey();
            // Need to get all entries for this vehicle (not just monthly) to calculate cycles correctly
            List<FuelEntry> allVehicleEntries = fuelEntryRepository.findByVehicleOrderByDateTimeAsc(vehicle);
            List<FuelEntry> validVehicleEntries = getValidEntries(allVehicleEntries);
            
            // Calculate cycles for entire vehicle history
            List<ConsumptionCycle> vehicleCycles = calculateConsumptionCycles(validVehicleEntries, vehicle);
            
            // Filter cycles to only include those within the month
            for (ConsumptionCycle cycle : vehicleCycles) {
                if (cycle.toDate.isAfter(startDate) && cycle.toDate.isBefore(endDate)) {
                    allCycles.add(cycle);
                }
            }
        }

        // Calculate average consumption from valid cycles in this month
        double avgConsumption = 0.0;
        if (!allCycles.isEmpty()) {
            double totalConsumption = allCycles.stream()
                    .mapToDouble(ConsumptionCycle::consumption)
                    .sum();
            avgConsumption = totalConsumption / allCycles.size();
        }

        return new MonthlyStatistics(
            yearMonth,
            validEntries.size(),
            totalLitres,
            totalCost,
            avgPricePerLitre,
            avgConsumption
        );
    }

    /**
     * Get monthly statistics for all months where user has fuel entries
     * Recalculates using corrected valid entries only
     */
    public Map<YearMonth, MonthlyStatistics> getAllMonthlyStatistics(User user) {
        List<FuelEntry> allEntries = fuelEntryRepository.findByUserId(user.getId());
        
        if (allEntries.isEmpty()) {
            return new HashMap<>();
        }

        // Group entries by year-month
        Map<YearMonth, List<FuelEntry>> entriesByMonth = allEntries.stream()
                .collect(Collectors.groupingBy(entry -> 
                    YearMonth.from(entry.getDateTime())));

        Map<YearMonth, MonthlyStatistics> statistics = new HashMap<>();
        
        for (Map.Entry<YearMonth, List<FuelEntry>> entry : entriesByMonth.entrySet()) {
            statistics.put(entry.getKey(), getMonthlyStatistics(user, entry.getKey()));
        }

        return statistics;
    }

    // Helper class for consumption cycles
    private record ConsumptionCycle(
        Double fromOdometer,
        Double toOdometer,
        LocalDateTime fromDate,
        LocalDateTime toDate,
        double distance,
        double fuelConsumed,
        double consumption
    ) {}

    // Data classes for analytics results
    public record ConsumptionData(
        LocalDateTime fromDate,
        LocalDateTime toDate,
        double distanceKm,
        double litres,
        double consumptionPer100km
    ) {}

    public record MonthlyStatistics(
        YearMonth month,
        int entryCount,
        double totalLitres,
        double totalCost,
        double avgPricePerLitre,
        double avgConsumptionPer100km
    ) {}
}
