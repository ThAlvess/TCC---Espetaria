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

-- Produtos
INSERT INTO produto
(nome, descricao, categoria, preco, quantidade_estoque)
VALUES
    ('Espeto de Carne', 'Espeto bovino', 'ESPETO', 10.00, 50),
    ('Espeto de Frango', 'Espeto de frango', 'ESPETO', 9.00, 50),
    ('Espeto de Linguiça', 'Espeto de linguiça', 'ESPETO', 9.50, 50),
    ('Batata Frita', 'Porção de batata frita', 'PORCAO', 28.00, 20),
    ('Refrigerante Lata', '350 ml', 'BEBIDA', 6.00, 100),
    ('Água', '500 ml', 'BEBIDA', 4.00, 100);