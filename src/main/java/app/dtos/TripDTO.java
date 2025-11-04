package app.dtos;

import app.entities.Guide;
import app.entities.Item;
import app.entities.Trip;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
@Builder
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class TripDTO {
    private Integer tripId;
    private String name;
    private LocalDateTime start;
    private LocalDateTime end;
    private String locationCoordinates;
    private int price;
    private Trip.Category category;
    private GuideDTO guideDTO;

    private List<Item> packingItems;

    public TripDTO(Trip trip) {
        if(trip.getTripId()!=null)
            this.tripId=trip.getTripId();
        this.name = trip.getName();
        this.start = trip.getStart();
        this.end = trip.getEnd();
        this.locationCoordinates = trip.getLocationCoordinates();
        this.price = trip.getPrice();
        this.category = trip.getCategory();
        this.guideDTO = new GuideDTO(trip.getGuide());


    }


//denne dto henter trip og converterer den til en dto
    public static Set<TripDTO> getEntities(List<Trip> trips) {//TODO forstå - hvorfor set ?
        return trips.stream().map(trip -> new TripDTO(trip)).collect(Collectors.toSet());
    }

    public void setTripId(Integer id) {
        this.tripId = id;
    }

    public void setGuide(Guide guide) {
        this.guideDTO = new GuideDTO(guide);
    }



    public void setPackingItems(List<Item> packingItems) {
        this.packingItems = packingItems;
    }
}
