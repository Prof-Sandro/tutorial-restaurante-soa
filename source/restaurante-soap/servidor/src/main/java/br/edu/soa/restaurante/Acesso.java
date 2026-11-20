// Finalidade: Concentrar autorização e tradução das falhas para o contrato SOAP.
package br.edu.soa.restaurante;

import br.edu.soa.restaurante.aplicacao.Falha;
import br.edu.soa.restaurante.soap.*;
import jakarta.xml.ws.WebServiceContext;
import java.util.function.Supplier;

public final class Acesso {

    private Acesso() {}

    // Concentra autorização, execução e tradução segura de falhas.
    public static <T> T executar(
            WebServiceContext contexto,
            boolean admin,
            Supplier<T> acao
    ) throws ErroNegocio_Exception {

        try {
            if (contexto == null || !(contexto.isUserInRole("ADMIN") ||  (!admin && contexto.isUserInRole("GARCOM")))) {

                throw new Falha(
                    "ACESSO_NEGADO", Falha.Tipo.CONFLITO,
                    "Seu perfil não permite esta operação."
                );
            }

            return acao.get();

        } catch (Falha e) {
            ErroNegocio detalhe = new ErroNegocio();
            detalhe.setCodigo(e.codigo());
            detalhe.setMensagem(e.getMessage());

            throw new ErroNegocio_Exception(e.getMessage(), detalhe);

        } catch (RuntimeException e) {
            System.getLogger(Acesso.class.getName()).log(
                System.Logger.Level.ERROR,
                "Erro interno no adaptador SOAP: {0}",
                e.getClass().getSimpleName()
            );

            ErroNegocio detalhe = new ErroNegocio();
            detalhe.setCodigo("INTERNO");
            detalhe.setMensagem("Não foi possível concluir a operação.");

            throw new ErroNegocio_Exception(detalhe.getMensagem(), detalhe);
        }
    }
}