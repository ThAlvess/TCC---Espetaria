-- =========================================================
-- ESPETINHOS TREVIZAN - CRIACAO DAS TABELAS
-- Arquivo 02/03
-- Ordem organizada para respeitar as chaves estrangeiras.
-- =========================================================

USE trevizan_espetinhos;

SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS movimentacao_caixa;
DROP TABLE IF EXISTS item_comanda;
DROP TABLE IF EXISTS pagamento;
DROP TABLE IF EXISTS comanda;
DROP TABLE IF EXISTS caixa;
DROP TABLE IF EXISTS produto;
DROP TABLE IF EXISTS categoria;
DROP TABLE IF EXISTS mesa;
DROP TABLE IF EXISTS usuario;

SET FOREIGN_KEY_CHECKS = 1;

-- =========================================================
-- USUARIO
-- Mantido conforme a versao atual do projeto:
-- ativo em VARCHAR, alem dos campos cpf e perfil.
-- =========================================================
CREATE TABLE usuario (
    id_usuario INT NOT NULL AUTO_INCREMENT,
    nome VARCHAR(100) COLLATE utf8mb4_unicode_ci NOT NULL,
    login VARCHAR(50) COLLATE utf8mb4_unicode_ci NOT NULL,
    senha VARCHAR(255) COLLATE utf8mb4_unicode_ci NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    cpf VARCHAR(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    perfil VARCHAR(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    PRIMARY KEY (id_usuario),
    UNIQUE KEY uk_usuario_login (login)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;

-- =========================================================
-- MESA
-- =========================================================
CREATE TABLE mesa (
    id_mesa INT NOT NULL AUTO_INCREMENT,
    numero INT NOT NULL,
    status ENUM('LIVRE','OCUPADA') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'LIVRE',
    PRIMARY KEY (id_mesa),
    UNIQUE KEY uk_mesa_numero (numero)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;

-- =========================================================
-- CATEGORIA
-- =========================================================
CREATE TABLE categoria (
    id_categoria INT NOT NULL AUTO_INCREMENT,
    nome VARCHAR(50) COLLATE utf8mb4_unicode_ci NOT NULL,
    ativo TINYINT(1) NOT NULL DEFAULT 1,
    PRIMARY KEY (id_categoria),
    UNIQUE KEY uk_categoria_nome (nome)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;

-- =========================================================
-- PRODUTO
-- =========================================================
CREATE TABLE produto (
    id_produto INT NOT NULL AUTO_INCREMENT,
    id_categoria INT NOT NULL,
    nome VARCHAR(100) COLLATE utf8mb4_unicode_ci NOT NULL,
    descricao VARCHAR(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    preco DECIMAL(10,2) NOT NULL,
    quantidade_estoque INT NOT NULL DEFAULT 0,
    ativo TINYINT(1) NOT NULL DEFAULT 1,
    PRIMARY KEY (id_produto),
    KEY fk_produto_categoria (id_categoria),
    CONSTRAINT fk_produto_categoria
        FOREIGN KEY (id_categoria)
        REFERENCES categoria (id_categoria)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;

-- =========================================================
-- CAIXA
-- =========================================================
CREATE TABLE caixa (
    id_caixa INT NOT NULL AUTO_INCREMENT,
    id_usuario_abertura INT NOT NULL,
    data_hora_abertura DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_hora_fechamento DATETIME DEFAULT NULL,
    valor_inicial DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    valor_final DECIMAL(10,2) DEFAULT NULL,
    status ENUM('ABERTO','FECHADO') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'ABERTO',
    PRIMARY KEY (id_caixa),
    KEY fk_caixa_usuario (id_usuario_abertura),
    CONSTRAINT fk_caixa_usuario
        FOREIGN KEY (id_usuario_abertura)
        REFERENCES usuario (id_usuario)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;

-- =========================================================
-- COMANDA
-- =========================================================
CREATE TABLE comanda (
    id_comanda INT NOT NULL AUTO_INCREMENT,
    id_mesa INT NOT NULL,
    id_usuario INT NOT NULL,
    nome_cliente VARCHAR(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    data_abertura DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_fechamento DATETIME DEFAULT NULL,
    status ENUM('ABERTA','FECHADA','CANCELADA') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'ABERTA',
    valor_total DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    PRIMARY KEY (id_comanda),
    KEY fk_comanda_mesa (id_mesa),
    KEY fk_comanda_usuario (id_usuario),
    CONSTRAINT fk_comanda_mesa
        FOREIGN KEY (id_mesa)
        REFERENCES mesa (id_mesa),
    CONSTRAINT fk_comanda_usuario
        FOREIGN KEY (id_usuario)
        REFERENCES usuario (id_usuario)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;

-- =========================================================
-- ITEM_COMANDA
-- =========================================================
CREATE TABLE item_comanda (
    id_item_comanda INT NOT NULL AUTO_INCREMENT,
    id_comanda INT NOT NULL,
    id_produto INT NOT NULL,
    quantidade INT NOT NULL,
    preco_unitario DECIMAL(10,2) NOT NULL,
    observacao VARCHAR(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    subtotal DECIMAL(10,2) NOT NULL,
    status_item ENUM('PENDENTE','EM_PREPARO','PRONTO','ENTREGUE','CANCELADO') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'PENDENTE',
    PRIMARY KEY (id_item_comanda),
    KEY fk_item_comanda_comanda (id_comanda),
    KEY fk_item_comanda_produto (id_produto),
    CONSTRAINT fk_item_comanda_comanda
        FOREIGN KEY (id_comanda)
        REFERENCES comanda (id_comanda),
    CONSTRAINT fk_item_comanda_produto
        FOREIGN KEY (id_produto)
        REFERENCES produto (id_produto)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;

-- =========================================================
-- PAGAMENTO
-- =========================================================
CREATE TABLE pagamento (
    id_pagamento INT NOT NULL AUTO_INCREMENT,
    id_comanda INT NOT NULL,
    forma_pagamento ENUM('DINHEIRO','PIX','DEBITO','CREDITO') COLLATE utf8mb4_unicode_ci NOT NULL,
    valor DECIMAL(10,2) NOT NULL,
    data_hora DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id_pagamento),
    KEY fk_pagamento_comanda (id_comanda),
    CONSTRAINT fk_pagamento_comanda
        FOREIGN KEY (id_comanda)
        REFERENCES comanda (id_comanda)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;

-- =========================================================
-- MOVIMENTACAO_CAIXA
-- =========================================================
CREATE TABLE movimentacao_caixa (
    id_movimentacao INT NOT NULL AUTO_INCREMENT,
    id_caixa INT NOT NULL,
    id_pagamento INT DEFAULT NULL,
    tipo ENUM('ENTRADA','SAIDA') COLLATE utf8mb4_unicode_ci NOT NULL,
    descricao VARCHAR(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    valor DECIMAL(10,2) NOT NULL,
    data_hora DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id_movimentacao),
    KEY fk_movimentacao_caixa (id_caixa),
    KEY fk_movimentacao_pagamento (id_pagamento),
    CONSTRAINT fk_movimentacao_caixa
        FOREIGN KEY (id_caixa)
        REFERENCES caixa (id_caixa),
    CONSTRAINT fk_movimentacao_pagamento
        FOREIGN KEY (id_pagamento)
        REFERENCES pagamento (id_pagamento)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;
