package app.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Guide {
    //Each guide has personal information (name, email, phone, years of experience)
    // and can be associated with one or more trips.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer guideId;
    private String name;
    private String email;
    private String phone;
    private int yearsOfExp;
    //TODO gør ALL til noget andet og sæt opherval noget på så du selv skla slette
    @Builder.Default//måske
    @OneToMany(mappedBy = "guide", cascade = { CascadeType.PERSIST, CascadeType.MERGE }, // ingen REMOVE!
            fetch = FetchType.LAZY
           // orphanRemoval = false undladt fordi den pr. default er false
    )    private Set<Trip> trips = new HashSet<>();


    /** Helper: hold bidirektionel relation i sync */
    //måske noget addTrip(Trip trip)
    // samme med remove


}
