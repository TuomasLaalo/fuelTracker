package fi.laalo.fueltracker.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "vehicles")

public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "make", nullable = false)
    private String make;

    @Column(name = "model", nullable = false)
    private String model;

    @Column(name = "fuel_type", nullable = false)
    private String fuelType;

    @Column(name = "manufacturing_year", nullable = false)
    private Integer manufacturingYear;

    @Column(name = "license_plate", nullable = false, unique = true)
    private String licensePlate;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at")
    private Instant updatedAt;

    


    // Relation to User
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Relation to FuelEntry
    @OneToMany(mappedBy = "vehicle", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FuelEntry> fuelEntries;

    // Lifecycle callbacks
    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    // Constructors

    public Vehicle() {}

    public Vehicle(Long id, String make, String model, String fuelType, Integer manufacturingYear, String licensePlate,
            Instant createdAt, User user, List<FuelEntry> fuelEntries) {
        this.id = id;
        this.make = make;
        this.model = model;
        this.fuelType = fuelType;
        this.manufacturingYear = manufacturingYear;
        this.licensePlate = licensePlate;
        this.createdAt = createdAt;
        this.user = user;
        this.fuelEntries = fuelEntries;
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMake() {
        return make;
    }

    public void setMake(String make) {
        this.make = make;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getFuelType() {
        return fuelType;
    }

    public void setFuelType(String fuelType) {
        this.fuelType = fuelType;
    }

    public Integer getManufacturingYear() {
        return manufacturingYear;
    }

    public void setManufacturingYear(Integer manufacturingYear) {
        this.manufacturingYear = manufacturingYear;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<FuelEntry> getFuelEntries() {
        return fuelEntries;
    }

    public void setFuelEntries(List<FuelEntry> fuelEntries) {
        this.fuelEntries = fuelEntries;
    }



}
