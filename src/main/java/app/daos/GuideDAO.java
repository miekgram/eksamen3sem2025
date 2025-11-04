package app.daos;

import app.entities.Guide;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.TypedQuery;

import java.util.List;
import java.util.zip.GZIPInputStream;

public class GuideDAO implements IDAO<Guide, Integer>{
    EntityManagerFactory emf;
    public GuideDAO (EntityManagerFactory emf){
        this.emf = emf;
    }

    @Override
    public Guide create(Guide guide) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            em.persist(guide);
            em.getTransaction().commit();
        }
        return guide;
    }

    @Override
    public List<Guide> getAll() {
        try (EntityManager em = emf.createEntityManager()) {
            TypedQuery<Guide> query = em.createQuery("SELECT g FROM Guide g", Guide.class);
            return query.getResultList();
        }
    }

    @Override
    public Guide getById(Integer id) {
        try (EntityManager em = emf.createEntityManager()) {
            //em.getTransaction().begin();
            Guide guide = em.find(Guide.class, id);
            //em.getTransaction().commit();
            return guide;
        }
    }

    @Override
    public Guide update(Guide guide) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            Guide updatedGuide = em.merge(guide);
            em.getTransaction().commit();
            return updatedGuide;
        }
    }

    @Override
    public boolean delete(Integer id) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            Guide guideToBeDeleted = em.find(Guide.class, id);
            if (guideToBeDeleted != null) {
                em.remove(guideToBeDeleted);
                em.getTransaction().commit();
                return true;
            } else {
                return false;
            }
        }
    }
}
