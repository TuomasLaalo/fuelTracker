package fi.laalo.fueltracker.controller;

import fi.laalo.fueltracker.model.User;
import fi.laalo.fueltracker.model.Vehicle;
import fi.laalo.fueltracker.service.FuelAnalyticsService;
import fi.laalo.fueltracker.service.UserService;
import fi.laalo.fueltracker.service.VehicleService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final FuelAnalyticsService analyticsService;
    private final UserService userService;
    private final VehicleService vehicleService;

    public AnalyticsController(FuelAnalyticsService analyticsService, 
                               UserService userService, 
                               VehicleService vehicleService) {
        this.analyticsService = analyticsService;
        this.userService = userService;
        this.vehicleService = vehicleService;
    }

    private String getCurrentEmail() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    /**
     * Get average consumption for a specific vehicle
     */
    @GetMapping("/vehicles/{vehicleId}/consumption")
    public double getVehicleConsumption(@PathVariable Long vehicleId) {
        String email = getCurrentEmail();
        User user = userService.getByEmail(email);
        
        Vehicle vehicle = vehicleService.getById(vehicleId);
        
        if (!vehicle.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Not your vehicle");
        }
        
        return analyticsService.getConsumptionPerVehicle(vehicle);
    }

    /**
     * Get consumption history for a specific vehicle
     */
    @GetMapping("/vehicles/{vehicleId}/history")
    public List<FuelAnalyticsService.ConsumptionData> getVehicleConsumptionHistory(@PathVariable Long vehicleId) {
        String email = getCurrentEmail();
        User user = userService.getByEmail(email);
        
        Vehicle vehicle = vehicleService.getById(vehicleId);
        
        if (!vehicle.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Not your vehicle");
        }
        
        return analyticsService.getConsumptionHistory(vehicle);
    }

    /**
     * Get monthly statistics for a specific month
     */
    @GetMapping("/monthly/{year}/{month}")
    public FuelAnalyticsService.MonthlyStatistics getMonthlyStatistics(
            @PathVariable int year, 
            @PathVariable int month) {
        String email = getCurrentEmail();
        User user = userService.getByEmail(email);
        
        YearMonth yearMonth = YearMonth.of(year, month);
        return analyticsService.getMonthlyStatistics(user, yearMonth);
    }

    /**
     * Get all monthly statistics for the current user
     */
    @GetMapping("/monthly")
    public Map<YearMonth, FuelAnalyticsService.MonthlyStatistics> getAllMonthlyStatistics() {
        String email = getCurrentEmail();
        User user = userService.getByEmail(email);
        
        return analyticsService.getAllMonthlyStatistics(user);
    }
}

