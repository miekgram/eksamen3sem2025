package app.services;

import app.daos.TripDAO;
import app.entities.Item;
import app.entities.Trip;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.persistence.EntityManagerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public class PackingService {


    public List<Item> getPackingItemsByCategory(Trip.Category category){
        List<Item> items = new ArrayList<>();

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);//TODO undersøg denne

        //Fetch data from API
        HttpClient httpClient = HttpClient.newHttpClient();

        try {
            // Create a request
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI("https://packingapi.cphbusinessapps.dk/packinglist/" +category.name().toLowerCase()))
                    .GET()
                    .build();

            // Send the request and get the response (data)
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("Raw JSON: " + response.body());
            //Check if it went well (200)
            if (response.statusCode() == 200) {
                String json = response.body();
                PackingResponseDTO packingResponseDTO = objectMapper.readValue(response.body(), PackingResponseDTO.class);

                items.addAll(packingResponseDTO.getItems());

            } else {
                System.out.println("Fejl ved læsning af packingItem-API. Fejl: " + response.statusCode());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return items;
    }

    public Integer getPackingWeight(Trip trip){
        List<Item> items = getPackingItemsByCategory(trip.getCategory());
        int count = 0;
        for(Item i : items){
            count += i.getWeightInGrams();
        }
        return count;
    }
}
