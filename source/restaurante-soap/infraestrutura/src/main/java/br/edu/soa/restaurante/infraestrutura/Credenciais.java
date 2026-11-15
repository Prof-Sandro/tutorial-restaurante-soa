// Finalidade: Reconhecer identidades e papéis pelas senhas da configuração externa.
package br.edu.soa.restaurante.infraestrutura;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * Identidades locais de laboratório. Publicação: HTTPS obrigatório, substituir
 * por IdP.
 */
public final class Credenciais {
	private final String admin;
	private final String garcom;

	public Credenciais(String admin, String garcom) {
		if (admin == null || garcom == null || admin.length() < 12 || garcom.length() < 12 || admin.equals(garcom))
			throw new IllegalStateException(
					"Configure senhas distintas de pelo menos 12 caracteres em RESTAURANTE_ADMIN_PASSWORD e RESTAURANTE_GARCOM_PASSWORD.");
		this.admin = admin;
		this.garcom = garcom;
	}

	// Compara a identidade recebida e devolve o papel reconhecido.
	public String autenticar(String usuario, String senha) {
		if (senha == null)
			return null;
		String esperada = "admin".equals(usuario) ? admin : "garcom".equals(usuario) ? garcom : null;
		return esperada != null && MessageDigest.isEqual(esperada.getBytes(StandardCharsets.UTF_8),
				senha.getBytes(StandardCharsets.UTF_8)) ? ("admin".equals(usuario) ? "ADMIN" : "GARCOM") : null;
	}
}
