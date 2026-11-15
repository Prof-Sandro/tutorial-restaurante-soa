// Finalidade: Verificar os cenários desta etapa por asserções automatizadas.
package br.edu.soa.restaurante.infraestrutura;

import br.edu.soa.restaurante.aplicacao.Restaurante;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Opt-in: usa apenas banco de teste explícito no MySQL localhost. Não apaga
 * tabelas.
 */
@EnabledIfEnvironmentVariable(named = "MYSQL_TEST_URL", matches = "jdbc:mysql://localhost:(?:3306|13306)/restaurante_teste(?:\\?.*)?")
class MySqlLocalTest {
	// Cenário: persiste conta no my sql real.
	@Test
	void persisteContaNoMySqlReal() {
		var ds = Banco.configurar(System.getenv("MYSQL_TEST_URL"), System.getenv("MYSQL_TEST_USER"),System.getenv().getOrDefault("MYSQL_TEST_PASSWORD", ""));
		
		var api = new Restaurante(new JdbcRepositorio(ds));
		
		var usados = api.listarMesas().stream().map(m -> m.numero()).collect(java.util.stream.Collectors.toSet());
		
		int numero = java.util.stream.IntStream.range(1000, 10000).filter(n -> !usados.contains(n)).findFirst().orElseThrow();
		
		var mesa = api.salvarMesa(0, numero);
		
		var cat = api.salvarCategoria(0, "Teste MySQL", "Dados de integração", true);
		
		var produto = api.salvarProduto(0, cat.id(), "Carbonara de teste", "", "", new BigDecimal("55.00"), "1 pessoa",true);
		
		var pedido = api.adicionarItem(api.abrirPedido(mesa.id()).id(), produto.id(), 2);
		
		assertEquals(new BigDecimal("110.00"), pedido.total());
		api.fecharPedido(pedido.id());
		
		var outraInstancia = new Restaurante(new JdbcRepositorio(ds));
		assertEquals(new BigDecimal("110.00"), outraInstancia.consultarPedido(pedido.id()).total());
		assertEquals(br.edu.soa.restaurante.dominio.SituacaoMesa.LIVRE,
				outraInstancia.consultarMesa(mesa.id()).situacao());
	}
}
