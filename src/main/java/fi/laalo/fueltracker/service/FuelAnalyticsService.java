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
     * Calculate average fuel consumption per 100km for a specific vehicle
     * @param vehicle The vehicle to calculate consumption for
     * @return Average consumption in liters per 100km, or 0.0 if insufficient data
     */
    public double getConsumptionPerVehicle(Vehicle vehicle) {
        List<FuelEntry> entries = fuelEntryRepository.findByVehicleOrderByDateTimeAsc(vehicle);
        
        if (entries.size() < 2) {
            return 0.0; // Not enough data
        }

        // Filter entries where fullTank is true for accurate calculation
        List<FuelEntry> fullTankEntries = entries.stream()
                .filter(FuelEntry::getFullTank)
                .sorted(Comparator.comparing(FuelEntry::getDateTime))
                .toList();

        if (fullTankEntries.size() < 2) {
            return 0.0;
        }

        double totalConsumption = 0.0;
        int calculations = 0;

        for (int i = 1; i < fullTankEntries.size(); i++) {
            FuelEntry current = fullTankEntries.get(i);
            FuelEntry previous = fullTankEntries.get(i - 1);

            double distance = current.getOdometer() - previous.getOdometer();
            
            if (distance > 0) {
                double consumption = (current.getLitres() / distance) * 100; // Liters per 100km
                totalConsumption += consumption;
                calculations++;
            }
        }

        return calculations > 0 ? totalConsumption / calculations : 0.0;
    }

    /**
     * Get consumption history for a vehicle (all consumption calculations between entries)
     * @param vehicle The vehicle to get history for
     * @return List of consumption data points
     */
    public List<ConsumptionData> getConsumptionHistory(Vehicle vehicle) {
        List<FuelEntry> entries = fuelEntryRepository.findByVehicleOrderByDateTimeAsc(vehicle);
        List<ConsumptionData> history = new ArrayList<>();

        if (entries.size() < 2) {
            return history;
        }

        // Filter full tank entries
        List<FuelEntry> fullTankEntries = entries.stream()
                .filter(FuelEntry::getFullTank)
                .sorted(Comparator.comparing(FuelEntry::getDateTime))
                .toList();

        for (int i = 1; i < fullTankEntries.size(); i++) {
            FuelEntry current = fullTankEntries.get(i);
            FuelEntry previous = fullTankEntries.get(i - 1);

            double distance = current.getOdometer() - previous.getOdometer();
            
            if (distance > 0) {
                double consumption = (current.getLitres() / distance) * 100;
                history.add(new ConsumptionData(
                    previous.getDateTime(),
                    current.getDateTime(),
                    distance,
                    current.getLitres(),
                    consumption
                ));
            }
        }

        return history;
    }

    /**
     * Get monthly statistics for a user
     * @param user The user to get statistics for
     * @param yearMonth The year and month to get statistics for
     * @return Monthly statistics
     */
    public MonthlyStatistics getMonthlyStatistics(User user, YearMonth yearMonth) {
        LocalDateTime startDate = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime endDate = yearMonth.plusMonths(1).atDay(1).atStartOfDay();

        List<FuelEntry> entries = fuelEntryRepository.findByUserIdAndDateRange(
            user.getId(), startDate, endDate);

        if (entries.isEmpty()) {
            return new MonthlyStatistics(yearMonth, 0, 0.0, 0.0, 0.0, 0.0);
        }

        int entryCount = entries.size();
        double totalLitres = entries.stream()
                .mapToDouble(FuelEntry::getLitres)
                .sum();
        double totalCost = entries.stream()
                .mapToDouble(FuelEntry::getTotalPrice)
                .sum();
        double avgPricePerLitre = entries.stream()
                .mapToDouble(FuelEntry::getPricePerLitre)
                .average()
                .orElse(0.0);

        // Calculate total distance and consumption
        List<FuelEntry> sortedEntries = entries.stream()
                .sorted(Comparator.comparing(FuelEntry::getDateTime))
                .toList();

        double totalDistance = 0.0;
        double totalFuelForDistance = 0.0;

        for (int i = 1; i < sortedEntries.size(); i++) {
            FuelEntry current = sortedEntries.get(i);
            FuelEntry previous = sortedEntries.get(i - 1);
            
            if (current.getFullTank() != null && current.getFullTank() && 
                previous.getFullTank() != null && previous.getFullTank()) {
                double distance = current.getOdometer() - previous.getOdometer();
                if (distance > 0) {
                    totalDistance += distance;
                    totalFuelForDistance += current.getLitres();
                }
            }
        }

        double avgConsumption = totalDistance > 0 ? (totalFuelForDistance / totalDistance) * 100 : 0.0;

        return new MonthlyStatistics(
            yearMonth,
            entryCount,
            totalLitres,
            totalCost,
            avgPricePerLitre,
            avgConsumption
        );
    }

    /**
     * Get monthly statistics for all months where user has fuel entries
     * @param user The user to get statistics for
     * @return Map of YearMonth to MonthlyStatistics
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

