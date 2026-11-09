package br.edu.soa.restaurante.dominio;

import java.math.BigDecimal;

public record ItemPedido(
		long id,
		long produtoId,
		String nomeProduto,
		int quantidade,
		BigDecimal precoUnitario
		) {
	
	public ItemPedido {
		if (quantidade <= 0 || quantidade > 100)
			throw new IllegalArgumentException("Quantidade deve estar entre 1 e 100");
	}

	public BigDecimal subtotal() {
		return precoUnitario.multiply(BigDecimal.valueOf(quantidade));
	}
}
