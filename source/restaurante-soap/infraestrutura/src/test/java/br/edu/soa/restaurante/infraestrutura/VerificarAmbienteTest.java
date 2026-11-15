package br.edu.soa.restaurante.infraestrutura;

import org.junit.jupiter.api.Test;

class VerificarAmbienteTest {

    @Test
    void verificarVariaveis() {

        String url = System.getenv("MYSQL_TEST_URL");
        String usuario = System.getenv("MYSQL_TEST_USER");

        System.out.println("MYSQL_TEST_URL = " + url);
        System.out.println("MYSQL_TEST_USER = " + usuario);
        System.out.println(
            "MYSQL_TEST_PASSWORD definida = "
            + (System.getenv("MYSQL_TEST_PASSWORD") != null)
        );
    }
}
