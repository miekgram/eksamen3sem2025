package app.controllers;

import app.daos.GuideDAO;
import app.daos.TripDAO;
import app.dtos.GuideDTO;
import app.dtos.GuideTotalPriceDTO;
import app.dtos.TripDTO;
import app.entities.Guide;
import app.entities.Item;
import app.entities.Trip;
import app.exceptions.ApiException;
import app.services.Converters;
import app.services.PackingService;
import io.javalin.http.Handler;
import io.javalin.http.HttpStatus;
import io.javalin.http.UnauthorizedResponse;
import jakarta.persistence.EntityManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TripController implements IController {

    private static final Logger log = LoggerFactory.getLogger(TripController.class);

    private final Converters converter;
    private final TripDAO tripDAO;
    private final GuideDAO guideDAO;
    private final EntityManagerFactory emf;
    private final PackingService packingService = new PackingService();

    public TripController(EntityManagerFactory emf) {
        this.converter = new Converters();
        this.tripDAO = new TripDAO(emf);
        this.guideDAO = new GuideDAO(emf);
        this.emf = emf;
    }

    // GET /trips?category=...
    @Override
    public Handler getAll() {
        return ctx -> {
            String category = ctx.queryParam("category");
            log.info("GET /trips (category={})", category);

            List<Trip> trips = tripDAO.getAll();
            if (category != null && !category.isBlank()) {
                String wanted = category.trim();
                trips = trips.stream()
                        .filter(t -> t.getCategory().name().equalsIgnoreCase(wanted))
                        .collect(Collectors.toList());
            }

            ctx.status(HttpStatus.OK).json(TripDTO.getEntities(trips));
        };
    }

    // GET /trips/{id}
    @Override
    public Handler getById() {
        return ctx -> {
            Integer id = Integer.parseInt(ctx.pathParam("id"));
            if (id <= 0) throw new ApiException(400, "id must be bigger than 0");
            log.info("GET /trips/{}", id);

            Trip trip = tripDAO.getById(id);
            if (trip == null) {
                log.warn("trip {} not found", id);
                throw new ApiException(404, "No trip with id: " + id);
            }

            TripDTO dto = new TripDTO(trip);
            dto.setPackingItems(packingService.getPackingItemsByCategory(trip.getCategory()));
            ctx.status(HttpStatus.OK).json(dto);
        };
    }

    // POST /trips
    @Override
    public Handler create() {
        return ctx -> {

            log.info("CREATE Authorization header = {}", ctx.header("Authorization"));

            String header = ctx.header("Authorization");

            if (header == null || !header.toLowerCase().startsWith("bearer ")) {
                throw new UnauthorizedResponse("Login required");
            }

            log.info("POST /trips");
            TripDTO body = ctx.bodyAsClass(TripDTO.class);

            if (body.getName() == null || body.getName().isBlank())
                throw new ApiException(400, "name is required");

            Trip entity = converter.convertTripDTOToTrip(body);

            if (body.getGuideDTO() != null && body.getGuideDTO().getGuideId() != null) {
                Guide guide = guideDAO.getById(body.getGuideDTO().getGuideId());
                if (guide == null) throw new ApiException(404, "guide not found: " + body.getGuideDTO().getGuideId());
                entity.setGuide(guide);
            }

            Trip created = tripDAO.create(entity);
            log.info("created trip id={}", created.getTripId());
            ctx.status(HttpStatus.CREATED).json(new TripDTO(created));
        };
    }

    // PUT /trips/{id}
    @Override
    public Handler update() {
        return ctx -> {
            Integer id = Integer.parseInt(ctx.pathParam("id"));
            if (id <= 0) throw new ApiException(400, "id must be > 0");
            log.info("PUT /trips/{}", id);

            TripDTO body = ctx.bodyAsClass(TripDTO.class);
            if (body.getName() == null || body.getName().isBlank())
                throw new ApiException(400, "name is required");
            if (body.getCategory() == null)
                throw new ApiException(400, "category is required");

            Trip existing = tripDAO.getById(id);
            if (existing == null) throw new ApiException(404, "No trip with id: " + id);

            body.setTripId(id);
            Trip saved = tripDAO.update(converter.convertTripDTOToTrip(body));
            ctx.json(new TripDTO(saved));
        };
    }

    // DELETE /trips/{id}
    @Override
    public Handler delete() {
        return ctx -> {
            Integer id = Integer.parseInt(ctx.pathParam("id"));
            if (id <= 0) throw new ApiException(400, "id must be > 0");

            log.info("DELETE /trips/{}", id);
            Trip found = tripDAO.getById(id);
            if (found == null) {
                log.warn("trip {} not found", id);
                throw new ApiException(404, "No trip with that id: " + id);
            }

            TripDTO dto = new TripDTO(found);
            tripDAO.delete(dto.getTripId());
            ctx.json(dto); // bevar 200 + slettet body som før
        };
    }

    // PUT /trips/{id}/guides/{guideId}
    public Handler updateGuideForTrip() {
        return ctx -> {
            Integer tripId = Integer.parseInt(ctx.pathParam("id"));
            Integer guideId = Integer.parseInt(ctx.pathParam("guideId"));
            if (tripId <= 0 || guideId <= 0) throw new ApiException(400, "ids must be > 0");

            log.info("PUT /trips/{}/guides/{}", tripId, guideId);

            Trip trip = tripDAO.getById(tripId);
            if (trip == null) throw new ApiException(404, "No trip with id: " + tripId);
            Guide guide = guideDAO.getById(guideId);
            if (guide == null) throw new ApiException(404, "No guide with id: " + guideId);

            TripDTO tripFound = new TripDTO(trip);
            GuideDTO guideFound = new GuideDTO(guide);
            guideFound.setGuideId(guideId);
            tripFound.setTripId(tripId);
            tripFound.setGuide(converter.convertGuideDTOToGuide(guideFound));

            Trip updated = tripDAO.update(converter.convertTripDTOToTrip(tripFound));
            ctx.json(new TripDTO(updated));
        };
    }

    // GET /trips/category/{category}
    public Handler getByCategory() {
        return ctx -> {
            String category = ctx.pathParam("category");
            if (category == null || category.isBlank()) throw new ApiException(400, "category is required");

            log.info("GET /trips/category/{}", category);
            List<Trip> trips = tripDAO.getAll().stream()
                    .filter(t -> t.getCategory().name().equalsIgnoreCase(category))
                    .collect(Collectors.toList());

            ctx.json(TripDTO.getEntities(trips));
        };
    }

    // GET /trips/{id}/packing/items
    public Handler getPackingItems() {
        return ctx -> {
            Integer tripId = Integer.parseInt(ctx.pathParam("id"));
            if (tripId <= 0) throw new ApiException(400, "id must be > 0");
            log.info("GET /trips/{}/packing/items", tripId);

            Trip trip = tripDAO.getById(tripId);
            if (trip == null) throw new ApiException(404, "No trip with id: " + tripId);

            List<Item> items = packingService.getPackingItemsByCategory(trip.getCategory());
            ctx.json(items);
        };
    }

    // GET /trips/{id}/packing/weight
    public Handler getPackingWeight() {
        return ctx -> {
            Integer tripId = Integer.parseInt(ctx.pathParam("id"));
            if (tripId <= 0) throw new ApiException(400, "id must be > 0");
            log.info("GET /trips/{}/packing/weight", tripId);

            Trip trip = tripDAO.getById(tripId);
            if (trip == null) throw new ApiException(404, "No trip with id: " + tripId);

            int packingWeight = packingService.getPackingWeight(trip);
            ctx.json("U must pack " + packingWeight + " grams - Good luck");
        };
    }

    // GET /guides/totalprice
    public Handler getGuidesTotalPrice() {
        return ctx -> {
            log.info("GET /guides/totalprice");
            List<Guide> guides = guideDAO.getAll();
            List<GuideTotalPriceDTO> res = new ArrayList<>();
            for (Guide g : guides) {
                int totalPrice = 0;
                for (Trip t : g.getTrips()) {
                    totalPrice += t.getPrice();
                }
                res.add(new GuideTotalPriceDTO(
                        g.getName(), g.getEmail(), g.getTrips().size(), totalPrice
                ));
            }
            ctx.json(res);
        };
    }
}
