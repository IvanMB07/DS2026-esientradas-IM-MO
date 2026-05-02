package edu.esi.ds.esientradas.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import edu.esi.ds.esientradas.model.Token;

public interface TokenDao extends JpaRepository<Token, String> {
	// Buscar todos los tokens asociados a un usuario
	List<Token> findByEmailUsuario(String emailUsuario);

	// Busca tokens de un usuario ordenados por fecha (más recientes primero)
	@Query("SELECT t FROM Token t WHERE t.emailUsuario = :email ORDER BY t.hora DESC")
	List<Token> findByEmailUsuarioOrderByHoraDesc(@Param("email") String email);

	// Busca tokens sin vinculación (emailUsuario es null), ordenados por fecha
	@Query("SELECT t FROM Token t WHERE t.emailUsuario IS NULL ORDER BY t.hora DESC")
	List<Token> findRecentUnboundTokens();
}