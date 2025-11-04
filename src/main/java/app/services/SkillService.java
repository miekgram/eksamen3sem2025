package app.services;

import app.entities.SkillStatsApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SkillService {

    private static final String BASE_URL =
            "https://apiprovider.cphbusinessapps.dk/api/v1/skills/stats?slugs=";

    public List<SkillStatsApiResponse.SkillData> getStatsBySlugs(Collection<String> slugs) {
        List<SkillStatsApiResponse.SkillData> items = new ArrayList<>();

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        HttpClient httpClient = HttpClient.newHttpClient();

        try {
            if (slugs == null || slugs.isEmpty()) {
                return items;
            }

            String joined = String.join(",", slugs).toLowerCase();
            String url = BASE_URL + URLEncoder.encode(joined, StandardCharsets.UTF_8);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(url))
                    .GET()
                    .build();

            HttpResponse<String> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("Skill Stats raw JSON: " + response.body());

            if (response.statusCode() == 200) {
                // Læs direkte til en response-klasse og kopier ud i items
                SkillStatsApiResponse apiResponse =
                        objectMapper.readValue(response.body(), SkillStatsApiResponse.class);

                if (apiResponse != null && apiResponse.getData() != null) {
                    for (SkillStatsApiResponse.SkillData s : apiResponse.getData()) {
                        items.add(s);
                    }
                }
            } else {
                System.out.println("Fejl ved læsning af Skill Stats API. HTTP: " + response.statusCode());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return items;
    }
}
