package app.daos;

import app.entities.Skill;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.TypedQuery;

import java.util.List;

public class SkillDAO implements IDAO<Skill, Integer>{
    EntityManagerFactory emf;
    public SkillDAO(EntityManagerFactory emf){
        this.emf = emf;
    }

    @Override
    public Skill create(Skill skill) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            em.persist(skill);
            em.getTransaction().commit();
        }
        return skill;
    }

    @Override
    public List<Skill> getAll() {
        try (EntityManager em = emf.createEntityManager()) {
            TypedQuery<Skill> query = em.createQuery("SELECT g FROM Skill g", Skill.class);
            return query.getResultList();
        }
    }

    @Override
    public Skill getById(Integer id) {
        try (EntityManager em = emf.createEntityManager()) {
            //em.getTransaction().begin();
            Skill skill = em.find(Skill.class, id);
            //em.getTransaction().commit();
            return skill;
        }
    }

    @Override
    public Skill update(Skill skill) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            Skill updatedSkill = em.merge(skill);
            em.getTransaction().commit();
            return updatedSkill;
        }
    }

    @Override
    public boolean delete(Integer id) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            Skill skillToBeDeleted = em.find(Skill.class, id);
            if (skillToBeDeleted != null) {
                em.remove(skillToBeDeleted);
                em.getTransaction().commit();
                return true;
            } else {
                return false;
            }
        }
    }
}
