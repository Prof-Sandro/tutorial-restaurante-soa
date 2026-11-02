
# Modelo de Entidades — Restaurante

## Objetivo

Apresentar os relacionamentos entre as entidades
do sistema de restaurante SOAP e REST.

## Diagrama de entidades

```mermaid
erDiagram
    MESA ||--o{ PEDIDO : recebe
    CATEGORIA ||--o{ PRODUTO : classifica
    PEDIDO ||--o{ ITEM_PEDIDO : contem
    PRODUTO ||--o{ ITEM_PEDIDO : referencia

    MESA {
        bigint id PK
        int numero UK
        string situacao
    }

    CATEGORIA {
        bigint id PK
        string nome
        string descricao
        boolean ativo
    }

    PRODUTO {
        bigint id PK
        bigint categoria_id FK
        string nome
        decimal preco
    }

    PEDIDO {
        bigint id PK
        bigint mesa_id FK
        timestamp data_hora
        string situacao
    }

    ITEM_PEDIDO {
        bigint id PK
        bigint pedido_id FK
        bigint produto_id FK
        int quantidade
        decimal preco_unitario
    }
```

## Explicação dos relacionamentos

### Mesa e Pedido
Uma mesa pode receber vários pedidos ao longo do tempo.

### Categoria e Produto
Uma categoria pode classificar vários produtos.

### Pedido e ItemPedido
Um pedido pode conter vários itens.

### Produto e ItemPedido
Um produto pode aparecer em vários itens de pedidos.

## Conceitos importantes

- PK: chave primária.
- FK: chave estrangeira.
- UK: chave única.
- 1:N: relacionamento de um para muitos.

