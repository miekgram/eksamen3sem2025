package app.daos;

import app.config.HibernateConfig;
import app.entities.Candidate;
import app.entities.CandidateSkill;
import app.entities.Skill;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CandidateDAOTest {

    private EntityManagerFactory emf;
    private CandidateDAO candidateDAO;
    private SkillDAO skillDAO;

    @BeforeAll
    void setUpAll() {
        HibernateConfig.setTest(true);
        emf = HibernateConfig.getEntityManagerFactoryForTest();
        candidateDAO = new CandidateDAO(emf);
        skillDAO = new SkillDAO(emf);
    }

    @BeforeEach
    void cleanDatabase() {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            // Ryd i korrekt rækkefølge pga. FK’er
            em.createQuery("DELETE FROM CandidateSkill").executeUpdate();
            em.createQuery("DELETE FROM Candidate").executeUpdate();
            em.createQuery("DELETE FROM Skill").executeUpdate();
            em.getTransaction().commit();
        }
    }

    @Test
    void create_and_getById() {
        Candidate c = Candidate.builder()
                .name("Maja")
                .phone("11112222")
                .education("BSc Software Development")
                .build();

        Candidate persisted = candidateDAO.create(c);
        assertNotNull(persisted.getCandidateId());

        Candidate found = candidateDAO.getById(persisted.getCandidateId());
        assertNotNull(found);
        assertEquals("Maja", found.getName());
        assertEquals("11112222", found.getPhone());
        assertEquals("BSc Software Development", found.getEducation());
    }

    @Test
    void getAll_returnsOne() {
        candidateDAO.create(Candidate.builder()
                .name("Jonas")
                .phone("22223333")
                .education("AP Computer Science")
                .build());
        List<Candidate> all = candidateDAO.getAll();
        assertEquals(1, all.size());
    }

    @Test
    void update_changesPersisted() {
        Candidate c = candidateDAO.create(Candidate.builder()
                .name("Old")
                .phone("1")
                .education("E")
                .build());

        c.setName("New");
        Candidate updated = candidateDAO.update(c);

        assertEquals("New", updated.getName());
        assertEquals("New", candidateDAO.getById(c.getCandidateId()).getName());
    }

    @Test
    void delete_removesRow_andReturnsTrue() {
        Candidate c = candidateDAO.create(Candidate.builder()
                .name("ToDelete")
                .phone("9")
                .education("X")
                .build());

        Integer id = c.getCandidateId();
        assertTrue(candidateDAO.delete(id));
        assertNull(candidateDAO.getById(id));
    }

    @Test
    void link_skill_to_candidate_persistsJoinRow() {
        // Arrange
        Candidate c = candidateDAO.create(
                Candidate.builder().name("Maja").phone("11112222").education("BSc").build()
        );
        Skill java = skillDAO.create(
                Skill.builder()
                        .name("Java")
                        .slug("java")
                        .category(Skill.Category.PROG_LANG)
                        .description("General-purpose language")
                        .build()
        );

        // Act: persistér join-row (samme mønster som i controlleren)
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            c = em.merge(c);
            java = em.merge(java);
            em.persist(CandidateSkill.builder().candidate(c).skill(java).build());
            em.getTransaction().commit();
        } finally {
            em.close();
        }

        // Assert: hent igen og verificér linket
        Candidate found = candidateDAO.getById(c.getCandidateId());
        assertNotNull(found);
        assertNotNull(found.getSkills());
        assertEquals(1, found.getSkills().size());

        CandidateSkill link = found.getSkills().iterator().next();
        assertNotNull(link.getSkill());
        assertEquals("Java", link.getSkill().getName());
        assertEquals("java", link.getSkill().getSlug());
    }

    @Test
    void delete_candidate_clearsJoinRowsSafely() {
        Candidate c = candidateDAO.create(
                Candidate.builder().name("DeleteMe").phone("0000").education("X").build()
        );
        Skill s1 = skillDAO.create(Skill.builder()
                .name("Java").slug("java").category(Skill.Category.PROG_LANG).description("...").build());
        Skill s2 = skillDAO.create(Skill.builder()
                .name("PostgreSQL").slug("postgresql").category(Skill.Category.DB).description("...").build());

        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            c = em.merge(c);
            s1 = em.merge(s1);
            s2 = em.merge(s2);
            em.persist(CandidateSkill.builder().candidate(c).skill(s1).build());
            em.persist(CandidateSkill.builder().candidate(c).skill(s2).build());
            em.getTransaction().commit();
        } finally {
            em.close();
        }

        Integer id = c.getCandidateId();
        assertTrue(candidateDAO.delete(id));
        assertNull(candidateDAO.getById(id));

        // Verificér at join-rows er væk (forventer 0 med orphanRemoval=true)
        try (EntityManager chk = emf.createEntityManager()) {
            long count = chk.createQuery("SELECT COUNT(cs) FROM CandidateSkill cs", Long.class)
                    .getSingleResult();
            assertEquals(0L, count);
        }
    }
}
