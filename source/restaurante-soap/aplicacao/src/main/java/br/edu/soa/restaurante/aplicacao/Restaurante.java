// Finalidade: Coordenar os casos de uso do restaurante pela porta Repositorio.
package br.edu.soa.restaurante.aplicacao;

import br.edu.soa.restaurante.dominio.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.util.List;
import java.util.function.Function;

/** Casos de uso. O transporte não fornece total, preço de item nem situação do pedido. */
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
    	
        if (id < 0 || numero < 1 || numero > 9999) throw Falha.invalido("Número da mesa entre 1 e 9999; ID não negativo.");
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
            s.excluirMesa(id); return null;
        });
    }
}

