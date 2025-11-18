package fi.laalo.fueltracker.controller;

import fi.laalo.fueltracker.model.FuelEntry;
import fi.laalo.fueltracker.service.FuelEntryService;
import fi.laalo.fueltracker.service.UserService;
import fi.laalo.fueltracker.model.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

@RestController
@RequestMapping("/api/fuel_entries")
@CrossOrigin(origins = "*") // Dev use

public class FuelEntryController {

    @Autowired
    private FuelEntryService service;

    @Autowired
    private UserService userService;

    // Helper method to get current authenticated user
    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        return userService.findByEmail(email).orElseThrow();
    }
    // Add new fuel entry
    @PostMapping
    public FuelEntry addFuelEntry(@RequestBody FuelEntry entry) {
        User currentUser = getCurrentUser();
        return service.createForUser(entry, currentUser);
    }
    // Get all fuel entries for current user
    @GetMapping
    public List<FuelEntry> getUserFuelEntries() {
        User currentUser = getCurrentUser();
        return service.getAllForUser(currentUser.getId());
    }

    // Delete fuel entry by ID
    @DeleteMapping("/{id}")
    public void deleteEntry(@PathVariable Long id) {
        service.deleteEntry(id);
    }

    // Calculate average consumption
    @GetMapping("/consumption")
    public double calculateConsumption() {
        return service.calculateConsumption();
    }

}
