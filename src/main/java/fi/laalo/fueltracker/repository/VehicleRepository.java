package fi.laalo.fueltracker.repository;


import fi.laalo.fueltracker.model.Vehicle;
import fi.laalo.fueltracker.model.User;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    List<Vehicle> findByUser(User user);
    List<Vehicle> findByUserId(Long userId);
    
    boolean existsByLicensePlate(String licensePlate);

}
