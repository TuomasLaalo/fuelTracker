package fi.laalo.fueltracker.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record VehicleRequestDTO(
        @NotBlank(message = "Make is required")
        String make,
        
        @NotBlank(message = "Model is required")
        String model,
        
        @NotBlank(message = "Fuel type is required")
        String fuelType,
        
        @NotNull(message = "Manufacturing year is required")
        @Min(value = 1900, message = "Manufacturing year must be at least 1900")
        @Positive(message = "Manufacturing year must be positive")
        Integer manufacturingYear,
        
        @NotBlank(message = "License plate is required")
        String licensePlate
) {}