// Finalidade: Compartilhar o fluxo de cadastro dos painéis Swing.
package br.edu.soa.restaurante;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/** Lê campos na EDT e envia um snapshot ao SwingWorker. */
abstract class CadastroPanel<T, E> extends JPanel {
	protected final Janela janela;
	protected final ConexaoSoap conexao;
	protected final JPanel formulario = new JPanel(new GridLayout(0, 2, 6, 6));
	protected final DefaultTableModel modelo;
	protected final JTable tabela;
	protected List<T> registros = List.of();
	protected long id;

	protected CadastroPanel(Janela janela, String... colunas) {
		super(new BorderLayout(8, 8));
		this.janela = janela;
		conexao = janela.conexao;
		setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
		modelo = new DefaultTableModel(colunas, 0) {
			@Override
			public boolean isCellEditable(int r, int c) {
				return false;
			}
		};
		tabela = new JTable(modelo);
		tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		add(formulario, BorderLayout.NORTH);
		add(new JScrollPane(tabela), BorderLayout.CENTER);
		JPanel botoes = new JPanel();
		for (String acao : new String[] { "Novo", "Salvar", "Excluir", "Atualizar" }) {
			JButton b = new JButton(acao);
			botoes.add(b);
			b.addActionListener(e -> {
				try {
					switch (acao) {
					case "Novo" -> {
						id = 0;
						tabela.clearSelection();
						limpar();
					}
					case "Atualizar" -> carregar();
					case "Salvar" -> {
						E entrada = ler();
						long alvo = id;
						janela.remoto(() -> {
							salvar(alvo, entrada);
							return listar();
						}, this::exibir);
					}
					case "Excluir" -> {
						if (id == 0)
							throw new IllegalArgumentException("Selecione um registro.");
						if (JOptionPane.showConfirmDialog(this, "Excluir registro selecionado?", "Excluir",
								JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
							long alvo = id;
							janela.remoto(() -> {
								excluir(alvo);
								return listar();
							}, this::exibir);
						}
					}
					}
				} catch (RuntimeException x) {
					janela.erro(x);
				}
			});
		}
		add(botoes, BorderLayout.SOUTH);
		tabela.getSelectionModel().addListSelectionListener(e -> {
			int r = tabela.getSelectedRow();
			if (!e.getValueIsAdjusting() && r >= 0)
				preencher(registros.get(r));
		});
	}

	protected void campo(String rotulo, JComponent componente) {
		JLabel label = new JLabel(rotulo);
		label.setLabelFor(componente);
		formulario.add(label);
		formulario.add(componente);
	}

	// Obtém os dados necessários e atualiza a apresentação pelo fluxo da janela.
	protected void carregar() {
		janela.remoto(this::listar, this::exibir);
	}

	// Apresenta o retorno do servidor na janela.
	protected void exibir(List<T> valores) {
		registros = valores;
		modelo.setRowCount(0);
		valores.forEach(v -> modelo.addRow(linha(v)));
		id = 0;
		limpar();
	}

	protected abstract List<T> listar() throws Exception;

	protected abstract E ler();

	protected abstract void salvar(long id, E entrada) throws Exception;

	protected abstract void excluir(long id) throws Exception;

	protected abstract Object[] linha(T valor);

	protected abstract void preencher(T valor);

	protected abstract void limpar();
}
