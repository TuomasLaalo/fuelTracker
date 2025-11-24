package fi.laalo.fueltracker.dto;

public record VehicleResponseDTO(
        Long id,
        String make,
        String model,
        String fuelType,
        Integer manufacturingYear,
        String licensePlate,
        Double initialOdometer,
        Double tankCapacityLiters
) {}