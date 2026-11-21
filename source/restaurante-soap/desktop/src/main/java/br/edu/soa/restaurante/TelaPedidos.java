// Finalidade: Apresentar as operações do garçom e o estado devolvido pelo servidor.
package br.edu.soa.restaurante;

import br.edu.soa.restaurante.soap.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public final class TelaPedidos extends Janela {
	private final JComboBox<Mesa> mesa = new JComboBox<>();
	private final JComboBox<Produto> produto = new JComboBox<>();
	private final JSpinner quantidade = new JSpinner(new SpinnerNumberModel(1, 1, 10000, 1));
	private final DefaultTableModel pedidos = modelo("Pedido", "Mesa ID", "Situação", "Total R$"),
			itens = modelo("Item ID", "Produto", "Quantidade", "Preço R$", "Subtotal R$");
	private final JTable grade = new JTable(pedidos), gradeItens = new JTable(itens);
	private final JLabel conta = new JLabel("Selecione um pedido");
	private final JButton adicionar = new JButton("Adicionar"), alterar = new JButton("Alterar quantidade"),
			remover = new JButton("Remover item"), fechar = new JButton("Fechar conta"),
			cancelar = new JButton("Cancelar pedido"), abrir = new JButton("Abrir pedido");
	private List<Pedido> lista = List.of();
	private Pedido selecionado;

	public TelaPedidos(ConexaoSoap conexao) {
		super("Restaurante • Garçom", conexao);
		mesa.setRenderer(new DefaultListCellRenderer() {
			@Override
			public Component getListCellRendererComponent(JList<?> l, Object v, int i, boolean s, boolean f) {
				return super.getListCellRendererComponent(l,
						v instanceof Mesa m ? "Mesa " + m.getNumero() + " • " + m.getSituacao() : "", i, s, f);
			}
		});
		produto.setRenderer(new DefaultListCellRenderer() {
			@Override
			public Component getListCellRendererComponent(JList<?> l, Object v, int i, boolean s, boolean f) {
				return super.getListCellRendererComponent(l,
						v instanceof Produto p ? p.getNome() + " • R$ " + p.getPreco() : "", i, s, f);
			}
		});
		JPanel topo = new JPanel(new GridLayout(0, 1, 8, 8)), mesas = new JPanel(), linha = new JPanel();
		JButton atualizar = new JButton("Atualizar");
		mesas.add(new JLabel("Mesa"));
		mesas.add(mesa);
		mesas.add(abrir);
		mesas.add(atualizar);
		linha.add(new JLabel("Produto"));
		linha.add(produto);
		linha.add(new JLabel("Quantidade"));
		linha.add(quantidade);
		linha.add(adicionar);
		linha.add(alterar);
		linha.add(remover);
		topo.add(mesas);
		topo.add(linha);
		add(topo, BorderLayout.NORTH);
		grade.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		gradeItens.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JScrollPane(grade),
				new JScrollPane(gradeItens));
		split.setResizeWeight(.45);
		JPanel centro = new JPanel(new BorderLayout());
		centro.add(split);
		JPanel rodape = new JPanel();
		rodape.add(conta);
		rodape.add(fechar);
		rodape.add(cancelar);
		centro.add(rodape, BorderLayout.SOUTH);
		add(centro);
		atualizar.addActionListener(e -> carregar());
		abrir.addActionListener(e -> {
			Mesa m = (Mesa) mesa.getSelectedItem();
			if (m != null)
				remoto(() -> conexao.pedidos().abrirPedido(m.getId()), p -> {
					exibir(p);
					carregar();
				});
		});
		grade.getSelectionModel().addListSelectionListener(e -> {
			int r = grade.getSelectedRow();
			if (!e.getValueIsAdjusting() && r >= 0) {
				long id = lista.get(r).getId();
				remoto(() -> conexao.pedidos().consultarPedido(id), this::exibir);
			}
		});
		gradeItens.getSelectionModel().addListSelectionListener(e -> {
			int r = gradeItens.getSelectedRow();
			if (r >= 0 && selecionado != null)
				quantidade.setValue(selecionado.getItens().get(r).getQuantidade());
		});
		adicionar.addActionListener(e -> {
			if (selecionado == null)
				return;
			Produto p = (Produto) produto.getSelectedItem();
			if (p == null)
				return;
			long id = selecionado.getId();
			int q = (int) quantidade.getValue();
			remoto(() -> conexao.pedidos().adicionarItem(id, p.getId(), q), this::aposAlteracao);
		});
		alterar.addActionListener(e -> {
			int r = gradeItens.getSelectedRow();
			if (r < 0 || selecionado == null)
				return;
			long id = selecionado.getId(), item = selecionado.getItens().get(r).getId();
			int q = (int) quantidade.getValue();
			remoto(() -> conexao.pedidos().atualizarQuantidadeItem(id, item, q), this::aposAlteracao);
		});
		remover.addActionListener(e -> {
			int r = gradeItens.getSelectedRow();
			if (r < 0 || selecionado == null)
				return;
			long id = selecionado.getId(), item = selecionado.getItens().get(r).getId();
			remoto(() -> conexao.pedidos().removerItem(id, item), this::aposAlteracao);
		});
		fechar.addActionListener(e -> encerrar(false));
		cancelar.addActionListener(e -> encerrar(true));
		mesa.addActionListener(e -> atualizarEstado());
		carregar();
	}

	private void aposAlteracao(Pedido p) {
		exibir(p);
		carregar();
	}

	// Solicita a operação remota e apresenta o resultado devolvido pelo servidor.
	private void encerrar(boolean cancelamento) {
		if (selecionado == null)
			return;
		long id = selecionado.getId();
		if (JOptionPane.showConfirmDialog(this, cancelamento ? "Cancelar este pedido?" : "Fechar conta e liberar mesa?",
				"Confirmar", JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION)
			return;
		remoto(() -> cancelamento ? conexao.pedidos().cancelarPedido(id) : conexao.pedidos().fecharPedido(id), p -> {
			exibir(p);
			JOptionPane.showMessageDialog(this, "Pedido " + p.getId() + " • " + p.getSituacao() + " | Total R$ "
					+ p.getTotal() + " | Mesa liberada.");
			carregar();
		});
	}

	private record Dados(List<Mesa> mesas, List<Produto> produtos, List<Pedido> pedidos) {
	}

	// Obtém os dados necessários e atualiza a apresentação pelo fluxo da janela.
	private void carregar() {
		remoto(() -> {
			var ativas = conexao.categorias().listarCategorias().stream().filter(Categoria::isAtivo)
					.map(Categoria::getId).toList();
			return new Dados(conexao.mesas().listarMesas(),
					conexao.produtos().listarProdutos().stream()
							.filter(p -> p.isDisponivel() && ativas.contains(p.getCategoriaId())).toList(),
					conexao.pedidos().listarPedidos());
		}, d -> {
			long mesaId = mesa.getSelectedItem() instanceof Mesa m ? m.getId() : 0;
			mesa.removeAllItems();
			d.mesas().forEach(m -> {
				mesa.addItem(m);
				if (m.getId() == mesaId)
					mesa.setSelectedItem(m);
			});
			produto.removeAllItems();
			d.produtos().forEach(produto::addItem);
			lista = d.pedidos();
			pedidos.setRowCount(0);
			lista.forEach(
					p -> pedidos.addRow(new Object[] { p.getId(), p.getMesaId(), p.getSituacao(), p.getTotal() }));
			if (selecionado != null)
				lista.stream().filter(p -> p.getId() == selecionado.getId()).findFirst().ifPresent(this::exibir);
		});
	}

	// Apresenta o retorno do servidor na janela.
	private void exibir(Pedido p) {
		selecionado = p;
		itens.setRowCount(0);
		p.getItens().forEach(i -> itens.addRow(new Object[] { i.getId(), i.getNomeProduto(), i.getQuantidade(),
				i.getPrecoUnitario(), i.getSubtotal() }));
		conta.setText("Pedido " + p.getId() + " • " + p.getSituacao() + " • Total R$ " + p.getTotal());
		atualizarEstado();
	}

	// Ajusta os controles ao estado recebido do pedido.
	@Override
	protected void atualizarEstado() {
		boolean aberto = selecionado != null && selecionado.getSituacao() == SituacaoPedido.ABERTO;
		for (JButton b : new JButton[] { adicionar, alterar, remover, fechar, cancelar })
			b.setEnabled(aberto);
		abrir.setEnabled(mesa.getSelectedItem() instanceof Mesa m && m.getSituacao() == SituacaoMesa.LIVRE);
	}

	private static DefaultTableModel modelo(String... nomes) {
		return new DefaultTableModel(nomes, 0) {
			@Override
			public boolean isCellEditable(int r, int c) {
				return false;
			}
		};
	}

	// Inicia a aplicação correspondente a esta classe.
	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			var c = login("garcom");
			if (c != null)
				new TelaPedidos(c).setVisible(true);
		});
	}
}
