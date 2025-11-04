package app.daos;

import app.config.HibernateConfig;
import app.entities.Trip;
import app.security.entities.User;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.*;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserDAOTest {

    private EntityManagerFactory emf;
    private UserDAO userDAO;
    private TripDAO tripDAO;

    @BeforeAll
    void setUpAll() {
        HibernateConfig.setTest(true);
        emf = HibernateConfig.getEntityManagerFactoryForTest();
        userDAO = new UserDAO(emf);   // eller UserDAO.getInstance(emf);
        tripDAO = new TripDAO(emf);   // eller TripDAO.getInstance(emf);
    }

    @BeforeEach
    void cleanDatabase() {
        try (var em = emf.createEntityManager()) {
            em.getTransaction().begin();
            // Slet afhængigheder først hvis de findes i din model
            // em.createQuery("DELETE FROM TripGuide").executeUpdate();
            // em.createQuery("DELETE FROM UserEvent").executeUpdate();
            // em.createQuery("DELETE FROM UserRole").executeUpdate();

            em.createQuery("DELETE FROM User").executeUpdate();
            em.createQuery("DELETE FROM Trip").executeUpdate();
            // em.createQuery("DELETE FROM Guide").executeUpdate();
            em.getTransaction().commit();
        }
    }

    @Test
    void createUser() {
        User u = new User();
        u.setUsername("Mie");
        u.setPassword("secret");     // hvis @NotNull
        u.setAddress("Testvej 1");
        u.setPhone("12345678");

        User persisted = userDAO.create(u);
        assertNotNull(persisted);
        assertNotNull(persisted.getId());

        User found = userDAO.getById(persisted.getId());
        assertNotNull(found);
        assertEquals("Mie", found.getUsername());
    }

    @Test
    void createTrip() {
        LocalDateTime start = LocalDateTime.now().plusDays(1).withSecond(0).withNano(0);
        LocalDateTime end   = start.plusHours(2);

        Trip t = new Trip();
        t.setName("gåtur");
        t.setCategory(Trip.Category.CITY); // vælg en gyldig enum
        t.setStart(start);
        t.setEnd(end);
        t.setLocationCoordinates("noget");
        t.setPrice(199);

        Trip persisted = tripDAO.create(t);
        assertNotNull(persisted);
        assertNotNull(persisted.getTripId());

        Trip found = tripDAO.getById(persisted.getTripId());
        assertNotNull(found);
        assertEquals("gåtur", found.getName());
    }

    @Test
    void getAll_users() {
        User u1 = new User();
        u1.setUsername("Mie");
        u1.setPassword("secret");
        u1.setAddress("Testvej 1");
        u1.setPhone("1");
        userDAO.create(u1);

        List<User> all = userDAO.getAll();
        assertEquals(1, all.size());
    }

    @Test
    void getAll_trips() {
        LocalDateTime start = LocalDateTime.now().plusDays(1).withSecond(0).withNano(0);
        LocalDateTime end   = start.plusHours(1);

        Trip t1 = new Trip();
        t1.setName("gåtur");
        t1.setCategory(Trip.Category.CITY);
        t1.setStart(start);
        t1.setEnd(end);
        t1.setLocationCoordinates("noget");
        t1.setPrice(50);
        tripDAO.create(t1);

        List<Trip> all = tripDAO.getAll();
        assertEquals(1, all.size());
    }

    @Test
    void user_updated() {
        User u = new User();
        u.setUsername("Malene");
        u.setPassword("secret");
        u.setAddress("A");
        u.setPhone("12345678");
        u = userDAO.create(u);

        u.setUsername("Malene mussemås");
        User updated = userDAO.update(u);

        assertEquals("Malene mussemås", updated.getUsername());
        assertEquals("Malene mussemås", userDAO.getById(u.getId()).getUsername());
    }

    @Test
    void trip_updated() {
        LocalDateTime start = LocalDateTime.now().plusDays(2).withSecond(0).withNano(0);
        LocalDateTime end   = start.plusHours(3);

        Trip t = new Trip();
        t.setName("ski");
        t.setCategory(Trip.Category.SNOW);
        t.setStart(start);
        t.setEnd(end);
        t.setLocationCoordinates("noget");
        t.setPrice(999);
        t = tripDAO.create(t);

        t.setName("sø");
        Trip updated = tripDAO.update(t);

        assertEquals("sø", updated.getName());
        assertEquals("sø", tripDAO.getById(t.getTripId()).getName());
    }

    @Test
    void user_delete_removesRow_andReturnsTrue() {
        User u = new User();
        u.setUsername("Mie");
        u.setPassword("secret");
        u.setAddress("Testvej 1");
        u.setPhone("12345678");
        u = userDAO.create(u);

        Integer id = u.getId();
        assertTrue(userDAO.delete(id));
        assertNull(userDAO.getById(id));
    }

    @Test
    void trip_delete_removesRow_andReturnsTrue() {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end   = start.plusHours(2);

        Trip t = new Trip();
        t.setName("gåtur");
        t.setCategory(Trip.Category.CITY);
        t.setStart(start);
        t.setEnd(end);
        t.setLocationCoordinates("noget");
        t.setPrice(10);
        t = tripDAO.create(t);

        Integer id = t.getTripId();
        assertTrue(tripDAO.delete(id));
        assertNull(tripDAO.getById(id));
    }
}
