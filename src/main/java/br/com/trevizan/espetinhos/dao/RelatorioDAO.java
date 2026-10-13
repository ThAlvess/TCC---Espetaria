package br.com.trevizan.espetinhos.dao;

import br.com.trevizan.espetinhos.connection.ConnectionFactory;
import br.com.trevizan.espetinhos.model.ProdutoVendido;
import br.com.trevizan.espetinhos.model.ResumoPeriodo;
import br.com.trevizan.espetinhos.model.ResumoRelatorio;
import br.com.trevizan.espetinhos.model.VendaDetalhada;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
        COALESCE(SUM(p.valor), 0) AS faturamento_total,
        COUNT(DISTINCT p.id_comanda) AS quantidade_vendas,
        COALESCE(
            SUM(p.valor) / NULLIF(COUNT(DISTINCT p.id_comanda), 0),
            0
        ) AS ticket_medio
    FROM pagamento p
    INNER JOIN comanda c
        ON c.id_comanda = p.id_comanda
    WHERE p.data_hora BETWEEN ? AND ?
      AND c.status = 'FECHADA'
    """;

        String sqlFormaPrincipal = """
            SELECT p.forma_pagamento, SUM(p.valor) AS total_forma
            FROM pagamento p
            INNER JOIN comanda c
                ON c.id_comanda = p.id_comanda
            WHERE p.data_hora BETWEEN ? AND ?
            AND c.status = 'FECHADA'
            GROUP BY p.forma_pagamento
            ORDER BY SUM(p.valor) DESC
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
            SELECT p.forma_pagamento AS forma, SUM(p.valor) AS total
            FROM pagamento p
            INNER JOIN comanda c
                ON c.id_comanda = p.id_comanda
            WHERE p.data_hora BETWEEN ? AND ?
            AND c.status = 'FECHADA'
            GROUP BY p.forma_pagamento
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
            case HORA -> "LPAD(HOUR(p.data_hora), 2, '0')";
            case DIA -> "DATE_FORMAT(p.data_hora, '%d/%m')";
            case MES -> "DATE_FORMAT(p.data_hora, '%Y-%m')";
        };

        String sql = """
            SELECT %s AS rotulo, SUM(p.valor) AS total
            FROM pagamento p
            INNER JOIN comanda c
                ON c.id_comanda = p.id_comanda
            WHERE p.data_hora BETWEEN ? AND ?
            AND c.status = 'FECHADA'
            GROUP BY %s
            ORDER BY MIN(p.data_hora)
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

    // =====================================================================
    // Consultas usadas só na exportação para PDF (tabelas de detalhamento)
    // =====================================================================

    /**
     * Lista, uma por linha, as comandas fechadas que receberam pagamento no
     * período (usada na tabela "Vendas do dia" do filtro Diário). Segue a
     * mesma regra dos cartões de KPI (pagamento.data_hora), então a soma
     * da coluna "Total" bate com o "Faturamento Total" da tela.
     */
    public List<VendaDetalhada> listarVendasDetalhadas(LocalDateTime inicio, LocalDateTime fim) {

        String sql = """
            SELECT c.id_comanda,
                   m.numero AS numero_mesa,
                   c.nome_cliente,
                   u.nome AS atendente,
                   c.data_abertura,
                   c.data_fechamento,
                   (SELECT COALESCE(SUM(ic.quantidade), 0)
                      FROM item_comanda ic
                     WHERE ic.id_comanda = c.id_comanda
                       AND ic.status_item <> 'CANCELADO') AS quantidade_itens,
                   GROUP_CONCAT(DISTINCT p.forma_pagamento
                                ORDER BY p.forma_pagamento SEPARATOR ',') AS formas,
                   SUM(p.valor) AS total
            FROM pagamento p
            INNER JOIN comanda c ON c.id_comanda = p.id_comanda
            INNER JOIN mesa m ON m.id_mesa = c.id_mesa
            INNER JOIN usuario u ON u.id_usuario = c.id_usuario
            WHERE p.data_hora BETWEEN ? AND ?
              AND c.status = 'FECHADA'
            GROUP BY c.id_comanda, m.numero, c.nome_cliente, u.nome,
                     c.data_abertura, c.data_fechamento
            ORDER BY c.data_fechamento, c.id_comanda
            """;

        List<VendaDetalhada> vendas = new ArrayList<>();

        try (
                Connection conexao = ConnectionFactory.getConnection();
                PreparedStatement stmt = conexao.prepareStatement(sql)
        ) {
            stmt.setTimestamp(1, Timestamp.valueOf(inicio));
            stmt.setTimestamp(2, Timestamp.valueOf(fim));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    VendaDetalhada venda = new VendaDetalhada();
                    venda.setIdComanda(rs.getInt("id_comanda"));
                    venda.setNumeroMesa(rs.getInt("numero_mesa"));
                    venda.setNomeCliente(rs.getString("nome_cliente"));
                    venda.setAtendente(rs.getString("atendente"));

                    Timestamp abertura = rs.getTimestamp("data_abertura");
                    Timestamp fechamento = rs.getTimestamp("data_fechamento");
                    venda.setDataAbertura(abertura != null ? abertura.toLocalDateTime() : null);
                    venda.setDataFechamento(fechamento != null ? fechamento.toLocalDateTime() : null);

                    venda.setQuantidadeItens(rs.getInt("quantidade_itens"));
                    venda.setFormasPagamento(nomesFormasPagamento(rs.getString("formas")));
                    venda.setValorTotal(rs.getBigDecimal("total"));
                    vendas.add(venda);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar vendas detalhadas.", e);
        }

        return vendas;
    }

    /**
     * Quantidade e valor vendidos de cada produto no período, do mais
     * vendido (em R$) para o menos vendido. Mesma regra do ranking de
     * produtos da tela (comanda.data_fechamento, itens não cancelados).
     */
    public List<ProdutoVendido> produtosVendidos(LocalDateTime inicio, LocalDateTime fim, int limite) {

        String sql = """
            SELECT pr.nome AS nome,
                   cat.nome AS categoria,
                   SUM(ic.quantidade) AS quantidade,
                   SUM(ic.subtotal) AS total
            FROM item_comanda ic
            JOIN produto pr ON pr.id_produto = ic.id_produto
            JOIN categoria cat ON cat.id_categoria = pr.id_categoria
            JOIN comanda c ON c.id_comanda = ic.id_comanda
            WHERE c.status = 'FECHADA'
              AND c.data_fechamento BETWEEN ? AND ?
              AND ic.status_item <> 'CANCELADO'
            GROUP BY pr.id_produto, pr.nome, cat.nome
            ORDER BY total DESC
            LIMIT ?
            """;

        List<ProdutoVendido> produtos = new ArrayList<>();

        try (
                Connection conexao = ConnectionFactory.getConnection();
                PreparedStatement stmt = conexao.prepareStatement(sql)
        ) {
            stmt.setTimestamp(1, Timestamp.valueOf(inicio));
            stmt.setTimestamp(2, Timestamp.valueOf(fim));
            stmt.setInt(3, limite);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    produtos.add(new ProdutoVendido(
                            rs.getString("nome"),
                            rs.getString("categoria"),
                            rs.getInt("quantidade"),
                            rs.getBigDecimal("total")));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar produtos vendidos.", e);
        }

        return produtos;
    }

    /**
     * Resumo semana a semana do intervalo (usado no filtro Mensal). As
     * semanas vão de segunda a domingo e são "cortadas" no primeiro e no
     * último dia do intervalo — ex.: setembro/2026 começa numa terça, então
     * a Semana 1 é 01/09 a 06/09. Semanas sem venda também aparecem.
     */
    public List<ResumoPeriodo> resumoSemanal(LocalDateTime inicio, LocalDateTime fim) {
        Map<LocalDate, TotalDiario> totais = totaisPorDia(inicio, fim);

        List<ResumoPeriodo> semanas = new ArrayList<>();
        LocalDate ultimoDia = fim.toLocalDate();
        LocalDate diaInicial = inicio.toLocalDate();
        int numero = 1;

        while (!diaInicial.isAfter(ultimoDia)) {
            LocalDate diaFinal = diaInicial.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
            if (diaFinal.isAfter(ultimoDia)) {
                diaFinal = ultimoDia;
            }

            ResumoPeriodo semana = new ResumoPeriodo("Semana " + numero, diaInicial, diaFinal);
            acumularDias(semana, totais);
            semanas.add(semana);

            diaInicial = diaFinal.plusDays(1);
            numero++;
        }

        return semanas;
    }

    /**
     * Resumo mês a mês do intervalo (usado nos filtros Anual e Fiscal).
     * Todos os meses do intervalo aparecem, inclusive os sem venda.
     */
    public List<ResumoPeriodo> resumoMensal(LocalDateTime inicio, LocalDateTime fim) {
        Map<LocalDate, TotalDiario> totais = totaisPorDia(inicio, fim);

        List<ResumoPeriodo> meses = new ArrayList<>();
        YearMonth mes = YearMonth.from(inicio);
        YearMonth ultimoMes = YearMonth.from(fim);
        Locale ptBr = new Locale("pt", "BR");

        while (!mes.isAfter(ultimoMes)) {
            String nomeMes = mes.getMonth().getDisplayName(TextStyle.FULL, ptBr);
            String rotulo = Character.toUpperCase(nomeMes.charAt(0)) + nomeMes.substring(1)
                    + "/" + mes.getYear();

            LocalDate primeiroDia = mes.atDay(1).isBefore(inicio.toLocalDate())
                    ? inicio.toLocalDate() : mes.atDay(1);
            LocalDate ultimoDia = mes.atEndOfMonth().isAfter(fim.toLocalDate())
                    ? fim.toLocalDate() : mes.atEndOfMonth();

            ResumoPeriodo resumoMes = new ResumoPeriodo(rotulo, primeiroDia, ultimoDia);
            acumularDias(resumoMes, totais);
            meses.add(resumoMes);

            mes = mes.plusMonths(1);
        }

        return meses;
    }

    private void acumularDias(ResumoPeriodo periodo, Map<LocalDate, TotalDiario> totais) {
        for (LocalDate dia = periodo.getDataInicio(); !dia.isAfter(periodo.getDataFim()); dia = dia.plusDays(1)) {
            TotalDiario total = totais.get(dia);
            if (total != null) {
                periodo.acumular(dia, total.vendas(), total.faturamento());
            }
        }
    }

    private record TotalDiario(int vendas, BigDecimal faturamento) { }

    /** Faturamento e quantidade de vendas de cada dia do intervalo (só dias com venda). */
    private Map<LocalDate, TotalDiario> totaisPorDia(LocalDateTime inicio, LocalDateTime fim) {

        String sql = """
            SELECT DATE(p.data_hora) AS dia,
                   COUNT(DISTINCT p.id_comanda) AS vendas,
                   SUM(p.valor) AS total
            FROM pagamento p
            INNER JOIN comanda c ON c.id_comanda = p.id_comanda
            WHERE p.data_hora BETWEEN ? AND ?
              AND c.status = 'FECHADA'
            GROUP BY DATE(p.data_hora)
            """;

        Map<LocalDate, TotalDiario> totais = new HashMap<>();

        try (
                Connection conexao = ConnectionFactory.getConnection();
                PreparedStatement stmt = conexao.prepareStatement(sql)
        ) {
            stmt.setTimestamp(1, Timestamp.valueOf(inicio));
            stmt.setTimestamp(2, Timestamp.valueOf(fim));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    totais.put(
                            rs.getDate("dia").toLocalDate(),
                            new TotalDiario(rs.getInt("vendas"), rs.getBigDecimal("total")));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar totais por dia.", e);
        }

        return totais;
    }

    /** "CREDITO,PIX" -> "Crédito + Pix" (nomes curtos, pra caber na tabela do PDF). */
    private String nomesFormasPagamento(String formasBanco) {
        if (formasBanco == null || formasBanco.isBlank()) {
            return "—";
        }
        StringBuilder nomes = new StringBuilder();
        for (String forma : formasBanco.split(",")) {
            if (nomes.length() > 0) {
                nomes.append(" + ");
            }
            nomes.append(switch (forma.trim()) {
                case "DINHEIRO" -> "Dinheiro";
                case "PIX" -> "Pix";
                case "DEBITO" -> "Débito";
                case "CREDITO" -> "Crédito";
                default -> forma.trim();
            });
        }
        return nomes.toString();
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
