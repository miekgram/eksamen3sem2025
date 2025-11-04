package app.routes;

import app.services.Populator;
import io.javalin.apibuilder.EndpointGroup;
import jakarta.persistence.EntityManagerFactory;

import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.path;

public class Routes {
    EntityManagerFactory emf;
    TripRoutes tripRoutes;

    public Routes(EntityManagerFactory emf){
        this.tripRoutes = new TripRoutes(emf);
        this.emf = emf;
    }

    public EndpointGroup getRoutes() {
        return () -> {
            get("/", context -> context.json("Api is running").status(200));
            get("/populate", Populator.populate());
            path("/trips", tripRoutes.getRoutes());
        };
    }
}
