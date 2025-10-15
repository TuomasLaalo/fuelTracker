package fi.laalo.fueltracker.service;

import fi.laalo.fueltracker.model.FuelEntry;
import fi.laalo.fueltracker.repository.FuelEntryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FuelEntryService {

    @Autowired
    private FuelEntryRepository repository;

    // CRUD operations

    public List<FuelEntry> getAllEntries() {
        return repository.findAll();
    }

    public FuelEntry addEntry(FuelEntry entry) {
        return repository.save(entry);
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
}
