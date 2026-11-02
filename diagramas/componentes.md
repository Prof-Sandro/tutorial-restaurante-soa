# Arquitetura do sistema do restaurante

O laboratório possui duas versões independentes: uma SOAP e outra REST.
Cada versão tem seu próprio banco de dados.

As duas reutilizam o código das regras de negócio, mas não compartilham
os dados. Um pedido criado no SOAP não aparece automaticamente no REST.

```mermaid
flowchart LR
    subgraph SOAP["Versão SOAP — localhost:8080/restaurante"]
        A["Administração / Swing"] -->|SOAP/XML| S["Serviços SOAP / Tomcat 10.1 + Metro"]
        G["Garçom / Swing: registra pedidos"] -->|SOAP/XML| S

        WS["Celular: cardápio SOAP"] -->|HTTP/JSON público| B["BFF Servlet no Tomcat"]

        S --> US["Casos de uso / aplicação SOAP"]
        B --> US

        US --> DS["Domínio puro Java 17"]
        US --> PS["Porta Repositorio / Sessao"]

        JS["JDBC + Flyway da versão SOAP"] -. implementa .-> PS
        JS --> DBS[("MySQL: restaurante_soap")]
    end

    subgraph REST["Versão REST — localhost:8081"]
        T["Swagger UI / cliente HTTP: testes de pedidos"] -->|HTTP/JSON autenticado| R["Spring Boot REST"]

        WR["Celular: cardápio REST"] -->|HTTP/JSON público| R

        R --> UR["Casos de uso / aplicação REST"]

        UR --> DR["Domínio puro Java 17"]
        UR --> PR["Porta Repositorio / Sessao"]

        JR["JDBC + Flyway da versão REST"] -. implementa .-> PR
        JR --> DBR[("MySQL: restaurante_rest")]
    end
```

## Onde o garçom faz o pedido?

Na versão SOAP, o garçom utiliza a janela Swing `TelaPedidos`:

1. Seleciona a mesa.
2. Clica em **Abrir pedido**.
3. Seleciona o produto e a quantidade.
4. Clica em **Adicionar**.
5. O servidor SOAP verifica as regras e grava em `restaurante_soap`.
6. Ao finalizar o atendimento, clica em **Fechar conta**.

## Como os pedidos são feitos na versão REST?

A API REST também oferece operações para abrir pedidos, adicionar itens
e fechar contas.

No tutorial atual, essas operações são testadas pelo Swagger UI ou por
um cliente HTTP, com autenticação. Os dados ficam em `restaurante_rest`.

Ainda não existe uma tela de atendimento do garçom ligada à API REST.

## Para que serve o cardápio no celular?

Serve para consultar categorias, produtos, descrições e preços.

O cardápio SOAP consulta os dados da versão SOAP.
O cardápio REST consulta os dados da versão REST.

Nenhum dos dois permite registrar pedidos na implementação atual.

## Por que os componentes aparecem duas vezes?

Cada aplicação executa suas próprias instâncias dos casos de uso,
do domínio e da infraestrutura de persistência.

Reutilizar o mesmo código Java não significa compartilhar o mesmo banco.

## O que representa a seta pontilhada?

A seta `implementa` indica que o repositório JDBC fornece a implementação
da interface `Repositorio`, utilizada pelos casos de uso.

Ela representa uma relação entre componentes do código.
Não representa sincronização entre os bancos.

