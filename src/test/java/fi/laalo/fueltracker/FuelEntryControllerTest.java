package fi.laalo.fueltracker;

import fi.laalo.fueltracker.controller.FuelEntryController;
import fi.laalo.fueltracker.dto.FuelEntryResponseDTO;
import fi.laalo.fueltracker.mapper.FuelEntryMapper;
import fi.laalo.fueltracker.model.FuelEntry;
import fi.laalo.fueltracker.model.User;
import fi.laalo.fueltracker.model.Vehicle;
import fi.laalo.fueltracker.service.FuelEntryService;
import fi.laalo.fueltracker.service.UserService;
import fi.laalo.fueltracker.service.VehicleService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FuelEntryController.class)
public class FuelEntryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FuelEntryService fuelEntryService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private VehicleService vehicleService;

    private User createTestUser() {
        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setName("Test User");
        return user;
    }

    private Vehicle createTestVehicle(User user) {
        Vehicle vehicle = new Vehicle();
        vehicle.setId(1L);
        vehicle.setMake("Toyota");
        vehicle.setModel("Corolla");
        vehicle.setUser(user);
        return vehicle;
    }

    private FuelEntry createTestFuelEntry(User user, Vehicle vehicle) {
        FuelEntry entry = new FuelEntry();
        entry.setId(1L);
        entry.setLitres(45.5);
        entry.setPricePerLitre(1.89);
        entry.setTotalPrice(86.00);
        entry.setOdometer(12500.0);
        entry.setDateTime(LocalDateTime.now());
        entry.setVehicle(vehicle);
        entry.setUser(user);
        return entry;
    }

    // Test 1: POST /api/fuelentries - Create new fuel entry
    @Test
    @WithMockUser(username = "test@example.com")
    void testCreateFuelEntry() throws Exception {
        // Arrange
        User user = createTestUser();
        Vehicle vehicle = createTestVehicle(user);
        FuelEntry entry = createTestFuelEntry(user, vehicle);

        when(userService.getByEmail("test@example.com")).thenReturn(user);
        when(vehicleService.getById(1L)).thenReturn(vehicle);
        when(fuelEntryService.save(any(FuelEntry.class))).thenReturn(entry);

        // Act & Assert
        mockMvc.perform(post("/api/fuelentries")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "vehicleId": 1,
                        "dateTime": "2024-01-15T10:00:00",
                        "litres": 45.5,
                        "pricePerLitre": 1.89,
                        "totalPrice": 86.00,
                        "odometer": 12500.0
                    }
                    """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.litres").value(45.5))
                .andExpect(jsonPath("$.odometer").value(12500.0));

        verify(fuelEntryService, times(1)).save(any(FuelEntry.class));
        System.out.println("✅ POST /api/fuelentries - Creates new fuel entry");
    }

    // Test 2: GET /api/fuelentries/{id} - Get fuel entry by ID
    @Test
    @WithMockUser(username = "test@example.com")
    void testGetFuelEntryById() throws Exception {
        // Arrange
        User user = createTestUser();
        Vehicle vehicle = createTestVehicle(user);
        FuelEntry entry = createTestFuelEntry(user, vehicle);

        when(userService.getByEmail("test@example.com")).thenReturn(user);
        when(fuelEntryService.getEntryById(1L)).thenReturn(entry);

        // Act & Assert
        mockMvc.perform(get("/api/fuelentries/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.litres").value(45.5))
                .andExpect(jsonPath("$.odometer").value(12500.0));

        verify(fuelEntryService, times(1)).getEntryById(1L);
        System.out.println("✅ GET /api/fuelentries/{id} - Returns fuel entry by ID");
    }

    // Test 3: GET /api/fuelentries/vehicle/{vehicleId} - Get entries by vehicle
    @Test
    @WithMockUser(username = "test@example.com")
    void testGetEntriesByVehicle() throws Exception {
        // Arrange
        User user = createTestUser();
        Vehicle vehicle = createTestVehicle(user);
        FuelEntry entry1 = createTestFuelEntry(user, vehicle);
        FuelEntry entry2 = createTestFuelEntry(user, vehicle);
        entry2.setId(2L);
        entry2.setOdometer(13000.0);

        List<FuelEntry> entries = Arrays.asList(entry1, entry2);

        when(userService.getByEmail("test@example.com")).thenReturn(user);
        when(vehicleService.getById(1L)).thenReturn(vehicle);
        when(fuelEntryService.getByVehicle(vehicle)).thenReturn(entries);

        // Act & Assert
        mockMvc.perform(get("/api/fuelentries/vehicle/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].litres").value(45.5))
                .andExpect(jsonPath("$[0].odometer").value(12500.0))
                .andExpect(jsonPath("$[1].odometer").value(13000.0));

        verify(fuelEntryService, times(1)).getByVehicle(vehicle);
        System.out.println("✅ GET /api/fuelentries/vehicle/{vehicleId} - Returns entries by vehicle");
    }

    // Test 4: PUT /api/fuelentries/{id} - Update fuel entry
    @Test
    @WithMockUser(username = "test@example.com")
    void testUpdateFuelEntry() throws Exception {
        // Arrange
        User user = createTestUser();
        Vehicle vehicle = createTestVehicle(user);
        FuelEntry entry = createTestFuelEntry(user, vehicle);
        entry.setLitres(50.0);

        when(userService.getByEmail("test@example.com")).thenReturn(user);
        when(fuelEntryService.getEntryById(1L)).thenReturn(entry);
        when(vehicleService.getById(1L)).thenReturn(vehicle);
        when(fuelEntryService.save(any(FuelEntry.class))).thenReturn(entry);

        // Act & Assert
        mockMvc.perform(put("/api/fuelentries/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "vehicleId": 1,
                        "dateTime": "2024-01-15T10:00:00",
                        "litres": 50.0,
                        "pricePerLitre": 1.92,
                        "totalPrice": 96.00,
                        "odometer": 12500.0
                    }
                    """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.litres").value(50.0));

        verify(fuelEntryService, times(1)).save(any(FuelEntry.class));
        System.out.println("✅ PUT /api/fuelentries/{id} - Updates fuel entry");
    }

    // Test 5: DELETE /api/fuelentries/{id} - Delete fuel entry
    @Test
    @WithMockUser(username = "test@example.com")
    void testDeleteFuelEntry() throws Exception {
        // Arrange
        User user = createTestUser();
        Vehicle vehicle = createTestVehicle(user);
        FuelEntry entry = createTestFuelEntry(user, vehicle);

        when(userService.getByEmail("test@example.com")).thenReturn(user);
        when(fuelEntryService.getEntryById(1L)).thenReturn(entry);

        // Act & Assert
        mockMvc.perform(delete("/api/fuelentries/1"))
                .andExpect(status().isNoContent());

        verify(fuelEntryService, times(1)).deleteEntry(1L);
        System.out.println("✅ DELETE /api/fuelentries/{id} - Deletes fuel entry");
    }
}
