package edu.esi.ds.esientradas.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import edu.esi.ds.esientradas.model.Token;

public interface TokenDao extends JpaRepository<Token, String> {
	Optional<Token> findByEntradaIdAndSessionId(Long entradaId, String sessionId);
}