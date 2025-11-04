package app.daos;

import app.entities.Candidate;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.TypedQuery;

import java.util.List;

public class CandidateDAO implements IDAO<Candidate, Integer> {
    EntityManagerFactory emf;

    public CandidateDAO(EntityManagerFactory emf){
        this.emf = emf;

    }


    @Override
    public Candidate create(Candidate candidate) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            em.persist(candidate);
            em.getTransaction().commit();
        }
        return candidate;
    }

    @Override
    public List<Candidate> getAll() {
        try (EntityManager em = emf.createEntityManager()) {
            TypedQuery<Candidate> query = em.createQuery("SELECT t FROM Candidate t", Candidate.class);
            return query.getResultList();
        }
    }

    @Override
    public Candidate getById(Integer id) {
        try (EntityManager em = emf.createEntityManager()) {
            //em.getTransaction().begin();
            Candidate candidate = em.find(Candidate.class, id);
            //em.getTransaction().commit();
            return candidate;
        }
    }

    @Override
    public Candidate update(Candidate trip) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            Candidate updatedCandidate = em.merge(trip);
            em.getTransaction().commit();
            return updatedCandidate;
        }
    }

    @Override
    public boolean delete(Integer id) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            Candidate candidateToBeDeleted = em.find(Candidate.class, id);
            if (candidateToBeDeleted != null) {
                em.remove(candidateToBeDeleted);
                em.getTransaction().commit();
                return true;
            } else {
                return false;
            }
        }
    }
}
