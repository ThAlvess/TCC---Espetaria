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

        String sql = """
            INSERT INTO movimentacao_caixa
                (id_caixa, id_pagamento, tipo, descricao, valor)
            VALUES
                (?, NULL, 'SAIDA', ?, ?)
            """;

        try (
                Connection connection =
                        ConnectionFactory.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setInt(1, idCaixa);
            statement.setString(2, descricao);
            statement.setBigDecimal(3, valor);

            statement.executeUpdate();

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