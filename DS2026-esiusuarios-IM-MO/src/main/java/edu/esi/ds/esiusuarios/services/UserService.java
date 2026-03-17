package edu.esi.ds.esiusuarios.services;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import edu.esi.ds.esiusuarios.auxiliares.Manager;
import edu.esi.ds.esiusuarios.model.User;

@Service
public class UserService {
    private final List<User> users;

    public UserService(EmailService emailService) {
        this.users = new ArrayList<>(List.of(
                new User("Pepe", "pepe123", "1234"),
                new User("Ana", "ana123", "5678")));
    }

    public String login(String name, String password) {
        for (User user : this.users) {
            if (user.getName().equals(name) && user.getPassword().equals(password)) {
                return user.getToken();
            }
        }
        return null;
    }

    public String checkToken(String token) {
        for (User user : this.users) {
            if (user.getToken().equals(token)) {
                return user.getName();
            }
        }
        return null;
    }

    public String registrar(String email, String pwd1) {
        for (User user : this.users) {
            if (user.getName().equalsIgnoreCase(email)) {
                return null;
            }
        }

        User newUser = new User(email, pwd1, String.valueOf(this.users.size() + 1));
        this.users.add(newUser);

        Manager.getInstance().getEmailService().sendEmail(email,
                "asunto", "Bienvenido a esiusuarios",
                "texto", "Bienvenido al sistema. Confirma tu registro aqui: http://localhost:8080/confirmar?token="
                        + newUser.getToken());
        return "hecho";
    }

}
