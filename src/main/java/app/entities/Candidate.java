package app.entities;

import jakarta.persistence.*;
import lombok.*;


import java.util.HashSet;
import java.util.Set;

@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Candidate {
    //Each candidate has a name, phone, and education background.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer candidateId;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private String phone;
    @Column(nullable = false)
    private String education;
    @Builder.Default
    @OneToMany(mappedBy = "candidate", cascade = { CascadeType.PERSIST, CascadeType.MERGE }, orphanRemoval = true,fetch = FetchType.EAGER) //ingen remove ,  // orphanRemoval = false (default)
    private Set<CandidateSkill> skills = new HashSet<>();





}
