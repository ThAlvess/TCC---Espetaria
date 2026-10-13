ALTER TABLE produto
    ADD COLUMN local_preparo
        ENUM('COZINHA', 'BALCAO')
NOT NULL DEFAULT 'COZINHA'
AFTER quantidade_estoque;

ALTER TABLE item_comanda
    ADD COLUMN data_envio_cozinha DATETIME
        DEFAULT CURRENT_TIMESTAMP
    AFTER status_item;

SELECT
    NOW() AS horario_mysql,
    @@session.time_zone AS fuso_sessao,
    @@system_time_zone AS fuso_sistema;


ALTER TABLE caixa
    ADD COLUMN valor_contado DECIMAL(10,2) NULL,
    ADD COLUMN diferenca_caixa DECIMAL(10,2) NULL;

ALTER TABLE usuario
    ADD COLUMN troca_senha_obrigatoria BOOLEAN NOT NULL DEFAULT FALSE;

UPDATE usuario
SET troca_senha_obrigatoria = TRUE
WHERE login = 'nome do login no bd';