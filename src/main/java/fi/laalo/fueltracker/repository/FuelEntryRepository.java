package fi.laalo.fueltracker.repository;

import fi.laalo.fueltracker.model.FuelEntry;
import fi.laalo.fueltracker.model.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FuelEntryRepository extends JpaRepository<FuelEntry, Long> {

    List<FuelEntry> findByUserId(Long userId);
    List<FuelEntry> findByVehicle(Vehicle vehicle);
    List<FuelEntry> findByVehicleOrderByDateTimeAsc(Vehicle vehicle);
    
    @Query("SELECT f FROM FuelEntry f WHERE f.vehicle = :vehicle AND f.dateTime >= :startDate AND f.dateTime < :endDate ORDER BY f.dateTime ASC")
    List<FuelEntry> findByVehicleAndDateRange(@Param("vehicle") Vehicle vehicle, 
                                               @Param("startDate") LocalDateTime startDate, 
                                               @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT f FROM FuelEntry f WHERE f.user.id = :userId AND f.dateTime >= :startDate AND f.dateTime < :endDate ORDER BY f.dateTime ASC")
    List<FuelEntry> findByUserIdAndDateRange(@Param("userId") Long userId, 
                                              @Param("startDate") LocalDateTime startDate, 
                                              @Param("endDate") LocalDateTime endDate);
        
}
