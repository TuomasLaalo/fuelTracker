/* package fi.laalo.fueltracker;

import fi.laalo.fueltracker.controller.VehicleController;
import fi.laalo.fueltracker.model.Vehicle;
import fi.laalo.fueltracker.model.User;
import fi.laalo.fueltracker.service.VehicleService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
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

    // 🔹 Test 1: GET /api/vehicles - Get all vehicles
    @Test
    void testGetAllVehicles() throws Exception {
        // Arrange
        User user = new User();
        user.setId(1L);
        user.setName("Test User");

        Vehicle v1 = new Vehicle();
        v1.setId(1L);
        v1.setMake("Toyota");
        v1.setModel("Corolla");
        v1.setManufacturingYear(2015);
        v1.setFuelType("Gasoline");
        v1.setLicensePlate("ABC-123");
        v1.setUser(user);

        Vehicle v2 = new Vehicle();
        v2.setId(2L);
        v2.setMake("Volvo");
        v2.setModel("V70");
        v2.setManufacturingYear(2010);
        v2.setFuelType("Diesel");
        v2.setLicensePlate("XYZ-789");
        v2.setUser(user);

        List<Vehicle> vehicles = Arrays.asList(v1, v2);
        when(vehicleService.getAllVehicles()).thenReturn(vehicles);

        // Act & Assert
        mockMvc.perform(get("/api/vehicles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].make").value("Toyota"))
                .andExpect(jsonPath("$[0].model").value("Corolla"))
                .andExpect(jsonPath("$[0].licensePlate").value("ABC-123"))
                .andExpect(jsonPath("$[1].make").value("Volvo"))
                .andExpect(jsonPath("$[1].model").value("V70"));

        verify(vehicleService, times(1)).getAllVehicles();
        System.out.println("✅ GET /api/vehicles - Returns all vehicles");
    }

    // 🔹 Test 2: GET /api/vehicles/{id} - Get vehicle by ID
    @Test
    void testGetVehicleById() throws Exception {
        // Arrange
        User user = new User();
        user.setId(1L);

        Vehicle vehicle = new Vehicle();
        vehicle.setId(1L);
        vehicle.setMake("Toyota");
        vehicle.setModel("Corolla");
        vehicle.setManufacturingYear(2015);
        vehicle.setFuelType("Gasoline");
        vehicle.setLicensePlate("ABC-123");
        vehicle.setUser(user);

        when(vehicleService.getVehicleById(1L)).thenReturn(vehicle);

        // Act & Assert
        mockMvc.perform(get("/api/vehicles/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.make").value("Toyota"))
                .andExpect(jsonPath("$.model").value("Corolla"))
                .andExpect(jsonPath("$.licensePlate").value("ABC-123"));

        verify(vehicleService, times(1)).getVehicleById(1L);
        System.out.println("✅ GET /api/vehicles/{id} - Returns vehicle by ID");
    }

    // 🔹 Test 3: POST /api/vehicles - Create new vehicle
    @Test
    void testAddVehicle() throws Exception {
        // Arrange
        User user = new User();
        user.setId(1L);

        Vehicle vehicle = new Vehicle();
        vehicle.setId(1L);
        vehicle.setMake("Toyota");
        vehicle.setModel("Corolla");
        vehicle.setManufacturingYear(2015);
        vehicle.setFuelType("Gasoline");
        vehicle.setLicensePlate("ABC-123");
        vehicle.setUser(user);

        when(vehicleService.saveVehicle(any(Vehicle.class))).thenReturn(vehicle);

        // Act & Assert
        mockMvc.perform(post("/api/vehicles")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "make": "Toyota",
                        "model": "Corolla",
                        "manufacturingYear": 2015,
                        "fuelType": "Gasoline",
                        "licensePlate": "ABC-123",
                        "user": {"id": 1}
                    }
                    """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.make").value("Toyota"))
                .andExpect(jsonPath("$.model").value("Corolla"));

        verify(vehicleService, times(1)).saveVehicle(any(Vehicle.class));
        System.out.println("✅ POST /api/vehicles - Creates new vehicle");
    }

    // 🔹 Test 4: DELETE /api/vehicles/{id} - Delete vehicle
    @Test
    void testDeleteVehicle() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/vehicles/1"))
                .andExpect(status().isOk());

        verify(vehicleService, times(1)).deleteVehicle(1L);
        System.out.println("✅ DELETE /api/vehicles/{id} - Deletes vehicle");
    }

    // 🔹 Test 5: GET /api/vehicles/user/{userId} - Get vehicles by user ID
    @Test
    void testGetVehiclesByUserId() throws Exception {
        // Arrange
        User user = new User();
        user.setId(1L);

        Vehicle vehicle = new Vehicle();
        vehicle.setId(1L);
        vehicle.setMake("Toyota");
        vehicle.setModel("Corolla");
        vehicle.setManufacturingYear(2015);
        vehicle.setFuelType("Gasoline");
        vehicle.setLicensePlate("ABC-123");
        vehicle.setUser(user);

        List<Vehicle> vehicles = Arrays.asList(vehicle);
        when(vehicleService.getVehiclesByUserId(1L)).thenReturn(vehicles);

        // Act & Assert
        mockMvc.perform(get("/api/vehicles/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].make").value("Toyota"))
                .andExpect(jsonPath("$[0].licensePlate").value("ABC-123"));

        verify(vehicleService, times(1)).getVehiclesByUserId(1L);
        System.out.println("✅ GET /api/vehicles/user/{userId} - Returns vehicles by user");
    }

    // 🔹 Test 6: GET /api/vehicles/exists/{licensePlate} - Check if vehicle exists
    @Test
    void testVehicleExistsByLicensePlate() throws Exception {
        // Arrange
        when(vehicleService.vehicleExistsByLicensePlate("ABC-123")).thenReturn(true);
        when(vehicleService.vehicleExistsByLicensePlate("XXX-000")).thenReturn(false);

        // Act & Assert - Existing license plate
        mockMvc.perform(get("/api/vehicles/exists/ABC-123"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        // Act & Assert - Non-existing license plate
        mockMvc.perform(get("/api/vehicles/exists/XXX-000"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));

        verify(vehicleService, times(1)).vehicleExistsByLicensePlate("ABC-123");
        verify(vehicleService, times(1)).vehicleExistsByLicensePlate("XXX-000");
        System.out.println("✅ GET /api/vehicles/exists/{licensePlate} - Checks vehicle existence");
    }
}
*/