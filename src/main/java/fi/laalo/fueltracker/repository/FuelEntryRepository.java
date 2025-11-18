package fi.laalo.fueltracker.repository;

import fi.laalo.fueltracker.model.FuelEntry;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FuelEntryRepository extends JpaRepository<FuelEntry, Long> {

    List<FuelEntry> findByUserId(Long userId);

        
}
