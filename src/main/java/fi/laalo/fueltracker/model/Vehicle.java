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

    
    @Column(name = "brand", nullable = false)
    private String brand;
    
    @Column(name = "model", nullable = false)
    private String model;

    @Column(name = "fuel_type", nullable = false)
    private String fuelType;

    @Column(name = "manufacturing_year", nullable = false)
    private Integer manufacturingYear;

    @Column(name = "license_plate", nullable = false)
    private String licensePlate;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();
    



    // Relation to User
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;



    // Relation to FuelEntry
    @OneToMany(mappedBy = "vehicle", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FuelEntry> fuelEntries;




    // Constructor

    public Vehicle() {}

    public Vehicle(Long id, String brand, String model, String fuelType, Integer manufacturingYear, String licensePlate,
            Instant createdAt, User user, List<FuelEntry> fuelEntries) {
        this.id = id;
        this.brand = brand;
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

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
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
