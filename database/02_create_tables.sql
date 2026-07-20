USE trevizan_espetinhos;

CREATE TABLE usuario (
                         id_usuario INT AUTO_INCREMENT PRIMARY KEY,
                         nome VARCHAR(100) NOT NULL,
                         login VARCHAR(50) NOT NULL UNIQUE,
                         senha VARCHAR(255) NOT NULL,
                         ativo BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE mesa (
                      id_mesa INT AUTO_INCREMENT PRIMARY KEY,
                      numero INT NOT NULL UNIQUE,
                      status ENUM('LIVRE', 'OCUPADA') NOT NULL DEFAULT 'LIVRE'
);

CREATE TABLE produto (
                         id_produto INT AUTO_INCREMENT PRIMARY KEY,
                         id_categoria INT NOT NULL,
                         nome VARCHAR(100) NOT NULL,
                         descricao VARCHAR(255),
                         preco DECIMAL(10,2) NOT NULL,
                         quantidade_estoque INT NOT NULL DEFAULT 0,
                         ativo BOOLEAN NOT NULL DEFAULT TRUE,

                         CONSTRAINT fk_produto_categoria
                             FOREIGN KEY (id_categoria)
                                 REFERENCES categoria(id_categoria)
);

CREATE TABLE comanda (
                         id_comanda INT AUTO_INCREMENT PRIMARY KEY,
                         id_mesa INT NOT NULL,
                         id_usuario INT NOT NULL,
                         nome_cliente VARCHAR(100),
                         data_abertura DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                         data_fechamento DATETIME,
                         status ENUM(
        'ABERTA',
        'FECHADA',
        'CANCELADA'
    ) NOT NULL DEFAULT 'ABERTA',
                         valor_total DECIMAL(10,2) NOT NULL DEFAULT 0,

                         CONSTRAINT fk_comanda_mesa
                             FOREIGN KEY (id_mesa)
                                 REFERENCES mesa(id_mesa),

                         CONSTRAINT fk_comanda_usuario
                             FOREIGN KEY (id_usuario)
                                 REFERENCES usuario(id_usuario)
);

CREATE TABLE item_comanda (
                              id_item_comanda INT AUTO_INCREMENT PRIMARY KEY,
                              id_comanda INT NOT NULL,
                              id_produto INT NOT NULL,
                              quantidade INT NOT NULL,
                              preco_unitario DECIMAL(10,2) NOT NULL,
                              observacao VARCHAR(255),
                              subtotal DECIMAL(10,2) NOT NULL,
                              status_item ENUM(
        'PENDENTE',
        'EM_PREPARO',
        'PRONTO',
        'ENTREGUE',
        'CANCELADO'
    ) NOT NULL DEFAULT 'PENDENTE',

                              CONSTRAINT fk_item_comanda_comanda
                                  FOREIGN KEY (id_comanda)
                                      REFERENCES comanda(id_comanda),

                              CONSTRAINT fk_item_comanda_produto
                                  FOREIGN KEY (id_produto)
                                      REFERENCES produto(id_produto)
);

CREATE TABLE pagamento (
                           id_pagamento INT AUTO_INCREMENT PRIMARY KEY,
                           id_comanda INT NOT NULL,
                           forma_pagamento ENUM(
        'DINHEIRO',
        'PIX',
        'DEBITO',
        'CREDITO'
    ) NOT NULL,
                           valor DECIMAL(10,2) NOT NULL,
                           data_hora DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

                           CONSTRAINT fk_pagamento_comanda
                               FOREIGN KEY (id_comanda)
                                   REFERENCES comanda(id_comanda)
);

CREATE TABLE caixa (
                       id_caixa INT AUTO_INCREMENT PRIMARY KEY,
                       id_usuario_abertura INT NOT NULL,
                       data_hora_abertura DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       data_hora_fechamento DATETIME,
                       valor_inicial DECIMAL(10,2) NOT NULL DEFAULT 0,
                       valor_final DECIMAL(10,2),
                       status ENUM(
        'ABERTO',
        'FECHADO'
    ) NOT NULL DEFAULT 'ABERTO',

                       CONSTRAINT fk_caixa_usuario
                           FOREIGN KEY (id_usuario_abertura)
                               REFERENCES usuario(id_usuario)
);

CREATE TABLE movimentacao_caixa (
                                    id_movimentacao INT AUTO_INCREMENT PRIMARY KEY,
                                    id_caixa INT NOT NULL,
                                    id_pagamento INT,
                                    tipo ENUM(
        'ENTRADA',
        'SAIDA'
    ) NOT NULL,
                                    descricao VARCHAR(255),
                                    valor DECIMAL(10,2) NOT NULL,
                                    data_hora DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                    CONSTRAINT fk_movimentacao_caixa
                                        FOREIGN KEY (id_caixa)
                                            REFERENCES caixa(id_caixa),

                                    CONSTRAINT fk_movimentacao_pagamento
                                        FOREIGN KEY (id_pagamento)
                                            REFERENCES pagamento(id_pagamento)
);

CREATE TABLE categoria (
                           id_categoria INT AUTO_INCREMENT PRIMARY KEY,
                           nome VARCHAR(50) NOT NULL UNIQUE,
                           ativo BOOLEAN NOT NULL DEFAULT TRUE
);