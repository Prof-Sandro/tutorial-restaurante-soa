// Finalidade: Converter o domínio para os DTOs gerados a partir dos contratos.
package br.edu.soa.restaurante;

import br.edu.soa.restaurante.soap.*;

public final class SoapDtos {
	
	private SoapDtos() {	}

	public static Mesa dto(br.edu.soa.restaurante.dominio.Mesa v) {
		Mesa d = new Mesa();
		d.setId(v.id());
		d.setNumero(v.numero());
		d.setSituacao(SituacaoMesa.valueOf(v.situacao().name()));
		return d;
	}

	public static Categoria dto(br.edu.soa.restaurante.dominio.Categoria v) {
		Categoria d = new Categoria();
		d.setId(v.id());
		d.setNome(v.nome());
		d.setDescricao(v.descricao());
		d.setAtivo(v.ativa());
		return d;
	}

	public static Produto dto(br.edu.soa.restaurante.dominio.Produto v) {
		Produto d = new Produto();
		d.setId(v.id());
		d.setCategoriaId(v.categoriaId());
		d.setNome(v.nome());
		d.setDescricao(v.descricao());
		d.setImagem(v.imagem());
		d.setPreco(v.preco());
		d.setPorcao(v.porcao());
		d.setDisponivel(v.disponivel());
		return d;
	}

	public static ItemPedido dto(br.edu.soa.restaurante.dominio.ItemPedido v) {
		ItemPedido d = new ItemPedido();
		d.setId(v.id());
		d.setProdutoId(v.produtoId());
		d.setNomeProduto(v.nomeProduto());
		d.setQuantidade(v.quantidade());
		d.setPrecoUnitario(v.precoUnitario());
		d.setSubtotal(v.subtotal());
		return d;
	}

	public static Pedido dto(br.edu.soa.restaurante.dominio.Pedido v) {
		Pedido d = new Pedido();
		d.setId(v.id());
		d.setMesaId(v.mesaId());
		d.setDataHora(v.dataHora().toString());
		d.setSituacao(SituacaoPedido.valueOf(v.situacao().name()));
		d.setTotal(v.total());
		v.itens().forEach(i -> d.getItens().add(dto(i)));
		return d;
	}
}
