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
    public FuelEntryResponseDTO createEntry(@RequestBody FuelEntryRequestDTO dto) {

        String email = getCurrentEmail();
        User user = userService.getByEmail(email);

        Vehicle v = vehicleService.getById(dto.vehicleId());

        if (!v.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Not your vehicle");
        }

        FuelEntry entry = new FuelEntry();
        entry.setUser(user);
        entry.setVehicle(v);
        entry.setDateTime(dto.dateTime());
        entry.setLitres(dto.litres());
        entry.setOdometer(dto.odometer());
        entry.setPricePerLitre(dto.pricePerLitre());
        entry.setTotalPrice(dto.totalPrice());
        entry.setLocation(dto.location());
        entry.setNotes(dto.notes());
        entry.setFullTank(dto.fullTank());

        return FuelEntryMapper.toDto(fuelEntryService.save(entry));
    }

    @GetMapping("/{vehicleId}")
    public List<FuelEntryResponseDTO> getEntries(@PathVariable Long vehicleId) {

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
}
