package fi.laalo.fueltracker.repository;

import org.springframework.stereotype.Repository;
import fi.laalo.fueltracker.model.User;

import org.springframework.data.jpa.repository.JpaRepository;

@Repository

public interface UserRepository extends JpaRepository<User, Long> {

    User findByEmail(String email);

}
