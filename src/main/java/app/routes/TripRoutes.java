package app.routes;

import app.controllers.TripController;
import app.security.entities.User;
import io.javalin.apibuilder.EndpointGroup;
import jakarta.persistence.EntityManagerFactory;


import static io.javalin.apibuilder.ApiBuilder.*;
import static io.javalin.apibuilder.ApiBuilder.delete;

public class TripRoutes {
    EntityManagerFactory emf;
    TripController tripController;

    public TripRoutes(EntityManagerFactory emf){
        tripController = new TripController(emf);
        this.emf = emf;
    }

    public EndpointGroup getRoutes(){
        return () -> {
            get("/", tripController.getAll(), User.Role.ANYONE);
            get("/{id}", tripController.getById(), User.Role.ANYONE); //TODO Include packing items for this route
            post("/", tripController.create(), User.Role.ADMIN);
            put("/{id}", tripController.update(), User.Role.ADMIN);
            delete("/{id}", tripController.delete(), User.Role.ADMIN);
            put("/{id}/guides/{guideId}", tripController.updateGuideForTrip(), User.Role.ADMIN);
            get("?category={category}", tripController.getByCategory(), User.Role.ANYONE);
            get("/{id}/packing", tripController.getPackingItems(), User.Role.ANYONE);
            get("/{id}/packing/weight", tripController.getPackingWeight(), User.Role.ANYONE);
            get("/guides/totalprice",tripController.getGuidesTotalPrice(), User.Role.ANYONE);
        };
    }
}
