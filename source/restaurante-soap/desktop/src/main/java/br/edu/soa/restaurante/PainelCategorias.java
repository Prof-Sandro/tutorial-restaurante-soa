// Finalidade: Criar PainelCategorias.java com formulário e tabela do cadastro.
package br.edu.soa.restaurante;

import br.edu.soa.restaurante.soap.*;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

final class PainelCategorias extends CadastroPanel<Categoria, CategoriaEntrada> {
	private final JTextField nome = new JTextField(), descricao = new JTextField();
	private final JCheckBox ativo = new JCheckBox("Ativa", true);

	PainelCategorias(Janela j) {
		super(j, "ID", "Nome", "Descrição", "Ativa");
		campo("Nome", nome);
		campo("Descrição", descricao);
		campo("Estado", ativo);
	}

	protected List<Categoria> listar() throws Exception {
		return conexao.categorias().listarCategorias();
	}

	// Captura os valores da interface para a operação.
	protected CategoriaEntrada ler() {
		var v = new CategoriaEntrada();
		v.setNome(nome.getText());
		v.setDescricao(descricao.getText());
		v.setAtivo(ativo.isSelected());
		return v;
	}

	protected void salvar(long id, CategoriaEntrada v) throws Exception {
		var api = conexao.categorias();
		if (id == 0)
			api.criarCategoria(v);
		else
			api.atualizarCategoria(id, v);
	}

	protected void excluir(long id) throws Exception {
		conexao.categorias().excluirCategoria(id);
	}

	protected Object[] linha(Categoria v) {
		return new Object[] { v.getId(), v.getNome(), v.getDescricao(), v.isAtivo() };
	}

	// Mostra os dados selecionados nos componentes da tela.
	protected void preencher(Categoria v) {
		id = v.getId();
		nome.setText(v.getNome());
		descricao.setText(v.getDescricao());
		ativo.setSelected(v.isAtivo());
	}

	// Prepara os campos para uma nova operação.
	protected void limpar() {
		nome.setText("");
		descricao.setText("");
		ativo.setSelected(true);
	}
}
