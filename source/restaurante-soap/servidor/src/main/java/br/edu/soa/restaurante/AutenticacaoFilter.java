// Finalidade: Autenticar as requisições HTTP destinadas aos endpoints SOAP.
package br.edu.soa.restaurante;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.Base64;

public final class AutenticacaoFilter implements Filter {
	// Autentica a requisição antes de encaminhá-la ao serviço.
	@Override
	public void doFilter(ServletRequest requisicao, ServletResponse resposta, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest req = (HttpServletRequest) requisicao;
		HttpServletResponse res = (HttpServletResponse) resposta;
		res.setHeader("X-Content-Type-Options", "nosniff");
		res.setHeader("Cache-Control", "no-store");
		String auth = req.getHeader("Authorization");
		String papel = null;
		String usuario = "";
		if (auth != null && auth.startsWith("Basic ") && auth.length() < 4096) {
			try {
				String par = new String(Base64.getDecoder().decode(auth.substring(6)), StandardCharsets.UTF_8);
				int sep = par.indexOf(':');
				if (sep > 0) {
					usuario = par.substring(0, sep);
					papel = Contexto.credenciais().autenticar(usuario, par.substring(sep + 1));
				}
			} catch (IllegalArgumentException ignored) {
			}
		}
		if (papel == null) {
			res.setHeader("WWW-Authenticate", "Basic realm=\"Restaurante\", charset=\"UTF-8\"");
			res.sendError(401, "Autenticação necessária.");
			return;
		}
		final String role = papel;
		final String user = usuario;
		chain.doFilter(new HttpServletRequestWrapper(req) {
			@Override
			public Principal getUserPrincipal() {
				return () -> user;
			}

			@Override
			public String getRemoteUser() {
				return user;
			}

			@Override
			public boolean isUserInRole(String r) {
				return role.equals(r);
			}
		}, res);
	}
}
