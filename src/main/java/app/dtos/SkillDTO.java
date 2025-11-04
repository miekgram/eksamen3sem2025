package app.dtos;

import app.entities.Skill;
import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
@Builder
public class SkillDTO {
    private Integer id;
    private String name;
    private String slug;
    private Skill.Category category;
    private String description;

    // Enrichment (US-5) – kan være null hvis ukendt i API
    private Integer popularityScore;
    private Integer averageSalary;

    public SkillDTO(Skill skill) {
        if (skill.getSkillId() != null) this.id = skill.getSkillId();
        this.name = skill.getName();
        this.slug = skill.getSlug();
        this.category = skill.getCategory();
        this.description = skill.getDescription();
    }
}
