ALTER TABLE produto
    ADD COLUMN local_preparo
        ENUM('COZINHA', 'BALCAO')
NOT NULL DEFAULT 'COZINHA'
AFTER quantidade_estoque;