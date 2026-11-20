// Finalidade: Adaptar o contrato SOAP aos casos de uso e aos tipos de resposta.
package br.edu.soa.restaurante;

import br.edu.soa.restaurante.soap.*;
import jakarta.jws.WebService;
import jakarta.annotation.Resource;
import jakarta.xml.ws.WebServiceContext;

@WebService(endpointInterface = "br.edu.soa.restaurante.soap.MesaServicePortType", serviceName = "MesaService", portName = "MesaPort", targetNamespace = "http://restaurante.soa.edu.br/v2", wsdlLocation = "WEB-INF/wsdl/MesaService.wsdl")
public class MesaEndpoint implements MesaServicePortType {
	@Resource
	private WebServiceContext contexto;

	@Override
	public Mesa criarMesa(MesaEntrada v) throws ErroNegocio_Exception {
		return Acesso.executar(contexto, true, () -> {
			var api = Contexto.restaurante();
			if (v == null)
				throw br.edu.soa.restaurante.aplicacao.Falha.invalido("Informe mesa.");
			return SoapDtos.dto(api.salvarMesa(0, v.getNumero()));
		});
	}

	// Autoriza a operação SOAP e delega aos casos de uso de Restaurante.
	@Override
	public Mesa consultarMesa(long id) throws ErroNegocio_Exception {
		return Acesso.executar(contexto, false, () -> {
			var api = Contexto.restaurante();
			return SoapDtos.dto(api.consultarMesa(id));
		});
	}

	// Autoriza a operação SOAP e delega aos casos de uso de Restaurante.
	@Override
	public java.util.List<Mesa> listarMesas() throws ErroNegocio_Exception {
		return Acesso.executar(contexto, false, () -> {
			var api = Contexto.restaurante();
			return api.listarMesas().stream().map(SoapDtos::dto).toList();
		});
	}

	@Override
	public Mesa atualizarMesa(long id, MesaEntrada v) throws ErroNegocio_Exception {
		return Acesso.executar(contexto, true, () -> {
			var api = Contexto.restaurante();
			if (id <= 0 || v == null)
				throw br.edu.soa.restaurante.aplicacao.Falha.invalido("ID positivo e dados obrigatórios.");
			return SoapDtos.dto(api.salvarMesa(id, v.getNumero()));
		});
	}

	// Autoriza a operação SOAP e delega aos casos de uso de Restaurante.
	@Override
	public boolean excluirMesa(long id) throws ErroNegocio_Exception {
		return Acesso.executar(contexto, true, () -> {
			var api = Contexto.restaurante();
			api.excluirMesa(id);
			return true;
		});
	}
}