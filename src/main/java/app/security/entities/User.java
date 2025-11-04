package app.security.entities;

import app.entities.Trip;
import io.javalin.security.RouteRole;
import jakarta.persistence.*;
import jakarta.persistence.Entity;
import lombok.*;
import org.mindrot.jbcrypt.BCrypt;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
@Data
@Builder
@Setter
@Getter

public class User implements Serializable, ISecurityUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String username;
    private String password;
    private String address;
    private String phone;
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_role",
            joinColumns = @JoinColumn(name = "id"),
            inverseJoinColumns = @JoinColumn(name = "role_name"))
    private Set<app.security.entities.Role> roles = new HashSet<>();
    @ManyToMany
    @JoinTable(
            name = "user_event",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "event_id")
    )
    private Set<Trip> trips = new HashSet<>();


    public User (String username, String password){
        this.username = username;
        String hashed = BCrypt.hashpw(password, BCrypt.gensalt());
        this.password = hashed;
    }


    public void addEvent (Trip trip){
        this.trips.add(trip);


    }

    @Override
    public boolean verifyPassword(String pw) {
        return BCrypt.checkpw(pw, password);
    }

    @Override
    public void addRole(app.security.entities.Role role) {

    }




    public enum Role implements RouteRole {
        ANYONE,
        USER,
        ADMIN
    }
}
