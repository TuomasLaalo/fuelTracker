package fi.laalo.fueltracker;

import fi.laalo.fueltracker.controller.VehicleController;
import fi.laalo.fueltracker.model.Vehicle;
import fi.laalo.fueltracker.model.User;
import fi.laalo.fueltracker.service.VehicleService;
import fi.laalo.fueltracker.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(VehicleController.class)
public class VehicleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private VehicleService vehicleService;

    @MockitoBean
    private UserService userService;

    private User createTestUser() {
        User user = new User();
        user.setId(1L);
        user.setName("Test User");
        user.setEmail("test@example.com");
        return user;
    }

    private Vehicle createTestVehicle(User user) {
        Vehicle vehicle = new Vehicle();
        vehicle.setId(1L);
        vehicle.setMake("Toyota");
        vehicle.setModel("Corolla");
        vehicle.setManufacturingYear(2015);
        vehicle.setFuelType("Gasoline");
        vehicle.setLicensePlate("ABC-123");
        vehicle.setUser(user);
        return vehicle;
    }

    // Test 1: GET /api/vehicles - Get all vehicles for current user
    @Test
    @WithMockUser(username = "test@example.com")
    void testGetAllVehicles() throws Exception {
        // Arrange
        User user = createTestUser();
        Vehicle v1 = createTestVehicle(user);
        Vehicle v2 = new Vehicle();
        v2.setId(2L);
        v2.setMake("Volvo");
        v2.setModel("V70");
        v2.setManufacturingYear(2010);
        v2.setFuelType("Diesel");
        v2.setLicensePlate("XYZ-789");
        v2.setUser(user);

        List<Vehicle> vehicles = Arrays.asList(v1, v2);
        when(userService.getByEmail("test@example.com")).thenReturn(user);
        when(vehicleService.getVehiclesByUser(user)).thenReturn(vehicles);

        // Act & Assert
        mockMvc.perform(get("/api/vehicles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].make").value("Toyota"))
                .andExpect(jsonPath("$[0].model").value("Corolla"))
                .andExpect(jsonPath("$[0].licensePlate").value("ABC-123"))
                .andExpect(jsonPath("$[1].make").value("Volvo"))
                .andExpect(jsonPath("$[1].model").value("V70"));

        verify(vehicleService, times(1)).getVehiclesByUser(user);
        System.out.println("✅ GET /api/vehicles - Returns all vehicles for user");
    }

    // Test 2: GET /api/vehicles/{id} - Get vehicle by ID
    @Test
    @WithMockUser(username = "test@example.com")
    void testGetVehicleById() throws Exception {
        // Arrange
        User user = createTestUser();
        Vehicle vehicle = createTestVehicle(user);

        when(userService.getByEmail("test@example.com")).thenReturn(user);
        when(vehicleService.getById(1L)).thenReturn(vehicle);

        // Act & Assert
        mockMvc.perform(get("/api/vehicles/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.make").value("Toyota"))
                .andExpect(jsonPath("$.model").value("Corolla"))
                .andExpect(jsonPath("$.licensePlate").value("ABC-123"));

        verify(vehicleService, times(1)).getById(1L);
        System.out.println("✅ GET /api/vehicles/{id} - Returns vehicle by ID");
    }

    // Test 3: POST /api/vehicles - Create new vehicle
    @Test
    @WithMockUser(username = "test@example.com")
    void testCreateVehicle() throws Exception {
        // Arrange
        User user = createTestUser();
        Vehicle vehicle = createTestVehicle(user);

        when(userService.getByEmail("test@example.com")).thenReturn(user);
        when(vehicleService.save(any(Vehicle.class))).thenReturn(vehicle);

        // Act & Assert
        mockMvc.perform(post("/api/vehicles")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "make": "Toyota",
                        "model": "Corolla",
                        "manufacturingYear": 2015,
                        "fuelType": "Gasoline",
                        "licensePlate": "ABC-123"
                    }
                    """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.make").value("Toyota"))
                .andExpect(jsonPath("$.model").value("Corolla"));

        verify(vehicleService, times(1)).save(any(Vehicle.class));
        System.out.println("✅ POST /api/vehicles - Creates new vehicle");
    }

    // Test 4: PUT /api/vehicles/{id} - Update vehicle
    @Test
    @WithMockUser(username = "test@example.com")
    void testUpdateVehicle() throws Exception {
        // Arrange
        User user = createTestUser();
        Vehicle vehicle = createTestVehicle(user);
        vehicle.setMake("Honda");

        when(userService.getByEmail("test@example.com")).thenReturn(user);
        when(vehicleService.getById(1L)).thenReturn(vehicle);
        when(vehicleService.save(any(Vehicle.class))).thenReturn(vehicle);

        // Act & Assert
        mockMvc.perform(put("/api/vehicles/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "make": "Honda",
                        "model": "Civic",
                        "manufacturingYear": 2018,
                        "fuelType": "Gasoline",
                        "licensePlate": "ABC-123"
                    }
                    """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.make").value("Honda"));

        verify(vehicleService, times(1)).save(any(Vehicle.class));
        System.out.println("✅ PUT /api/vehicles/{id} - Updates vehicle");
    }

    // Test 5: DELETE /api/vehicles/{id} - Delete vehicle
    @Test
    @WithMockUser(username = "test@example.com")
    void testDeleteVehicle() throws Exception {
        // Arrange
        User user = createTestUser();
        Vehicle vehicle = createTestVehicle(user);

        when(userService.getByEmail("test@example.com")).thenReturn(user);
        when(vehicleService.getById(1L)).thenReturn(vehicle);

        // Act & Assert
        mockMvc.perform(delete("/api/vehicles/1"))
                .andExpect(status().isNoContent());

        verify(vehicleService, times(1)).deleteVehicle(1L);
        System.out.println("✅ DELETE /api/vehicles/{id} - Deletes vehicle");
    }
}
