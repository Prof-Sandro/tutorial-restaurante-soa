// Finalidade: Adaptar o contrato SOAP aos casos de uso e aos tipos de resposta.
package br.edu.soa.restaurante;

import br.edu.soa.restaurante.soap.*;
import jakarta.jws.WebService;
import jakarta.annotation.Resource;
import jakarta.xml.ws.WebServiceContext;

@WebService(endpointInterface = "br.edu.soa.restaurante.soap.PedidoServicePortType", serviceName = "PedidoService", portName = "PedidoPort", targetNamespace = "http://restaurante.soa.edu.br/v2", wsdlLocation = "WEB-INF/wsdl/PedidoService.wsdl")
public class PedidoEndpoint implements PedidoServicePortType {
	@Resource
	private WebServiceContext contexto;

	// Autoriza a operação SOAP e delega aos casos de uso de Restaurante.
	@Override
	public Pedido abrirPedido(long mesaId) throws ErroNegocio_Exception {
		return Acesso.executar(contexto, false, () -> {
			var api = Contexto.restaurante();
			return SoapDtos.dto(api.abrirPedido(mesaId));
		});
	}

	@Override
	public Pedido consultarPedido(long id) throws ErroNegocio_Exception {
		return Acesso.executar(contexto, false, () -> {
			var api = Contexto.restaurante();
			return SoapDtos.dto(api.consultarPedido(id));
		});
	}

	@Override
	public java.util.List<Pedido> listarPedidos() throws ErroNegocio_Exception {
		return Acesso.executar(contexto, false, () -> {
			var api = Contexto.restaurante();
			return api.listarPedidos().stream().map(SoapDtos::dto).toList();
		});
	}

	@Override
	public java.util.List<Pedido> listarPedidosPorMesa(long mesaId) throws ErroNegocio_Exception {
		return Acesso.executar(contexto, false, () -> {
			var api = Contexto.restaurante();
			return api.listarPedidosPorMesa(mesaId).stream().map(SoapDtos::dto).toList();
		});
	}

	// Autoriza a operação SOAP e delega aos casos de uso de Restaurante.
	@Override
	public Pedido adicionarItem(long pedidoId, long produtoId, int quantidade) throws ErroNegocio_Exception {
		return Acesso.executar(contexto, false, () -> {
			var api = Contexto.restaurante();
			return SoapDtos.dto(api.adicionarItem(pedidoId, produtoId, quantidade));
		});
	}

	@Override
	public Pedido atualizarQuantidadeItem(long pedidoId, long itemId, int quantidade) throws ErroNegocio_Exception {
		return Acesso.executar(contexto, false, () -> {
			var api = Contexto.restaurante();
			return SoapDtos.dto(api.atualizarQuantidadeItem(pedidoId, itemId, quantidade));
		});
	}

	@Override
	public Pedido removerItem(long pedidoId, long itemId) throws ErroNegocio_Exception {
		return Acesso.executar(contexto, false, () -> {
			var api = Contexto.restaurante();
			return SoapDtos.dto(api.removerItem(pedidoId, itemId));
		});
	}

	// Autoriza a operação SOAP e delega aos casos de uso de Restaurante.
	@Override
	public Pedido fecharPedido(long id) throws ErroNegocio_Exception {
		return Acesso.executar(contexto, false, () -> {
			var api = Contexto.restaurante();
			return SoapDtos.dto(api.fecharPedido(id));
		});
	}

	// Autoriza a operação SOAP e delega aos casos de uso de Restaurante.
	@Override
	public Pedido cancelarPedido(long id) throws ErroNegocio_Exception {
		return Acesso.executar(contexto, false, () -> {
			var api = Contexto.restaurante();
			return SoapDtos.dto(api.cancelarPedido(id));
		});
	}
}