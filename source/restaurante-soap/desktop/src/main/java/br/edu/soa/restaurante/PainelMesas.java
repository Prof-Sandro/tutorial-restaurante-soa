// Finalidade: Criar PainelMesas.java com formulário e tabela do cadastro.
package br.edu.soa.restaurante;

import br.edu.soa.restaurante.soap.*;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

final class PainelMesas extends CadastroPanel<Mesa, MesaEntrada> {
	private final JSpinner numero = new JSpinner(new SpinnerNumberModel(1, 1, 9999, 1));

	PainelMesas(Janela j) {
		super(j, "ID", "Número", "Situação");
		campo("Número", numero);
	}

	protected List<Mesa> listar() throws Exception {
		return conexao.mesas().listarMesas();
	}

	// Captura os valores da interface para a operação.
	protected MesaEntrada ler() {
		var v = new MesaEntrada();
		v.setNumero((int) numero.getValue());
		return v;
	}

	protected void salvar(long id, MesaEntrada v) throws Exception {
		var api = conexao.mesas();
		if (id == 0)
			api.criarMesa(v);
		else
			api.atualizarMesa(id, v);
	}

	protected void excluir(long id) throws Exception {
		conexao.mesas().excluirMesa(id);
	}

	protected Object[] linha(Mesa v) {
		return new Object[] { v.getId(), v.getNumero(), v.getSituacao() };
	}

	// Mostra os dados selecionados nos componentes da tela.
	protected void preencher(Mesa v) {
		id = v.getId();
		numero.setValue(v.getNumero());
	}

	// Prepara os campos para uma nova operação.
	protected void limpar() {
		numero.setValue(1);
	}
}
