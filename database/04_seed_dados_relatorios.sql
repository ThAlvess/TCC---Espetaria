USE trevizan_espetinhos;

-- =====================================================================
-- Dados fictícios para a tela de Relatórios
-- =====================================================================
-- Gera comandas, itens de comanda e pagamentos de forma aleatória,
-- usando as mesas, usuários e produtos JÁ CADASTRADOS no banco
-- (não insere nada em categoria/produto/usuario/mesa).
--
-- Cobre 4 janelas de tempo para os filtros da tela (Diário, Mensal,
-- Anual, Fiscal): hoje, último mês, último ano e o ano anterior.
--
-- Pré-requisitos: ter pelo menos 1 usuário ativo, 1 mesa e alguns
-- produtos ativos cadastrados (já é o caso, conforme 03_insert_initial_data.sql).
--
-- Como rodar: execute este arquivo inteiro no MySQL Workbench
-- (Ctrl+Shift+Enter para "Execute SQL Script").
-- =====================================================================

DELIMITER $$

DROP PROCEDURE IF EXISTS gerar_dados_relatorio $$
CREATE PROCEDURE gerar_dados_relatorio(
    IN qtd_comandas INT,
    IN dias_min INT,
    IN dias_max INT
)
BEGIN
    DECLARE i INT DEFAULT 0;
    DECLARE j INT;
    DECLARE v_id_comanda INT;
    DECLARE v_id_mesa INT;
    DECLARE v_id_usuario INT;
    DECLARE v_dia INT;
    DECLARE v_data_abertura DATETIME;
    DECLARE v_data_fechamento DATETIME;
    DECLARE v_status VARCHAR(20);
    DECLARE v_valor_total DECIMAL(10,2);
    DECLARE v_qtd_itens INT;
    DECLARE v_id_produto INT;
    DECLARE v_preco DECIMAL(10,2);
    DECLARE v_quantidade INT;
    DECLARE v_subtotal DECIMAL(10,2);
    DECLARE v_status_item VARCHAR(20);
    DECLARE v_forma VARCHAR(20);
    DECLARE v_rand DOUBLE;

    WHILE i < qtd_comandas DO

        -- mesa e usuário aleatórios entre os já existentes
        SELECT id_mesa INTO v_id_mesa FROM mesa ORDER BY RAND() LIMIT 1;
        SELECT id_usuario INTO v_id_usuario FROM usuario WHERE ativo = TRUE ORDER BY RAND() LIMIT 1;

        -- data aleatória dentro da janela [dias_min, dias_max] dias atrás,
        -- em horário de funcionamento (11:00 às 23:30)
        SET v_dia = dias_min + FLOOR(RAND() * (dias_max - dias_min + 1));
        SET v_data_abertura = DATE(DATE_SUB(CURDATE(), INTERVAL v_dia DAY))
            + INTERVAL (11 * 60 + FLOOR(RAND() * 750)) MINUTE;
        SET v_data_fechamento = v_data_abertura + INTERVAL (15 + FLOOR(RAND() * 90)) MINUTE;

        -- 90% fechadas, 7% canceladas, 3% ainda abertas
        SET v_rand = RAND();
        SET v_status = CASE
            WHEN v_rand < 0.90 THEN 'FECHADA'
            WHEN v_rand < 0.97 THEN 'CANCELADA'
            ELSE 'ABERTA'
        END;

        INSERT INTO comanda (id_mesa, id_usuario, nome_cliente, data_abertura, data_fechamento, status, valor_total)
        VALUES (
            v_id_mesa,
            v_id_usuario,
            NULL,
            v_data_abertura,
            IF(v_status = 'ABERTA', NULL, v_data_fechamento),
            v_status,
            0
        );
        SET v_id_comanda = LAST_INSERT_ID();

        -- 1 a 4 itens por comanda
        SET v_valor_total = 0;
        SET v_qtd_itens = 1 + FLOOR(RAND() * 4);
        SET j = 0;
        WHILE j < v_qtd_itens DO
            SELECT id_produto, preco INTO v_id_produto, v_preco
            FROM produto WHERE ativo = TRUE ORDER BY RAND() LIMIT 1;

            SET v_quantidade = 1 + FLOOR(RAND() * 3);
            SET v_subtotal = v_preco * v_quantidade;
            SET v_valor_total = v_valor_total + v_subtotal;

            SET v_status_item = CASE
                WHEN v_status = 'CANCELADA' THEN 'CANCELADO'
                WHEN v_status = 'ABERTA' THEN (CASE WHEN RAND() < 0.5 THEN 'PENDENTE' ELSE 'EM_PREPARO' END)
                ELSE 'ENTREGUE'
            END;

            INSERT INTO item_comanda (id_comanda, id_produto, quantidade, preco_unitario, subtotal, status_item)
            VALUES (v_id_comanda, v_id_produto, v_quantidade, v_preco, v_subtotal, v_status_item);

            SET j = j + 1;
        END WHILE;

        UPDATE comanda SET valor_total = v_valor_total WHERE id_comanda = v_id_comanda;

        -- pagamento só é lançado para comandas fechadas
        IF v_status = 'FECHADA' THEN
            SET v_rand = RAND();
            SET v_forma = CASE
                WHEN v_rand < 0.35 THEN 'CREDITO'
                WHEN v_rand < 0.60 THEN 'PIX'
                WHEN v_rand < 0.85 THEN 'DEBITO'
                ELSE 'DINHEIRO'
            END;

            INSERT INTO pagamento (id_comanda, forma_pagamento, valor, data_hora)
            VALUES (v_id_comanda, v_forma, v_valor_total, v_data_fechamento);
        END IF;

        SET i = i + 1;
    END WHILE;
END $$

DELIMITER ;

-- =====================================================================
-- Execução: gera as comandas em 4 janelas de tempo
-- =====================================================================
CALL gerar_dados_relatorio(15,   0,   0);   -- hoje
CALL gerar_dados_relatorio(40,   1,  30);   -- último mês (exceto hoje)
CALL gerar_dados_relatorio(120, 31, 365);   -- último ano
CALL gerar_dados_relatorio(80,  366, 730);  -- ano anterior (histórico/fiscal)

DROP PROCEDURE gerar_dados_relatorio;

-- Conferência rápida
SELECT status, COUNT(*) AS total FROM comanda GROUP BY status;
SELECT forma_pagamento, COUNT(*) AS total, SUM(valor) AS valor_total FROM pagamento GROUP BY forma_pagamento;
