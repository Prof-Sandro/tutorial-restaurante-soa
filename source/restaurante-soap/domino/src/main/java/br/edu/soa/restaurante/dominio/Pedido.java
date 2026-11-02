package br.edu.soa.restaurante.dominio;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record Pedido(
		long id,
		long mesa,
		Instant dataHora,
		SituacaoPedido situacao,
		List<ItemPedido> itens
		) {

	public Pedido{ itens = List.copyOf(itens); }
	
	public BigDecimal total() {
		return itens.stream().map(ItemPedido::subtotal).reduce(new BigDecimal("0.00"), BigDecimal::add); }
	
}
