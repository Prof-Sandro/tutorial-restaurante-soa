// Finalidade: Construir os clientes SOAP com endereço e credenciais da sessão.
package br.edu.soa.restaurante;

import br.edu.soa.restaurante.soap.*;
import jakarta.xml.ws.*;
import javax.xml.namespace.QName;
import java.net.URI;

/**
 * WSDL local; cada operação recebe um proxy próprio com timeout e autenticação.
 */
public final class ConexaoSoap {
	private static final String NS = "http://restaurante.soa.edu.br/v2";
	private final String usuario, senha;

	public ConexaoSoap(String usuario, String senha) {
		this.usuario = usuario;
		this.senha = senha;
	}

	private <T> T port(String entidade, Class<T> tipo) {
		String base = System.getProperty("soap.base",
				System.getenv().getOrDefault("SOAP_BASE_URL", "http://localhost:8080/restaurante"));
		URI uri = URI.create(base);
		if (!java.util.Set.of("http", "https").contains(uri.getScheme()) || uri.getHost() == null)
			throw new IllegalArgumentException("SOAP_BASE_URL deve ser URL HTTP(S).");
		Service service = Service.create(ConexaoSoap.class.getResource("/wsdl/" + entidade + "Service.wsdl"),
				new QName(NS, entidade + "Service"));
		T proxy = service.getPort(new QName(NS, entidade + "Port"), tipo);
		var contexto = ((BindingProvider) proxy).getRequestContext();
		contexto.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
				base.replaceAll("/+$", "") + "/soap/" + entidade.toLowerCase() + "s");
		contexto.put(BindingProvider.USERNAME_PROPERTY, usuario);
		contexto.put(BindingProvider.PASSWORD_PROPERTY, senha);
		contexto.put("com.sun.xml.ws.connect.timeout", 5000);
		contexto.put("com.sun.xml.ws.request.timeout", 10000);
		return proxy;
	}

	public MesaServicePortType mesas() {
		return port("Mesa", MesaServicePortType.class);
	}

	public CategoriaServicePortType categorias() {
		return port("Categoria", CategoriaServicePortType.class);
	}

	public ProdutoServicePortType produtos() {
		return port("Produto", ProdutoServicePortType.class);
	}

	public PedidoServicePortType pedidos() {
		return port("Pedido", PedidoServicePortType.class);
	}
}
