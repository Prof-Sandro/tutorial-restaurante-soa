// Finalidade: Coordenar os casos de uso do restaurante pela porta Repositorio.
package br.edu.soa.restaurante.aplicacao;

import br.edu.soa.restaurante.dominio.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.util.List;
import java.util.function.Function;

/**
 * Casos de uso. O transporte não fornece total, preço de item nem situação do
 * pedido.
 */
public final class Restaurante {

	private final Repositorio repositorio;

	public Restaurante(Repositorio repositorio) {
		this.repositorio = repositorio;
	}

	// Consulta as mesas pela porta de persistência.
	public List<Mesa> listarMesas() {
		return repositorio.transacao(Repositorio.Sessao::mesas);
	}

	// Obtém uma mesa identificada pelo ID.
	public Mesa consultarMesa(long id) {
		return repositorio.transacao(s -> s.mesa(id, false));
	}

	// Valida o cadastro e impede alterar uma mesa ocupada.
	public Mesa salvarMesa(long id, int numero) {

		if (id < 0 || numero < 1 || numero > 9999)
			throw Falha.invalido("Número da mesa entre 1 e 9999; ID não negativo.");
		return repositorio.transacao(s -> {
			if (id > 0 && s.mesa(id, true).situacao() == SituacaoMesa.OCUPADA)
				throw Falha.conflito("MESA_OCUPADA", "Libere a mesa antes de alterar seu número.");
			return s.salvarMesa(id, numero);
		});
	}

	// Confere a situação da mesa antes de solicitar a exclusão.
	public void excluirMesa(long id) {
		repositorio.transacao(s -> {
			if (s.mesa(id, true).situacao() == SituacaoMesa.OCUPADA)
				throw Falha.conflito("MESA_OCUPADA", "Mesa possui pedido aberto.");
			s.excluirMesa(id);
			return null;
		});
	}

	// Consulta o catálogo de categorias.
	public List<Categoria> listarCategorias() {
		return repositorio.transacao(Repositorio.Sessao::categorias);
	}

	public Categoria consultarCategoria(long id) {
		return repositorio.transacao(s -> s.categoria(id));
	}

	// Valida os textos e coordena o cadastro da categoria.
	public Categoria salvarCategoria(long id, String nome, String descricao, boolean ativo) {

		if (id < 0)
			throw Falha.invalido("ID não negativo.");

		Categoria c = new Categoria(id, texto(nome, "Nome", 120), opcional(descricao, 2000), ativo);

		return repositorio.transacao(s -> {
			if (id > 0)
				s.categoria(id);
			return s.salvarCategoria(c);
		});
	}

	// Solicita a exclusão; vínculos existentes serão protegidos pela persistência.
	public void excluirCategoria(long id) {

		repositorio.transacao(s -> {
			s.categoria(id);
			s.excluirCategoria(id);
			return null;
		});

	}

	public Produto salvarProduto(long id, long categoriaId, String nome, String descricao, String imagem,
			BigDecimal preco, String porcao, boolean disponivel) {
		if (id < 0)
			throw Falha.invalido("ID não negativo.");
		if (preco == null || preco.signum() < 0 || preco.compareTo(new BigDecimal("9999999.99")) > 0)
			throw Falha.invalido("Preço entre 0 e 9999999,99.");
		BigDecimal dinheiro;
		try {
			dinheiro = preco.setScale(2, RoundingMode.UNNECESSARY);
		} catch (ArithmeticException e) {
			throw Falha.invalido("Preço deve ter no máximo duas casas decimais.");
		}
		Produto p = new Produto(id, categoriaId, texto(nome, "Nome", 160), opcional(descricao, 2000), imagem(imagem),
				dinheiro, texto(porcao, "Porção", 160), disponivel);
		return repositorio.transacao(s -> {
			if (id > 0)
				s.produto(id, true);
			if (!s.categoria(categoriaId).ativa())
				throw Falha.invalido("Categoria deve estar ativa.");
			return s.salvarProduto(p);
		});
	}

	public void excluirProduto(long id) {
		repositorio.transacao(s -> {
			s.produto(id, true);
			s.excluirProduto(id);
			return null;
		});
	}

	/** Projeção pública: não retorna pedidos, usuários ou mesas ocupadas. */
// Monta a projeção pública de categorias e produtos disponíveis.
	public Cardapio cardapio() {
		return repositorio.transacao(s -> {
			List<Categoria> cats = s.categorias().stream().filter(Categoria::ativa).toList();
			return new Cardapio(cats,
					s.produtos().stream().filter(p -> cats.stream().anyMatch(c -> c.id() == p.categoriaId())).toList());
		});
	}

	public record Cardapio(List<Categoria> categorias, List<Produto> produtos) {
	}

	// Exige mesa livre e registra a abertura e sua ocupação na mesma transação.
	public Pedido abrirPedido(long mesaId) {
		return repositorio.transacao(s -> {
			if (s.mesa(mesaId, true).situacao() != SituacaoMesa.LIVRE)
				throw Falha.conflito("MESA_OCUPADA", "Esta mesa já possui um pedido aberto.");
			Pedido pedido = s.inserirPedido(mesaId);
			s.ocuparMesa(mesaId, SituacaoMesa.OCUPADA);
			return pedido;
		});
	}

	public Pedido consultarPedido(long id) {
		return repositorio.transacao(s -> s.pedido(id, false));
	}

	public List<Pedido> listarPedidos() {
		return repositorio.transacao(Repositorio.Sessao::pedidos);
	}

	public List<Pedido> listarPedidosPorMesa(long mesaId) {
		return repositorio.transacao(s -> {
			s.mesa(mesaId, false);
			return s.pedidos().stream().filter(p -> p.mesaId() == mesaId).toList();
		});
	}

	// Confere disponibilidade e inclui o produto com seus dados históricos.
	public Pedido adicionarItem(long pedidoId, long produtoId, int quantidade) {
		quantidade(quantidade);
		// Exige pedido aberto e coordena a alteração dentro da transação.
		return alterar(pedidoId, s -> {
			Produto p = s.produto(produtoId, true);
			if (!p.disponivel() || !s.categoria(p.categoriaId()).ativo())
				throw Falha.conflito("PRODUTO_INDISPONIVEL", "Produto indisponível para novos itens.");
			s.inserirItem(pedidoId, p, quantidade);
			return null;
		});
	}

	public Pedido atualizarQuantidadeItem(long pedidoId, long itemId, int quantidade) {
		quantidade(quantidade);
		// Exige pedido aberto e coordena a alteração dentro da transação.
		return alterar(pedidoId, s -> {
			s.quantidadeItem(pedidoId, itemId, quantidade);
			return null;
		});
	}

	public Pedido removerItem(long pedidoId, long itemId) {
		// Exige pedido aberto e coordena a alteração dentro da transação.
		return alterar(pedidoId, s -> {
			s.removerItem(pedidoId, itemId);
			return null;
		});
	}

	// Solicita o encerramento como FECHADO.
	public Pedido fecharPedido(long id) {
		return encerrar(id, SituacaoPedido.FECHADO);
	}

	// Solicita o encerramento como CANCELADO.
	public Pedido cancelarPedido(long id) {
		return encerrar(id, SituacaoPedido.CANCELADO);
	}

	// Encerra o pedido e libera a mesa de forma transacional.
	private Pedido encerrar(long id, SituacaoPedido estado) {
		// Exige pedido aberto e coordena a alteração dentro da transação.
		return alterar(id, s -> {
			Pedido p = s.pedido(id, false);
			if (estado == SituacaoPedido.FECHADO && p.itens().isEmpty())
				throw Falha.invalido("Pedido vazio não pode ser fechado; use cancelar.");
			s.mesa(p.mesaId(), true);
			s.encerrarPedido(id, estado);
			s.ocuparMesa(p.mesaId(), SituacaoMesa.LIVRE);
			return null;
		});
	}

	// Exige pedido aberto e coordena a alteração dentro da transação.
	private Pedido alterar(long id, Function<Repositorio.Sessao, Void> acao) {
		return repositorio.transacao(s -> {
			Pedido atual = s.pedido(id, true);
			if (atual.situacao() != SituacaoPedido.ABERTO)
				throw Falha.conflito("PEDIDO_" + atual.situacao(),
						"Pedido " + atual.situacao().name().toLowerCase() + " não permite alterações.");
			acao.apply(s);
			return s.pedido(id, false);
		});
	}

	// Recusa quantidades fora do intervalo permitido.
	private static void quantidade(int q) {
		if (q < 1 || q > 10000)
			throw Falha.invalido("Quantidade entre 1 e 10000.");
	}

	private static String texto(String s, String nome, int limite) {

		if (s == null || s.isBlank() || s.length() > limite)
			throw Falha.invalido(nome + " obrigatório, limite de " + limite + " caracteres.");
		return s.trim();
	}

	private static String opcional(String s, int limite) {

		if (s == null)
			return "";

		if (s.length() > limite)
			throw Falha.invalido("Texto excede " + limite + " caracteres.");

		return s.trim();
	}

	private static String imagem(String s) {
		String valor = opcional(s, 1000);
		if (valor.isEmpty() || valor.matches("assets/[a-zA-Z0-9_-]+\\.(png|jpg|jpeg|webp|svg)"))
			return valor;
		try {
			URI uri = URI.create(valor);
			if ("https".equalsIgnoreCase(uri.getScheme()) && uri.getHost() != null && uri.getUserInfo() == null)
				return valor;
		} catch (IllegalArgumentException ignored) {
		}

		throw Falha.invalido("Imagem deve ser recurso em assets/ ou URL HTTPS sem credenciais.");
	}
}
