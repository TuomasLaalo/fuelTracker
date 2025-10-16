package fi.laalo.fueltracker.controller;

import fi.laalo.fueltracker.service.VehicleService;
import fi.laalo.fueltracker.model.Vehicle;

import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@RestController
@RequestMapping("/api/vehicles")
@CrossOrigin(origins = "*") // Dev use, Remove later


public class VehicleController {


@Autowired
private VehicleService service;

// Get all vehicles
@GetMapping
public List<Vehicle> getAllVehicles() {
    return service.getAllVehicles();
}

// Get vehicle by ID
@GetMapping("/{id}")
public Vehicle getVehicleById(@PathVariable Long id) {
    return service.getVehicleById(id);
}
// Add or update vehicle
@PostMapping
public Vehicle addOrUpdateVehicle(@RequestBody Vehicle vehicle) {
    return service.saveVehicle(vehicle);
}
// Get vehicles by user ID
@GetMapping("/user/{userId}")
public List<Vehicle> getVehiclesByUserId(@PathVariable Long userId) {
    return service.getVehiclesByUserId(userId);
}

// Delete vehicle
@DeleteMapping("/{id}")
public void deleteVehicle(@PathVariable Long id) {
    service.deleteVehicle(id);
}

// Check if vehicle exists by license plate
@GetMapping("/exists/{licensePlate}")
public boolean vehicleExistsByLicensePlate(@PathVariable String licensePlate) {
    return service.vehicleExistsByLicensePlate(licensePlate);
}




}