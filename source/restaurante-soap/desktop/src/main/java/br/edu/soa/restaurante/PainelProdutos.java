// Finalidade: Criar PainelProdutos.java com categoria, preço, imagem e pesquisa.
package br.edu.soa.restaurante;

import br.edu.soa.restaurante.soap.*;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

final class PainelProdutos extends CadastroPanel<Produto, ProdutoEntrada> {
	private final JTextField nome = new JTextField(), descricao = new JTextField(), imagem = new JTextField(),
			preco = new JTextField(), porcao = new JTextField(), busca = new JTextField();
	private final JComboBox<Categoria> categoria = new JComboBox<>();
	private final JCheckBox disponivel = new JCheckBox("Disponível", true);

	PainelProdutos(Janela j) {
		super(j, "ID", "Produto", "Categoria", "Preço", "Porção", "Disponível");
		categoria.setRenderer(new DefaultListCellRenderer() {
			@Override
			public Component getListCellRendererComponent(JList<?> l, Object v, int i, boolean s, boolean f) {
				return super.getListCellRendererComponent(l, v instanceof Categoria c ? c.getNome() : "", i, s, f);
			}
		});
		campo("Nome", nome);
		campo("Categoria", categoria);
		campo("Descrição", descricao);
		campo("Preço R$", preco);
		campo("Porção", porcao);
		campo("Estado", disponivel);
		JPanel img = new JPanel(new BorderLayout());
		img.add(imagem);
		JButton escolher = new JButton("Associar arquivo");
		img.add(escolher, BorderLayout.EAST);
		campo("Imagem: URL HTTPS ou assets/arquivo", img);
		escolher.addActionListener(e -> {
			JFileChooser seletor = new JFileChooser();
			seletor.setFileFilter(new FileNameExtensionFilter("Imagens", "png", "jpg", "jpeg", "webp"));
			if (seletor.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
				imagem.setText("assets/" + seletor.getSelectedFile().getName());
				JOptionPane.showMessageDialog(this,
						"Publique o arquivo em cardapio-web/assets antes de empacotar o servidor. A associação não transfere arquivos.");
			}
		});
		JButton pesquisar = new JButton("Pesquisar");
		JPanel pesquisa = new JPanel(new BorderLayout());
		pesquisa.add(busca);
		pesquisa.add(pesquisar, BorderLayout.EAST);
		campo("Pesquisa por nome", pesquisa);
		pesquisar.addActionListener(e -> {
			String termo = busca.getText().toLowerCase(java.util.Locale.ROOT);
			janela.remoto(
					() -> conexao.produtos().listarProdutos().stream()
							.filter(p -> p.getNome().toLowerCase(java.util.Locale.ROOT).contains(termo)).toList(),
					this::exibir);
		});
	}

	void categorias() {
		janela.remoto(() -> conexao.categorias().listarCategorias(), cats -> {
			categoria.removeAllItems();
			cats.forEach(categoria::addItem);
			carregar();
		});
	}

	protected List<Produto> listar() throws Exception {
		return conexao.produtos().listarProdutos();
	}

	// Captura os valores da interface para a operação.
	protected ProdutoEntrada ler() {
		Categoria c = (Categoria) categoria.getSelectedItem();
		if (c == null)
			throw new IllegalArgumentException("Cadastre uma categoria antes do produto.");
		var v = new ProdutoEntrada();
		v.setNome(nome.getText());
		v.setCategoriaId(c.getId());
		v.setDescricao(descricao.getText());
		v.setImagem(imagem.getText());
		v.setPreco(new BigDecimal(preco.getText().trim().replace(',', '.')));
		v.setPorcao(porcao.getText());
		v.setDisponivel(disponivel.isSelected());
		return v;
	}

	protected void salvar(long id, ProdutoEntrada v) throws Exception {
		var api = conexao.produtos();
		if (id == 0)
			api.criarProduto(v);
		else
			api.atualizarProduto(id, v);
	}

	protected void excluir(long id) throws Exception {
		conexao.produtos().excluirProduto(id);
	}

	protected Object[] linha(Produto v) {
		return new Object[] { v.getId(), v.getNome(), v.getCategoriaId(), v.getPreco(), v.getPorcao(),
				v.isDisponivel() };
	}

	// Mostra os dados selecionados nos componentes da tela.
	protected void preencher(Produto v) {
		id = v.getId();
		nome.setText(v.getNome());
		descricao.setText(v.getDescricao());
		imagem.setText(v.getImagem());
		preco.setText(v.getPreco().toPlainString());
		porcao.setText(v.getPorcao());
		disponivel.setSelected(v.isDisponivel());
		for (int i = 0; i < categoria.getItemCount(); i++)
			if (categoria.getItemAt(i).getId() == v.getCategoriaId())
				categoria.setSelectedIndex(i);
	}

	// Prepara os campos para uma nova operação.
	protected void limpar() {
		nome.setText("");
		descricao.setText("");
		imagem.setText("");
		preco.setText("");
		porcao.setText("Serve 1 pessoa");
		disponivel.setSelected(true);
	}
}
