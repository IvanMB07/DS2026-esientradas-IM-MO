package edu.esi.ds.esiusuarios.dao;

import org.springframework.data.repository.CrudRepository;
import edu.esi.ds.esiusuarios.model.User;
import java.util.Optional;

public interface UserDao extends CrudRepository<User, String> {
    // Spring es tan listo que con este nombre de método ya sabe buscar por email
    Optional<User> findByEmail(String email);
}