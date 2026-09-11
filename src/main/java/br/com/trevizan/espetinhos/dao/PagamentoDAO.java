package br.com.trevizan.espetinhos.dao;

import br.com.trevizan.espetinhos.connection.ConnectionFactory;
import br.com.trevizan.espetinhos.model.Pagamento;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PagamentoDAO {

    public int adicionar(Pagamento pagamento) {

        String sql = """
                INSERT INTO pagamento
                    (id_comanda, forma_pagamento, valor)
                VALUES
                    (?, ?, ?)
                """;

        try (
                Connection conn = ConnectionFactory.getConnection();
                PreparedStatement stmt = conn.prepareStatement(
                        sql,
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

            try (ResultSet rs = stmt.getGeneratedKeys()) {

                if (rs.next()) {

                    int idGerado = rs.getInt(1);

                    pagamento.setIdPagamento(
                            idGerado
                    );

                    return idGerado;
                }
            }

            throw new RuntimeException(
                    "Pagamento inserido, mas não foi possível obter o ID."
            );

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Erro ao registrar pagamento.",
                    e
            );
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