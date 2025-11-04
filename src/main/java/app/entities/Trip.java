package app.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Trip {
    // Each trip has a name, start and end times, location coordinates, price, and category
    // (e.g., beach, city, forest, lake, sea, snow).
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer tripId;
    private String name;
    @Column(name = "trip_start")
    private LocalDateTime start;
    @Column(name = "trip_end")
    private LocalDateTime end;
    private String locationCoordinates;
    private int price;
    @Enumerated(EnumType.STRING)
    private Category category;
    @ManyToOne
    @JoinColumn(name = "guide_id")//TODO hvorfor
    private Guide guide;

//    public void setGuide(Guide guide) {
//        this.guide = guide;
//    }


    public enum Category{
        BEACH, CITY, FOREST, LAKE, SEA, SNOW
    }
}
