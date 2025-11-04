package app.daos;

import app.security.entities.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.TypedQuery;

import java.util.List;

public class UserDAO implements IDAO<User, Integer> {
    private final EntityManagerFactory emf;
    public UserDAO(EntityManagerFactory emf) { this.emf = emf; };

//    public UserDAO(EntityManagerFactory emf) {
//        this.emf = emf;
//    }
//
//    public static UserDAO getInstance(EntityManagerFactory emf){
//        if(instance == null){
//            instance = new UserDAO(emf);
//            UserDAO.emf = emf;
//        }
//        return instance;
//    }


    @Override
    public User create(User user) {
        try(EntityManager em = emf.createEntityManager()){
            em.getTransaction().begin();
            em.persist(user);
            em.getTransaction().commit();
        }
        return user;
    }

    @Override
    public List<User> getAll() {
        try(EntityManager em = emf.createEntityManager()){
            TypedQuery<User> query = em.createQuery("SELECT m FROM User m", User.class);
            return query.getResultList();
        }
    }

    @Override
    public User getById(Integer id) {
        try (EntityManager em = emf.createEntityManager()) {
            //em.getTransaction().begin();
            User user = em.find(User.class, id);
            //em.getTransaction().commit();
            return user;
        }
    }

    @Override
    public User update(User user) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            User updatedUser = em.merge(user);
            em.getTransaction().commit();
            return updatedUser;
        }
    }

    @Override
    public boolean delete(Integer id) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            User userToBeDeleted = em.find(User.class, id);
            if (userToBeDeleted != null) {
                em.remove(userToBeDeleted);
                em.getTransaction().commit();
                return true;
            } else {
                return false;
            }
        }
    }
}
