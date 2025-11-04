package app.services;

import app.entities.SkillStatsApiResponse;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@ToString
@Getter
public class PackingResponseDTO {
    private List<SkillStatsApiResponse> items;
}
