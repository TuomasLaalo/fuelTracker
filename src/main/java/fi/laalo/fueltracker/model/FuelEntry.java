package fi.laalo.fueltracker.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.time.Instant;


@Entity
@Table(name = "fuel_entries")
public class FuelEntry {

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime dateTime;
    
    @NotNull
    @Positive
    @Column(nullable = false)
    private Double litres;
    
    @NotNull
    @Positive
    @Column(nullable = false)
    private Double odometer;
    
    @Column(nullable = false)
    private double pricePerLitre;
    
    @Column(nullable = false)
    private double totalPrice;
    
    @Column(length = 255)
    private String location;
    
    @Column(length = 1000)
    private String notes;
    
    @Column(nullable = false)
    private Boolean fullTank;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at")
    private Instant updatedAt;

    // Relation to Vehicle
    @ManyToOne
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    // Lifecycle callbacks
    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }


    // Getters and Setters


    public User getUser() {
        return user;
    }
    public void setUser(User user) {
        this.user = user;
    }
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public LocalDateTime getDateTime() {
        return dateTime;
    }
    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }
    public Double getLitres() {
        return litres;
    }
    public void setLitres(Double litres) {
        this.litres = litres;
    }
    public Double getOdometer() {
        return odometer;
    }
    public void setOdometer(Double odometer) {
        this.odometer = odometer;
    }
    public Double getPricePerLitre() {
        return pricePerLitre;
    }
    public void setPricePerLitre(Double pricePerLitre) {
        this.pricePerLitre = pricePerLitre;
    }
    public Double getTotalPrice() {
        return totalPrice;
    }
    public void setTotalPrice(Double totalPrice) {
        this.totalPrice = totalPrice;
    }
    public String getLocation() {
        return location;
    }
    public void setLocation(String location) {
        this.location = location;
    }
    public String getNotes() {
        return notes;
    }
    public void setNotes(String notes) {
        this.notes = notes;
    }
    public Boolean getFullTank() {
        return fullTank;
    }
    public void setFullTank(Boolean fullTank) {
        this.fullTank = fullTank;
    }
    public Vehicle getVehicle() {
        return vehicle;
    }
    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
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
   
}
