-- =========================================================
-- ESPETINHOS TREVIZAN - DADOS INICIAIS
-- Arquivo 03/03
-- Dados consolidados a partir dos dumps atuais do projeto.
-- =========================================================

USE trevizan_espetinhos;

-- =========================================================
-- USUARIOS
-- =========================================================
INSERT INTO usuario
    (id_usuario, nome, login, senha, ativo, cpf, perfil)
VALUES
    (1, 'Administrador', 'admin', 'admin123', 'ativo', '49080233870', 'Administrador'),
    (21, 'admin', 'vini', 'admin123', 'ativo', '413.830.238-75', 'Caixa');

-- =========================================================
-- MESAS
-- =========================================================
INSERT INTO mesa
    (id_mesa, numero, status)
VALUES
    (1, 1, 'LIVRE'),
    (2, 2, 'LIVRE'),
    (3, 3, 'LIVRE'),
    (4, 4, 'LIVRE'),
    (5, 5, 'LIVRE'),
    (6, 6, 'LIVRE'),
    (7, 7, 'LIVRE'),
    (8, 8, 'LIVRE'),
    (9, 9, 'LIVRE'),
    (10, 10, 'LIVRE');

-- =========================================================
-- CATEGORIAS
-- =========================================================
INSERT INTO categoria
    (id_categoria, nome, ativo)
VALUES
    (1, 'Espeto', 1),
    (2, 'Lanche', 1),
    (3, 'Porção', 1),
    (4, 'Bebida', 1),
    (5, 'Outro', 1);

-- =========================================================
-- PRODUTOS
-- =========================================================
INSERT INTO produto
    (id_produto, id_categoria, nome, descricao, preco, quantidade_estoque, ativo)
VALUES
    (1, 1, 'Espeto de Carne', 'Espeto bovino', 10.00, 50, 1),
    (2, 1, 'Espeto de Frango', 'Espeto de frango', 9.00, 50, 1),
    (3, 1, 'Espeto de Linguiça', 'Espeto de linguiça', 9.50, 50, 1),
    (4, 3, 'Batata Frita', 'Porção de batata frita', 28.00, 20, 1),
    (5, 4, 'Refrigerante Lata', 'Refrigerante de 350 ml', 6.00, 100, 1),
    (6, 4, 'Água', 'Garrafa de 500 ml', 4.00, 100, 1);
