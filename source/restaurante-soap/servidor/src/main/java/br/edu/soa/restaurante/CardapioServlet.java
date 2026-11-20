// Finalidade: Atender às consultas públicas do catálogo e à geração de QR no WAR.
package br.edu.soa.restaurante;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.Map;
import br.edu.soa.restaurante.infraestrutura.QrCode;
import br.edu.soa.restaurante.aplicacao.Falha;

/**
 * BFF somente leitura. O navegador não recebe credenciais nem consulta pedidos.
 */
public final class CardapioServlet extends HttpServlet {
	
	private final ObjectMapper json = new ObjectMapper();

	// Atende a consulta HTTP e escreve a resposta no formato previsto.
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
		res.setHeader("X-Content-Type-Options", "nosniff");
		res.setHeader("Cache-Control", "no-store");
		
		if ("/qr".equals(req.getPathInfo())) {
			String mesa = req.getParameter("mesa");
			if (mesa == null || !mesa.matches("[0-9]{1,4}")) {
				res.sendError(400, "Mesa numérica obrigatória.");
				return;
			}
			try {
				byte[] png = QrCode.gerar(System.getProperty("restaurante.PUBLIC_BASE_URL", System.getenv("PUBLIC_BASE_URL")),
						Integer.parseInt(mesa));
				res.setContentType("image/png");
				res.getOutputStream().write(png);
			} catch (Falha e) {
				res.sendError(e.tipo() == Falha.Tipo.VALIDACAO ? 400 : 503, e.getMessage());
			}
			return;
		}
		if (req.getPathInfo() != null && !"/cardapio".equals(req.getPathInfo())) {
			res.sendError(404);
			return;
		}
		res.setContentType("application/json;charset=UTF-8");
		try {
			json.writeValue(res.getOutputStream(), Contexto.restaurante().cardapio());
		} catch (RuntimeException e) {
			res.setStatus(503);
			json.writeValue(res.getOutputStream(),
					Map.of("codigo", "INDISPONIVEL", "mensagem", "Cardápio temporariamente indisponível."));
		}
	}
}
