package app.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Skill {
    //Each skill has a name, category (enum), and description.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer skillId;
    @Column(nullable = false)
    private String name;

    /** Unik slug til ekstern API (fx "spring-boot") *///TODO find ud af dette
    @Column(nullable = false, unique = true)
    private String slug;

    @Enumerated(EnumType.STRING)
    private Category category;
    @Column(nullable = false)
    private String description;
    @Builder.Default
    @OneToMany(mappedBy = "skill", cascade = { CascadeType.PERSIST, CascadeType.MERGE }, orphanRemoval = true, fetch = FetchType.EAGER)
    private Set<CandidateSkill> candidates = new HashSet<>();


    public enum Category {
        PROG_LANG, DB, DEVOPS, FRONTEND, TESTING, DATA, FRAMEWORK }


}
