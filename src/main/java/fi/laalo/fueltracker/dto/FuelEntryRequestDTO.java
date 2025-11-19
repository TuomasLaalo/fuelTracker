package fi.laalo.fueltracker.dto;

import java.time.LocalDateTime;

public record FuelEntryRequestDTO(
        Long vehicleId,
        LocalDateTime dateTime,
        Double litres,
        Double odometer,
        Double pricePerLitre,
        Double totalPrice,
        String location,
        String notes,
        Boolean fullTank
) {}