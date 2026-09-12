package br.com.trevizan.espetinhos.dao;

import br.com.trevizan.espetinhos.connection.ConnectionFactory;
import br.com.trevizan.espetinhos.model.ResumoRelatorio;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;

/**
 * Consultas usadas pela tela de Relatórios. Todos os métodos recebem o
 * intervalo [inicio, fim] já calculado pela tela (de acordo com o filtro
 * Diário/Mensal/Anual/Fiscal escolhido).
 *
 * Convenção adotada: os gráficos que detalham o que foi vendido (ranking de
 * produtos, vendas por categoria e evolução) usam comanda.data_fechamento
 * (comandas com status FECHADA) como "data da venda". Os cartões de
 * faturamento/ticket médio e o gráfico "Por Forma de Pagamento" usam
 * pagamento.data_hora, já que são baseados no dinheiro efetivamente recebido.
 */
public class RelatorioDAO {

    public enum Granularidade { HORA, DIA, MES }

    public ResumoRelatorio buscarResumo(LocalDateTime inicio, LocalDateTime fim) {

        ResumoRelatorio resumo = new ResumoRelatorio();

        String sqlResumo = """
                SELECT
                    COALESCE(SUM(valor), 0)                       AS faturamento_total,
                    COUNT(*)                                      AS quantidade_vendas,
                    COALESCE(SUM(valor) / NULLIF(COUNT(*), 0), 0)  AS ticket_medio
                FROM pagamento
                WHERE data_hora BETWEEN ? AND ?
                """;

        String sqlFormaPrincipal = """
                SELECT forma_pagamento, SUM(valor) AS total_forma
                FROM pagamento
                WHERE data_hora BETWEEN ? AND ?
                GROUP BY forma_pagamento
                ORDER BY SUM(valor) DESC
                LIMIT 1
                """;

        try (Connection conexao = ConnectionFactory.getConnection()) {

            try (PreparedStatement stmt = conexao.prepareStatement(sqlResumo)) {
                stmt.setTimestamp(1, Timestamp.valueOf(inicio));
                stmt.setTimestamp(2, Timestamp.valueOf(fim));

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        resumo.setFaturamentoTotal(rs.getBigDecimal("faturamento_total"));
                        resumo.setQuantidadeVendas(rs.getInt("quantidade_vendas"));
                        resumo.setTicketMedio(rs.getBigDecimal("ticket_medio"));
                    }
                }
            }

            try (PreparedStatement stmt = conexao.prepareStatement(sqlFormaPrincipal)) {
                stmt.setTimestamp(1, Timestamp.valueOf(inicio));
                stmt.setTimestamp(2, Timestamp.valueOf(fim));

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        resumo.setFormaPagamentoPrincipal(
                                nomeFormaPagamento(rs.getString("forma_pagamento")));
                        resumo.setValorFormaPagamentoPrincipal(rs.getBigDecimal("total_forma"));
                    }
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar resumo do relatório.", e);
        }

        return resumo;
    }

    public LinkedHashMap<String, BigDecimal> rankingProdutos(LocalDateTime inicio, LocalDateTime fim, int limite) {

        String sql = """
                SELECT pr.nome AS nome, SUM(ic.subtotal) AS total
                FROM item_comanda ic
                JOIN produto pr ON pr.id_produto = ic.id_produto
                JOIN comanda c ON c.id_comanda = ic.id_comanda
                WHERE c.status = 'FECHADA'
                  AND c.data_fechamento BETWEEN ? AND ?
                  AND ic.status_item <> 'CANCELADO'
                GROUP BY pr.id_produto, pr.nome
                ORDER BY total DESC
                LIMIT ?
                """;

        LinkedHashMap<String, BigDecimal> ranking = new LinkedHashMap<>();

        try (
                Connection conexao = ConnectionFactory.getConnection();
                PreparedStatement stmt = conexao.prepareStatement(sql)
        ) {
            stmt.setTimestamp(1, Timestamp.valueOf(inicio));
            stmt.setTimestamp(2, Timestamp.valueOf(fim));
            stmt.setInt(3, limite);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ranking.put(rs.getString("nome"), rs.getBigDecimal("total"));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar ranking de produtos.", e);
        }

        return ranking;
    }

    public LinkedHashMap<String, BigDecimal> vendasPorCategoria(LocalDateTime inicio, LocalDateTime fim) {

        String sql = """
                SELECT cat.nome AS nome, SUM(ic.subtotal) AS total
                FROM item_comanda ic
                JOIN produto pr ON pr.id_produto = ic.id_produto
                JOIN categoria cat ON cat.id_categoria = pr.id_categoria
                JOIN comanda c ON c.id_comanda = ic.id_comanda
                WHERE c.status = 'FECHADA'
                  AND c.data_fechamento BETWEEN ? AND ?
                  AND ic.status_item <> 'CANCELADO'
                GROUP BY cat.id_categoria, cat.nome
                ORDER BY total DESC
                """;

        LinkedHashMap<String, BigDecimal> vendas = new LinkedHashMap<>();

        try (
                Connection conexao = ConnectionFactory.getConnection();
                PreparedStatement stmt = conexao.prepareStatement(sql)
        ) {
            stmt.setTimestamp(1, Timestamp.valueOf(inicio));
            stmt.setTimestamp(2, Timestamp.valueOf(fim));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    vendas.put(rs.getString("nome"), rs.getBigDecimal("total"));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar vendas por categoria.", e);
        }

        return vendas;
    }

    public LinkedHashMap<String, BigDecimal> porFormaPagamento(LocalDateTime inicio, LocalDateTime fim) {

        String sql = """
                SELECT forma_pagamento AS forma, SUM(valor) AS total
                FROM pagamento
                WHERE data_hora BETWEEN ? AND ?
                GROUP BY forma_pagamento
                ORDER BY total DESC
                """;

        LinkedHashMap<String, BigDecimal> porForma = new LinkedHashMap<>();

        try (
                Connection conexao = ConnectionFactory.getConnection();
                PreparedStatement stmt = conexao.prepareStatement(sql)
        ) {
            stmt.setTimestamp(1, Timestamp.valueOf(inicio));
            stmt.setTimestamp(2, Timestamp.valueOf(fim));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    porForma.put(
                            nomeFormaPagamento(rs.getString("forma")),
                            rs.getBigDecimal("total"));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar totais por forma de pagamento.", e);
        }

        return porForma;
    }

    /**
     * Evolução do faturamento no período, agrupada por hora (filtro Diário),
     * por dia (filtro Mensal) ou por mês (filtros Anual/Fiscal).
     */
    public LinkedHashMap<String, BigDecimal> evolucaoFaturamento(
            LocalDateTime inicio, LocalDateTime fim, Granularidade granularidade) {

        String colunaAgrupamento = switch (granularidade) {
            case HORA -> "LPAD(HOUR(data_hora), 2, '0')";
            case DIA -> "DATE_FORMAT(data_hora, '%d/%m')";
            case MES -> "DATE_FORMAT(data_hora, '%Y-%m')";
        };

        String sql = """
                SELECT %s AS rotulo, SUM(valor) AS total
                FROM pagamento
                WHERE data_hora BETWEEN ? AND ?
                GROUP BY %s
                ORDER BY MIN(data_hora)
                """.formatted(colunaAgrupamento, colunaAgrupamento);

        LinkedHashMap<String, BigDecimal> evolucao = new LinkedHashMap<>();

        try (
                Connection conexao = ConnectionFactory.getConnection();
                PreparedStatement stmt = conexao.prepareStatement(sql)
        ) {
            stmt.setTimestamp(1, Timestamp.valueOf(inicio));
            stmt.setTimestamp(2, Timestamp.valueOf(fim));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    evolucao.put(rs.getString("rotulo"), rs.getBigDecimal("total"));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar evolução do faturamento.", e);
        }

        return evolucao;
    }

    private String nomeFormaPagamento(String valorBanco) {
        if (valorBanco == null) {
            return null;
        }
        return switch (valorBanco) {
            case "DINHEIRO" -> "Dinheiro";
            case "PIX" -> "Pix";
            case "DEBITO" -> "Cartão de Débito";
            case "CREDITO" -> "Cartão de Crédito";
            default -> valorBanco;
        };
    }
}
