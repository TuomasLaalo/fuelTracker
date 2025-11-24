package fi.laalo.fueltracker;

import fi.laalo.fueltracker.model.User;
import fi.laalo.fueltracker.model.Vehicle;
import fi.laalo.fueltracker.model.FuelEntry;
import fi.laalo.fueltracker.repository.UserRepository;
import fi.laalo.fueltracker.repository.VehicleRepository;
import fi.laalo.fueltracker.repository.FuelEntryRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.List;

@SpringBootTest
public class RepositoryIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private FuelEntryRepository fuelEntryRepository;

    //  1. Käyttäjän ja ajoneuvon luonti
    @Test
    void testAddVehicleWithUser() {
        User user = new User();
        user.setName("Testikäyttäjä");
        user.setEmail("testi@example.com");
        user.setPassword("salasana123");
        userRepository.save(user);
        System.out.println("✅ Luotu käyttäjä: " + user.getName() + " (id=" + user.getId() + ")");

        Vehicle v = new Vehicle();
        v.setMake("Toyota");
        v.setModel("Corolla");
        v.setManufacturingYear(2015);
        v.setFuelType("Bensa");
        v.setLicensePlate("ABC-123");
        v.setUser(user);
        vehicleRepository.save(v);
        System.out.println("✅ Lisätty ajoneuvo käyttäjälle " + user.getName() + ": " + v.getMake() + " " + v.getModel());
    }

    //  2. Tankkauksen luonti
    @Test
    void testAddFuelEntry() {
        Vehicle vehicle = vehicleRepository.findAll().stream().findFirst().orElseThrow(
                () -> new RuntimeException("Ajoneuvoa ei löytynyt, aja ensin testAddVehicleWithUser()")
        );

        FuelEntry entry = new FuelEntry();
        entry.setLitres(45.2);
        entry.setPricePerLitre(1.89);
        entry.setTotalPrice(45.2 * 1.89);
        entry.setOdometer(520.0);
        entry.setDateTime(LocalDateTime.now());
        entry.setVehicle(vehicle);

        fuelEntryRepository.save(entry);
        System.out.println("✅ Lisätty tankkaus ajoneuvolle " + vehicle.getLicensePlate());
    }

    //  3. Listaa kaikki ajoneuvot
    @Test
    void listAllVehicles() {
        List<Vehicle> vehicles = vehicleRepository.findAll();
        if (vehicles.isEmpty()) {
            System.out.println("ℹ️ Ei ajoneuvoja tietokannassa.");
        } else {
            System.out.println("🚗 Ajoneuvot tietokannassa:");
            for (Vehicle v : vehicles) {
                System.out.println("- " + v.getMake() + " " + v.getModel() +
                        " (" + v.getManufacturingYear() + "), rek: " + v.getLicensePlate());
            }
        }
    }

    //  4. Listaa kaikki tankkaukset
    @Test
    void listAllFuelEntries() {
        List<FuelEntry> entries = fuelEntryRepository.findAll();
        if (entries.isEmpty()) {
            System.out.println("ℹ️ Ei tankkauksia tietokannassa.");
        } else {
            System.out.println("⛽ Tankkaukset tietokannassa:");
            for (FuelEntry e : entries) {
                System.out.println("- " + e.getVehicle().getLicensePlate() +
                        ": " + e.getLitres() + " litraa, " +
                        e.getTotalPrice() + " €, " +
                        e.getOdometer() + " km, " +
                        e.getDateTime());
            }
        }
    }

    //  5. Test unique license plate constraint
    @Test
    void testDuplicateLicensePlate() {
        try {
            User user = userRepository.findAll().stream().findFirst().orElseThrow();
            
            Vehicle v1 = new Vehicle();
            v1.setMake("Volvo");
            v1.setModel("V70");
            v1.setManufacturingYear(2010);
            v1.setFuelType("Diesel");
            v1.setLicensePlate("XYZ-999");
            v1.setUser(user);
            vehicleRepository.save(v1);

            Vehicle v2 = new Vehicle();
            v2.setMake("Ford");
            v2.setModel("Focus");
            v2.setManufacturingYear(2012);
            v2.setFuelType("Bensa");
            v2.setLicensePlate("XYZ-999"); // Same license plate!
            v2.setUser(user);
            vehicleRepository.save(v2);

            System.out.println("❌ FAIL: Duplikaatti rekisteritunnus hyväksyttiin!");
        } catch (Exception e) {
            System.out.println("✅ PASS: Duplikaatti rekisteritunnus estetty oikein");
        }
    }

    //  6. Test finding vehicles by user
    @Test
    void testFindVehiclesByUser() {
        User user = userRepository.findAll().stream().findFirst().orElseThrow();
        List<Vehicle> userVehicles = vehicleRepository.findByUserId(user.getId());
        
        System.out.println("🔍 Käyttäjän " + user.getName() + " ajoneuvot:");
        if (userVehicles.isEmpty()) {
            System.out.println("   Ei ajoneuvoja");
        } else {
            for (Vehicle v : userVehicles) {
                System.out.println("   - " + v.getMake() + " " + v.getModel() + " (" + v.getLicensePlate() + ")");
            }
        }
    }

    //  7. Test vehicle existence check by license plate
    @Test
    void testVehicleExistsByLicensePlate() {
        boolean exists = vehicleRepository.existsByLicensePlate("ABC-123");
        boolean notExists = vehicleRepository.existsByLicensePlate("XXX-000");
        
        System.out.println("✅ ABC-123 exists: " + exists);
        System.out.println("✅ XXX-000 exists: " + notExists);
        
        if (exists && !notExists) {
            System.out.println("✅ PASS: existsByLicensePlate() toimii oikein");
        } else {
            System.out.println("❌ FAIL: existsByLicensePlate() ei toimi oikein");
        }
    }
}
