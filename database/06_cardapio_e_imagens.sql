-- =========================================================
-- ESPETINHOS TREVIZAN - CARDÁPIO REAL E FOTOS DOS PRODUTOS
-- Arquivo 06
--
-- Execute DEPOIS dos scripts 01, 02, 03 e 05.
--
-- 1) Cria a coluna "imagem" na tabela produto, onde o sistema
--    guarda a foto de cada produto (JPEG reduzido).
-- 2) Desativa as categorias/produtos genéricos de exemplo do
--    script 03 (não apaga, para não quebrar comandas antigas).
-- 3) Cadastra as categorias e os produtos reais da loja, todos
--    ATIVOS. Se o produto já existir (mesmo id), os dados são
--    atualizados e a foto, se houver, é mantida.
-- 4) Remove a coluna quantidade_estoque (o sistema não controla
--    estoque).
--
-- Pode ser executado mais de uma vez sem erro.
-- =========================================================

USE trevizan_espetinhos;

-- =========================================================
-- 1) FOTO DO PRODUTO (só cria a coluna se ela ainda não existir)
-- =========================================================
SET @existe_imagem := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'produto'
      AND COLUMN_NAME = 'imagem'
);
SET @comando := IF(
    @existe_imagem = 0,
    'ALTER TABLE produto ADD COLUMN imagem MEDIUMBLOB NULL AFTER ativo',
    'SELECT ''coluna imagem já existe'' AS aviso'
);
PREPARE passo FROM @comando;
EXECUTE passo;
DEALLOCATE PREPARE passo;

-- =========================================================
-- 2) DESATIVA OS DADOS GENÉRICOS DE EXEMPLO
-- (o "id > 0" no WHERE atende ao safe update mode do Workbench)
-- =========================================================
UPDATE produto
SET ativo = 0
WHERE id_produto > 0
  AND id_produto <= 10;

UPDATE categoria
SET ativo = 0
WHERE id_categoria > 0
  AND id_categoria <= 5;

-- =========================================================
-- 3) CATEGORIAS DO CARDÁPIO
-- =========================================================
INSERT INTO categoria
    (id_categoria, nome, ativo)
VALUES
    (6,'Espetinhos',1),
    (7,'Porções',1),
    (8,'Lanches na Brasa',1),
    (9,'Sobremesas',1),
    (10,'Molhos',1),
    (11,'Bebidas Alcoólicas',1),
    (12,'Bebidas Não Alcoólicas',1)
AS novo
ON DUPLICATE KEY UPDATE
    nome  = novo.nome,
    ativo = novo.ativo;

-- =========================================================
-- 4) PRODUTOS DO CARDÁPIO (todos ativos)
-- =========================================================
INSERT INTO produto
    (id_produto, id_categoria, nome, descricao, preco, local_preparo, ativo)
VALUES
    (11,6,'Coraçãozinho','Espeto de coraçãozinho',12.00,'COZINHA',1),
    (12,6,'Camarão com casca','Espetinho de camarão com casca',12.00,'COZINHA',1),
    (13,6,'Carne Angus','1 unidade',12.00,'COZINHA',1),
    (14,6,'Carneiro','Temperado no vinho e hortelã',15.00,'COZINHA',1),
    (15,6,'Choripan','Pão, linguiça toscana e queijo',12.00,'COZINHA',1),
    (16,6,'Contra Filé Black Angus',NULL,15.00,'COZINHA',1),
    (17,6,'Costela de Boi Angus','Costela de Boi Angus',15.00,'COZINHA',1),
    (18,6,'Espetinho de Cupim Angus','Espetinho de Cupim Angus',15.00,'COZINHA',1),
    (19,6,'Espetióca Queijo','Massa de tapioca com queijo',12.00,'COZINHA',1),
    (20,6,'Espeto de Tilápia','Espetinho de filé de tilápia',12.00,'COZINHA',1),
    (21,6,'Espeto Romeu e Julieta (Queijo Coalho e Goiabada)','Queijo Coalho e Goiabada',12.00,'COZINHA',1),
    (22,6,'Filé de Frango','Espetinho de filé de frango',12.00,'COZINHA',1),
    (23,6,'Jacaré','Espetinho de carne de jacaré',20.00,'COZINHA',1),
    (24,6,'Kafta','1 unidade',12.00,'COZINHA',1),
    (25,6,'Kafta com Pimenta Biquinho','Kafta caseira com Pimenta Biquinho',12.00,'COZINHA',1),
    (26,6,'Kafta com Queijo','1 unidade',12.00,'COZINHA',1),
    (27,6,'Linguiça Cuiabana','Espetinho de linguiça cuiabana bovina recheada com queijo',12.00,'COZINHA',1),
    (28,6,'Linguiça de Cordeiro','Linguiça de cordeiro temperada com ervas finas e com vinho',12.00,'COZINHA',1),
    (29,6,'Linguiça de Costela Angus','Espetinho de Linguiça de Costela Angus',12.00,'COZINHA',1),
    (30,6,'Linguiça de Pernil Fina','Linguiça de Pernil Fina',12.00,'COZINHA',1),
    (31,6,'Linguiça Pura Apimentada','Linguiça de Carne Suína com Pimenta',12.00,'COZINHA',1),
    (32,6,'Linguiça Pura Caseira','Linguiça de Carne Suína',12.00,'COZINHA',1),
    (33,6,'Linguiça Toscana','Linguiça Toscana',12.00,'COZINHA',1),
    (34,6,'Mandioca com Abobrinha','Opção vegetariana do medalhão de mandioca. Descrição parcial no cardápio',12.00,'COZINHA',1),
    (35,6,'Medalhão de Batata','Medalhão de batata',12.00,'COZINHA',1),
    (36,6,'Medalhão de Filé Mignon','Espetinho de Medalhão de Filé Mignon',15.00,'COZINHA',1),
    (37,6,'Medalhão de Frango','Espetinho de medalhão de frango',12.00,'COZINHA',1),
    (38,6,'Medalhão de Mandioca','Massa de mandioca com queijo envolvido no bacon',12.00,'COZINHA',1),
    (39,6,'Medalhão de Mandioca com Costela','Medalhão de Mandioca com Costela desfiada',12.00,'COZINHA',1),
    (40,6,'Medalhão de Queijo Coalho','Espetinho de Medalhão de Queijo Coalho',12.00,'COZINHA',1),
    (41,6,'Medalhão de Quiabo','Espetinho de medalhão de quiabo',12.00,'COZINHA',1),
    (42,6,'Misto','Carne Angus, Frango, Calabresa e Pimentão',12.00,'COZINHA',1),
    (43,6,'Panceta','Espetinho de Panceta',12.00,'COZINHA',1),
    (44,6,'Pão de Alho','Espetinho de Pão de Alho',10.00,'COZINHA',1),
    (45,6,'Picanha',NULL,15.00,'COZINHA',1),
    (46,6,'Queijo Coalho','Queijo Coalho',10.00,'COZINHA',1),
    (47,6,'Queijo Provolone','Espetinho de queijo provolone',10.00,'COZINHA',1),
    (48,6,'Salmão','Espetinho de Salmão',15.00,'COZINHA',1),
    (49,6,'Salsichão com Provolone','Espetinho Salsichão com Provolone',12.00,'COZINHA',1),
    (50,6,'Tulipinha','3 tulipas de frango',12.00,'COZINHA',1),
    (51,7,'Vulcão de Mandioca com Queijo','Aproximadamente 20 unidades',35.00,'COZINHA',1),
    (52,7,'Vulcão de Mandioca com Costela','Aproximadamente 400g',35.00,'COZINHA',1),
    (53,7,'Polenta com Calabresa','Com queijo na massa - 400g',30.00,'COZINHA',1),
    (54,7,'Nuggets','Aproximadamente 15 unidades',30.00,'COZINHA',1),
    (55,7,'Mini Bolinha de Queijo','400g',30.00,'COZINHA',1),
    (56,7,'Dadinho de Tapioca','400g; aproximadamente 18 unidades; acompanha geleia de pimenta',30.00,'COZINHA',1),
    (57,7,'Mini Bolinho de Tilápia','400g',30.00,'COZINHA',1),
    (58,7,'Mini Coxinha de Frango com Catupiry® Original','400g',30.00,'COZINHA',1),
    (59,7,'Mini Croquete','400g',30.00,'COZINHA',1),
    (60,7,'Mini Pastel de Camarão','6 unidades',30.00,'COZINHA',1),
    (61,7,'Mini Pastel Misto','6 unidades: 2 carne, 2 frango e 2 pizza',30.00,'COZINHA',1),
    (62,7,'Mini Kibe com Catupiry® Original','20 unidades; recheado com Catupiry® Original',30.00,'COZINHA',1),
    (63,7,'Mandioca Frita','400g',30.00,'COZINHA',1),
    (64,7,'Isca de Frango','500g; acompanha molho tártaro',40.00,'COZINHA',1),
    (65,7,'Filé de Tilápia','400g; acompanha molho tártaro',55.00,'COZINHA',1),
    (66,7,'Coxinha de Frango com Catupiry (BOTECO)','5 unidades',30.00,'COZINHA',1),
    (67,7,'Coxinha de Jaca','10 unidades',30.00,'COZINHA',1),
    (68,7,'Cebola Empanada','400g',30.00,'COZINHA',1),
    (69,7,'Camarão Empanado com Queijo','10 unidades; acompanha molho tártaro',55.00,'COZINHA',1),
    (70,7,'Bolinho de Bacalhau','12 unidades',40.00,'COZINHA',1),
    (71,7,'Bolinho de Costela com Gorgonzola','Porção de bolinho de costela com gorgonzola',40.00,'COZINHA',1),
    (72,7,'Batata Frita','400g; porções vendidas apenas inteiras',25.00,'COZINHA',1),
    (73,7,'Disquinhos de Catupiry® Original Empanado','12 unidades',30.00,'COZINHA',1),
    (74,7,'Bolinho de Carne Seca Picante','7 unidades',30.00,'COZINHA',1),
    (75,7,'Bolinho de Pernil','10 unidades',30.00,'COZINHA',1),
    (76,7,'Bolinha de Provolone Empanado','300g',30.00,'COZINHA',1),
    (77,7,'Batata individual','Porção de batata de 150g',15.00,'COZINHA',1),
    (78,7,'Disquinhos de Catupiry® Original Empanado Individual','6 unidades',15.00,'COZINHA',1),
    (79,7,'Nuggets recheado com Queijo Individual','6 unidades',15.00,'COZINHA',1),
    (80,8,'Trevizan Pernil','Pão brioche macio, hambúrguer de pernil assado na brasa, queijo mussarela derretido, rúcula e tomate frescos; descrição adicional cortada',42.00,'COZINHA',1),
    (81,8,'Burger do Montanha ?','Pão brioche, burger de picanha 160g na brasa, salada, ovo; descrição adicional cortada',56.00,'COZINHA',1),
    (82,8,'Big Trev','Pão brioche, dois burgers de picanha Angus 160g cada na brasa, queijo à sua escolha; descrição adicional cortada',55.00,'COZINHA',1),
    (83,8,'Trevizan Catupirado','Pão brioche, baconese, burger de picanha na brasa, queijo à sua escolha e disco de catupiry original empanado',47.00,'COZINHA',1),
    (84,8,'Trevizan Rodeio','Pão brioche, molho barbecue, burger de Picanha Angus 160g feito na brasa, queijo à sua escolha e anéis de cebola',42.00,'COZINHA',1),
    (85,8,'Trevizan Salada Bacon','Pão brioche, baconese, burger de picanha na brasa, queijo à escolha, alface americana, tomate e bacon',49.00,'COZINHA',1),
    (86,8,'Churrasquinho Filé mignon Com Salada','Pão parmesão, bife de filé mignon na brasa, muçarela fatiada, alface americana, tomate e maionese grill',42.00,'COZINHA',1),
    (87,8,'Churrasquinho Filé mignon 2.0 salada','Pão parmesão, 2 bifes de filé mignon na brasa, muçarela fatiada, alface americana, tomate e maionese grill',54.00,'COZINHA',1),
    (88,8,'Trevizan Burger','Pão brioche, baconese, burger de picanha 160g na brasa e queijo à sua escolha',35.00,'COZINHA',1),
    (89,8,'Trevizan Muçarelado','Pão brioche, baconese, burger de picanha na brasa, queijo à sua escolha e disco de queijo muçarela empanado',47.00,'COZINHA',1),
    (90,8,'Trevizan Salada','Pão brioche, baconese, burger de picanha 160g na brasa, queijo à sua escolha, alface americana e tomate',42.00,'COZINHA',1),
    (91,8,'Trevizan Bacon','Pão brioche, baconese, burger de picanha na brasa, queijo à sua escolha e bacon',42.00,'COZINHA',1),
    (92,8,'Trevizan Salada 2.0','Pão brioche, baconese, dois burgers de picanha 160g na brasa, queijo à sua escolha, alface americana e tomate',54.00,'COZINHA',1),
    (93,8,'Trevizan Bacon 2.0','Pão brioche, baconese, dois burgers de picanha 160g na brasa, queijo à sua escolha e bacon',54.00,'COZINHA',1),
    (94,8,'Trevizan Cordeiro','Pão brioche, maionese de ervas, burger de cordeiro 150g na brasa, queijo à sua escolha',35.00,'COZINHA',1),
    (95,8,'Trevizan Cordeiro Salada','Pão brioche, maionese de ervas, burger de cordeiro na brasa, queijo à sua escolha, alface americana e tomate',42.00,'COZINHA',1),
    (96,8,'Caipira Furioso','Pão brioche, baconese, burger de linguiça pura apimentada 150g, queijo muçarela, alface americana; descrição adicional cortada',35.00,'COZINHA',1),
    (97,8,'Trevizan Vegetariano','Pão brioche, disco de catupiry ou muçarela empanado, queijo; descrição adicional cortada',35.00,'COZINHA',1),
    (98,8,'Chicken Salad','Filé de frango empanado na farinha panko, pão parmesão, baconese, muçarela fatiada e salada; descrição adicional cortada',42.00,'COZINHA',1),
    (99,9,'Brownie de Nozes com Sorvete de Creme','Brownie de nozes acompanhado de uma bola de sorvete de creme e calda de chocolate',20.00,'COZINHA',1),
    (100,9,'Cookie c/ Nutella','Cookie c/ Nutella',15.00,'COZINHA',1),
    (101,9,'Mini Churros com Doce de Leite','400g; aproximadamente 20 unidades',30.00,'COZINHA',1),
    (102,9,'Mini Pudim de Leite Condensado',NULL,10.00,'COZINHA',1),
    (103,9,'Petit Gâteau','Bolinho com recheio de chocolate com sorvete de creme',20.00,'COZINHA',1),
    (104,10,'Pote - Geleia de Pimenta 75mL','Pote de 75mL',7.00,'COZINHA',1),
    (105,10,'Pote - Baconese 75mL','Pote de 75mL',7.00,'COZINHA',1),
    (106,10,'Pote - Molho Barbecue 75mL','Pote de 75mL',7.00,'COZINHA',1),
    (107,10,'Pote - Molho de Alho 75mL','Pote de 75mL',7.00,'COZINHA',1),
    (108,10,'Pote - Molho Mostarda e Mel 75mL','Pote de 75mL',7.00,'COZINHA',1),
    (109,10,'Pote - Cheddar Cremoso 75mL','Pote de 75mL',7.00,'COZINHA',1),
    (110,10,'Pote - Molho Ervas Finas 75mL','Pote de 75mL',7.00,'COZINHA',1),
    (111,10,'Pote - Molho de Pimenta 75mL','Pote de 75mL',7.00,'COZINHA',1),
    (112,10,'Pote - Molho Tártaro 75mL','Pote de 75mL',7.00,'COZINHA',1),
    (113,10,'Pote - Maionese Grill 75mL','Pote de 75mL',7.00,'COZINHA',1),
    (114,10,'Pote - Molho Chipotle 75mL','Pote de 75mL',7.00,'COZINHA',1),
    (115,11,'Stella Pure Gold 600 ml (É NECESSÁRIO VASILHAME)',NULL,16.00,'BALCAO',1),
    (116,11,'Heineken 600ml (É NECESSÁRIO VASILHAME)',NULL,16.00,'BALCAO',1),
    (117,11,'Brahma Lata','350mL',6.00,'BALCAO',1),
    (118,11,'Império Lata','350mL',6.00,'BALCAO',1),
    (119,11,'Skol Lata','350mL',6.00,'BALCAO',1),
    (120,11,'Amstel Lata','350mL',6.00,'BALCAO',1),
    (121,11,'Original Lata','350mL',6.00,'BALCAO',1),
    (122,11,'Heineken Lata','350mL',8.00,'BALCAO',1),
    (123,11,'Michelob Ultra Long Neck 330ml',NULL,10.00,'BALCAO',1),
    (124,11,'Stella Pure Gold Long Neck 330ml',NULL,10.00,'BALCAO',1),
    (125,11,'Budweiser Long Neck',NULL,10.00,'BALCAO',1),
    (126,11,'Corona Long Neck',NULL,10.00,'BALCAO',1),
    (127,11,'Heineken Long Neck 330ml',NULL,10.00,'BALCAO',1),
    (128,11,'Skol Beats Long Neck',NULL,12.00,'BALCAO',1),
    (129,11,'Gin Tônica Long Neck','313mL',12.00,'BALCAO',1),
    (130,11,'Skol Beats Red Mix - 269ml',NULL,12.00,'BALCAO',1),
    (131,11,'Original 600mL (É NECESSÁRIO VASILHAME)',NULL,14.00,'BALCAO',1),
    (132,11,'Chopp Vinho Draft 600mL',NULL,18.00,'BALCAO',1),
    (133,11,'Stella Artois Long Neck - 330ml',NULL,10.00,'BALCAO',1),
    (134,11,'Amstel 600ml (É NECESSÁRIO VASILHAME)',NULL,10.00,'BALCAO',1),
    (135,11,'Baden Baden Ipa (Maracujá) 600ml',NULL,20.00,'BALCAO',1),
    (136,11,'BadenBaden Peach 600ml (Pêssego)',NULL,20.00,'BALCAO',1),
    (137,11,'Baden Baden 600ml Crystal',NULL,20.00,'BALCAO',1),
    (138,11,'Cerveja Praya 600ml (Trigo com raspas de Limão Siciliano)',NULL,16.00,'BALCAO',1),
    (139,11,'Ice Smirnoff - 275ml',NULL,12.00,'BALCAO',1),
    (140,11,'Antarctica 300ml (É NECESSÁRIO VASILHAME)',NULL,5.00,'BALCAO',1),
    (141,11,'Brahma 300ml (É NECESSÁRIO VASILHAME)',NULL,5.00,'BALCAO',1),
    (142,11,'Brahma Malzbier 330ml',NULL,10.00,'BALCAO',1),
    (143,12,'Refrigerante lata 350ML',NULL,7.00,'BALCAO',1),
    (144,12,'Refrigerantes 600ml',NULL,10.00,'BALCAO',1),
    (145,12,'Refrigerante 2L','Refrigerante 2 litros',17.00,'BALCAO',1),
    (146,12,'Energético Monster Lata','473mL',15.00,'BALCAO',1),
    (147,12,'Del Valle lata','290mL',7.00,'BALCAO',1),
    (148,12,'Suco Del Valle Caixa - 1L',NULL,15.00,'BALCAO',1),
    (149,12,'Gatorade','500mL',7.00,'BALCAO',1),
    (150,12,'RedBull - 355ml','RedBull - 355ml',15.00,'BALCAO',1)
AS novo
ON DUPLICATE KEY UPDATE
    id_categoria       = novo.id_categoria,
    nome               = novo.nome,
    descricao          = novo.descricao,
    preco              = novo.preco,
    local_preparo      = novo.local_preparo,
    ativo              = novo.ativo;

-- =========================================================
-- 5) REMOVE O ESTOQUE (só se a coluna ainda existir)
-- =========================================================
SET @existe_estoque := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'produto'
      AND COLUMN_NAME = 'quantidade_estoque'
);
SET @comando := IF(
    @existe_estoque = 1,
    'ALTER TABLE produto DROP COLUMN quantidade_estoque',
    'SELECT ''coluna quantidade_estoque já foi removida'' AS aviso'
);
PREPARE passo FROM @comando;
EXECUTE passo;
DEALLOCATE PREPARE passo;
