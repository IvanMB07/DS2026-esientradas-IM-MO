package edu.esi.ds.esientradas.dao;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import edu.esi.ds.esientradas.model.Token;

/**
 * nombre_clase: TokenDao
 * parametros_clave: Token, String
 * funcion: persistencia y gestión de tokens de sesión/seguridad
 * flujo_en_el_que_participa: autenticación, validación de acceso y limpieza de
 * sesiones
 * comunicacion: AuthService, SeguridadService, TokenCleanupTask
 */
public interface TokenDao extends JpaRepository<Token, String> {

	/**
	 * nombre_metodo: findByEmailUsuario
	 * parametros: emailUsuario
	 * funcion: recupera tokens asociados a un usuario específico
	 * flujo_en_el_que_participa: auditoría de sesiones activas
	 */
	List<Token> findByEmailUsuario(String emailUsuario);

	/**
	 * nombre_metodo: findByEmailUsuarioOrderByHoraDesc
	 * parametros: email
	 * funcion: recupera tokens de un usuario ordenados por fecha de creación
	 * flujo_en_el_que_participa: obtención de la sesión más reciente
	 */
	@Query("SELECT t FROM Token t WHERE t.emailUsuario = :email ORDER BY t.hora DESC")
	List<Token> findByEmailUsuarioOrderByHoraDesc(@Param("email") String email);

	/**
	 * nombre_metodo: findRecentUnboundTokens
	 * parametros: ninguno
	 * funcion: recupera tokens sin usuario asociado (anónimos) recientes
	 * flujo_en_el_que_participa: gestión de sesiones pre-login
	 */
	@Query("SELECT t FROM Token t WHERE t.emailUsuario IS NULL ORDER BY t.hora DESC")
	List<Token> findRecentUnboundTokens();

	/**
	 * nombre_metodo: findByHoraBefore
	 * parametros: horaLimite
	 * funcion: busca tokens cuya creación es anterior a la marca temporal dada
	 * flujo_en_el_que_participa: proceso de purga de tokens caducados (timeout)
	 */
	List<Token> findByHoraBefore(Long horaLimite);
}