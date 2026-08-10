USE trevizan_espetinhos;

-- Usuário administrador
INSERT INTO usuario (nome, login, senha)
VALUES ('Administrador', 'admin', 'admin123');

-- Mesas
INSERT INTO mesa (numero)
VALUES
    (1),
    (2),
    (3),
    (4),
    (5),
    (6),
    (7),
    (8),
    (9),
    (10);

INSERT INTO categoria (nome)
VALUES
    ('Espeto'),
    ('Lanche'),
    ('Porção'),
    ('Bebida'),
    ('Outro');

-- Produtos
INSERT INTO produto
(id_categoria, nome, descricao, preco, quantidade_estoque)
VALUES
    (1, 'Espeto de Carne', 'Espeto bovino', 10.00, 50),
    (1, 'Espeto de Frango', 'Espeto de frango', 9.00, 50),
    (1, 'Espeto de Linguiça', 'Espeto de linguiça', 9.50, 50),
    (3, 'Batata Frita', 'Porção de batata frita', 28.00, 20),
    (4, 'Refrigerante Lata', 'Refrigerante de 350 ml', 6.00, 100),
    (4, 'Água', 'Garrafa de 500 ml', 4.00, 100);