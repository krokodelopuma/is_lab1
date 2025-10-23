package org.moviesystem.back.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

@Entity
@Table(name = "persons")
public class Person {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank
    @Column(nullable = false)
    private String name;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "eye_color")
    private Color eyeColor;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "hair_color")
    private Color hairColor;
    
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id")
    private Location location;
    
    @NotNull
    @Column(nullable = false)
    private LocalDate birthday;
    
    @Enumerated(EnumType.STRING)
    private Country nationality;
    
    public Person() {}
    
    public Person(String name, LocalDate birthday) {
        this.name = name;
        this.birthday = birthday;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public Color getEyeColor() {
        return eyeColor;
    }
    
    public void setEyeColor(Color eyeColor) {
        this.eyeColor = eyeColor;
    }
    
    public Color getHairColor() {
        return hairColor;
    }
    
    public void setHairColor(Color hairColor) {
        this.hairColor = hairColor;
    }
    
    public Location getLocation() {
        return location;
    }
    
    public void setLocation(Location location) {
        this.location = location;
    }
    
    public LocalDate getBirthday() {
        return birthday;
    }
    
    public void setBirthday(LocalDate birthday) {
        this.birthday = birthday;
    }
    
    public Country getNationality() {
        return nationality;
    }
    
    public void setNationality(Country nationality) {
        this.nationality = nationality;
    }
}
