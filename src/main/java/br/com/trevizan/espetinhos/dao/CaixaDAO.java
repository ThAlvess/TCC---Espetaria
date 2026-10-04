package br.com.trevizan.espetinhos.dao;

import br.com.trevizan.espetinhos.connection.ConnectionFactory;
import br.com.trevizan.espetinhos.model.Caixa;
import br.com.trevizan.espetinhos.model.Usuario;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class CaixaDAO {

    public Caixa buscarCaixaAberto() {

        String sql = """
                SELECT
                    c.id_caixa,
                    c.data_hora_abertura,
                    c.data_hora_fechamento,
                    c.valor_inicial,
                    c.valor_final,
                    c.status,
                    u.id_usuario,
                    u.nome,
                    u.login,
                    u.senha,
                    u.ativo
                FROM caixa c
                INNER JOIN usuario u
                    ON c.id_usuario_abertura = u.id_usuario
                WHERE c.status = 'ABERTO'
                ORDER BY c.data_hora_abertura DESC
                LIMIT 1
                """;

        try (
                Connection connection = ConnectionFactory.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()
        ) {

            if (resultSet.next()) {

                Usuario usuario = new Usuario();
                usuario.setIdUsuario(resultSet.getInt("id_usuario"));
                usuario.setNome(resultSet.getString("nome"));
                usuario.setLogin(resultSet.getString("login"));
                usuario.setSenha(resultSet.getString("senha"));
                usuario.setAtivo(resultSet.getBoolean("ativo"));

                Caixa caixa = new Caixa();
                caixa.setIdCaixa(resultSet.getInt("id_caixa"));
                caixa.setUsuarioAbertura(usuario);
                caixa.setValorInicial(resultSet.getBigDecimal("valor_inicial"));
                caixa.setValorFinal(resultSet.getBigDecimal("valor_final"));
                caixa.setStatus(resultSet.getString("status"));

                Timestamp abertura = resultSet.getTimestamp("data_hora_abertura");
                if (abertura != null) {
                    caixa.setDataHoraAbertura(abertura.toLocalDateTime());
                }

                Timestamp fechamento = resultSet.getTimestamp("data_hora_fechamento");
                if (fechamento != null) {
                    caixa.setDataHoraFechamento(fechamento.toLocalDateTime());
                }

                return caixa;
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erro ao buscar caixa aberto.",
                    e
            );
        }

        return null;
    }

    public void abrirCaixa(Caixa caixa) {

        String sql = """
                INSERT INTO caixa
                (id_usuario_abertura, valor_inicial, status)
                VALUES (?, ?, 'ABERTO')
                """;

        try (
                Connection connection = ConnectionFactory.getConnection();
                PreparedStatement statement = connection.prepareStatement(
                        sql,
                        Statement.RETURN_GENERATED_KEYS
                )
        ) {

            statement.setInt(
                    1,
                    caixa.getUsuarioAbertura().getIdUsuario()
            );

            statement.setBigDecimal(
                    2,
                    caixa.getValorInicial()
            );

            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {

                if (generatedKeys.next()) {
                    caixa.setIdCaixa(
                            generatedKeys.getInt(1)
                    );
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erro ao abrir caixa.",
                    e
            );
        }
    }

    public boolean caixaAbertoEhDeHoje(Caixa caixa) {

        if (caixa == null || caixa.getDataHoraAbertura() == null) {
            return false;
        }

        java.time.LocalDate dataAbertura =
                caixa.getDataHoraAbertura().toLocalDate();

        java.time.LocalDate hoje =
                java.time.LocalDate.now();

        return dataAbertura.equals(hoje);
    }

    public void fecharCaixa(int idCaixa, BigDecimal valorFinal) {

        String sql = """
                UPDATE caixa
                SET valor_final = ?,
                    status = 'FECHADO',
                    data_hora_fechamento = NOW()
                WHERE id_caixa = ?
                """;

        try (
                Connection connection = ConnectionFactory.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {

            statement.setBigDecimal(1, valorFinal);
            statement.setInt(2, idCaixa);

            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erro ao fechar caixa.",
                    e
            );
        }
    }

    public List<Caixa> listarCaixas() {

        String sql = """
            SELECT
                cx.id_caixa,
                cx.id_usuario_abertura,
                u.nome AS nome_usuario,
                cx.data_hora_abertura,
                cx.data_hora_fechamento,
                cx.valor_inicial,
                cx.valor_final,
                cx.status
            FROM caixa cx
            INNER JOIN usuario u
                ON u.id_usuario = cx.id_usuario_abertura
            ORDER BY cx.data_hora_abertura DESC
            """;

        List<Caixa> caixas = new ArrayList<>();

        try (
                Connection connection = ConnectionFactory.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()
        ) {

            while (resultSet.next()) {

                Caixa caixa = new Caixa();

                caixa.setIdCaixa(
                        resultSet.getInt("id_caixa")
                );

                Usuario usuario = new Usuario();

                usuario.setIdUsuario(
                        resultSet.getInt("id_usuario_abertura")
                );

                usuario.setNome(
                        resultSet.getString("nome_usuario")
                );

                caixa.setUsuarioAbertura(usuario);

                Timestamp abertura =
                        resultSet.getTimestamp("data_hora_abertura");

                if (abertura != null) {
                    caixa.setDataHoraAbertura(
                            abertura.toLocalDateTime()
                    );
                }

                Timestamp fechamento =
                        resultSet.getTimestamp("data_hora_fechamento");

                if (fechamento != null) {
                    caixa.setDataHoraFechamento(
                            fechamento.toLocalDateTime()
                    );
                }

                caixa.setValorInicial(
                        resultSet.getBigDecimal("valor_inicial")
                );

                caixa.setValorFinal(
                        resultSet.getBigDecimal("valor_final")
                );

                caixa.setStatus(
                        resultSet.getString("status")
                );

                caixas.add(caixa);
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erro ao listar histórico de caixas.",
                    e
            );
        }

        return caixas;
    }

    public BigDecimal calcularTotalVendas(int idCaixa) {

        String sql = """
            SELECT COALESCE(SUM(valor), 0) AS total
            FROM movimentacao_caixa
            WHERE id_caixa = ?
              AND tipo = 'ENTRADA'
            """;

        try (
                Connection connection =
                        ConnectionFactory.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setInt(1, idCaixa);

            try (ResultSet rs = statement.executeQuery()) {

                if (rs.next()) {
                    return rs.getBigDecimal("total");
                }
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Erro ao calcular total de vendas do caixa.",
                    e
            );
        }

        return BigDecimal.ZERO;
    }

    public BigDecimal calcularSangrias(int idCaixa) {

        String sql = """
                SELECT COALESCE(SUM(valor), 0) AS total
                FROM movimentacao_caixa
                WHERE id_caixa = ?
                  AND tipo = 'SAIDA'
                """;

        try (
                Connection connection = ConnectionFactory.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {

            statement.setInt(1, idCaixa);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getBigDecimal("total");
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erro ao calcular sangrias.",
                    e
            );
        }

        return BigDecimal.ZERO;
    }

    public int contarComandasAbertas() {

        String sql = """
                SELECT COUNT(*) AS total
                FROM comanda
                WHERE status = 'ABERTA'
                """;

        try (
                Connection connection = ConnectionFactory.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()
        ) {

            if (resultSet.next()) {
                return resultSet.getInt("total");
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erro ao contar comandas abertas.",
                    e
            );
        }

        return 0;
    }

    /**
     * Representação simples de uma comanda, só para exibição na tela de Caixa.
     */
    public static class ComandaResumo {
        public int idComanda;
        public int numeroMesa;
        public String nomeCliente;
        public String status;
        public BigDecimal valorTotal;
        public String tipoAtendimento;
    }

    public java.util.List<ComandaResumo> listarComandasDoCaixa(int idCaixa) {

        String sql = """
            
                SELECT
                c.id_comanda,
                m.numero AS numero_mesa,
                c.tipo_atendimento,
                c.nome_cliente,
                c.status,
                c.valor_total
            FROM movimentacao_caixa mc
            INNER JOIN pagamento p
                ON p.id_pagamento = mc.id_pagamento
            INNER JOIN comanda c
                ON c.id_comanda = p.id_comanda
            LEFT JOIN mesa m ON c.id_mesa = m.id_mesa
            WHERE mc.id_caixa = ?
              AND mc.tipo = 'ENTRADA'
              AND c.status = 'FECHADA'
            ORDER BY c.id_comanda DESC
            """;

        java.util.List<ComandaResumo> lista =
                new java.util.ArrayList<>();

        try (
                Connection connection =
                        ConnectionFactory.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setInt(1, idCaixa);

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                while (resultSet.next()) {

                    ComandaResumo c =
                            new ComandaResumo();

                    c.idComanda =
                            resultSet.getInt("id_comanda");

                    c.numeroMesa =
                            resultSet.getInt("numero_mesa");

                    c.nomeCliente =
                            resultSet.getString("nome_cliente");

                    c.status =
                            resultSet.getString("status");

                    c.valorTotal =
                            resultSet.getBigDecimal("valor_total");

                    c.tipoAtendimento = resultSet.getString("tipo_atendimento");

                    lista.add(c);
                }
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Erro ao listar comandas do caixa.",
                    e
            );
        }

        return lista;
    }
}