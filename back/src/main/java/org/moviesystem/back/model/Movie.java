package org.moviesystem.back.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import jakarta.persistence.PreRemove;
import java.time.LocalDateTime;

@Entity
@Table(name = "movies")
public class Movie {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank
    @Column(nullable = false)
    private String name;
    
    @NotNull
    @OneToOne(cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    @JoinColumn(name = "coordinates_id", nullable = false)
    private Coordinates coordinates;
    
    @NotNull
    @Column(name = "creation_date", nullable = false)
    private LocalDateTime creationDate;


    @Min(0)
    @Column(name = "oscars_count", nullable = false)
    private int oscarsCount;
    
    @Positive
    private Double budget;
    
    @Positive
    @Column(name = "total_box_office", nullable = false)
    private long totalBoxOffice;
    
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "mpaa_rating", nullable = false)
    private MpaaRating mpaaRating;
    
    @NotNull
    @OneToOne(cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    @JoinColumn(name = "director_id", nullable = false)
    private Person director;
    
    @OneToOne(cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    @JoinColumn(name = "screenwriter_id")
    private Person screenwriter;
    
    @NotNull
    @OneToOne(cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    @JoinColumn(name = "operator_id", nullable = false)
    private Person operator;
    
    @NotNull
    @Positive
    @Column(nullable = false)
    private Long length;

    @Min(0)
    @Column(name = "golden_palm_count")
    private Long goldenPalmCount;
    
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MovieGenre genre;
    
    public Movie() {
        this.creationDate = LocalDateTime.now();
    }
    
    public Movie(String name, Coordinates coordinates, int oscarsCount, Double budget, 
                long totalBoxOffice, MpaaRating mpaaRating, Person director, 
                Person screenwriter, Person operator, Long length, 
                Long goldenPalmCount, MovieGenre genre) {
        this();
        this.name = name;
        this.coordinates = coordinates;
        this.oscarsCount = oscarsCount;
        this.budget = budget;
        this.totalBoxOffice = totalBoxOffice;
        this.mpaaRating = mpaaRating;
        this.director = director;
        this.screenwriter = screenwriter;
        this.operator = operator;
        this.length = length;
        this.goldenPalmCount = goldenPalmCount;
        this.genre = genre;
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
    
    public Coordinates getCoordinates() {
        return coordinates;
    }
    
    public void setCoordinates(Coordinates coordinates) {
        this.coordinates = coordinates;
    }
    
    public LocalDateTime getCreationDate() {
        return creationDate;
    }
    
    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }
    
    public int getOscarsCount() {
        return oscarsCount;
    }
    
    public void setOscarsCount(int oscarsCount) {
        this.oscarsCount = oscarsCount;
    }
    
    public Double getBudget() {
        return budget;
    }
    
    public void setBudget(Double budget) {
        this.budget = budget;
    }
    
    public long getTotalBoxOffice() {
        return totalBoxOffice;
    }
    
    public void setTotalBoxOffice(long totalBoxOffice) {
        this.totalBoxOffice = totalBoxOffice;
    }
    
    public MpaaRating getMpaaRating() {
        return mpaaRating;
    }
    
    public void setMpaaRating(MpaaRating mpaaRating) {
        this.mpaaRating = mpaaRating;
    }
    
    public Person getDirector() {
        return director;
    }
    
    public void setDirector(Person director) {
        this.director = director;
    }
    
    public Person getScreenwriter() {
        return screenwriter;
    }
    
    public void setScreenwriter(Person screenwriter) {
        this.screenwriter = screenwriter;
    }
    
    public Person getOperator() {
        return operator;
    }
    
    public void setOperator(Person operator) {
        this.operator = operator;
    }
    
    public Long getLength() {
        return length;
    }
    
    public void setLength(Long length) {
        this.length = length;
    }
    
    public Long getGoldenPalmCount() {
        return goldenPalmCount;
    }
    
    public void setGoldenPalmCount(Long goldenPalmCount) {
        this.goldenPalmCount = goldenPalmCount;
    }
    
    public MovieGenre getGenre() {
        return genre;
    }
    
    public void setGenre(MovieGenre genre) {
        this.genre = genre;
    }
    
    @PreRemove
    private void preRemove() {
        // Отключаем валидацию при удалении
        System.out.println("DEBUG: PreRemove callback для фильма: " + this.name);
    }
}
