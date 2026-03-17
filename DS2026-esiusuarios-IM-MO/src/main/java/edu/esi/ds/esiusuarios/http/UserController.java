package edu.esi.ds.esiusuarios.http;

import java.util.Map;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import edu.esi.ds.esiusuarios.services.UserService;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService service;

    @PostMapping("/login")
    public String login(@RequestBody Map<String, String> credentials) {
        JSONObject jsoCredentials = new JSONObject(credentials);
        String name = jsoCredentials.optString("name");
        String password = jsoCredentials.optString("pwd");

        if (name.isEmpty() || password.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        String result = this.service.login(name, password);
        if (result == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }
        return result;
    }

    @PostMapping("/register")
    public String registrar(@RequestBody Map<String, String> credentials) {
        JSONObject jsoCredentials = new JSONObject(credentials);
        String email = jsoCredentials.optString("email");
        String pwd1 = jsoCredentials.optString("pwd1");
        String pwd2 = jsoCredentials.optString("pwd2");

        if (email.isEmpty() || pwd1.isEmpty() || pwd2.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        if (!pwd1.equals(pwd2)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Passwords do not match");
        }

        String result = this.service.registrar(email, pwd1);
        if (result == null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User already exists");
        }
        return result;
    }

}
