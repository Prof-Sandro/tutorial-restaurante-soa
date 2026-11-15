// Finalidade: Verificar os cenários desta etapa por asserções automatizadas.
package br.edu.soa.restaurante.infraestrutura;

import br.edu.soa.restaurante.aplicacao.*;
import br.edu.soa.restaurante.dominio.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.*;
import javax.sql.DataSource;

class RestauranteBancoTest {
	Restaurante api;
	JdbcRepositorio repo;
	DataSource ds;
	Mesa mesa;
	Categoria categoria;
	Produto produto;

	@BeforeEach
	void preparar() {
		ds = Banco.configurar("jdbc:h2:mem:" + UUID.randomUUID() + ";MODE=MySQL;DB_CLOSE_DELAY=-1", "sa", "");
		repo = new JdbcRepositorio(ds);
		api = new Restaurante(repo);
		mesa = api.salvarMesa(0, 2);
		categoria = api.salvarCategoria(0, "Massas", "Artesanais", true);
		produto = api.salvarProduto(0, categoria.id(), "Carbonara", "Ovos, queijo e guanciale", "assets/carbonara.svg",
				new BigDecimal("55.00"), "Serve 1 pessoa", true);
	}

	// Cenário: cadastros e consultas.
	@Test
	void cadastrosEConsultas() {
		assertEquals(2, api.consultarMesa(mesa.id()).numero());
		assertEquals("Massas", api.consultarCategoria(categoria.id()).nome());
		assertEquals(new BigDecimal("55.00"), api.consultarProduto(produto.id()).preco());
		assertEquals(1, api.listarProdutosPorCategoria(categoria.id()).size());
		assertEquals(1, api.cardapio().categorias().size());
	}

	// Cenário: numero duplicado.
	@Test
	void numeroDuplicado() {
		assertEquals("DUPLICADO", assertThrows(Falha.class, () -> api.salvarMesa(0, 2)).codigo());
	}

	// Cenário: categoria inexistente.
	@Test
	void categoriaInexistente() {
		assertThrows(Falha.class, () -> api.salvarProduto(0, 999, "Suco", "", "", BigDecimal.TEN, "Copo", true));
	}

	// Cenário: abrir pedido ocupa mesa eassocia.
	@Test
	void abrirPedidoOcupaMesaEAssocia() {
		Pedido p = api.abrirPedido(mesa.id());
		assertEquals(mesa.id(), p.mesaId());
		assertEquals(SituacaoMesa.OCUPADA, api.consultarMesa(mesa.id()).situacao());
		assertEquals("MESA_OCUPADA", assertThrows(Falha.class, () -> api.abrirPedido(mesa.id())).codigo());
	}

	// Cenário: subtotal e total servidor.
	@Test
	void subtotalETotalServidor() {
		Pedido p = api.abrirPedido(mesa.id());
		p = api.adicionarItem(p.id(), produto.id(), 2);
		assertEquals(new BigDecimal("110.00"), p.itens().get(0).subtotal());
		assertEquals(new BigDecimal("110.00"), p.total());
	}

	// Cenário: conta exemplo 149.
	@Test
	void contaExemplo149() {
		Produto suco = api.salvarProduto(0, categoria.id(), "Suco", "", "", new BigDecimal("12.00"), "Copo", true);
		Produto pudim = api.salvarProduto(0, categoria.id(), "Pudim", "", "", new BigDecimal("15.00"), "Fatia", true);
		Pedido p = api.abrirPedido(mesa.id());
		api.adicionarItem(p.id(), produto.id(), 2);
		api.adicionarItem(p.id(), suco.id(), 2);
		assertEquals(new BigDecimal("149.00"), api.adicionarItem(p.id(), pudim.id(), 1).total());
	}

	// Cenário: quantidade e remocao.
	@Test
	void quantidadeERemocao() {
		Pedido p = api.adicionarItem(api.abrirPedido(mesa.id()).id(), produto.id(), 1);
		long item = p.itens().get(0).id();
		assertEquals(new BigDecimal("165.00"), api.atualizarQuantidadeItem(p.id(), item, 3).total());
		assertEquals(new BigDecimal("0.00"), api.removerItem(p.id(), item).total());
	}

	// Cenário: identidade e preco historico por inclusao.
	@Test
	void identidadeEPrecoHistoricoPorInclusao() {
		Pedido p = api.adicionarItem(api.abrirPedido(mesa.id()).id(), produto.id(), 1);
		long item = p.itens().get(0).id();
		api.salvarProduto(produto.id(), categoria.id(), "Carbonara nova", "", "", new BigDecimal("60.00"), "1 pessoa",
				true);
		p = api.adicionarItem(p.id(), produto.id(), 1);
		assertNotEquals(item, p.itens().get(1).id());
		assertEquals(new BigDecimal("115.00"), p.total());
		p = api.atualizarQuantidadeItem(p.id(), item, 2);
		assertEquals(new BigDecimal("170.00"), p.total());
		assertEquals("Carbonara", p.itens().get(0).nomeProduto());
	}

	// Cenário: fechar libera mesa eimpede alteracao.
	@Test
	void fecharLiberaMesaEImpedeAlteracao() {
		Pedido p = api.adicionarItem(api.abrirPedido(mesa.id()).id(), produto.id(), 1);
		assertEquals(SituacaoPedido.FECHADO, api.fecharPedido(p.id()).situacao());
		assertEquals(SituacaoMesa.LIVRE, api.consultarMesa(mesa.id()).situacao());
		assertEquals("PEDIDO_FECHADO",
				assertThrows(Falha.class, () -> api.adicionarItem(p.id(), produto.id(), 1)).codigo());
		assertThrows(Falha.class, () -> api.removerItem(p.id(), p.itens().get(0).id()));
		assertThrows(Falha.class, () -> api.atualizarQuantidadeItem(p.id(), p.itens().get(0).id(), 2));
		assertThrows(Falha.class, () -> api.cancelarPedido(p.id()));
		assertNotEquals(p.id(), api.abrirPedido(mesa.id()).id());
	}

	// Cenário: cancelar preserva historico.
	@Test
	void cancelarPreservaHistorico() {
		Pedido p = api.abrirPedido(mesa.id());
		api.cancelarPedido(p.id());
		assertEquals(SituacaoMesa.LIVRE, api.consultarMesa(mesa.id()).situacao());
		assertEquals("PEDIDO_CANCELADO", assertThrows(Falha.class, () -> api.cancelarPedido(p.id())).codigo());
	}

	// Cenário: vazio somente pode ser cancelado.
	@Test
	void vazioSomentePodeSerCancelado() {
		Pedido p = api.abrirPedido(mesa.id());
		assertThrows(Falha.class, () -> api.fecharPedido(p.id()));
	}

	// Cenário: indisponivel nao entra em pedido.
	@Test
	void indisponivelNaoEntraEmPedido() {
		api.salvarProduto(produto.id(), categoria.id(), produto.nome(), "", "", produto.preco(), produto.porcao(),
				false);
		Pedido p = api.abrirPedido(mesa.id());
		assertEquals("PRODUTO_INDISPONIVEL",
				assertThrows(Falha.class, () -> api.adicionarItem(p.id(), produto.id(), 1)).codigo());
		assertEquals(1, api.cardapio().produtos().size());
		assertFalse(api.cardapio().produtos().get(0).disponivel());
	}

	// Cenário: categoria inativa sai do cardapio enao aceita itens.
	@Test
	void categoriaInativaSaiDoCardapioENaoAceitaItens() {
		api.salvarCategoria(categoria.id(), categoria.nome(), "", false);
		assertTrue(api.cardapio().produtos().isEmpty());
		Pedido p = api.abrirPedido(mesa.id());
		assertThrows(Falha.class, () -> api.adicionarItem(p.id(), produto.id(), 1));
	}

	// Cenário: exclusao preserva historico.
	@Test
	void exclusaoPreservaHistorico() {
		Pedido p = api.adicionarItem(api.abrirPedido(mesa.id()).id(), produto.id(), 1);
		api.fecharPedido(p.id());
		assertEquals("REFERENCIADO", assertThrows(Falha.class, () -> api.excluirProduto(produto.id())).codigo());
		assertThrows(Falha.class, () -> api.excluirCategoria(categoria.id()));
		assertThrows(Falha.class, () -> api.excluirMesa(mesa.id()));
	}

	// Cenário: item de outro pedido nao pode ser alterado.
	@Test
	void itemDeOutroPedidoNaoPodeSerAlterado() {
		Pedido p = api.adicionarItem(api.abrirPedido(mesa.id()).id(), produto.id(), 1);
		Pedido outro = api.abrirPedido(api.salvarMesa(0, 3).id());
		assertThrows(Falha.class, () -> api.removerItem(outro.id(), p.itens().get(0).id()));
		assertEquals(1, api.consultarPedido(p.id()).itens().size());
	}

	// Cenário: rollback unidade de trabalho.
	@Test
	void rollbackUnidadeDeTrabalho() {
		assertThrows(IllegalStateException.class, () -> repo.transacao(s -> {
			s.salvarMesa(0, 99);
			throw new IllegalStateException("Simulação");
		}));
		assertEquals(1, api.listarMesas().size());
	}

	// Cenário: concorrencia nao abre dois pedidos.
	@Test
	void concorrenciaNaoAbreDoisPedidos() throws Exception {
		ExecutorService pool = Executors.newFixedThreadPool(2);
		CountDownLatch inicio = new CountDownLatch(1);
		Callable<Boolean> abrir = () -> {
			inicio.await();
			try {
				api.abrirPedido(mesa.id());
				return true;
			} catch (Falha e) {
				return false;
			}
		};
		try {
			var a = pool.submit(abrir);
			var b = pool.submit(abrir);
			inicio.countDown();
			assertNotEquals(a.get(15, TimeUnit.SECONDS), b.get(15, TimeUnit.SECONDS));
			assertEquals(1, api.listarPedidos().size());
		} finally {
			pool.shutdownNow();
		}
	}

	// Cenário: falha de conexao nao expoe detalhes.
	@Test
	void falhaDeConexaoNaoExpoeDetalhes() {
		org.h2.jdbcx.JdbcDataSource indisponivel = new org.h2.jdbcx.JdbcDataSource();
		indisponivel.setURL("jdbc:h2:mem:ausente;IFEXISTS=TRUE");
		Falha f = assertThrows(Falha.class, () -> new Restaurante(new JdbcRepositorio(indisponivel)).listarMesas());
		assertEquals("PERSISTENCIA", f.codigo());
		assertFalse(f.getMessage().contains("jdbc:"));
	}
}
