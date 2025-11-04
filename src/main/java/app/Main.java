package app;
import jakarta.persistence.EntityManagerFactory;


import app.config.ApplicationConfig;
import app.config.HibernateConfig;

public class Main {
    public static void main(String[] args) {
        ApplicationConfig.startServer(7070);
    }

}




