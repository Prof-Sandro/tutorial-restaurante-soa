// Finalidade: Adaptar o contrato SOAP aos casos de uso e aos tipos de resposta.
package br.edu.soa.restaurante;

import br.edu.soa.restaurante.soap.*;
import jakarta.jws.WebService;
import jakarta.annotation.Resource;
import jakarta.xml.ws.WebServiceContext;

@WebService(endpointInterface = "br.edu.soa.restaurante.soap.CategoriaServicePortType", serviceName = "CategoriaService", portName = "CategoriaPort", targetNamespace = "http://restaurante.soa.edu.br/v2", wsdlLocation = "WEB-INF/wsdl/CategoriaService.wsdl")
public class CategoriaEndpoint implements CategoriaServicePortType {
	@Resource
	private WebServiceContext contexto;

	@Override
	public Categoria criarCategoria(CategoriaEntrada v) throws ErroNegocio_Exception {
		return Acesso.executar(contexto, true, () -> {
			var api = Contexto.restaurante();
			if (v == null)
				throw br.edu.soa.restaurante.aplicacao.Falha.invalido("Informe categoria.");
			return SoapDtos.dto(api.salvarCategoria(0, v.getNome(), v.getDescricao(), v.isAtivo()));
		});
	}

	@Override
	public Categoria consultarCategoria(long id) throws ErroNegocio_Exception {
		return Acesso.executar(contexto, false, () -> {
			var api = Contexto.restaurante();
			return SoapDtos.dto(api.consultarCategoria(id));
		});
	}

	// Autoriza a operação SOAP e delega aos casos de uso de Restaurante.
	@Override
	public java.util.List<Categoria> listarCategorias() throws ErroNegocio_Exception {
		return Acesso.executar(contexto, false, () -> {
			var api = Contexto.restaurante();
			return api.listarCategorias().stream().map(SoapDtos::dto).toList();
		});
	}

	@Override
	public Categoria atualizarCategoria(long id, CategoriaEntrada v) throws ErroNegocio_Exception {
		return Acesso.executar(contexto, true, () -> {
			var api = Contexto.restaurante();
			if (id <= 0 || v == null)
				throw br.edu.soa.restaurante.aplicacao.Falha.invalido("ID positivo e dados obrigatórios.");
			return SoapDtos.dto(api.salvarCategoria(id, v.getNome(), v.getDescricao(), v.isAtivo()));
		});
	}

	// Autoriza a operação SOAP e delega aos casos de uso de Restaurante.
	@Override
	public boolean excluirCategoria(long id) throws ErroNegocio_Exception {
		return Acesso.executar(contexto, true, () -> {
			var api = Contexto.restaurante();
			api.excluirCategoria(id);
			return true;
		});
	}
}