package fi.laalo.fueltracker.repository;

import fi.laalo.fueltracker.model.FuelEntry;
import fi.laalo.fueltracker.model.Vehicle;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FuelEntryRepository extends JpaRepository<FuelEntry, Long> {

    List<FuelEntry> findByUserId(Long userId);
    List<FuelEntry> findByVehicle(Vehicle vehicle);
    List<FuelEntry> findByVehicleOrderByDateTimeAsc(Vehicle vehicle);
        
}
