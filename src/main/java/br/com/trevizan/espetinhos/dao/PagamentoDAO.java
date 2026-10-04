package br.com.trevizan.espetinhos.dao;

import br.com.trevizan.espetinhos.connection.ConnectionFactory;
import br.com.trevizan.espetinhos.model.Pagamento;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import br.com.trevizan.espetinhos.model.Caixa;

public class PagamentoDAO {

    private final CaixaDAO caixaDAO =
            new CaixaDAO();

    private final MovimentacaoCaixaDAO movimentacaoCaixaDAO =
            new MovimentacaoCaixaDAO();

    public int adicionar(Pagamento pagamento) {

        String sqlPagamento = """
            INSERT INTO pagamento
                (id_comanda, forma_pagamento, valor)
            VALUES
                (?, ?, ?)
            """;

        Connection conn = null;

        try {

            conn = ConnectionFactory.getConnection();

            // Inicia a transação
            conn.setAutoCommit(false);

            // ----------------------------------------
            // 1. Verifica o caixa aberto
            // ----------------------------------------

            Caixa caixaAberto =
                    caixaDAO.buscarCaixaAberto();

            if (caixaAberto == null) {
                throw new RuntimeException(
                        "Não existe caixa aberto para registrar o pagamento."
                );
            }

            // ----------------------------------------
            // 2. Registra o pagamento
            // ----------------------------------------

            int idGerado;

            try (
                    PreparedStatement stmt =
                            conn.prepareStatement(
                                    sqlPagamento,
                                    Statement.RETURN_GENERATED_KEYS
                            )
            ) {

                stmt.setInt(
                        1,
                        pagamento.getIdComanda()
                );

                stmt.setString(
                        2,
                        pagamento.getFormaPagamento()
                );

                stmt.setBigDecimal(
                        3,
                        pagamento.getValor()
                );

                stmt.executeUpdate();

                try (
                        ResultSet rs =
                                stmt.getGeneratedKeys()
                ) {

                    if (!rs.next()) {
                        throw new SQLException(
                                "Não foi possível obter o ID do pagamento."
                        );
                    }

                    idGerado = rs.getInt(1);
                }
            }

            // ----------------------------------------
            // 3. Registra a movimentação
            // ----------------------------------------

            movimentacaoCaixaDAO.registrarEntradaPagamento(
                    conn,
                    caixaAberto.getIdCaixa(),
                    idGerado,
                    pagamento.getIdComanda(),
                    pagamento.getValor()
            );

            // ----------------------------------------
            // 4. Tudo funcionou
            // ----------------------------------------

            conn.commit();

            pagamento.setIdPagamento(idGerado);

            return idGerado;

        } catch (Exception e) {

            // ----------------------------------------
            // Alguma coisa falhou
            // ----------------------------------------

            if (conn != null) {

                try {
                    conn.rollback();
                } catch (SQLException rollbackException) {
                    e.addSuppressed(rollbackException);
                }
            }

            throw new RuntimeException(
                    "Erro ao registrar pagamento.",
                    e
            );

        } finally {

            if (conn != null) {

                try {
                    conn.setAutoCommit(true);
                    conn.close();

                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public List<Pagamento> listarPorComanda(
            int idComanda
    ) {

        String sql = """
                SELECT
                    id_pagamento,
                    id_comanda,
                    forma_pagamento,
                    valor,
                    data_hora
                FROM pagamento
                WHERE id_comanda = ?
                ORDER BY data_hora
                """;

        List<Pagamento> pagamentos =
                new ArrayList<>();

        try (
                Connection conn = ConnectionFactory.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)
        ) {

            stmt.setInt(
                    1,
                    idComanda
            );

            try (ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {

                    Pagamento pagamento =
                            new Pagamento();

                    pagamento.setIdPagamento(
                            rs.getInt("id_pagamento")
                    );

                    pagamento.setIdComanda(
                            rs.getInt("id_comanda")
                    );

                    pagamento.setFormaPagamento(
                            rs.getString(
                                    "forma_pagamento"
                            )
                    );

                    pagamento.setValor(
                            rs.getBigDecimal("valor")
                    );

                    Timestamp dataHora =
                            rs.getTimestamp("data_hora");

                    if (dataHora != null) {

                        pagamento.setDataHora(
                                dataHora.toLocalDateTime()
                        );
                    }

                    pagamentos.add(
                            pagamento
                    );
                }
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Erro ao listar pagamentos da comanda.",
                    e
            );
        }

        return pagamentos;
    }

    public BigDecimal obterTotalPago(
            int idComanda
    ) {

        String sql = """
                SELECT COALESCE(SUM(valor), 0)
                AS total_pago
                FROM pagamento
                WHERE id_comanda = ?
                """;

        try (
                Connection conn = ConnectionFactory.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)
        ) {

            stmt.setInt(
                    1,
                    idComanda
            );

            try (ResultSet rs = stmt.executeQuery()) {

                if (rs.next()) {

                    return rs.getBigDecimal(
                            "total_pago"
                    );
                }
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Erro ao calcular total pago.",
                    e
            );
        }

        return BigDecimal.ZERO;
    }

    public java.util.Map<String, BigDecimal> listarTotaisPorFormaDoCaixa(
            int idCaixa
    ) {

        String sql = """
            SELECT
                p.forma_pagamento,
                COALESCE(SUM(p.valor), 0) AS total
            FROM pagamento p
            INNER JOIN comanda c
                ON c.id_comanda = p.id_comanda
            INNER JOIN caixa cx
                ON cx.id_caixa = ?
            WHERE p.data_hora >= cx.data_hora_abertura
              AND p.data_hora <= COALESCE(
                    cx.data_hora_fechamento,
                    NOW()
              )
              AND c.status = 'FECHADA'
            GROUP BY p.forma_pagamento
            ORDER BY total DESC
            """;

        java.util.Map<String, BigDecimal> totais =
                new java.util.LinkedHashMap<>();

        try (
                Connection conn = ConnectionFactory.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)
        ) {

            stmt.setInt(1, idCaixa);

            try (ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {

                    totais.put(
                            rs.getString("forma_pagamento"),
                            rs.getBigDecimal("total")
                    );
                }
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Erro ao listar formas de pagamento do caixa.",
                    e
            );
        }

        return totais;
    }



    public void excluir(
            int idPagamento
    ) {

        String sql = """
                DELETE FROM pagamento
                WHERE id_pagamento = ?
                """;

        try (
                Connection conn = ConnectionFactory.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)
        ) {

            stmt.setInt(
                    1,
                    idPagamento
            );

            stmt.executeUpdate();

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Erro ao excluir pagamento.",
                    e
            );
        }
    }
}