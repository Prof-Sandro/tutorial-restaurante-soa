package br.edu.soa.restaurante.aplicacao;

import br.edu.soa.restaurante.dominio.*;
import java.util.List;
import java.util.function.Function;

/** Unidade de trabalho: toda função usa uma conexão e termina com commit ou rollback. */
public interface Repositorio {
    // Executa uma unidade de trabalho; a implementação administra confirmação e reversão.
    <T> T transacao(Function<Sessao, T> trabalho);

    interface Sessao {
        Mesa mesa(long id, boolean bloquear);
        List<Mesa> mesas();
        // Define a gravação do número da mesa; as regras de ocupação pertencem à aplicação.
        Mesa salvarMesa(long id, int numero);
        void ocuparMesa(long id, SituacaoMesa situacao);
        // Exclui a mesa pelo ID; a aplicação confere sua situação antes de chamar a persistência.
        void excluirMesa(long id);
        Categoria categoria(long id);
        List<Categoria> categorias();
        // Insere ou atualiza os dados da categoria pelo contrato de persistência.
        Categoria salvarCategoria(Categoria categoria);
        // Executa a exclusão; chaves estrangeiras protegem os vínculos existentes.
        void excluirCategoria(long id);
        Produto produto(long id, boolean bloquear);
        List<Produto> produtos();
        // Grava os dados recebidos do produto; a validação de negócio ocorre em Restaurante.
        Produto salvarProduto(Produto produto);
        void excluirProduto(long id);
        Pedido pedido(long id, boolean bloquear);
        List<Pedido> pedidos();
        Pedido inserirPedido(long mesaId);
        // Guarda nome e preço do produto como dados históricos da inclusão.
        void inserirItem(long pedidoId, Produto produto, int quantidade);
        void quantidadeItem(long pedidoId, long itemId, int quantidade);
        void removerItem(long pedidoId, long itemId);
        void encerrarPedido(long id, SituacaoPedido situacao);
    }
}