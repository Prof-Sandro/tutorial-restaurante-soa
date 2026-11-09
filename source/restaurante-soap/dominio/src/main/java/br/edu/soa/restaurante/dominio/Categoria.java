package br.edu.soa.restaurante.dominio;

public record Categoria(
		long id,
		String nome,
		String descricao,
		boolean ativa
		) {

}
