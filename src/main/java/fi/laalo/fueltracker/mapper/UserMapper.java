package fi.laalo.fueltracker.mapper;

import fi.laalo.fueltracker.dto.UserResponseDTO;
import fi.laalo.fueltracker.model.User;

public class UserMapper {

    public static UserResponseDTO toDto(User user) {
        return new UserResponseDTO(
                user.getId(),
                user.getEmail()
        );
    }
}