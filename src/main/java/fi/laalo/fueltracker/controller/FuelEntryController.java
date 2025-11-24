package fi.laalo.fueltracker.controller;

import fi.laalo.fueltracker.dto.FuelEntryRequestDTO;
import fi.laalo.fueltracker.dto.FuelEntryResponseDTO;
import fi.laalo.fueltracker.mapper.FuelEntryMapper;
import fi.laalo.fueltracker.model.FuelEntry;
import fi.laalo.fueltracker.model.User;
import fi.laalo.fueltracker.model.Vehicle;
import fi.laalo.fueltracker.service.FuelEntryService;
import fi.laalo.fueltracker.service.UserService;
import fi.laalo.fueltracker.service.VehicleService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/fuelentries")
public class FuelEntryController {

    private final FuelEntryService fuelEntryService;
    private final UserService userService;
    private final VehicleService vehicleService;

    public FuelEntryController(FuelEntryService fuelEntryService, UserService userService, VehicleService vehicleService) {
        this.fuelEntryService = fuelEntryService;
        this.userService = userService;
        this.vehicleService = vehicleService;
    }

    private String getCurrentEmail() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    @PostMapping
    public FuelEntryResponseDTO createEntry(@Valid @RequestBody FuelEntryRequestDTO dto) {

        String email = getCurrentEmail();
        User user = userService.getByEmail(email);

        Vehicle v = vehicleService.getById(dto.vehicleId());

        if (!v.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Not your vehicle");
        }

        // Calculate odometer reading
        Double odometerReading = dto.odometer();
        
        if (dto.useTrip() != null && dto.useTrip() && dto.tripDistance() != null && dto.tripDistance() > 0) {
            // Use trip distance: get last entry's odometer or vehicle's initial odometer
            List<FuelEntry> existingEntries = fuelEntryService.getByVehicle(v);
            
            if (existingEntries.isEmpty()) {
                // First entry: use vehicle's initial odometer + trip
                if (v.getInitialOdometer() != null) {
                    odometerReading = v.getInitialOdometer() + dto.tripDistance();
                } else {
                    throw new RuntimeException("Vehicle has no initial odometer. Please set it when creating the vehicle or enter odometer reading directly.");
                }
            } else {
                // Not first entry: use last entry's odometer + trip
                // Sort by date to get the most recent entry
                FuelEntry lastEntry = existingEntries.stream()
                        .max((e1, e2) -> e1.getDateTime().compareTo(e2.getDateTime()))
                        .orElse(null);
                
                if (lastEntry != null) {
                    odometerReading = lastEntry.getOdometer() + dto.tripDistance();
                } else {
                    throw new RuntimeException("Could not find previous fuel entry");
                }
            }
        } else if (odometerReading == null) {
            throw new RuntimeException("Either odometer reading or trip distance must be provided");
        }

        FuelEntry entry = new FuelEntry();
        entry.setUser(user);
        entry.setVehicle(v);
        entry.setDateTime(dto.dateTime());
        entry.setLitres(dto.litres());
        entry.setOdometer(odometerReading);
        entry.setPricePerLitre(dto.pricePerLitre());
        entry.setTotalPrice(dto.totalPrice());
        entry.setLocation(dto.location());
        entry.setNotes(dto.notes());

        return FuelEntryMapper.toDto(fuelEntryService.save(entry));
    }

    @GetMapping("/vehicle/{vehicleId}")
    public List<FuelEntryResponseDTO> getEntriesByVehicle(@PathVariable Long vehicleId) {

        String email = getCurrentEmail();
        User user = userService.getByEmail(email);

        Vehicle v = vehicleService.getById(vehicleId);

        if (!v.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Not your vehicle");
        }

        return fuelEntryService.getByVehicle(v).stream()
                .map(FuelEntryMapper::toDto)
                .toList();
    }

    @GetMapping("/{id}")
    public FuelEntryResponseDTO getEntry(@PathVariable Long id) {
        String email = getCurrentEmail();
        User user = userService.getByEmail(email);
        
        FuelEntry entry = fuelEntryService.getEntryById(id);
        
        if (entry == null) {
            throw new RuntimeException("Fuel entry not found");
        }
        
        if (!entry.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Not your fuel entry");
        }
        
        return FuelEntryMapper.toDto(entry);
    }

    @PutMapping("/{id}")
    public FuelEntryResponseDTO updateEntry(@PathVariable Long id, @Valid @RequestBody FuelEntryRequestDTO dto) {
        String email = getCurrentEmail();
        User user = userService.getByEmail(email);
        
        FuelEntry entry = fuelEntryService.getEntryById(id);
        
        if (entry == null) {
            throw new RuntimeException("Fuel entry not found");
        }
        
        if (!entry.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Not your fuel entry");
        }
        
        // Verify vehicle ownership
        Vehicle v = vehicleService.getById(dto.vehicleId());
        if (!v.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Not your vehicle");
        }
        
        // Calculate odometer reading for update
        Double odometerReading = dto.odometer();
        
        if (dto.useTrip() != null && dto.useTrip() && dto.tripDistance() != null && dto.tripDistance() > 0) {
            // Use trip distance: get previous entry's odometer (not the one being updated)
            List<FuelEntry> existingEntries = fuelEntryService.getByVehicle(v);
            FuelEntry previousEntry = existingEntries.stream()
                    .filter(e -> !e.getId().equals(id))
                    .max((e1, e2) -> e1.getDateTime().compareTo(e2.getDateTime()))
                    .orElse(null);
            
            if (previousEntry != null) {
                odometerReading = previousEntry.getOdometer() + dto.tripDistance();
            } else if (v.getInitialOdometer() != null) {
                odometerReading = v.getInitialOdometer() + dto.tripDistance();
            } else {
                throw new RuntimeException("Cannot calculate odometer from trip. Please enter odometer reading directly.");
            }
        } else if (odometerReading == null) {
            throw new RuntimeException("Either odometer reading or trip distance must be provided");
        }
        
        // Update fields
        entry.setVehicle(v);
        entry.setDateTime(dto.dateTime());
        entry.setLitres(dto.litres());
        entry.setOdometer(odometerReading);
        entry.setPricePerLitre(dto.pricePerLitre());
        entry.setTotalPrice(dto.totalPrice());
        entry.setLocation(dto.location());
        entry.setNotes(dto.notes());
        
        FuelEntry updated = fuelEntryService.save(entry);
        return FuelEntryMapper.toDto(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEntry(@PathVariable Long id) {
        String email = getCurrentEmail();
        User user = userService.getByEmail(email);
        
        FuelEntry entry = fuelEntryService.getEntryById(id);
        
        if (entry == null) {
            throw new RuntimeException("Fuel entry not found");
        }
        
        if (!entry.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Not your fuel entry");
        }
        
        fuelEntryService.deleteEntry(id);
        return ResponseEntity.noContent().build();
    }
}
