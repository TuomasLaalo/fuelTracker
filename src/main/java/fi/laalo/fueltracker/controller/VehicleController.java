package fi.laalo.fueltracker.controller;

import fi.laalo.fueltracker.dto.VehicleRequestDTO;
import fi.laalo.fueltracker.dto.VehicleResponseDTO;
import fi.laalo.fueltracker.mapper.VehicleMapper;
import fi.laalo.fueltracker.model.User;
import fi.laalo.fueltracker.model.Vehicle;
import fi.laalo.fueltracker.service.UserService;
import fi.laalo.fueltracker.service.VehicleService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vehicles")
public class VehicleController {

    private final VehicleService vehicleService;
    private final UserService userService;

    public VehicleController(VehicleService vehicleService, UserService userService) {
        this.vehicleService = vehicleService;
        this.userService = userService;
    }

    private String getCurrentEmail() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    @GetMapping
    public List<VehicleResponseDTO> getVehicles() {
        String email = getCurrentEmail();
        User user = userService.getByEmail(email);

        return vehicleService.getVehiclesByUser(user)
                .stream()
                .map(VehicleMapper::toDto)
                .toList();
    }

    @PostMapping
    public VehicleResponseDTO createVehicle(@RequestBody VehicleRequestDTO dto) {

        String email = getCurrentEmail();
        User user = userService.getByEmail(email);

        Vehicle v = VehicleMapper.fromDto(dto);
        v.setUser(user);

        Vehicle saved = vehicleService.save(v);
        return VehicleMapper.toDto(saved);
    }
}
