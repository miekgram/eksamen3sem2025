package app.dtos;

import app.entities.Candidate;
import app.entities.CandidateSkill;
import lombok.*;

import java.util.Set;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
@Builder
public class CandidateDTO {
    private Integer id;
    private String name;
    private String phone;
    private String education;


    @ToString.Exclude
    @Builder.Default
    private Set<SkillDTO> skills = Set.of();

    public CandidateDTO(Candidate candidate) {
        if (candidate.getCandidateId() != null) this.id = candidate.getCandidateId();
        this.name = candidate.getName();
        this.phone = candidate.getPhone();
        this.education = candidate.getEducation();


    }
}
