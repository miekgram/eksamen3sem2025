package app.routes;

import app.controllers.CandidateController;
import app.security.entities.User;
import io.javalin.apibuilder.EndpointGroup;
import jakarta.persistence.EntityManagerFactory;


import static io.javalin.apibuilder.ApiBuilder.*;
import static io.javalin.apibuilder.ApiBuilder.delete;

public class CandidateRoutes {
    EntityManagerFactory emf;
    CandidateController candidateController;

    public CandidateRoutes(EntityManagerFactory emf){
        candidateController = new CandidateController(emf);
        this.emf = emf;
    }

    public EndpointGroup getRoutes(){
        return () -> {

            // LIST + FILTER (?category=FRONTEND) håndteres i controllerens getAll()
            get("/", candidateController.getAll(), User.Role.ANYONE);
            get("/{id}", candidateController.getById(), User.Role.ANYONE);
            //CRUD
            post("/", candidateController.create(), User.Role.ADMIN);
            put("/{id}", candidateController.update(), User.Role.ADMIN);
            delete("/{id}", candidateController.delete(), User.Role.ADMIN);

            //US-3: link eksisterende skill til candidate
            put("/{candidateId}/skills/{skillId}", candidateController.linkSkill(), User.Role.ADMIN);


            //put("/{id}/guides/{guideId}", candidateController.updateGuideForTrip(), User.Role.ADMIN);
            //get("?category={category}", candidateController.getByCategory(), User.Role.ANYONE);
            //get("/{id}/packing", candidateController.getPackingItems(), User.Role.ANYONE);
           // get("/{id}/packing/weight", candidateController.getPackingWeight(), User.Role.ANYONE);
           // get("/guides/totalprice", candidateController.getGuidesTotalPrice(), User.Role.ANYONE);
        };
    }
}
