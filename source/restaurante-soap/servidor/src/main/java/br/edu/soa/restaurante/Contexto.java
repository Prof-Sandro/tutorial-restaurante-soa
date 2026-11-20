// Finalidade: Compor os objetos da aplicação ao iniciar o contexto web.
package br.edu.soa.restaurante;

import br.edu.soa.restaurante.aplicacao.Restaurante;
import br.edu.soa.restaurante.infraestrutura.*;
import jakarta.servlet.*;

public final class Contexto implements ServletContextListener {
	private static volatile Restaurante restaurante;
	private static volatile Credenciais credenciais;

	// Monta credenciais, banco, repositório e casos de uso ao iniciar o WAR.
	@Override
	public void contextInitialized(ServletContextEvent evento) {
		credenciais = new Credenciais(valor("RESTAURANTE_ADMIN_PASSWORD"), valor("RESTAURANTE_GARCOM_PASSWORD"));
		restaurante = new Restaurante(
				new JdbcRepositorio(Banco.configurar(valor("DB_URL"), valor("DB_USER"), valor("DB_PASSWORD"))));
	}

	// Remove as referências da aplicação ao encerrar o contexto.
	@Override
	public void contextDestroyed(ServletContextEvent evento) {
		restaurante = null;
		credenciais = null;
	}

	public static Restaurante restaurante() {
		if (restaurante == null)
			throw new IllegalStateException("Aplicação ainda não inicializada.");
		return restaurante;
	}

	public static Credenciais credenciais() {
		return credenciais;
	}

	private static String valor(String nome) {
		return System.getProperty("restaurante." + nome, System.getenv(nome));
	}
}
