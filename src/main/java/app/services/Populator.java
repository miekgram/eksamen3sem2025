package app.services;

import app.config.HibernateConfig;
import app.daos.CandidateDAO;
import app.daos.SkillDAO;
import app.entities.Candidate;
import app.entities.CandidateSkill;
import app.entities.Skill;
import app.security.daos.SecurityDAO;
import app.security.entities.Role;
import app.security.entities.User;
import io.javalin.http.Handler;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import java.util.Set;

public class Populator {

    public static Handler populate() {
        return ctx -> {
            EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();
            EntityManager em = emf.createEntityManager();

            CandidateDAO candidateDAO = new CandidateDAO(emf);
            SkillDAO skillDAO = new SkillDAO(emf);
            SecurityDAO securityDAO = new SecurityDAO(emf);

            try {
                // --- ROLES (du kan lade SecurityDAO styre tx internt) ---
                Role userRole = securityDAO.createRole("USER");
                Role adminRole = securityDAO.createRole("ADMIN");

                // --- SKILLS (med slug + kategori) ---
                Skill java = Skill.builder()
                        .name("Java")
                        .slug("java")
                        .category(Skill.Category.PROG_LANG)
                        .description("General-purpose language for backend/Android")
                        .build();

                Skill springBoot = Skill.builder()
                        .name("Spring Boot")
                        .slug("spring-boot")
                        .category(Skill.Category.FRAMEWORK)
                        .description("Java framework for REST APIs & microservices")
                        .build();

                Skill postgres = Skill.builder()
                        .name("PostgreSQL")
                        .slug("postgresql")
                        .category(Skill.Category.DB)
                        .description("Relational database")
                        .build();

                Skill docker = Skill.builder()
                        .name("Docker")
                        .slug("docker")
                        .category(Skill.Category.DEVOPS)
                        .description("Containerization platform")
                        .build();

                skillDAO.create(java);
                skillDAO.create(springBoot);
                skillDAO.create(postgres);
                skillDAO.create(docker);

                // --- CANDIDATES ---
                Candidate c1 = Candidate.builder()
                        .name("Maja Hansen")
                        .phone("11112222")
                        .education("BSc Software Development")
                        .build();

                Candidate c2 = Candidate.builder()
                        .name("Jonas Lund")
                        .phone("33334444")
                        .education("AP Computer Science")
                        .build();

                candidateDAO.create(c1);
                candidateDAO.create(c2);

                // --- LINKS (brug dit eget EM + MERGE først for at re-attache) ---
                em.getTransaction().begin();

                c1         = em.merge(c1);
                c2         = em.merge(c2);
                java       = em.merge(java);
                springBoot = em.merge(springBoot);
                postgres   = em.merge(postgres);
                docker     = em.merge(docker);

                em.persist(CandidateSkill.builder().candidate(c1).skill(java).build());
                em.persist(CandidateSkill.builder().candidate(c1).skill(postgres).build());
                em.persist(CandidateSkill.builder().candidate(c2).skill(springBoot).build());
                em.persist(CandidateSkill.builder().candidate(c2).skill(docker).build());

                em.getTransaction().commit();

                // --- USERS (til JWT-tests) ---
                User user = User.builder()
                        .username("user")
                        .password("user123") // antag SecurityDAO hasher ved createUser
                        .phone("55556666")
                        .address("Devvej 1")
                        .roles(Set.of(userRole))
                        .build();

                User admin = User.builder()
                        .username("Mie")
                        .password("1234")
                        .phone("77778888")
                        .address("Adminvej 42")
                        .roles(Set.of(userRole, adminRole))
                        .build();

                securityDAO.createUser(user);
                securityDAO.createUser(admin);

                ctx.status(201).result("Database populated");
            } catch (Exception e) {
                if (em.getTransaction().isActive()) em.getTransaction().rollback();
                throw new RuntimeException("populate failed: " + e.getMessage(), e);
            } finally {
                em.close();
            }
        };
    }
}
