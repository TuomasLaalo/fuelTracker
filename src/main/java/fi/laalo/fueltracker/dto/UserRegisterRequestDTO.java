package fi.laalo.fueltracker.dto;

public record UserRegisterRequestDTO(
        String email,
        String password
) {}