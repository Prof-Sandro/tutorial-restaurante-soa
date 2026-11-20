// Finalidade: Adaptar o contrato SOAP aos casos de uso e aos tipos de resposta.
package br.edu.soa.restaurante;

import br.edu.soa.restaurante.soap.*;
import jakarta.jws.WebService;
import jakarta.annotation.Resource;
import jakarta.xml.ws.WebServiceContext;

@WebService(endpointInterface = "br.edu.soa.restaurante.soap.ProdutoServicePortType", serviceName = "ProdutoService", portName = "ProdutoPort", targetNamespace = "http://restaurante.soa.edu.br/v2", wsdlLocation = "WEB-INF/wsdl/ProdutoService.wsdl")
public class ProdutoEndpoint implements ProdutoServicePortType {
	@Resource
	private WebServiceContext contexto;

	@Override
	public Produto criarProduto(ProdutoEntrada v) throws ErroNegocio_Exception {
		return Acesso.executar(contexto, true, () -> {
			var api = Contexto.restaurante();
			if (v == null)
				throw br.edu.soa.restaurante.aplicacao.Falha.invalido("Informe produto.");
			return SoapDtos.dto(api.salvarProduto(0, v.getCategoriaId(), v.getNome(), v.getDescricao(), v.getImagem(),
					v.getPreco(), v.getPorcao(), v.isDisponivel()));
		});
	}

	@Override
	public Produto consultarProduto(long id) throws ErroNegocio_Exception {
		return Acesso.executar(contexto, false, () -> {
			var api = Contexto.restaurante();
			return SoapDtos.dto(api.consultarProduto(id));
		});
	}

	@Override
	public java.util.List<Produto> listarProdutos() throws ErroNegocio_Exception {
		return Acesso.executar(contexto, false, () -> {
			var api = Contexto.restaurante();
			return api.listarProdutos().stream().map(SoapDtos::dto).toList();
		});
	}

	@Override
	public Produto atualizarProduto(long id, ProdutoEntrada v) throws ErroNegocio_Exception {
		return Acesso.executar(contexto, true, () -> {
			var api = Contexto.restaurante();
			if (id <= 0 || v == null)
				throw br.edu.soa.restaurante.aplicacao.Falha.invalido("ID positivo e dados obrigatórios.");
			return SoapDtos.dto(api.salvarProduto(id, v.getCategoriaId(), v.getNome(), v.getDescricao(), v.getImagem(),
					v.getPreco(), v.getPorcao(), v.isDisponivel()));
		});
	}

	@Override
	public boolean excluirProduto(long id) throws ErroNegocio_Exception {
		return Acesso.executar(contexto, true, () -> {
			var api = Contexto.restaurante();
			api.excluirProduto(id);
			return true;
		});
	}

	@Override
	public java.util.List<Produto> listarProdutosPorCategoria(long categoriaId) throws ErroNegocio_Exception {
		return Acesso.executar(contexto, false, () -> {
			var api = Contexto.restaurante();
			return api.listarProdutosPorCategoria(categoriaId).stream().map(SoapDtos::dto).toList();
		});
	}
}
