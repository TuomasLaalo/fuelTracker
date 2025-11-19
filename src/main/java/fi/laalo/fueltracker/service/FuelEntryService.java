package fi.laalo.fueltracker.service;

import fi.laalo.fueltracker.model.FuelEntry;
import fi.laalo.fueltracker.repository.FuelEntryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import fi.laalo.fueltracker.model.User;
import fi.laalo.fueltracker.model.Vehicle;

import java.util.List;

@Service
public class FuelEntryService {

    @Autowired
    private FuelEntryRepository repository;

    // CRUD operations

    public FuelEntry createForUser(FuelEntry entry, User user) {
        entry.setUser(user);
        return repository.save(entry);
    }

    public List<FuelEntry> getAllForUser(Long userId) {
        return repository.findByUserId(userId);
    }
  
    public FuelEntry getEntryById(Long id) {
        return repository.findById(id).orElse(null);
    }

    public void deleteEntry(Long id) {
        repository.deleteById(id);
    }

    // Calculation of average fuel consumption

    public double calculateConsumption() {

        List<FuelEntry> all = repository.findAll();

        if (all.size() < 2) {
            return 0.0; // Not enough data to calculate consumption
        }

        FuelEntry latest = all.get(all.size() - 1);
        FuelEntry previous = all.get(all.size() - 2);

        double distance = latest.getOdometer() - previous.getOdometer();

        if (distance <= 0) {
            return 0.0; // Invalid odometer readings
        }

        return (latest.getLitres() / distance) * 100; // Liters per 100 km
    }

    // Adapter convenience methods expected by controllers
    public FuelEntry save(FuelEntry entry) {
        return repository.save(entry);
    }

    public List<FuelEntry> getByVehicle(Vehicle vehicle) {
        return repository.findByVehicleOrderByDateTimeAsc(vehicle);
    }
}
