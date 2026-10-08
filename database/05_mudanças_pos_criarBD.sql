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