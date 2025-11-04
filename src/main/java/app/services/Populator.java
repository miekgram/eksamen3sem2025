package app.services;

import app.config.HibernateConfig;
import app.daos.GuideDAO;
import app.daos.TripDAO;
import app.daos.UserDAO;
import app.entities.Guide;
import app.entities.Trip;
import app.security.daos.SecurityDAO;
import app.security.entities.Role;
import app.security.entities.User;
import io.javalin.http.Handler;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public class Populator {

    public static Handler populate() {
        return ctx -> {
            EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();
            EntityManager em = emf.createEntityManager();
            TripDAO tripDAO = new TripDAO(emf);
            GuideDAO guideDAO = new GuideDAO(emf);
            SecurityDAO userDAO = new SecurityDAO(emf);

            try {
                em.getTransaction().begin();
                SecurityDAO securityDAO = new SecurityDAO(emf);

                Role userRole = securityDAO.createRole("USER");   //Gem i DB først
                Role adminRole = securityDAO.createRole("ADMIN");

                Guide guide1 = Guide.builder()
                        .name("Adrian")
                        .email("adrian@mail.dk")
                        .phone("12345678")
                        .yearsOfExp(2)
                        .build();


                Guide guide2 = Guide.builder()
                        .name("Allan")
                        .email("allan@mail.dk")
                        .phone("87654321")
                        .yearsOfExp(5)
                        .build();

                guideDAO.create(guide1);
                guideDAO.create(guide2);

                List<Trip> trips = List.of(
                        Trip.builder()
                                .name("Bellevue Yoga")
                                .start(LocalDateTime.of(2026, 07, 25, 10, 00))
                                .end(LocalDateTime.of(2026, 07, 25, 12, 00))
                                .price(3500)
                                .locationCoordinates("42.900, 23.700")
                                .category(Trip.Category.BEACH)
                                .guide(guide1)
                                .build(),
                        Trip.builder()
                                .name("Cykeltur i KBH")
                                .start(LocalDateTime.of(2025, 11, 25, 12, 00))
                                .end(LocalDateTime.of(2025, 11, 25, 15, 00))
                                .price(350)
                                .locationCoordinates("55.6761, 12.5683")
                                .category(Trip.Category.CITY)
                                .guide(guide1)
                                .build(),
                        Trip.builder()
                                .name("Kanalrundfart i KBH i julebelysning")
                                .start(LocalDateTime.of(2025, 12, 13, 19, 00))
                                .end(LocalDateTime.of(2025, 12, 13, 20, 30))
                                .price(150)
                                .locationCoordinates("55.6761, 12.5683")
                                .category(Trip.Category.SEA)
                                .guide(guide2)
                                .build()
//                        Trip.builder()
//                                .name("Skitur i Norge")
//                                .start(LocalDateTime.of(2025, 12, 13, 19, 00))
//                                .end(LocalDateTime.of(2025, 12, 13, 20, 30))
//                                .price(4999)
//                                .locationCoordinates("55.6761, 12.5683")
//                                .category(Trip.Category.SNOW)
//                                .guide(guide2)
//                                .build()
                );

                for (Trip trip : trips) {
                    tripDAO.create(trip);
                }

                // --- USERS  ---

                User user = User.builder()
                        .username("Mie")
                        .password("1234")
                        .phone("12345678")
                        .address("Hejvej 123")

                        .roles(Set.of(userRole, adminRole))
                        .build();

                User admin = User.builder()
                        .username("Anna")
                        .password("admin123")
                        .phone("87654321")
                        .address("Adminvej 42")
                        .roles(Set.of(userRole))
                        .build();

                userDAO.createUser(user);
                userDAO.createUser(admin);

                em.getTransaction().commit();

            } catch (Exception e) {
                throw new RuntimeException(e + "populate failed");

            }
            em.close();
        };


    }
}
