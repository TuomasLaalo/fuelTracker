package fi.laalo.fueltracker.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDateTime;

public record FuelEntryRequestDTO(
        @NotNull(message = "Vehicle ID is required")
        @Positive(message = "Vehicle ID must be positive")
        Long vehicleId,
        
        @NotNull(message = "Date and time is required")
        LocalDateTime dateTime,
        
        @NotNull(message = "Litres is required")
        @Positive(message = "Litres must be positive")
        Double litres,
        
        @NotNull(message = "Odometer is required")
        @Positive(message = "Odometer must be positive")
        Double odometer,
        
        @NotNull(message = "Price per litre is required")
        @Positive(message = "Price per litre must be positive")
        Double pricePerLitre,
        
        @NotNull(message = "Total price is required")
        @Positive(message = "Total price must be positive")
        Double totalPrice,
        
        String location,
        
        String notes,
        
        @NotNull(message = "Full tank indicator is required")
        Boolean fullTank
) {}