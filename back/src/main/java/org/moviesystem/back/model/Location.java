package org.moviesystem.back.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "locations")
public class Location {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private int x;
    private long y;
    
    @NotNull
    @Column(nullable = false)
    private Double z;
    
    public Location() {}
    
    public Location(int x, long y, Double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public int getX() {
        return x;
    }
    
    public void setX(int x) {
        this.x = x;
    }
    
    public long getY() {
        return y;
    }
    
    public void setY(long y) {
        this.y = y;
    }
    
    public Double getZ() {
        return z;
    }
    
    public void setZ(Double z) {
        this.z = z;
    }
}
