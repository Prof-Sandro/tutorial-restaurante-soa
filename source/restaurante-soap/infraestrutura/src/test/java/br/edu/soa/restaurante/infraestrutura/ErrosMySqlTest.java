// Finalidade: Verificar os cenários desta etapa por asserções automatizadas.
package br.edu.soa.restaurante.infraestrutura;

import java.sql.SQLException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ErrosMySqlTest {
	// Cenário: distingue duplicado de chave estrangeira no mesmo sql state.
	@Test
	void distingueDuplicadoDeChaveEstrangeiraNoMesmoSqlState() {
		assertEquals("DUPLICADO",
				JdbcRepositorio.falhaSql(new SQLException("Detalhe privado", "23000", 1062)).codigo());
		for (int codigo : new int[] { 1451, 1452 })
			assertEquals("REFERENCIADO",
					JdbcRepositorio.falhaSql(new SQLException("Detalhe privado", "23000", codigo)).codigo());
	}

	// Cenário: lock timeout edeadlock pedem nova tentativa.
	@Test
	void lockTimeoutEDeadlockPedemNovaTentativa() {
		for (int codigo : new int[] { 1205, 1213 })
			assertEquals("CONCORRENCIA",
					JdbcRepositorio.falhaSql(new SQLException("Detalhe privado", "HY000", codigo)).codigo());
	}
}
