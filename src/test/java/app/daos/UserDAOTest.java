package app.daos;

import app.config.HibernateConfig;
import app.security.entities.User;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserDAOTest {

    private EntityManagerFactory emf;
    private UserDAO userDAO;

    @BeforeAll
    void setUpAll() {
        HibernateConfig.setTest(true);
        emf = HibernateConfig.getEntityManagerFactoryForTest();
        userDAO = new UserDAO(emf);
    }

    @BeforeEach
    void cleanDatabase() {
        try (var em = emf.createEntityManager()) {
            em.getTransaction().begin();
            em.createQuery("DELETE FROM User").executeUpdate();
            em.getTransaction().commit();
        }
    }

    @Test
    void create_and_getById() {
        User u = User.builder()
                .username("Mie")
                .password("secret")
                .address("Testvej 1")
                .phone("12345678")
                .build();

        User persisted = userDAO.create(u);
        assertNotNull(persisted.getId());

        User found = userDAO.getById(persisted.getId());
        assertNotNull(found);
        assertEquals("Mie", found.getUsername());
    }

    @Test
    void getAll_returnsOne() {
        userDAO.create(User.builder().username("A").password("x").address("a").phone("1").build());
        List<User> all = userDAO.getAll();
        assertEquals(1, all.size());
    }

    @Test
    void update_changesPersisted() {
        User u = userDAO.create(User.builder().username("Old").password("x").address("a").phone("1").build());
        u.setUsername("New");
        User updated = userDAO.update(u);

        assertEquals("New", updated.getUsername());
        assertEquals("New", userDAO.getById(u.getId()).getUsername());
    }

    @Test
    void delete_removesRow_andReturnsTrue() {
        User u = userDAO.create(User.builder().username("X").password("x").address("a").phone("1").build());
        Integer id = u.getId();
        assertTrue(userDAO.delete(id));
        assertNull(userDAO.getById(id));
    }
}
