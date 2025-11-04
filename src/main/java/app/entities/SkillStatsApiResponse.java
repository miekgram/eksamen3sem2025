package app.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.List;

@ToString
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class SkillStatsApiResponse {
    private List<SkillData> data;


    @Getter @Setter
    @NoArgsConstructor @AllArgsConstructor @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SkillData {
        private String id;
        private String slug;
        private String name;
        private String categoryKey;
        private String description;
        private Integer popularityScore;
        private Integer averageSalary;
        private OffsetDateTime updatedAt; // "2025-10-01T10:15:00.000Z"
    }
}

