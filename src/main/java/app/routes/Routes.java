package app.routes;

import app.services.Populator;
import io.javalin.apibuilder.EndpointGroup;
import jakarta.persistence.EntityManagerFactory;

import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.path;

public class Routes {
    EntityManagerFactory emf;
    CandidateRoutes candidateRoutes;
    ReportRoutes reportRoutes;


    public Routes(EntityManagerFactory emf){
        this.candidateRoutes = new CandidateRoutes(emf);
        this.reportRoutes = new ReportRoutes(emf);
        this.emf = emf;
    }

    public EndpointGroup getRoutes() {
        return () -> {
            get("/", context -> context.json("Api is running").status(200));
            //få noget i db
            get("/populate", Populator.populate());
            //alt med kandidaterne
            path("/candidates", candidateRoutes.getRoutes());
            path("/reports", reportRoutes.getRoutes());

        };
    }
}
