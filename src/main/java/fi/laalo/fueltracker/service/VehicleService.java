package fi.laalo.fueltracker.service;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import fi.laalo.fueltracker.repository.VehicleRepository;
import fi.laalo.fueltracker.model.Vehicle;
import fi.laalo.fueltracker.model.User;


import java.util.List;


@Service
public class VehicleService {

    @Autowired
    private VehicleRepository repository;

 // CRUD Operations

    // Get all vehicles
    public List<Vehicle> getAllVehicles() {
        return repository.findAll();
    }

    // Find vehicle by ID
    public Vehicle getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vehicle not found"));
    }

    // Add or update vehicle
    public Vehicle save(Vehicle vehicle) {
        return repository.save(vehicle);
    }

    // Delete vehicle
    public void deleteVehicle(Long id) {
        repository.deleteById(id);
    }

    // Custom query methods

    // Find all vehicles by user ID
    public List<Vehicle> getVehiclesByUserId(Long userId) {
        return repository.findByUserId(userId);
    }

    // Check if a vehicle exists by license plate
    public boolean vehicleExistsByLicensePlate(String licensePlate) {
        return repository.existsByLicensePlate(licensePlate);
    }

    public List<Vehicle> getVehiclesByUser(User user) {
        return repository.findByUser(user);
    }



}