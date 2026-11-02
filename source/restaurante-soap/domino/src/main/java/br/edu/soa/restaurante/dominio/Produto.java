package br.edu.soa.restaurante.dominio;

import java.math.BigDecimal;

public record Produto(
		long id,
		long categoriaId,
		String nome,
		String descricao,
		String imagem,
		BigDecimal preco,
		String porcao,
		boolean disponivel
		
		) {

}
