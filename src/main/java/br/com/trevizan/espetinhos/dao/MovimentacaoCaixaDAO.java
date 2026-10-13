package br.com.trevizan.espetinhos.dao;

import br.com.trevizan.espetinhos.connection.ConnectionFactory;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class MovimentacaoCaixaDAO {

    public void registrarEntradaPagamento(
            int idCaixa,
            int idPagamento,
            int idComanda,
            BigDecimal valor
    ) {

        String sql = """
                INSERT INTO movimentacao_caixa
                    (id_caixa, id_pagamento, tipo, descricao, valor)
                VALUES
                    (?, ?, 'ENTRADA', ?, ?)
                """;

        try (
                Connection connection = ConnectionFactory.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setInt(1, idCaixa);
            statement.setInt(2, idPagamento);

            statement.setString(
                    3,
                    "Pagamento da comanda #" + idComanda
            );

            statement.setBigDecimal(4, valor);

            statement.executeUpdate();

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Erro ao registrar entrada no caixa.",
                    e
            );
        }
    }

    public void registrarEntradaPagamento(
            Connection connection,
            int idCaixa,
            int idPagamento,
            int idComanda,
            BigDecimal valor
    ) throws SQLException {

        String sql = """
            INSERT INTO movimentacao_caixa
                (id_caixa, id_pagamento, tipo, descricao, valor)
            VALUES
                (?, ?, 'ENTRADA', ?, ?)
            """;

        try (
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setInt(1, idCaixa);
            statement.setInt(2, idPagamento);

            statement.setString(
                    3,
                    "Pagamento da comanda #" + idComanda
            );

            statement.setBigDecimal(4, valor);

            statement.executeUpdate();
        }
    }


    public void registrarSaida(
            int idCaixa,
            String descricao,
            BigDecimal valor
    ) {
        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "O valor da sangria deve ser maior que zero."
            );
        }

        String sqlCaixa = """
        SELECT valor_inicial, status
        FROM caixa
        WHERE id_caixa = ?
        FOR UPDATE
        """;

        String sqlDinheiro = """
        SELECT COALESCE(SUM(mc.valor), 0) AS total
        FROM movimentacao_caixa mc
        INNER JOIN pagamento p
            ON p.id_pagamento = mc.id_pagamento
        WHERE mc.id_caixa = ?
          AND mc.tipo = 'ENTRADA'
          AND UPPER(TRIM(p.forma_pagamento)) = 'DINHEIRO'
        """;

        String sqlSangrias = """
        SELECT COALESCE(SUM(valor), 0) AS total
        FROM movimentacao_caixa
        WHERE id_caixa = ?
          AND tipo = 'SAIDA'
        """;

        String sqlInserir = """
        INSERT INTO movimentacao_caixa
            (id_caixa, id_pagamento, tipo, descricao, valor)
        VALUES (?, NULL, 'SAIDA', ?, ?)
        """;

        try (Connection connection = ConnectionFactory.getConnection()) {

            connection.setAutoCommit(false);

            try {
                BigDecimal valorInicial;
                String status;

                // Bloqueia o registro do caixa durante a operação.
                try (PreparedStatement ps =
                             connection.prepareStatement(sqlCaixa)) {

                    ps.setInt(1, idCaixa);

                    try (java.sql.ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new IllegalStateException(
                                    "Caixa não encontrado."
                            );
                        }

                        valorInicial = rs.getBigDecimal("valor_inicial");
                        status = rs.getString("status");
                    }
                }

                if (!"ABERTO".equalsIgnoreCase(status)) {
                    throw new IllegalStateException(
                            "Não é possível registrar sangria em caixa fechado."
                    );
                }

                BigDecimal dinheiroRecebido = BigDecimal.ZERO;

                try (PreparedStatement ps =
                             connection.prepareStatement(sqlDinheiro)) {

                    ps.setInt(1, idCaixa);

                    try (java.sql.ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            dinheiroRecebido = rs.getBigDecimal("total");
                        }
                    }
                }

                BigDecimal sangriasAnteriores = BigDecimal.ZERO;

                try (PreparedStatement ps =
                             connection.prepareStatement(sqlSangrias)) {

                    ps.setInt(1, idCaixa);

                    try (java.sql.ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            sangriasAnteriores = rs.getBigDecimal("total");
                        }
                    }
                }

                BigDecimal saldoDisponivel = valorInicial
                        .add(dinheiroRecebido)
                        .subtract(sangriasAnteriores);

                if (valor.compareTo(saldoDisponivel) > 0) {
                    throw new IllegalArgumentException(
                            "Saldo insuficiente para sangria. Disponível: R$ "
                                    + saldoDisponivel.toPlainString()
                    );
                }

                try (PreparedStatement ps =
                             connection.prepareStatement(sqlInserir)) {

                    ps.setInt(1, idCaixa);
                    ps.setString(2, descricao);
                    ps.setBigDecimal(3, valor);
                    ps.executeUpdate();
                }

                connection.commit();

            } catch (SQLException | RuntimeException e) {
                connection.rollback();
                throw e;
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erro ao registrar sangria do caixa.",
                    e
            );
        }
    }


    public java.util.List<MovimentacaoResumo> listarPorCaixa(
            int idCaixa
    ) {

        String sql = """
            SELECT
                id_movimentacao,
                tipo,
                descricao,
                valor,
                data_hora
            FROM movimentacao_caixa
            WHERE id_caixa = ?
            ORDER BY data_hora ASC, id_movimentacao ASC
            """;

        java.util.List<MovimentacaoResumo> lista =
                new java.util.ArrayList<>();

        try (
                Connection connection =
                        ConnectionFactory.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setInt(1, idCaixa);

            try (java.sql.ResultSet resultSet =
                         statement.executeQuery()) {

                while (resultSet.next()) {

                    MovimentacaoResumo movimentacao =
                            new MovimentacaoResumo();

                    movimentacao.idMovimentacao =
                            resultSet.getInt("id_movimentacao");

                    movimentacao.tipo =
                            resultSet.getString("tipo");

                    movimentacao.descricao =
                            resultSet.getString("descricao");

                    movimentacao.valor =
                            resultSet.getBigDecimal("valor");

                    movimentacao.dataHora =
                            resultSet
                                    .getTimestamp("data_hora")
                                    .toLocalDateTime();

                    lista.add(movimentacao);
                }
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Erro ao listar movimentações do caixa.",
                    e
            );
        }

        return lista;
    }

    public static class MovimentacaoResumo {

        public int idMovimentacao;
        public String tipo;
        public String descricao;
        public BigDecimal valor;
        public java.time.LocalDateTime dataHora;
    }
}