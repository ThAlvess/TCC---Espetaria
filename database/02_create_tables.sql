CREATE TABLE IF NOT EXISTS usuario (
                                       id_usuario INT NOT NULL AUTO_INCREMENT,
                                       nome VARCHAR(100) NOT NULL,
    login VARCHAR(50) NOT NULL,
    senha VARCHAR(255) NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    cpf VARCHAR(20) DEFAULT NULL,
    perfil VARCHAR(50) DEFAULT NULL,
    PRIMARY KEY (id_usuario),
    UNIQUE KEY uk_usuario_login (login)
    ) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS mesa (
                                    id_mesa INT NOT NULL AUTO_INCREMENT,
                                    numero INT NOT NULL,
                                    status ENUM('LIVRE','OCUPADA') NOT NULL DEFAULT 'LIVRE',
    PRIMARY KEY (id_mesa),
    UNIQUE KEY uk_mesa_numero (numero)
    ) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS categoria (
                                         id_categoria INT NOT NULL AUTO_INCREMENT,
                                         nome VARCHAR(50) NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    PRIMARY KEY (id_categoria),
    UNIQUE KEY uk_categoria_nome (nome)
    ) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS produto (
                                       id_produto INT NOT NULL AUTO_INCREMENT,
                                       id_categoria INT NOT NULL,
                                       nome VARCHAR(100) NOT NULL,
    descricao VARCHAR(255) DEFAULT NULL,
    preco DECIMAL(10,2) NOT NULL,
    quantidade_estoque INT NOT NULL DEFAULT 0,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    PRIMARY KEY (id_produto),
    CONSTRAINT fk_produto_categoria
    FOREIGN KEY (id_categoria) REFERENCES categoria(id_categoria)
    ) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS caixa (
                                     id_caixa INT NOT NULL AUTO_INCREMENT,
                                     id_usuario_abertura INT NOT NULL,
                                     data_hora_abertura DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                     data_hora_fechamento DATETIME DEFAULT NULL,
                                     valor_inicial DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    valor_final DECIMAL(10,2) DEFAULT NULL,
    status ENUM('ABERTO','FECHADO') NOT NULL DEFAULT 'ABERTO',
    PRIMARY KEY (id_caixa),
    CONSTRAINT fk_caixa_usuario_abertura
    FOREIGN KEY (id_usuario_abertura) REFERENCES usuario(id_usuario)
    ) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS comanda (
                                       id_comanda INT NOT NULL AUTO_INCREMENT,
                                       id_mesa INT NOT NULL,
                                       id_usuario INT NOT NULL,
                                       nome_cliente VARCHAR(100) DEFAULT NULL,
    data_abertura DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_fechamento DATETIME DEFAULT NULL,
    status ENUM('ABERTA','FECHADA','CANCELADA') NOT NULL DEFAULT 'ABERTA',
    valor_total DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    PRIMARY KEY (id_comanda),
    CONSTRAINT fk_comanda_mesa
    FOREIGN KEY (id_mesa) REFERENCES mesa(id_mesa),
    CONSTRAINT fk_comanda_usuario
    FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario)
    ) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS item_comanda (
                                            id_item_comanda INT NOT NULL AUTO_INCREMENT,
                                            id_comanda INT NOT NULL,
                                            id_produto INT NOT NULL,
                                            quantidade INT NOT NULL,
                                            preco_unitario DECIMAL(10,2) NOT NULL,
    observacao VARCHAR(255) DEFAULT NULL,
    subtotal DECIMAL(10,2) NOT NULL,
    status_item ENUM('PENDENTE','EM_PREPARO','PRONTO','ENTREGUE','CANCELADO')
    NOT NULL DEFAULT 'PENDENTE',
    PRIMARY KEY (id_item_comanda),
    CONSTRAINT fk_item_comanda_comanda
    FOREIGN KEY (id_comanda) REFERENCES comanda(id_comanda),
    CONSTRAINT fk_item_comanda_produto
    FOREIGN KEY (id_produto) REFERENCES produto(id_produto)
    ) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS pagamento (
                                         id_pagamento INT NOT NULL AUTO_INCREMENT,
                                         id_comanda INT NOT NULL,
                                         forma_pagamento ENUM('DINHEIRO','PIX','DEBITO','CREDITO') NOT NULL,
    valor DECIMAL(10,2) NOT NULL,
    data_hora DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id_pagamento),
    CONSTRAINT fk_pagamento_comanda
    FOREIGN KEY (id_comanda) REFERENCES comanda(id_comanda)
    ) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS movimentacao_caixa (
                                                  id_movimentacao INT NOT NULL AUTO_INCREMENT,
                                                  id_caixa INT NOT NULL,
                                                  id_pagamento INT DEFAULT NULL,
                                                  tipo ENUM('ENTRADA','SAIDA') NOT NULL,
    descricao VARCHAR(255) DEFAULT NULL,
    valor DECIMAL(10,2) NOT NULL,
    data_hora DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id_movimentacao),
    CONSTRAINT fk_movimentacao_caixa
    FOREIGN KEY (id_caixa) REFERENCES caixa(id_caixa),
    CONSTRAINT fk_movimentacao_pagamento
    FOREIGN KEY (id_pagamento) REFERENCES pagamento(id_pagamento)
    ) ENGINE=InnoDB;