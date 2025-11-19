package fi.laalo.fueltracker.dto;


public record VehicleRequestDTO(
        String make,
        String model,
        String fuelType,
        Integer manufacturingYear,
        String licensePlate
) {}