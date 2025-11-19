package fi.laalo.fueltracker.mapper;

import fi.laalo.fueltracker.dto.VehicleRequestDTO;
import fi.laalo.fueltracker.dto.VehicleResponseDTO;
import fi.laalo.fueltracker.model.Vehicle;

public class VehicleMapper {

    public static VehicleResponseDTO toDto(Vehicle v) {
        return new VehicleResponseDTO(
                v.getId(),
                v.getMake(),
                v.getModel(),
                v.getFuelType(),
                v.getManufacturingYear(),
                v.getLicensePlate()
        );
    }

    public static Vehicle fromDto(VehicleRequestDTO dto) {
        Vehicle v = new Vehicle();
        v.setMake(dto.make());
        v.setModel(dto.model());
        v.setFuelType(dto.fuelType());
        v.setManufacturingYear(dto.manufacturingYear());
        v.setLicensePlate(dto.licensePlate());
        return v;
    }
}