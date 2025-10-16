package fi.laalo.fueltracker.controller;


import fi.laalo.fueltracker.model.FuelEntry;
import fi.laalo.fueltracker.service.FuelEntryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/fuel_entries")
@CrossOrigin(origins = "*") // Dev use

public class FuelEntryController {



    @Autowired
    private FuelEntryService service;


    // Get all
    @GetMapping
    public List<FuelEntry> getAll() {
        return service.getAllEntries();
    }

    // Get by ID
    @GetMapping("/{id}")
    public FuelEntry getById(@PathVariable Long id) {
        return service.getEntryById(id);
    }

    // Add new entry
    @PostMapping
    public FuelEntry addEntry(@RequestBody FuelEntry entry) {
        return service.addEntry(entry);
    }

    // Delete entry
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
