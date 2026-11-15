// Finalidade: Implementar a porta de persistência com SQL parametrizado e transações JDBC.
package br.edu.soa.restaurante.infraestrutura;

import br.edu.soa.restaurante.aplicacao.*;
import br.edu.soa.restaurante.dominio.*;
import java.sql.*;
import java.util.*;
import java.util.function.Function;
import javax.sql.DataSource;

/**
 * JDBC parametrizado, uma conexão por transação, sem dependência do servidor
 * Web.
 */
public final class JdbcRepositorio implements Repositorio {
	private static final System.Logger LOG = System.getLogger(JdbcRepositorio.class.getName());
	private final DataSource dataSource;

	public JdbcRepositorio(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	// Executa uma unidade de trabalho; a implementação administra confirmação e
	// reversão.
	@Override
	public <T> T transacao(Function<Sessao, T> trabalho) {
		try (Connection c = dataSource.getConnection()) {
			c.setAutoCommit(false);
			c.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
			try {
				T resultado = trabalho.apply(new JdbcSessao(c));
				c.commit();
				return resultado;
			} catch (RuntimeException | Error e) {
				try {
					c.rollback();
				} catch (SQLException rollback) {
					e.addSuppressed(rollback);
				}
				throw e;
			}
		} catch (SQLException e) {
			throw falhaSql(e);
		}
	}

	// Traduz códigos de persistência para falhas da aplicação sem expor a mensagem
	// SQL.
	static Falha falhaSql(SQLException e) {
		String state = Objects.toString(e.getSQLState(), "");
		int codigo = e.getErrorCode();
		// MySQL usa SQLState 23000 para diferentes violações; o código distingue-as.
		if (codigo == 1062 || state.equals("23505"))
			return Falha.conflito("DUPLICADO", "Número de mesa ou pedido aberto duplicado.");
		if (codigo == 1451 || codigo == 1452 || state.equals("23503"))
			return Falha.conflito("REFERENCIADO", "Registro possui vínculos. Desative-o para preservar o histórico.");
		if (codigo == 1205 || codigo == 1213 || state.startsWith("40"))
			return Falha.conflito("CONCORRENCIA", "Operação concorrente. Atualize os dados e tente novamente.");
		// Nunca registrar parâmetros, credenciais ou a mensagem SQL na resposta
		// pública.
		LOG.log(System.Logger.Level.ERROR, "Falha de persistência; SQLState={0}, código={1}", state, e.getErrorCode());
		return new Falha("PERSISTENCIA", Falha.Tipo.INTERNO,
				"Não foi possível persistir os dados. Tente novamente ou contate a administração.");
	}

	private static final class JdbcSessao implements Sessao {
		private final Connection c;

		JdbcSessao(Connection c) {
			this.c = c;
		}

		@FunctionalInterface
		interface Mapper<T> {
			T map(ResultSet r) throws SQLException;
		}

		// Cria o statement e vincula os parâmetros à consulta.
		private PreparedStatement stmt(String sql, Object... args) throws SQLException {
			PreparedStatement p = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			for (int i = 0; i < args.length; i++)
				p.setObject(i + 1, args[i]);
			p.setQueryTimeout(10);
			return p;
		}

		// Percorre o resultado e aplica o mapeador de cada linha.
		private <T> List<T> list(String sql, Mapper<T> mapper, Object... args) {
			try (PreparedStatement p = stmt(sql, args); ResultSet r = p.executeQuery()) {
				List<T> values = new ArrayList<>();
				while (r.next())
					values.add(mapper.map(r));
				return values;
			} catch (SQLException e) {
				throw falhaSql(e);
			}
		}

		// Exige um registro no resultado da consulta.
		private <T> T one(String sql, Mapper<T> mapper, Object... args) {
			// Percorre o resultado e aplica o mapeador de cada linha.
			return list(sql, mapper, args).stream().findFirst().orElseThrow(
					() -> new Falha("NAO_ENCONTRADO", Falha.Tipo.NAO_ENCONTRADO, "Registro não encontrado."));
		}

		// Executa a inserção e recupera a chave gerada.
		private long insert(String sql, Object... args) {
			try (PreparedStatement p = stmt(sql, args)) {
				p.executeUpdate();
				try (ResultSet r = p.getGeneratedKeys()) {
					if (r.next())
						return r.getLong(1);
				}
				throw new SQLException("Chave não retornada");
			} catch (SQLException e) {
				throw falhaSql(e);
			}
		}

		// Executa a atualização e confere o efeito esperado.
		private void update(String sql, Object... args) {
			try (PreparedStatement p = stmt(sql, args)) {
				if (p.executeUpdate() == 0)
					throw new Falha("NAO_ENCONTRADO", Falha.Tipo.NAO_ENCONTRADO, "Registro não encontrado.");
			} catch (SQLException e) {
				throw falhaSql(e);
			}
		}

		private Mesa mesa(ResultSet r) throws SQLException {
			return new Mesa(r.getLong("id"), r.getInt("numero"), SituacaoMesa.valueOf(r.getString("situacao")));
		}

		public Mesa mesa(long id, boolean lock) {
			return one("SELECT * FROM mesa WHERE id=?" + (lock ? " FOR UPDATE" : ""), this::mesa, id);
		}

		public List<Mesa> mesas() {
			return list("SELECT * FROM mesa ORDER BY numero", this::mesa);
		}

		// Insere ou atualiza o número da mesa por SQL parametrizado.
		public Mesa salvarMesa(long id, int numero) {
			if (id == 0)
				id = insert("INSERT INTO mesa(numero) VALUES (?)", numero);
			// Executa a atualização e confere o efeito esperado.
			else
				update("UPDATE mesa SET numero=? WHERE id=?", numero, id);
			return mesa(id, false);
		}

		public void ocuparMesa(long id, SituacaoMesa s) {
			update("UPDATE mesa SET situacao=? WHERE id=?", s.name(), id);
		}

		// Exclui a mesa pelo ID; a aplicação confere sua situação antes de chamar a
		// persistência.
		public void excluirMesa(long id) {
			update("DELETE FROM mesa WHERE id=?", id);
		}

		private Categoria categoria(ResultSet r) throws SQLException {
			return new Categoria(r.getLong("id"), r.getString("nome"), r.getString("descricao"), r.getBoolean("ativo"));
		}

		public Categoria categoria(long id) {
			return one("SELECT * FROM categoria WHERE id=?", this::categoria, id);
		}

		public List<Categoria> categorias() {
			return list("SELECT * FROM categoria ORDER BY nome,id", this::categoria);
		}

		// Insere ou atualiza os dados da categoria pelo contrato de persistência.
		public Categoria salvarCategoria(Categoria v) {
			long id = v.id();
			if (id == 0)
				id = insert("INSERT INTO categoria(nome,descricao,ativo) VALUES (?,?,?)", v.nome(), v.descricao(),
						v.ativa());
			// Executa a atualização e confere o efeito esperado.
			else
				update("UPDATE categoria SET nome=?,descricao=?,ativo=? WHERE id=?", v.nome(), v.descricao(), v.ativa(),
						id);
			return categoria(id);
		}

		// Executa a exclusão; chaves estrangeiras protegem os vínculos existentes.
		public void excluirCategoria(long id) {
			update("DELETE FROM categoria WHERE id=?", id);
		}

		private Produto produto(ResultSet r) throws SQLException {
			return new Produto(r.getLong("id"), r.getLong("categoria_id"), r.getString("nome"),
					r.getString("descricao"), r.getString("imagem"), r.getBigDecimal("preco"), r.getString("porcao"),
					r.getBoolean("disponivel"));
		}

		public Produto produto(long id, boolean lock) {
			return one("SELECT * FROM produto WHERE id=?" + (lock ? " FOR UPDATE" : ""), this::produto, id);
		}

		public List<Produto> produtos() {
			return list("SELECT * FROM produto ORDER BY nome,id", this::produto);
		}

		// Grava os dados recebidos do produto; a validação de negócio ocorre em
		// Restaurante.
		public Produto salvarProduto(Produto v) {
			long id = v.id();
			Object[] a = { v.categoriaId(), v.nome(), v.descricao(), v.imagem(), v.preco(), v.porcao(),
					v.disponivel() };
			if (id == 0)
				id = insert(
						"INSERT INTO produto(categoria_id,nome,descricao,imagem,preco,porcao,disponivel) VALUES (?,?,?,?,?,?,?)",
						a);
			else {
				Object[] b = Arrays.copyOf(a, 8);
				b[7] = id;
				update("UPDATE produto SET categoria_id=?,nome=?,descricao=?,imagem=?,preco=?,porcao=?,disponivel=? WHERE id=?",
						b);
			}
			return produto(id, false);
		}

		public void excluirProduto(long id) {
			update("DELETE FROM produto WHERE id=?", id);
		}

		private ItemPedido item(ResultSet r) throws SQLException {
			return new ItemPedido(r.getLong("id"), r.getLong("produto_id"), r.getString("nome_produto"),
					r.getInt("quantidade"), r.getBigDecimal("preco_unitario"));
		}

		private Pedido pedido(ResultSet r) throws SQLException {
			long id = r.getLong("id");
			return new Pedido(id, r.getLong("mesa_id"),
					r.getObject("data_hora", java.time.LocalDateTime.class).toInstant(java.time.ZoneOffset.UTC),
					SituacaoPedido.valueOf(r.getString("situacao")),
					list("SELECT * FROM item_pedido WHERE pedido_id=? ORDER BY id", this::item, id));
		}

		public Pedido pedido(long id, boolean lock) {
			return one("SELECT * FROM pedido WHERE id=?" + (lock ? " FOR UPDATE" : ""), this::pedido, id);
		}

		public List<Pedido> pedidos() {
			return list("SELECT * FROM pedido ORDER BY id DESC", this::pedido);
		}

		public Pedido inserirPedido(long mesaId) {
			var agoraUtc = java.time.LocalDateTime.ofInstant(java.time.Instant.now(), java.time.ZoneOffset.UTC);
			return pedido(insert("INSERT INTO pedido(mesa_id,data_hora,situacao,mesa_aberta) VALUES (?,?,'ABERTO',?)",
					mesaId, agoraUtc, mesaId), false);
		}

		// Guarda nome e preço do produto como dados históricos da inclusão.
		public void inserirItem(long pedidoId, Produto p, int q) {
			insert("INSERT INTO item_pedido(pedido_id,produto_id,nome_produto,quantidade,preco_unitario) VALUES (?,?,?,?,?)",
					pedidoId, p.id(), p.nome(), q, p.preco());
		}

		public void quantidadeItem(long pedidoId, long itemId, int q) {
			update("UPDATE item_pedido SET quantidade=? WHERE id=? AND pedido_id=?", q, itemId, pedidoId);
		}

		public void removerItem(long pedidoId, long itemId) {
			update("DELETE FROM item_pedido WHERE id=? AND pedido_id=?", itemId, pedidoId);
		}

		public void encerrarPedido(long id, SituacaoPedido s) {
			update("UPDATE pedido SET situacao=?,mesa_aberta=NULL WHERE id=?", s.name(), id);
		}
	}
}
