package app.dtos;

import app.entities.Guide;
import lombok.*;
import java.util.HashSet;
import java.util.Set;


@Getter
@NoArgsConstructor
@ToString
@EqualsAndHashCode
@AllArgsConstructor
@Builder

public class GuideDTO {
    private Integer guideId;
    private String name;
    private String email;
    private String phone;
    private int yearsOfExp;
    @ToString.Exclude
    @Builder.Default
    private Set<TripDTO> tripDTOs = new HashSet<>();

    public GuideDTO (Guide guide){
        this.guideId = guide.getGuideId();
        this.name = guide.getName();
        this.email = guide.getEmail();
        this.phone =guide.getPhone();
        this.yearsOfExp = guide.getYearsOfExp();
        //this.tripDTOs = guide.getTrips().stream().map(trip -> new TripDTO(trip)).collect(Collectors.toSet());
    }

    public void setGuideId(Integer guideId) {
        this.guideId = guideId;
    }
}

