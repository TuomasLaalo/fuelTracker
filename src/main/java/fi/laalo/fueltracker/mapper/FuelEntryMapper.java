package fi.laalo.fueltracker.mapper;

import fi.laalo.fueltracker.dto.FuelEntryResponseDTO;
import fi.laalo.fueltracker.model.FuelEntry;

public class FuelEntryMapper {

    public static FuelEntryResponseDTO toDto(FuelEntry f) {
        return new FuelEntryResponseDTO(
                f.getId(),
                f.getVehicle().getId(),
                f.getLitres(),
                f.getOdometer(),
                f.getPricePerLitre(),
                f.getTotalPrice(),
                f.getDateTime(),
                f.getLocation(),
                f.getNotes()
        );
    }
}