package app.routes;

import app.controllers.CandidateController;
import app.security.entities.User;
import io.javalin.apibuilder.EndpointGroup;
import jakarta.persistence.EntityManagerFactory;

import static io.javalin.apibuilder.ApiBuilder.get;

public class ReportRoutes {
    private final CandidateController candidateController;

    public ReportRoutes(EntityManagerFactory emf){
        this.candidateController = new CandidateController(emf);
    }

    public EndpointGroup getRoutes(){
        return () -> {
            // US-6: top kandidat efter gennemsnitlig popularity
            get("/candidates/top-by-popularity", candidateController.getTopByPopularity(), User.Role.ANYONE);
        };
    }
}
