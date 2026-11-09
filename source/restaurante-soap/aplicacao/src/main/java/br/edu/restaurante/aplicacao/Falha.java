// Finalidade: representar uma falha da aplicação com código, tipo e mensagem.
// [1] O pacote corresponde às pastas br/edu/soa/restaurante/aplicacao.
package br.edu.restaurante.aplicacao;

// [2] public: outros módulos podem usar esta classe.
// final: não permite criar subclasses de Falha.
// extends RuntimeException: permite lançar e capturar uma Falha como exceção.
public final class Falha extends RuntimeException {

    // [3] Categoria geral do problema. Para usar fora da classe: Falha.Tipo.
    public enum Tipo {
        VALIDACAO,       // A entrada não atende a uma regra.
        NAO_ENCONTRADO,   // O registro solicitado não existe.
        CONFLITO,        // O estado atual impede a operação.
        INTERNO          // Um problema técnico impediu a operação.
    }

    // [4] Dados adicionais da exceção.
    // private: acesso por métodos; final: atribuídos no construtor uma única vez.
    private final String codigo;
    private final Tipo tipo;

    // [5] Construtor: recebe os três dados de uma falha.
    public Falha(String codigo, Tipo tipo, String mensagem) {
        // Guarda a mensagem na parte herdada de RuntimeException.
        // Por isso podemos recuperá-la depois com getMessage().
        super(mensagem);
        // this.codigo é o campo; codigo é o argumento recebido.
        this.codigo = codigo;
        this.tipo = tipo;
    }

    // [6] Métodos de consulta dos campos privados.
    public String codigo() {
        return codigo;
    }

    public Tipo tipo() {
        return tipo;
    }

    // [7] Método de criação: chamado diretamente por Falha.invalido(...).
    // O chamador informa a mensagem; código e tipo são preenchidos aqui.
    public static Falha invalido(String mensagem) {
        return new Falha("ENTRADA_INVALIDA", Tipo.VALIDACAO, mensagem);
    }

    // [8] Um conflito pode ter códigos diferentes: MESA_OCUPADA, PEDIDO_FECHADO...
    // O tipo é fixo; o chamador informa o código específico e a mensagem.
    public static Falha conflito(String codigo, String mensagem) {
        return new Falha(codigo, Tipo.CONFLITO, mensagem);
    }
}
