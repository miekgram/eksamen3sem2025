package app.security.daos;


import app.security.entities.Role;
import app.security.entities.User;
import app.security.exceptions.ApiException;
import app.security.exceptions.ValidationException;
import dk.bugelhartmann.UserDTO;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityNotFoundException;
import org.mindrot.jbcrypt.BCrypt;

import java.util.Set;
import java.util.stream.Collectors;


/**
 * Purpose: To handle security in the API
 * Author: Thomas Hartmann
 */
public class SecurityDAO implements ISecurityDAO {

    private static ISecurityDAO instance;
    private static EntityManagerFactory emf;

    public SecurityDAO(EntityManagerFactory _emf) {
        emf = _emf;
    }

    private EntityManager getEntityManager() {
        return emf.createEntityManager();
    }



    @Override
    public UserDTO getVerifiedUser(String username, String password) throws ValidationException {
        try (EntityManager em = getEntityManager()) {

            // Slå op på username (ikke ID) og JOIN FETCH roller i samme query
            User user = em.createQuery(
                            "SELECT u FROM User u LEFT JOIN FETCH u.roles WHERE u.username = :un",
                            User.class)
                    .setParameter("un", username)
                    .getResultStream()
                    .findFirst()
                    .orElse(null);

            if (user == null) {
                throw new EntityNotFoundException("No user found with username: " + username);
            }

            if (!user.verifyPassword(password)) {
                throw new ValidationException("Wrong password");
            }

            return new UserDTO(
                    user.getUsername(),
                    user.getRoles().stream().map(r -> r.getRoleName()).collect(Collectors.toSet())
            );
        }
    }

    @Override
    public User createUser(String username, String password) {
        return null;
    }


    public User createUser(User user) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();

//Hash password før persist
// "$2a$" er indikator på at password er hashed, så derfor vil vi gerne undgå at dobbelt-hashe det
            if (user.getPassword() != null && !user.getPassword().startsWith("$2a$")) {
                String hashed = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt());
                user.setPassword(hashed);
            }

            em.persist(user);
            em.getTransaction().commit();
        }
        return user;
    }

    public Role createRole(String roleName) {
        Role role = new Role(roleName);
        try(EntityManager em = emf.createEntityManager()){
            Role existing = em.find(Role.class, roleName);
            if (existing != null) return existing;
            em.getTransaction().begin();
            em.persist(role);
            em.getTransaction().commit();
            return role;
        }
    }

    @Override
    public User addRole(UserDTO userDTO, String newRole) {
        try (EntityManager em = getEntityManager()) {
            User user = em.find(User.class, userDTO.getUsername());
            if (user == null)
                throw new EntityNotFoundException("No user found with username: " + userDTO.getUsername());
            em.getTransaction().begin();
                Role role = em.find(Role.class, newRole);
                if (role == null) {
                    role = new Role(newRole);
                    em.persist(role);
                }
                user.addRole(role);
                //em.merge(user);
            em.getTransaction().commit();
            return user;
        }
    }
}

