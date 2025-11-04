package app.daos;

import app.entities.Trip;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Index;
import jakarta.persistence.TypedQuery;

import java.util.List;

public class TripDAO implements IDAO<Trip, Integer> {
    EntityManagerFactory emf;

    public TripDAO(EntityManagerFactory emf){
        this.emf = emf;

    }


    @Override
    public Trip create(Trip trip) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            em.persist(trip);
            em.getTransaction().commit();
        }
        return trip;
    }

    @Override
    public List<Trip> getAll() {
        try (EntityManager em = emf.createEntityManager()) {
            TypedQuery<Trip> query = em.createQuery("SELECT t FROM Trip t", Trip.class);
            return query.getResultList();
        }
    }

    @Override
    public Trip getById(Integer id) {
        try (EntityManager em = emf.createEntityManager()) {
            //em.getTransaction().begin();
            Trip trip = em.find(Trip.class, id);
            //em.getTransaction().commit();
            return trip;
        }
    }

    @Override
    public Trip update(Trip trip) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            Trip updatedTrip = em.merge(trip);
            em.getTransaction().commit();
            return updatedTrip;
        }
    }

    @Override
    public boolean delete(Integer id) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            Trip tripToBeDeleted = em.find(Trip.class, id);
            if (tripToBeDeleted != null) {
                em.remove(tripToBeDeleted);
                em.getTransaction().commit();
                return true;
            } else {
                return false;
            }
        }
    }
}
