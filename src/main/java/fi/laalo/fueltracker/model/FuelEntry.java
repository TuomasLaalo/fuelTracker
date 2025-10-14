package fi.laalo.fueltracker.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;



public class FuelEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime dateTime;
    private Double litres;
    private Double odometer;
    private Double pricePerLitre;
    private Double totalPrice;
    private String location;
    private String notes;
    private Boolean fullTank;


    // Getters and Setters


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
   
}
