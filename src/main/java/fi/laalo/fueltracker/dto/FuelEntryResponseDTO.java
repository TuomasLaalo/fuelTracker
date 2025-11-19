package fi.laalo.fueltracker.dto;

import java.time.LocalDateTime;

public record FuelEntryResponseDTO(
        Long id,
        Long vehicleId,
        Double litres,
        Double odometer,
        Double pricePerLitre,
        Double totalPrice,
        LocalDateTime dateTime,
        String location,
        String notes,
        Boolean fullTank
) {}