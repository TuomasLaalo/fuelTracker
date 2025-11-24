package fi.laalo.fueltracker.controller;

import fi.laalo.fueltracker.dto.VehicleRequestDTO;
import fi.laalo.fueltracker.dto.VehicleResponseDTO;
import fi.laalo.fueltracker.mapper.VehicleMapper;
import fi.laalo.fueltracker.model.User;
import fi.laalo.fueltracker.model.Vehicle;
import fi.laalo.fueltracker.service.UserService;
import fi.laalo.fueltracker.service.VehicleService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
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

    @GetMapping("/{id}")
    public VehicleResponseDTO getVehicle(@PathVariable Long id) {
        String email = getCurrentEmail();
        User user = userService.getByEmail(email);
        
        Vehicle vehicle = vehicleService.getById(id);
        
        if (!vehicle.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Not your vehicle");
        }
        
        return VehicleMapper.toDto(vehicle);
    }

    @PostMapping
    public VehicleResponseDTO createVehicle(@Valid @RequestBody VehicleRequestDTO dto) {

        String email = getCurrentEmail();
        User user = userService.getByEmail(email);

        Vehicle v = VehicleMapper.fromDto(dto);
        v.setUser(user);

        Vehicle saved = vehicleService.save(v);
        return VehicleMapper.toDto(saved);
    }

    @PutMapping("/{id}")
    public VehicleResponseDTO updateVehicle(@PathVariable Long id, @Valid @RequestBody VehicleRequestDTO dto) {
        String email = getCurrentEmail();
        User user = userService.getByEmail(email);
        
        Vehicle vehicle = vehicleService.getById(id);
        
        if (!vehicle.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Not your vehicle");
        }
        
        // Update fields
        vehicle.setMake(dto.make());
        vehicle.setModel(dto.model());
        vehicle.setFuelType(dto.fuelType());
        vehicle.setManufacturingYear(dto.manufacturingYear());
        vehicle.setLicensePlate(dto.licensePlate());
        vehicle.setInitialOdometer(dto.initialOdometer());
        vehicle.setTankCapacityLiters(dto.tankCapacityLiters());
        
        Vehicle updated = vehicleService.save(vehicle);
        return VehicleMapper.toDto(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVehicle(@PathVariable Long id) {
        String email = getCurrentEmail();
        User user = userService.getByEmail(email);
        
        Vehicle vehicle = vehicleService.getById(id);
        
        if (!vehicle.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Not your vehicle");
        }
        
        vehicleService.deleteVehicle(id);
        return ResponseEntity.noContent().build();
    }
}
