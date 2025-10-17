package fi.laalo.fueltracker;

import fi.laalo.fueltracker.controller.FuelEntryController;
import fi.laalo.fueltracker.model.FuelEntry;
import fi.laalo.fueltracker.model.Vehicle;
import fi.laalo.fueltracker.service.FuelEntryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
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

    // 🔹 Test 1: GET /api/fuel_entries - Get all fuel entries
    @Test
    void testGetAllFuelEntries() throws Exception {
        // Arrange
        Vehicle vehicle = new Vehicle();
        vehicle.setId(1L);
        vehicle.setMake("Toyota");
        vehicle.setModel("Corolla");

        FuelEntry entry1 = new FuelEntry();
        entry1.setId(1L);
        entry1.setLitres(45.5);
        entry1.setPricePerLitre(1.89);
        entry1.setTotalPrice(86.00);
        entry1.setOdometer(12500.0);
        entry1.setFullTank(true);
        entry1.setDateTime(LocalDateTime.now());
        entry1.setVehicle(vehicle);

        FuelEntry entry2 = new FuelEntry();
        entry2.setId(2L);
        entry2.setLitres(50.0);
        entry2.setPricePerLitre(1.92);
        entry2.setTotalPrice(96.00);
        entry2.setOdometer(13000.0);
        entry2.setFullTank(true);
        entry2.setDateTime(LocalDateTime.now());
        entry2.setVehicle(vehicle);

        List<FuelEntry> entries = Arrays.asList(entry1, entry2);
        when(fuelEntryService.getAllEntries()).thenReturn(entries);

        // Act & Assert
        mockMvc.perform(get("/api/fuel_entries"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].litres").value(45.5))
                .andExpect(jsonPath("$[0].odometer").value(12500.0))
                .andExpect(jsonPath("$[1].litres").value(50.0))
                .andExpect(jsonPath("$[1].odometer").value(13000.0));

        verify(fuelEntryService, times(1)).getAllEntries();
        System.out.println("✅ GET /api/fuel_entries - Returns all fuel entries");
    }

    // 🔹 Test 2: GET /api/fuel_entries/{id} - Get fuel entry by ID
    @Test
    void testGetFuelEntryById() throws Exception {
        // Arrange
        Vehicle vehicle = new Vehicle();
        vehicle.setId(1L);

        FuelEntry entry = new FuelEntry();
        entry.setId(1L);
        entry.setLitres(45.5);
        entry.setPricePerLitre(1.89);
        entry.setTotalPrice(86.00);
        entry.setOdometer(12500.0);
        entry.setFullTank(true);
        entry.setDateTime(LocalDateTime.now());
        entry.setVehicle(vehicle);

        when(fuelEntryService.getEntryById(1L)).thenReturn(entry);

        // Act & Assert
        mockMvc.perform(get("/api/fuel_entries/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.litres").value(45.5))
                .andExpect(jsonPath("$.odometer").value(12500.0))
                .andExpect(jsonPath("$.fullTank").value(true));

        verify(fuelEntryService, times(1)).getEntryById(1L);
        System.out.println("✅ GET /api/fuel_entries/{id} - Returns fuel entry by ID");
    }

    // 🔹 Test 3: POST /api/fuel_entries - Create new fuel entry
    @Test
    void testAddFuelEntry() throws Exception {
        // Arrange
        Vehicle vehicle = new Vehicle();
        vehicle.setId(1L);

        FuelEntry entry = new FuelEntry();
        entry.setId(1L);
        entry.setLitres(45.5);
        entry.setPricePerLitre(1.89);
        entry.setTotalPrice(86.00);
        entry.setOdometer(12500.0);
        entry.setFullTank(true);
        entry.setDateTime(LocalDateTime.now());
        entry.setVehicle(vehicle);

        when(fuelEntryService.addEntry(any(FuelEntry.class))).thenReturn(entry);

        // Act & Assert
        mockMvc.perform(post("/api/fuel_entries")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "litres": 45.5,
                        "pricePerLitre": 1.89,
                        "totalPrice": 86.00,
                        "odometer": 12500.0,
                        "fullTank": true,
                        "vehicle": {"id": 1}
                    }
                    """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.litres").value(45.5))
                .andExpect(jsonPath("$.odometer").value(12500.0));

        verify(fuelEntryService, times(1)).addEntry(any(FuelEntry.class));
        System.out.println("✅ POST /api/fuel_entries - Creates new fuel entry");
    }

    // 🔹 Test 4: DELETE /api/fuel_entries/{id} - Delete fuel entry
    @Test
    void testDeleteFuelEntry() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/fuel_entries/1"))
                .andExpect(status().isOk());

        verify(fuelEntryService, times(1)).deleteEntry(1L);
        System.out.println("✅ DELETE /api/fuel_entries/{id} - Deletes fuel entry");
    }

    // 🔹 Test 5: GET /api/fuel_entries/consumption - Calculate average consumption
    @Test
    void testCalculateConsumption() throws Exception {
        // Arrange
        when(fuelEntryService.calculateConsumption()).thenReturn(7.5);

        // Act & Assert
        mockMvc.perform(get("/api/fuel_entries/consumption"))
                .andExpect(status().isOk())
                .andExpect(content().string("7.5"));

        verify(fuelEntryService, times(1)).calculateConsumption();
        System.out.println("✅ GET /api/fuel_entries/consumption - Calculates average consumption");
    }
}
