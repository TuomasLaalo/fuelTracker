package fi.laalo.fueltracker.config;

import fi.laalo.fueltracker.model.FuelEntry;
import fi.laalo.fueltracker.model.User;
import fi.laalo.fueltracker.model.Vehicle;
import fi.laalo.fueltracker.repository.FuelEntryRepository;
import fi.laalo.fueltracker.repository.UserRepository;
import fi.laalo.fueltracker.repository.VehicleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class TestDataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final VehicleRepository vehicleRepository;
    private final FuelEntryRepository fuelEntryRepository;
    private final PasswordEncoder passwordEncoder;

    public TestDataInitializer(UserRepository userRepository,
                               VehicleRepository vehicleRepository,
                               FuelEntryRepository fuelEntryRepository,
                               PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.vehicleRepository = vehicleRepository;
        this.fuelEntryRepository = fuelEntryRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // Check if test user already exists
        if (userRepository.findByEmail("test@example.com") != null) {
            System.out.println("Test data already exists, skipping initialization.");
            return;
        }

        System.out.println("Initializing test data...");

        // Create test user
        User testUser = new User();
        testUser.setEmail("test@example.com");
        testUser.setPassword(passwordEncoder.encode("testpassword123"));
        testUser.setName("Test User");
        testUser.setRole("USER");
        testUser = userRepository.save(testUser);
        System.out.println("Created test user: " + testUser.getEmail());

        // Create test vehicle
        Vehicle testVehicle = new Vehicle();
        testVehicle.setMake("Toyota");
        testVehicle.setModel("Corolla");
        testVehicle.setFuelType("Gasoline");
        testVehicle.setManufacturingYear(2020);
        testVehicle.setLicensePlate("ABC-123");
        testVehicle.setUser(testUser);
        testVehicle = vehicleRepository.save(testVehicle);
        System.out.println("Created test vehicle: " + testVehicle.getMake() + " " + testVehicle.getModel());

        // Create test fuel entries
        LocalDateTime baseDate = LocalDateTime.now().minusMonths(2);
        
        // Entry 1
        FuelEntry entry1 = new FuelEntry();
        entry1.setUser(testUser);
        entry1.setVehicle(testVehicle);
        entry1.setDateTime(baseDate);
        entry1.setLitres(45.0);
        entry1.setOdometer(10000.0);
        entry1.setPricePerLitre(1.85);
        entry1.setTotalPrice(83.25);
        entry1.setLocation("Helsinki");
        entry1.setNotes("First fill-up");
        entry1.setFullTank(true);
        fuelEntryRepository.save(entry1);

        // Entry 2 (2 weeks later)
        FuelEntry entry2 = new FuelEntry();
        entry2.setUser(testUser);
        entry2.setVehicle(testVehicle);
        entry2.setDateTime(baseDate.plusWeeks(2));
        entry2.setLitres(42.5);
        entry2.setOdometer(10600.0);
        entry2.setPricePerLitre(1.88);
        entry2.setTotalPrice(79.90);
        entry2.setLocation("Espoo");
        entry2.setNotes("Second fill-up");
        entry2.setFullTank(true);
        fuelEntryRepository.save(entry2);

        // Entry 3 (1 month later)
        FuelEntry entry3 = new FuelEntry();
        entry3.setUser(testUser);
        entry3.setVehicle(testVehicle);
        entry3.setDateTime(baseDate.plusMonths(1));
        entry3.setLitres(40.0);
        entry3.setOdometer(11200.0);
        entry3.setPricePerLitre(1.90);
        entry3.setTotalPrice(76.00);
        entry3.setLocation("Vantaa");
        entry3.setNotes("Third fill-up");
        entry3.setFullTank(true);
        fuelEntryRepository.save(entry3);

        // Entry 4 (recent)
        FuelEntry entry4 = new FuelEntry();
        entry4.setUser(testUser);
        entry4.setVehicle(testVehicle);
        entry4.setDateTime(LocalDateTime.now().minusDays(3));
        entry4.setLitres(43.0);
        entry4.setOdometer(11800.0);
        entry4.setPricePerLitre(1.92);
        entry4.setTotalPrice(82.56);
        entry4.setLocation("Helsinki");
        entry4.setNotes("Recent fill-up");
        entry4.setFullTank(true);
        fuelEntryRepository.save(entry4);

        System.out.println("Created 4 test fuel entries");
        System.out.println("Test data initialization complete!");
        System.out.println("You can login with:");
        System.out.println("  Email: test@example.com");
        System.out.println("  Password: testpassword123");
    }
}

