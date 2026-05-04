package edu.esi.ds.esientradas.dao;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import edu.esi.ds.esientradas.model.Token;

public interface TokenDao extends JpaRepository<Token, String> {

	// --- MÉTODOS EXISTENTES ---
	List<Token> findByEmailUsuario(String emailUsuario);

	@Query("SELECT t FROM Token t WHERE t.emailUsuario = :email ORDER BY t.hora DESC")
	List<Token> findByEmailUsuarioOrderByHoraDesc(@Param("email") String email);

	@Query("SELECT t FROM Token t WHERE t.emailUsuario IS NULL ORDER BY t.hora DESC")
	List<Token> findRecentUnboundTokens();

	// --- NUEVO MÉTODO PARA EL TIMEOUT ---
	// Busca tokens creados antes de una hora determinada
	List<Token> findByHoraBefore(Long horaLimite);
}