// Finalidade: Verificar os cenários desta etapa por asserções automatizadas.
package br.edu.soa.restaurante.aplicacao;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.math.BigDecimal;

class RestauranteTest {
	
    // Cenário: rejeita quantidade antes de acessar repositorio.
    @Test void rejeitaQuantidadeAntesDeAcessarRepositorio() {
    	
        Repositorio repo = mock(Repositorio.class); Restaurante api = new Restaurante(repo);
        assertThrows(Falha.class,() -> api.adicionarItem(1,1,0));
        assertThrows(Falha.class,() -> api.atualizarQuantidadeItem(1,1,-1));
        
        verifyNoInteractions(repo);
    }
    // Cenário: rejeita preco negativo ou fracionario.
    @Test void rejeitaPrecoNegativoOuFracionario() {
    	
        Repositorio repo=mock(Repositorio.class); Restaurante api=new Restaurante(repo);
        
        for(String v:new String[]{"-0.01","1.001","10000000.00"})
        	
            assertThrows(Falha.class,() -> api.salvarProduto(0,1,"Suco","","",new BigDecimal(v),"1 copo",true));
        
        verifyNoInteractions(repo);
    }
    // Cenário: rejeita imagem com protocolo executavel ou caminho local.
    @Test void rejeitaImagemComProtocoloExecutavelOuCaminhoLocal() {
    	
        Repositorio repo=mock(Repositorio.class); Restaurante api=new Restaurante(repo);
        
        for(String imagem:new String[]{"javascript:alert(1)","file:///C:/privado.png","assets/../segredo.png"})
        	
            assertThrows(Falha.class,() -> api.salvarProduto(0,1,"Suco","",imagem,BigDecimal.ONE,"1 copo",true));
        
        verifyNoInteractions(repo);
    }
}
