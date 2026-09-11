USE trevizan_espetinhos;

-- Usuário administrador
INSERT INTO usuario (nome, login, senha)
VALUES ('Administrador', 'admin', 'admin123');

-- Mesas
INSERT INTO mesa (numero, status)
SELECT n, 'LIVRE'
FROM (
         SELECT 1 n UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL
         SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL
         SELECT 9 UNION ALL SELECT 10
     ) x
WHERE NOT EXISTS (SELECT 1 FROM mesa m WHERE m.numero = x.n);

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