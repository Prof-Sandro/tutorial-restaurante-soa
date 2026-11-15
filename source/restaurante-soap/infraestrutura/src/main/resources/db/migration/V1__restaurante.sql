-- Finalidade: Criar db/migration/V1__restaurante.sql nos recursos da infraestrutura.
CREATE TABLE mesa (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 numero INTEGER NOT NULL UNIQUE CHECK (numero BETWEEN 1 AND 9999),
 situacao VARCHAR(20) NOT NULL DEFAULT 'LIVRE' CHECK (situacao IN ('LIVRE','OCUPADA'))
) ENGINE=InnoDB;
CREATE TABLE categoria (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 nome VARCHAR(120) NOT NULL,
 descricao VARCHAR(2000) NOT NULL,
 ativo BOOLEAN NOT NULL
) ENGINE=InnoDB;
CREATE TABLE produto (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 categoria_id BIGINT NOT NULL,
 nome VARCHAR(160) NOT NULL,
 descricao VARCHAR(2000) NOT NULL,
 imagem VARCHAR(1000) NOT NULL,
 preco NUMERIC(9,2) NOT NULL CHECK (preco >= 0),
 porcao VARCHAR(160) NOT NULL,
 disponivel BOOLEAN NOT NULL,
 CONSTRAINT fk_produto_categoria FOREIGN KEY (categoria_id) REFERENCES categoria(id)
) ENGINE=InnoDB;
CREATE TABLE pedido (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 mesa_id BIGINT NOT NULL,
 data_hora DATETIME(6) NOT NULL,
 situacao VARCHAR(20) NOT NULL CHECK (situacao IN ('ABERTO','FECHADO','CANCELADO')),
 CONSTRAINT fk_pedido_mesa FOREIGN KEY (mesa_id) REFERENCES mesa(id),
 CONSTRAINT fk_pedido_aberta FOREIGN KEY (mesa_aberta) REFERENCES mesa(id),
 mesa_aberta BIGINT UNIQUE,
 CHECK ((situacao = 'ABERTO' AND mesa_aberta IS NOT NULL AND mesa_aberta = mesa_id)
     OR (situacao <> 'ABERTO' AND mesa_aberta IS NULL))
) ENGINE=InnoDB;
CREATE TABLE item_pedido (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 pedido_id BIGINT NOT NULL,
 produto_id BIGINT NOT NULL,
 nome_produto VARCHAR(160) NOT NULL,
 quantidade INTEGER NOT NULL CHECK (quantidade BETWEEN 1 AND 10000),
 preco_unitario NUMERIC(9,2) NOT NULL CHECK (preco_unitario >= 0),
 CONSTRAINT fk_item_pedido FOREIGN KEY (pedido_id) REFERENCES pedido(id),
 CONSTRAINT fk_item_produto FOREIGN KEY (produto_id) REFERENCES produto(id)
) ENGINE=InnoDB;
CREATE INDEX ix_produto_categoria ON produto(categoria_id);
CREATE INDEX ix_pedido_mesa ON pedido(mesa_id);
CREATE INDEX ix_item_pedido ON item_pedido(pedido_id);
CREATE INDEX ix_item_produto ON item_pedido(produto_id);
