/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package br.com.trevizan.espetinhos.view;

import br.com.trevizan.espetinhos.PanelArredondado;
import br.com.trevizan.espetinhos.dao.RelatorioDAO;
import br.com.trevizan.espetinhos.model.ResumoRelatorio;
import br.com.trevizan.espetinhos.util.DadosRelatorio;
import br.com.trevizan.espetinhos.util.PeriodoRelatorio;
import br.com.trevizan.espetinhos.util.RelatorioExcelExporter;
import br.com.trevizan.espetinhos.util.RelatorioPdfExporter;
import br.com.trevizan.espetinhos.util.SessaoUsuario;

import com.github.lgooddatepicker.components.DatePicker;
import com.github.lgooddatepicker.components.DatePickerSettings;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;

/**
 *
 * @author marlo
 */
public class Relatorio extends javax.swing.JPanel {

    private final RelatorioDAO relatorioDAO = new RelatorioDAO();

    private PeriodoRelatorio.Tipo tipoPeriodoAtual = PeriodoRelatorio.Tipo.DIARIO;

    private javax.swing.JPanel painelPeriodo;
    private java.awt.CardLayout seletorCardLayout;
    private DatePicker datePickerDiario;
    private javax.swing.JComboBox<String> comboMes;
    private javax.swing.JComboBox<Integer> comboAnoMensal;
    private DatePicker datePickerDe;
    private DatePicker datePickerAte;
    private boolean ajustandoDatasPersonalizado = false;
    private javax.swing.JLabel jLabelSubtituloPagamento;
    private javax.swing.JLabel jLabelComparacaoFaturamento;
    private javax.swing.JLabel jLabelComparacaoVendas;
    private javax.swing.JLabel jLabelComparacaoTicket;
    private javax.swing.JButton btnExportar;

    // Último estado exibido na tela, reaproveitado na exportação (PDF/Excel)
    // — assim o arquivo sai exatamente com o que o usuário está vendo.
    private PeriodoRelatorio ultimoPeriodo;
    private PeriodoRelatorio ultimoPeriodoAnterior;
    private ResumoRelatorio ultimoResumo;
    private ResumoRelatorio ultimoResumoAnterior;
    private LinkedHashMap<String, BigDecimal> ultimosTotaisPagamento = new LinkedHashMap<>();
    private LinkedHashMap<String, BigDecimal> ultimasVendasPorCategoria = new LinkedHashMap<>();
    private LinkedHashMap<String, BigDecimal> ultimaEvolucao = new LinkedHashMap<>();
    private List<String> ultimosRotulosEvolucaoAnterior = new ArrayList<>();
    private List<BigDecimal> ultimaEvolucaoAnterior = new ArrayList<>();
    private org.jfree.chart.JFreeChart graficoRanking;
    private org.jfree.chart.JFreeChart graficoCategoria;
    private org.jfree.chart.JFreeChart graficoPagamento;
    private org.jfree.chart.JFreeChart graficoEvolucao;

    private static final java.text.NumberFormat FORMATO_MOEDA =
            java.text.NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

    private static final java.text.NumberFormat FORMATO_PERCENTUAL = criarFormatoPercentual();

    private static java.text.NumberFormat criarFormatoPercentual() {
        java.text.NumberFormat formato = java.text.NumberFormat.getPercentInstance(new Locale("pt", "BR"));
        formato.setMinimumFractionDigits(1);
        formato.setMaximumFractionDigits(1);
        return formato;
    }

    /**
     * Dicas (tooltips) dos gráficos: aparecem logo ao passar o mouse sobre
     * uma barra, fatia ou ponto, mostrando o valor exato.
     */
    private static void configurarDicas(org.jfree.chart.ChartPanel painel) {
        painel.setDisplayToolTips(true);
        painel.setInitialDelay(0);
        painel.setReshowDelay(0);
        painel.setDismissDelay(15000);
    }

    /**
     * Creates new form Relatorio
     */
    public Relatorio() {
        initComponents();
        br.com.trevizan.espetinhos.util.PadraoTela.aplicarTitulo(jLabel1); // referência do padrão de títulos
        montarSeletorPeriodo();
        adicionarBadgesKpi();
        atualizarRelatorios();
        atualizarAoExibir();
    }

    /** Data de "hoje" da última vez que a tela foi exibida (ver atualizarAoExibir). */
    private LocalDate ultimoDiaExibido = LocalDate.now();

    /**
     * A tela de Relatórios é criada uma única vez, quando o MainScreen abre,
     * e fica guardada no CardLayout. Por isso, sem este listener, vendas
     * fechadas depois disso só apareciam reiniciando o sistema.
     *
     * Agora, toda vez que o usuário entra na aba Relatórios (o CardLayout
     * torna o painel visível e dispara componentShown), os dados são
     * consultados de novo no banco, mantendo o filtro que estava selecionado.
     */
    private void atualizarAoExibir() {
        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentShown(java.awt.event.ComponentEvent evento) {
                LocalDate hoje = LocalDate.now();

                // Se o sistema ficou aberto de um dia para o outro e o filtro
                // Diário ainda estava no "hoje" antigo, avança para o dia atual
                // (o setDate já dispara atualizarRelatorios pelo listener do DatePicker).
                boolean virouODia = !hoje.equals(ultimoDiaExibido);
                boolean diarioNoHojeAntigo = ultimoDiaExibido.equals(datePickerDiario.getDate());
                ultimoDiaExibido = hoje;

                if (virouODia && diarioNoHojeAntigo) {
                    datePickerDiario.setDate(hoje);
                } else {
                    atualizarRelatorios();
                }
            }
        });
    }

    private void montarGraficoRanking(java.util.LinkedHashMap<String, java.math.BigDecimal> dados) {
        org.jfree.data.category.DefaultCategoryDataset dataset = new org.jfree.data.category.DefaultCategoryDataset();
        for (java.util.Map.Entry<String, java.math.BigDecimal> entrada : dados.entrySet()) {
            dataset.addValue(entrada.getValue(), "Vendas", entrada.getKey());
        }

        org.jfree.chart.JFreeChart chart = org.jfree.chart.ChartFactory.createBarChart(
            null, null, null, dataset,
            org.jfree.chart.plot.PlotOrientation.HORIZONTAL,
            false, false, false);

        org.jfree.chart.plot.CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(java.awt.Color.WHITE);
        plot.setOutlineVisible(false);
        plot.setRangeGridlinePaint(new java.awt.Color(225, 220, 213));
        plot.setDomainGridlinesVisible(false);

        java.awt.Font fonteEixos = new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 12);

        org.jfree.chart.axis.NumberAxis eixoValores = (org.jfree.chart.axis.NumberAxis) plot.getRangeAxis();
        eixoValores.setNumberFormatOverride(new java.text.DecimalFormat("'R$'#,##0"));
        eixoValores.setAxisLineVisible(false);
        eixoValores.setTickMarksVisible(false);
        eixoValores.setTickLabelFont(fonteEixos);
        eixoValores.setUpperMargin(0.15);

        plot.getDomainAxis().setAxisLineVisible(false);
        plot.getDomainAxis().setTickMarksVisible(false);
        plot.getDomainAxis().setTickLabelFont(fonteEixos);

        org.jfree.chart.renderer.category.BarRenderer renderer =
            (org.jfree.chart.renderer.category.BarRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, new java.awt.Color(210, 84, 43));
        renderer.setShadowVisible(false);
        renderer.setMaximumBarWidth(0.12);
        renderer.setBarPainter(new org.jfree.chart.renderer.category.StandardBarPainter());

        renderer.setDefaultItemLabelGenerator(
            new org.jfree.chart.labels.StandardCategoryItemLabelGenerator(
                "{2}", new java.text.DecimalFormat("'R$'#,##0")));
        renderer.setDefaultItemLabelsVisible(true);
        renderer.setDefaultItemLabelFont(fonteEixos);
        renderer.setDefaultItemLabelPaint(new java.awt.Color(70, 70, 70));
        renderer.setDefaultPositiveItemLabelPosition(
            new org.jfree.chart.labels.ItemLabelPosition(
                org.jfree.chart.labels.ItemLabelAnchor.OUTSIDE3,
                org.jfree.chart.ui.TextAnchor.CENTER_LEFT));

        // Dica ao passar o mouse na barra: "Batata Frita: R$ 532,00"
        // (gerador padrão do JFreeChart, que pode ser clonado — o PDF clona este gráfico)
        renderer.setDefaultToolTipGenerator(
            new org.jfree.chart.labels.StandardCategoryToolTipGenerator("{1}: {2}", FORMATO_MOEDA));

        chart.setBackgroundPaint(java.awt.Color.WHITE);
        chart.setBorderVisible(false);
        graficoRanking = chart;

        org.jfree.chart.ChartPanel painel = new org.jfree.chart.ChartPanel(chart);
        painel.setPopupMenu(null);
        configurarDicas(painel);
        painel.setBackground(java.awt.Color.WHITE);

        javax.swing.JLabel titulo = new javax.swing.JLabel("Ranking de Produtos");
        titulo.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 15));
        titulo.setForeground(new java.awt.Color(45, 45, 45));
        titulo.setBorder(javax.swing.BorderFactory.createEmptyBorder(18, 20, 10, 20));

        chartRankingPanel.removeAll();
        chartRankingPanel.setLayout(new java.awt.BorderLayout());
        chartRankingPanel.add(titulo, java.awt.BorderLayout.NORTH);
        chartRankingPanel.add(painel, java.awt.BorderLayout.CENTER);
        chartRankingPanel.revalidate();
        chartRankingPanel.repaint();
    }
    
    private void montarGraficoCategoria(java.util.LinkedHashMap<String, java.math.BigDecimal> dados) {
        org.jfree.data.general.DefaultPieDataset<String> dataset = new org.jfree.data.general.DefaultPieDataset<>();
        for (java.util.Map.Entry<String, java.math.BigDecimal> entrada : dados.entrySet()) {
            dataset.setValue(entrada.getKey(), entrada.getValue());
        }

        org.jfree.chart.plot.RingPlot plot = new org.jfree.chart.plot.RingPlot(dataset);
        plot.setSectionDepth(0.35);
        plot.setBackgroundPaint(java.awt.Color.WHITE);
        plot.setOutlineVisible(false);
        plot.setSeparatorsVisible(false);
        plot.setShadowPaint(null);
        plot.setInteriorGap(0.12); // dá espaço pro rótulo + linha de chamada não serem cortados pela borda do card

        java.awt.Color[] paletaCategorias = {
            new java.awt.Color(25, 100, 25),
            new java.awt.Color(230, 140, 60),
            new java.awt.Color(178, 58, 38),
            new java.awt.Color(70, 110, 150),
            new java.awt.Color(150, 140, 130)
        };
        int indiceCorCategoria = 0;
        for (String categoria : dados.keySet()) {
            plot.setSectionPaint(categoria, paletaCategorias[indiceCorCategoria % paletaCategorias.length]);
            indiceCorCategoria++;
        }

        plot.setLabelGenerator(new org.jfree.chart.labels.StandardPieSectionLabelGenerator(
            "{2}", new java.text.DecimalFormat("0"), new java.text.DecimalFormat("0%")));
        plot.setLabelFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
        plot.setLabelPaint(new java.awt.Color(70, 70, 70));
        plot.setLabelBackgroundPaint(null);
        plot.setLabelOutlinePaint(null);
        plot.setLabelShadowPaint(null);
        plot.setLabelLinkPaint(new java.awt.Color(190, 185, 178));
        plot.setLabelLinkStroke(new java.awt.BasicStroke(1f));

        // Dica ao passar o mouse na fatia: "Espeto: R$ 541,50 (33,1%)"
        plot.setToolTipGenerator(new org.jfree.chart.labels.StandardPieToolTipGenerator(
            "{0}: {1} ({2})", FORMATO_MOEDA, FORMATO_PERCENTUAL));

        org.jfree.chart.JFreeChart chart = new org.jfree.chart.JFreeChart(
            null, org.jfree.chart.JFreeChart.DEFAULT_TITLE_FONT, plot, true);
        chart.setBackgroundPaint(java.awt.Color.WHITE);
        chart.setBorderVisible(false);

        java.awt.Font fonteLegenda = new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 12);
        org.jfree.chart.title.LegendTitle legenda = chart.getLegend();
        legenda.setPosition(org.jfree.chart.ui.RectangleEdge.BOTTOM);
        legenda.setBackgroundPaint(java.awt.Color.WHITE);
        legenda.setItemFont(fonteLegenda);
        legenda.setBorder(0, 0, 0, 0);
        graficoCategoria = chart;

        org.jfree.chart.ChartPanel painel = new org.jfree.chart.ChartPanel(chart);
        painel.setPopupMenu(null);
        configurarDicas(painel);
        painel.setBackground(java.awt.Color.WHITE);
        painel.setMinimumDrawWidth(0);
        painel.setMinimumDrawHeight(0);
        painel.setMaximumDrawWidth(Integer.MAX_VALUE);
        painel.setMaximumDrawHeight(Integer.MAX_VALUE);

        javax.swing.JLabel titulo = new javax.swing.JLabel("Vendas por Categoria");
        titulo.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 15));
        titulo.setForeground(new java.awt.Color(45, 45, 45));
        titulo.setBorder(javax.swing.BorderFactory.createEmptyBorder(18, 20, 10, 20));

        chartCategoriaPanel.removeAll();
        chartCategoriaPanel.setLayout(new java.awt.BorderLayout());
        chartCategoriaPanel.add(titulo, java.awt.BorderLayout.NORTH);
        chartCategoriaPanel.add(painel, java.awt.BorderLayout.CENTER);
        chartCategoriaPanel.revalidate();
        chartCategoriaPanel.repaint();
    }
    
    private void montarGraficoPagamento(java.util.LinkedHashMap<String, java.math.BigDecimal> dados) {
        org.jfree.data.general.DefaultPieDataset<String> dataset = new org.jfree.data.general.DefaultPieDataset<>();
        for (java.util.Map.Entry<String, java.math.BigDecimal> entrada : dados.entrySet()) {
            dataset.setValue(entrada.getKey(), entrada.getValue());
        }

        org.jfree.chart.plot.RingPlot plot = new org.jfree.chart.plot.RingPlot(dataset);
        plot.setSectionDepth(0.35);
        plot.setBackgroundPaint(java.awt.Color.WHITE);
        plot.setOutlineVisible(false);
        plot.setSeparatorsVisible(false);
        plot.setShadowPaint(null);
        plot.setInteriorGap(0.12);

        for (String forma : dados.keySet()) {
            plot.setSectionPaint(forma, corParaFormaPagamento(forma));
        }

        plot.setLabelGenerator(new org.jfree.chart.labels.StandardPieSectionLabelGenerator(
            "{2}", new java.text.DecimalFormat("0"), new java.text.DecimalFormat("0%")));
        plot.setLabelFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
        plot.setLabelPaint(new java.awt.Color(70, 70, 70));
        plot.setLabelBackgroundPaint(null);
        plot.setLabelOutlinePaint(null);
        plot.setLabelShadowPaint(null);
        plot.setLabelLinkPaint(new java.awt.Color(190, 185, 178));
        plot.setLabelLinkStroke(new java.awt.BasicStroke(1f));

        // Dica ao passar o mouse na fatia: "Espeto: R$ 541,50 (33,1%)"
        plot.setToolTipGenerator(new org.jfree.chart.labels.StandardPieToolTipGenerator(
            "{0}: {1} ({2})", FORMATO_MOEDA, FORMATO_PERCENTUAL));

        org.jfree.chart.JFreeChart chart = new org.jfree.chart.JFreeChart(
            null, org.jfree.chart.JFreeChart.DEFAULT_TITLE_FONT, plot, true);
        chart.setBackgroundPaint(java.awt.Color.WHITE);
        chart.setBorderVisible(false);

        java.awt.Font fonteLegenda = new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 12);
        org.jfree.chart.title.LegendTitle legenda = chart.getLegend();
        legenda.setPosition(org.jfree.chart.ui.RectangleEdge.BOTTOM);
        legenda.setBackgroundPaint(java.awt.Color.WHITE);
        legenda.setItemFont(fonteLegenda);
        legenda.setBorder(0, 0, 0, 0);
        graficoPagamento = chart;

        org.jfree.chart.ChartPanel painel = new org.jfree.chart.ChartPanel(chart);
        painel.setPopupMenu(null);
        configurarDicas(painel);
        painel.setBackground(java.awt.Color.WHITE);

        javax.swing.JLabel titulo = new javax.swing.JLabel("Por Forma de Pagamento");
        titulo.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 15));
        titulo.setForeground(new java.awt.Color(45, 45, 45));
        titulo.setBorder(javax.swing.BorderFactory.createEmptyBorder(18, 20, 10, 20));

        chartPagamentoPanel.removeAll();
        chartPagamentoPanel.setLayout(new java.awt.BorderLayout());
        chartPagamentoPanel.add(titulo, java.awt.BorderLayout.NORTH);
        chartPagamentoPanel.add(painel, java.awt.BorderLayout.CENTER);
        chartPagamentoPanel.revalidate();
        chartPagamentoPanel.repaint();
    }

    private java.awt.Color corParaFormaPagamento(String forma) {
        return switch (forma) {
            case "Cartão de Crédito" -> new java.awt.Color(230, 140, 60);
            case "Cartão de Débito" -> new java.awt.Color(178, 58, 38);
            case "Dinheiro" -> new java.awt.Color(25, 100, 25);
            case "Pix" -> new java.awt.Color(45, 110, 150);
            default -> new java.awt.Color(150, 150, 150);
        };
    }

    /**
     * Gráfico de evolução. Quando há período anterior para comparar, ele
     * aparece como uma segunda linha, cinza e tracejada, com legenda embaixo.
     *
     * @param dados             pontos do período atual (rótulo -> faturamento)
     * @param rotulosAnteriores rótulo de cada ponto correspondente no período anterior
     * @param valoresAnteriores faturamento de cada ponto correspondente no período anterior
     * @param descricaoAnterior ex.: "agosto/2026" (null = sem comparação)
     */
    private void montarGraficoEvolucao(LinkedHashMap<String, BigDecimal> dados,
            List<String> rotulosAnteriores, List<BigDecimal> valoresAnteriores, String descricaoAnterior) {

        final String serieAtual = "Período atual";
        final String serieAnterior = "Período anterior" + (descricaoAnterior != null ? " (" + descricaoAnterior + ")" : "");
        final boolean comparar = descricaoAnterior != null && !dados.isEmpty()
                && valoresAnteriores != null && !valoresAnteriores.isEmpty();

        org.jfree.data.category.DefaultCategoryDataset dataset = new org.jfree.data.category.DefaultCategoryDataset();
        int indice = 0;
        for (java.util.Map.Entry<String, BigDecimal> entrada : dados.entrySet()) {
            dataset.addValue(entrada.getValue(), serieAtual, entrada.getKey());
            if (comparar) {
                // null = sem ponto correspondente (ex.: dia 31 num mês de 30): a linha só não é desenhada ali
                BigDecimal anterior = indice < valoresAnteriores.size() ? valoresAnteriores.get(indice) : null;
                dataset.addValue(anterior, serieAnterior, entrada.getKey());
            }
            indice++;
        }

        org.jfree.chart.JFreeChart chart = org.jfree.chart.ChartFactory.createLineChart(
            null, null, null, dataset,
            org.jfree.chart.plot.PlotOrientation.VERTICAL,
            comparar, true, false);

        org.jfree.chart.plot.CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(java.awt.Color.WHITE);
        plot.setOutlineVisible(false);
        plot.setRangeGridlinePaint(new java.awt.Color(225, 220, 213));
        plot.setDomainGridlinesVisible(false);
        // desenha a linha do período atual por cima da tracejada
        plot.setRowRenderingOrder(org.jfree.chart.util.SortOrder.DESCENDING);

        java.awt.Font fonteEixosEvolucao = new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 11);

        org.jfree.chart.axis.NumberAxis eixoValoresEvolucao = (org.jfree.chart.axis.NumberAxis) plot.getRangeAxis();
        eixoValoresEvolucao.setNumberFormatOverride(new java.text.DecimalFormat("'R$'#,##0"));
        eixoValoresEvolucao.setAxisLineVisible(false);
        eixoValoresEvolucao.setTickMarksVisible(false);
        eixoValoresEvolucao.setTickLabelFont(fonteEixosEvolucao);
        eixoValoresEvolucao.setUpperMargin(0.20);

        // Período sem nenhuma venda (linha toda em R$ 0): fixa o eixo em
        // 0–100 em vez da escala automática, que mostrava "-R$0 ... R$0".
        boolean tudoZerado = dados.values().stream().allMatch(valor -> valor == null || valor.signum() == 0)
                && (!comparar || valoresAnteriores.stream().allMatch(valor -> valor == null || valor.signum() == 0));
        if (tudoZerado) {
            eixoValoresEvolucao.setRange(0, 100);
        }

        plot.getDomainAxis().setAxisLineVisible(false);
        plot.getDomainAxis().setTickMarksVisible(false);
        plot.getDomainAxis().setTickLabelFont(fonteEixosEvolucao);
        plot.getDomainAxis().setCategoryLabelPositions(
            org.jfree.chart.axis.CategoryLabelPositions.UP_45);

        org.jfree.chart.renderer.category.LineAndShapeRenderer rendererEvolucao =
            (org.jfree.chart.renderer.category.LineAndShapeRenderer) plot.getRenderer();
        rendererEvolucao.setSeriesPaint(0, new java.awt.Color(25, 100, 25));
        rendererEvolucao.setSeriesStroke(0, new java.awt.BasicStroke(2.5f));
        rendererEvolucao.setSeriesShapesVisible(0, true);
        rendererEvolucao.setSeriesShape(0, new java.awt.geom.Ellipse2D.Double(-3, -3, 6, 6));
        rendererEvolucao.setDefaultItemLabelsVisible(false);
        rendererEvolucao.setUseFillPaint(true);
        rendererEvolucao.setSeriesFillPaint(0, java.awt.Color.WHITE);

        if (comparar) {
            rendererEvolucao.setSeriesPaint(1, new java.awt.Color(160, 150, 140));
            rendererEvolucao.setSeriesStroke(1, new java.awt.BasicStroke(1.8f,
                    java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_ROUND,
                    1f, new float[] {6f, 5f}, 0f));
            rendererEvolucao.setSeriesShapesVisible(1, false);

            org.jfree.chart.title.LegendTitle legenda = chart.getLegend();
            legenda.setPosition(org.jfree.chart.ui.RectangleEdge.BOTTOM);
            legenda.setBackgroundPaint(java.awt.Color.WHITE);
            legenda.setItemFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 11));
            legenda.setBorder(0, 0, 0, 0);
        }

        // Dica ao passar o mouse: "05/09: R$ 120,00 (anterior: R$ 90,00, +33,3%)"
        rendererEvolucao.setDefaultToolTipGenerator((ds, linha, coluna) -> {
            Number valor = ds.getValue(linha, coluna);
            if (valor == null) {
                return null;
            }
            if (linha == 0) {
                String dica = ds.getColumnKey(coluna) + ": " + FORMATO_MOEDA.format(valor);
                if (comparar && coluna < valoresAnteriores.size() && valoresAnteriores.get(coluna) != null) {
                    BigDecimal anterior = valoresAnteriores.get(coluna);
                    String variacao = DadosRelatorio.textoVariacao(new BigDecimal(valor.toString()), anterior);
                    dica += "  (anterior: " + FORMATO_MOEDA.format(anterior)
                            + (variacao != null ? ", " + variacao : "") + ")";
                }
                return dica;
            }
            String rotulo = coluna < rotulosAnteriores.size() ? rotulosAnteriores.get(coluna) : "";
            return serieAnterior + " – " + rotulo + ": " + FORMATO_MOEDA.format(valor);
        });

        chart.setBackgroundPaint(java.awt.Color.WHITE);
        chart.setBorderVisible(false);
        graficoEvolucao = chart;

        org.jfree.chart.ChartPanel painelEvolucao = new org.jfree.chart.ChartPanel(chart);
        painelEvolucao.setPopupMenu(null);
        painelEvolucao.setBackground(java.awt.Color.WHITE);
        configurarDicas(painelEvolucao);

        javax.swing.JLabel tituloEvolucao = new javax.swing.JLabel("Evolução do Faturamento");
        tituloEvolucao.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 15));
        tituloEvolucao.setForeground(new java.awt.Color(45, 45, 45));
        tituloEvolucao.setBorder(javax.swing.BorderFactory.createEmptyBorder(18, 20, 10, 20));

        jPanel18.removeAll();
        jPanel18.setLayout(new java.awt.BorderLayout());
        jPanel18.add(tituloEvolucao, java.awt.BorderLayout.NORTH);
        jPanel18.add(painelEvolucao, java.awt.BorderLayout.CENTER);
        jPanel18.revalidate();
        jPanel18.repaint();
    }

    private void atualizarKpis(ResumoRelatorio resumo, ResumoRelatorio anterior, PeriodoRelatorio periodoAnterior) {
        jLabel9.setText(FORMATO_MOEDA.format(resumo.getFaturamentoTotal()));
        jLabel11.setText(String.valueOf(resumo.getQuantidadeVendas()));
        jLabel13.setText(FORMATO_MOEDA.format(resumo.getTicketMedio()));
        jLabel23.setText(resumo.getFormaPagamentoPrincipal() != null
                ? resumo.getFormaPagamentoPrincipal() : "—");
        jLabelSubtituloPagamento.setText(resumo.getFormaPagamentoPrincipal() != null
                ? "Total: " + FORMATO_MOEDA.format(resumo.getValorFormaPagamentoPrincipal())
                : " ");

        String referencia = periodoAnterior != null ? periodoAnterior.getDescricaoComparacao() : null;
        boolean temAnterior = anterior != null && referencia != null;

        atualizarComparacao(jLabelComparacaoFaturamento, resumo.getFaturamentoTotal(),
                temAnterior ? anterior.getFaturamentoTotal() : null, referencia, true);
        atualizarComparacao(jLabelComparacaoVendas, BigDecimal.valueOf(resumo.getQuantidadeVendas()),
                temAnterior ? BigDecimal.valueOf(anterior.getQuantidadeVendas()) : null, referencia, false);
        atualizarComparacao(jLabelComparacaoTicket, resumo.getTicketMedio(),
                temAnterior ? anterior.getTicketMedio() : null, referencia, true);
    }

    private static final java.awt.Color COR_ALTA = new java.awt.Color(25, 120, 25);
    private static final java.awt.Color COR_QUEDA = new java.awt.Color(190, 55, 35);
    private static final java.awt.Color COR_NEUTRA = new java.awt.Color(140, 140, 140);

    /**
     * Linha de comparação embaixo do valor do cartão, ex.: "▲ 12,3% vs. agosto/2026"
     * (verde se subiu, vermelho se caiu). O tooltip mostra o valor do período anterior.
     */
    private void atualizarComparacao(javax.swing.JLabel label, BigDecimal atual, BigDecimal anterior,
            String referencia, boolean moeda) {
        label.setIcon(null);
        label.setToolTipText(null);

        if (referencia == null || anterior == null) {
            label.setForeground(COR_NEUTRA);
            label.setText(" ");
            return;
        }

        label.setToolTipText("Período anterior (" + referencia + "): "
                + (moeda ? FORMATO_MOEDA.format(anterior) : anterior.toPlainString()));

        BigDecimal variacao = DadosRelatorio.variacaoPercentual(atual, anterior);
        if (variacao == null) {
            label.setForeground(COR_NEUTRA);
            label.setText("sem vendas em " + referencia);
            return;
        }

        // a seta já indica se subiu ou caiu, então o número vai sem sinal
        String percentual = new java.text.DecimalFormat("0.0'%'",
                new java.text.DecimalFormatSymbols(new Locale("pt", "BR"))).format(variacao.abs());
        java.awt.Color cor = variacao.signum() > 0 ? COR_ALTA : variacao.signum() < 0 ? COR_QUEDA : COR_NEUTRA;
        label.setForeground(cor);
        if (variacao.signum() != 0) {
            label.setIcon(criarIconeSeta(variacao.signum() > 0, cor));
        }
        label.setText(percentual + " vs. " + referencia);
    }

    /** Triângulo pequeno (▲ / ▼) desenhado na hora — não depende da fonte ter o símbolo. */
    private javax.swing.Icon criarIconeSeta(boolean paraCima, java.awt.Color cor) {
        return new javax.swing.Icon() {
            @Override
            public void paintIcon(java.awt.Component c, java.awt.Graphics g, int x, int y) {
                java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                g2.translate(x, y);
                g2.setColor(cor);
                int[] xs = {0, 8, 4};
                int[] ys = paraCima ? new int[] {7, 7, 1} : new int[] {1, 1, 7};
                g2.fillPolygon(xs, ys, 3);
                g2.dispose();
            }

            @Override
            public int getIconWidth() {
                return 9;
            }

            @Override
            public int getIconHeight() {
                return 8;
            }
        };
    }

    /** Período selecionado nos filtros da tela. */
    private PeriodoRelatorio periodoAtual() {
        LocalDate hoje = LocalDate.now();

        return switch (tipoPeriodoAtual) {
            case DIARIO -> PeriodoRelatorio.diario(
                    datePickerDiario.getDate() != null ? datePickerDiario.getDate() : hoje);
            case MENSAL -> PeriodoRelatorio.mensal(YearMonth.of(
                    (Integer) comboAnoMensal.getSelectedItem(), comboMes.getSelectedIndex() + 1));
            case ANUAL -> PeriodoRelatorio.anual(hoje);
            case PERSONALIZADO -> PeriodoRelatorio.personalizado(
                    datePickerDe.getDate() != null ? datePickerDe.getDate() : hoje,
                    datePickerAte.getDate() != null ? datePickerAte.getDate() : hoje);
        };
    }

    private void atualizarRelatorios() {
        PeriodoRelatorio periodo = periodoAtual();
        PeriodoRelatorio anterior = periodo.getPeriodoAnterior();
        RelatorioDAO.Granularidade granularidade = periodo.getGranularidade();
        LocalDateTime inicio = periodo.getInicio();
        LocalDateTime fim = periodo.getFim();

        try {
            ResumoRelatorio resumo = relatorioDAO.buscarResumo(inicio, fim);
            ResumoRelatorio resumoAnterior = anterior != null
                    ? relatorioDAO.buscarResumo(anterior.getInicio(), anterior.getFim())
                    : null;
            atualizarKpis(resumo, resumoAnterior, anterior);

            LinkedHashMap<String, BigDecimal> totaisPagamento = relatorioDAO.porFormaPagamento(inicio, fim);
            LinkedHashMap<String, BigDecimal> vendasPorCategoria = relatorioDAO.vendasPorCategoria(inicio, fim);

            montarGraficoRanking(relatorioDAO.rankingProdutos(inicio, fim, 6));
            montarGraficoCategoria(vendasPorCategoria);
            montarGraficoPagamento(totaisPagamento);

            LinkedHashMap<String, BigDecimal> evolucao = montarSerieEvolucao(
                    relatorioDAO.evolucaoFaturamento(inicio, fim, granularidade), periodo, granularidade);

            List<String> rotulosAnteriores = new ArrayList<>();
            List<BigDecimal> valoresAnteriores = new ArrayList<>();
            if (anterior != null) {
                LinkedHashMap<String, BigDecimal> evolucaoAnterior = montarSerieEvolucao(
                        relatorioDAO.evolucaoFaturamento(anterior.getInicio(), anterior.getFim(), granularidade),
                        anterior, granularidade);
                alinharSerieAnterior(evolucao, evolucaoAnterior, granularidade, rotulosAnteriores, valoresAnteriores);
            }
            montarGraficoEvolucao(evolucao, rotulosAnteriores, valoresAnteriores,
                    anterior != null ? anterior.getDescricaoComparacao() : null);

            ultimoPeriodo = periodo;
            ultimoPeriodoAnterior = anterior;
            ultimoResumo = resumo;
            ultimoResumoAnterior = resumoAnterior;
            ultimosTotaisPagamento = totaisPagamento;
            ultimasVendasPorCategoria = vendasPorCategoria;
            ultimaEvolucao = evolucao;
            ultimosRotulosEvolucaoAnterior = rotulosAnteriores;
            ultimaEvolucaoAnterior = valoresAnteriores;

        } catch (RuntimeException erro) {
            // Não deixa uma falha de conexão com o banco travar a tela inteira
            // (o MainScreen cria a tela de Relatórios antecipadamente, mesmo
            // antes do usuário clicar na aba).
            java.util.logging.Logger.getLogger(Relatorio.class.getName())
                    .log(java.util.logging.Level.WARNING, "Falha ao atualizar relatórios.", erro);

            jLabel9.setText("—");
            jLabel11.setText("—");
            jLabel13.setText("—");
            jLabel23.setText("Sem conexão com o banco");
            jLabelSubtituloPagamento.setText(" ");
            atualizarComparacao(jLabelComparacaoFaturamento, null, null, null, true);
            atualizarComparacao(jLabelComparacaoVendas, null, null, null, false);
            atualizarComparacao(jLabelComparacaoTicket, null, null, null, true);

            montarGraficoRanking(new LinkedHashMap<>());
            montarGraficoCategoria(new LinkedHashMap<>());
            montarGraficoPagamento(new LinkedHashMap<>());
            montarGraficoEvolucao(new LinkedHashMap<>(), new ArrayList<>(), new ArrayList<>(), null);

            ultimoPeriodo = null;
            ultimoPeriodoAnterior = null;
            ultimoResumo = null;
            ultimoResumoAnterior = null;
            ultimosTotaisPagamento = new LinkedHashMap<>();
            ultimasVendasPorCategoria = new LinkedHashMap<>();
            ultimaEvolucao = new LinkedHashMap<>();
            ultimosRotulosEvolucaoAnterior = new ArrayList<>();
            ultimaEvolucaoAnterior = new ArrayList<>();
        }
    }

    /**
     * Janela de funcionamento usada no eixo do gráfico de evolução quando ele
     * é por hora: as horas entre a abertura e o fechamento aparecem mesmo sem
     * venda (com R$ 0). Se houver venda fora dessa janela, o eixo se estende
     * para incluí-la.
     */
    private static final int HORA_ABERTURA = 11;
    private static final int HORA_FECHAMENTO = 23;

    /**
     * Monta a série do gráfico de evolução com TODOS os pontos do período
     * (horas, dias ou meses), preenchendo com zero os que não tiveram venda —
     * a consulta só devolve os pontos com venda, o que fazia a linha "pular"
     * (ex.: de 13h direto para 15h). Pontos no futuro não são desenhados,
     * pra linha não despencar a zero depois de "agora".
     *
     * Também deixa os rótulos do eixo X mais legíveis (a consulta traz "14"
     * para hora, "05/09" para dia, "2026-09" para mês).
     */
    private LinkedHashMap<String, BigDecimal> montarSerieEvolucao(LinkedHashMap<String, BigDecimal> bruto,
            PeriodoRelatorio periodo, RelatorioDAO.Granularidade granularidade) {

        LinkedHashMap<String, BigDecimal> formatado = new LinkedHashMap<>();
        LocalDateTime agora = LocalDateTime.now();
        LocalDate hoje = agora.toLocalDate();
        LocalDate primeiroDia = periodo.getInicio().toLocalDate();
        LocalDate ultimoDia = periodo.getFim().toLocalDate().isAfter(hoje) ? hoje : periodo.getFim().toLocalDate();

        if (primeiroDia.isAfter(hoje)) {
            return formatado; // período no futuro: gráfico vazio
        }

        switch (granularidade) {
            case HORA -> {
                int primeiraHora = HORA_ABERTURA;
                int ultimaHora = primeiroDia.equals(hoje)
                        ? Math.min(HORA_FECHAMENTO, agora.getHour())
                        : HORA_FECHAMENTO;
                for (String chave : bruto.keySet()) {
                    int hora = Integer.parseInt(chave.trim());
                    primeiraHora = Math.min(primeiraHora, hora);
                    ultimaHora = Math.max(ultimaHora, hora);
                }

                for (int hora = primeiraHora; hora <= ultimaHora; hora++) {
                    String chave = String.format("%02d", hora);
                    formatado.put(chave + "h", bruto.getOrDefault(chave, BigDecimal.ZERO));
                }
            }
            case DIA -> {
                java.time.format.DateTimeFormatter diaMes = java.time.format.DateTimeFormatter.ofPattern("dd/MM");
                for (LocalDate dia = primeiroDia; !dia.isAfter(ultimoDia); dia = dia.plusDays(1)) {
                    String chave = dia.format(diaMes);
                    formatado.put(chave, bruto.getOrDefault(chave, BigDecimal.ZERO));
                }
            }
            case MES -> {
                Locale ptBr = new Locale("pt", "BR");
                YearMonth ultimoMes = YearMonth.from(ultimoDia);
                for (YearMonth ym = YearMonth.from(primeiroDia); !ym.isAfter(ultimoMes); ym = ym.plusMonths(1)) {
                    String chave = ym.toString(); // "2026-09", mesmo formato da consulta
                    String mesAbrev = ym.getMonth().getDisplayName(TextStyle.SHORT, ptBr);
                    formatado.put(mesAbrev + "/" + ym.getYear(), bruto.getOrDefault(chave, BigDecimal.ZERO));
                }
            }
        }

        return formatado;
    }

    /**
     * Casa cada ponto do período atual com o ponto correspondente do período
     * anterior: por hora, a mesma hora (19h com 19h); por dia ou por mês, a
     * mesma posição (1º dia com 1º dia, 2º mês com 2º mês...).
     */
    private void alinharSerieAnterior(LinkedHashMap<String, BigDecimal> atual,
            LinkedHashMap<String, BigDecimal> anterior, RelatorioDAO.Granularidade granularidade,
            List<String> rotulos, List<BigDecimal> valores) {

        List<java.util.Map.Entry<String, BigDecimal>> pontosAnteriores = new ArrayList<>(anterior.entrySet());
        int indice = 0;
        for (String rotulo : atual.keySet()) {
            if (granularidade == RelatorioDAO.Granularidade.HORA) {
                rotulos.add(rotulo);
                valores.add(anterior.getOrDefault(rotulo, BigDecimal.ZERO));
            } else if (indice < pontosAnteriores.size()) {
                rotulos.add(pontosAnteriores.get(indice).getKey());
                valores.add(pontosAnteriores.get(indice).getValue());
            } else {
                rotulos.add("");
                valores.add(null);
            }
            indice++;
        }
    }

    private void montarSeletorPeriodo() {
        painelPeriodo = new javax.swing.JPanel();
        seletorCardLayout = new java.awt.CardLayout();
        painelPeriodo.setLayout(seletorCardLayout);
        painelPeriodo.setOpaque(false);

        java.awt.Color verdeEscuro = new java.awt.Color(25, 100, 25);
        java.awt.Color verdeClaro = new java.awt.Color(222, 235, 222);
        java.awt.Font fonteControle = new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 13);

        datePickerDiario = new DatePicker(criarConfiguracaoData(verdeEscuro, verdeClaro, false));
        datePickerDiario.setDate(LocalDate.now());
        datePickerDiario.addDateChangeListener(evento -> atualizarRelatorios());
        datePickerDiario.setBorder(javax.swing.BorderFactory.createEmptyBorder(2, 2, 2, 2));

        javax.swing.JPanel painelDiario = criarCartaoSeletor(
                new javax.swing.JLabel(criarIconeCalendario(verdeEscuro)), datePickerDiario);

        String[] nomesMeses = {
            "Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho",
            "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"
        };
        comboMes = new javax.swing.JComboBox<>(nomesMeses);
        comboMes.setSelectedIndex(LocalDate.now().getMonthValue() - 1);
        comboMes.addActionListener(evento -> atualizarRelatorios());
        estilizarComboPeriodo(comboMes, fonteControle, verdeEscuro);

        int anoAtual = LocalDate.now().getYear();
        Integer[] anosDisponiveis = {anoAtual - 2, anoAtual - 1, anoAtual, anoAtual + 1};

        comboAnoMensal = new javax.swing.JComboBox<>(anosDisponiveis);
        comboAnoMensal.setSelectedItem(anoAtual);
        comboAnoMensal.addActionListener(evento -> atualizarRelatorios());
        estilizarComboPeriodo(comboAnoMensal, fonteControle, verdeEscuro);

        javax.swing.JPanel controlesMensal = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 6, 0));
        controlesMensal.setOpaque(false);
        controlesMensal.add(comboMes);
        controlesMensal.add(comboAnoMensal);

        javax.swing.JPanel painelMensal = criarCartaoSeletor(
                new javax.swing.JLabel(criarIconeCalendario(verdeEscuro)), controlesMensal);

        javax.swing.JLabel labelAnual = new javax.swing.JLabel("Últimos 12 meses");
        labelAnual.setFont(fonteControle);
        labelAnual.setForeground(verdeEscuro);

        javax.swing.JPanel painelAnual = criarCartaoSeletor(
                new javax.swing.JLabel(criarIconeTendencia(verdeEscuro)), labelAnual);

        // Personalizado: intervalo livre "De ... até ...". Começa nos últimos 7 dias.
        datePickerDe = new DatePicker(criarConfiguracaoData(verdeEscuro, verdeClaro, true));
        datePickerDe.setDate(LocalDate.now().minusDays(6));
        datePickerDe.setBorder(javax.swing.BorderFactory.createEmptyBorder(2, 2, 2, 2));
        datePickerDe.addDateChangeListener(evento -> aoMudarDataPersonalizada(true));

        datePickerAte = new DatePicker(criarConfiguracaoData(verdeEscuro, verdeClaro, true));
        datePickerAte.setDate(LocalDate.now());
        datePickerAte.setBorder(javax.swing.BorderFactory.createEmptyBorder(2, 2, 2, 2));
        datePickerAte.addDateChangeListener(evento -> aoMudarDataPersonalizada(false));

        javax.swing.JLabel rotuloDe = new javax.swing.JLabel("De");
        rotuloDe.setFont(fonteControle);
        rotuloDe.setForeground(verdeEscuro);
        javax.swing.JLabel rotuloAte = new javax.swing.JLabel("até");
        rotuloAte.setFont(fonteControle);
        rotuloAte.setForeground(verdeEscuro);

        javax.swing.JPanel controlesPersonalizado = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 6, 0));
        controlesPersonalizado.setOpaque(false);
        controlesPersonalizado.add(rotuloDe);
        controlesPersonalizado.add(datePickerDe);
        controlesPersonalizado.add(rotuloAte);
        controlesPersonalizado.add(datePickerAte);

        javax.swing.JPanel painelPersonalizado = criarCartaoSeletor(
                new javax.swing.JLabel(criarIconeCalendario(verdeEscuro)), controlesPersonalizado);

        painelPeriodo.add(painelDiario, "diario");
        painelPeriodo.add(painelMensal, "mensal");
        painelPeriodo.add(painelAnual, "anual");
        painelPeriodo.add(painelPersonalizado, "personalizado");

        jPanel2.removeAll();
        jPanel2.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 8, 4));
        jPanel2.add(btnDiario);
        jPanel2.add(btnMensal);
        jPanel2.add(btnAnual);
        jPanel2.add(btnPersonalizado);
        jPanel2.add(javax.swing.Box.createHorizontalStrut(20));
        jPanel2.add(painelPeriodo);
        jPanel2.add(javax.swing.Box.createHorizontalStrut(20));
        jPanel2.add(criarBotaoExportar());
        jPanel2.revalidate();
        jPanel2.repaint();

        destacarBotaoPeriodo(btnDiario);
    }

    /**
     * Configuração visual dos seletores de data (cada DatePicker precisa da
     * sua própria instância). No Personalizado usa o formato curto
     * dd/MM/aaaa, pra caberem as duas datas na barra de filtros.
     */
    private DatePickerSettings criarConfiguracaoData(java.awt.Color verdeEscuro, java.awt.Color verdeClaro,
            boolean formatoCurto) {
        DatePickerSettings configuracao = new DatePickerSettings(new Locale("pt", "BR"));
        configuracao.setColor(DatePickerSettings.DateArea.CalendarBackgroundSelectedDate, verdeEscuro);
        configuracao.setColor(DatePickerSettings.DateArea.BackgroundTodayLabel, verdeClaro);
        configuracao.setColor(DatePickerSettings.DateArea.TextFieldBackgroundValidDate, java.awt.Color.WHITE);
        configuracao.setColor(DatePickerSettings.DateArea.BackgroundMonthAndYearNavigationButtons, java.awt.Color.WHITE);
        if (formatoCurto) {
            configuracao.setFormatForDatesCommonEra("dd/MM/yyyy");
        }
        return configuracao;
    }

    /**
     * Ao trocar uma das datas do Personalizado: se o início ficar depois do
     * fim (ou o contrário), a outra data acompanha, pra o intervalo nunca
     * ficar invertido. Depois recarrega o relatório.
     */
    private void aoMudarDataPersonalizada(boolean mudouInicio) {
        if (ajustandoDatasPersonalizado) {
            return;
        }
        LocalDate de = datePickerDe.getDate();
        LocalDate ate = datePickerAte.getDate();
        if (de == null || ate == null) {
            return; // campo vazio ou data ainda sendo digitada
        }

        if (de.isAfter(ate)) {
            ajustandoDatasPersonalizado = true;
            try {
                if (mudouInicio) {
                    datePickerAte.setDate(de);
                } else {
                    datePickerDe.setDate(ate);
                }
            } finally {
                ajustandoDatasPersonalizado = false;
            }
        }

        if (tipoPeriodoAtual == PeriodoRelatorio.Tipo.PERSONALIZADO) {
            atualizarRelatorios();
        }
    }

    /**
     * Cartão branco arredondado (mesmo estilo dos cartões de gráfico) que
     * envolve um ícone + o controle de seleção de período, pra destacar o
     * seletor visualmente dentro da barra de filtros.
     *
     * O preenchimento (empty border) nas laterais é igual ao raio do
     * arredondamento do PanelArredondado: como o conteúdo nunca fica mais
     * perto da borda esquerda/direita do que o raio da curva, ele nunca cai
     * dentro da área que a curva do canto recorta — é o que evita tanto o
     * ícone quanto o controle aparecerem cortados/serrilhados no canto.
     */
    private javax.swing.JPanel criarCartaoSeletor(javax.swing.JComponent icone, javax.swing.JComponent controle) {
        int raio = 14;

        javax.swing.JPanel conteudo = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 8, 0));
        conteudo.setOpaque(false);
        conteudo.add(icone);
        conteudo.add(controle);

        // GridBagLayout com GridBagConstraints padrão centraliza o conteúdo
        // tanto na horizontal quanto na vertical, independente de quanto o
        // CardLayout (que mostra só um cartão por vez) estique este cartão
        // para o tamanho do maior card do baralho (o do DatePicker).
        PanelArredondado cartao = new PanelArredondado(raio);
        cartao.setBackground(java.awt.Color.WHITE);
        cartao.setLayout(new java.awt.GridBagLayout());
        cartao.setBorder(javax.swing.BorderFactory.createEmptyBorder(6, raio, 6, raio));
        cartao.add(conteudo, new java.awt.GridBagConstraints());
        return cartao;
    }

    /**
     * Ícones desenhados na hora (em vez de emoji): ficam nítidos em qualquer
     * tamanho, não dependem de fonte de emoji instalada e não sofrem o corte
     * que o glifo colorido do emoji sofria dentro do cartão arredondado.
     */
    private javax.swing.Icon criarIconeCalendario(java.awt.Color cor) {
        return new javax.swing.Icon() {
            @Override
            public void paintIcon(java.awt.Component c, java.awt.Graphics g, int x, int y) {
                java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                g2.translate(x, y);
                g2.setColor(cor);
                g2.setStroke(new java.awt.BasicStroke(1.4f));
                g2.drawRoundRect(1, 3, 15, 13, 3, 3);
                g2.drawLine(1, 7, 16, 7);
                g2.drawLine(5, 1, 5, 4);
                g2.drawLine(12, 1, 12, 4);
                g2.fillRect(10, 10, 3, 3);
                g2.dispose();
            }

            @Override
            public int getIconWidth() {
                return 18;
            }

            @Override
            public int getIconHeight() {
                return 18;
            }
        };
    }

    private javax.swing.Icon criarIconeTendencia(java.awt.Color cor) {
        return new javax.swing.Icon() {
            @Override
            public void paintIcon(java.awt.Component c, java.awt.Graphics g, int x, int y) {
                java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                g2.translate(x, y);
                g2.setColor(cor);
                g2.setStroke(new java.awt.BasicStroke(1.6f, java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_ROUND));
                int[] xs = {1, 6, 10, 16};
                int[] ys = {13, 8, 11, 2};
                g2.drawPolyline(xs, ys, xs.length);
                g2.fillOval(xs[3] - 2, ys[3] - 2, 4, 4);
                g2.dispose();
            }

            @Override
            public int getIconWidth() {
                return 18;
            }

            @Override
            public int getIconHeight() {
                return 18;
            }
        };
    }

    private javax.swing.Icon criarIconeDocumento(java.awt.Color cor) {
        return new javax.swing.Icon() {
            @Override
            public void paintIcon(java.awt.Component c, java.awt.Graphics g, int x, int y) {
                java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                g2.translate(x, y);
                g2.setColor(cor);
                g2.setStroke(new java.awt.BasicStroke(1.4f));
                g2.drawRoundRect(2, 1, 13, 16, 3, 3);
                g2.drawLine(5, 6, 14, 6);
                g2.drawLine(5, 9, 14, 9);
                g2.drawLine(5, 12, 11, 12);
                g2.dispose();
            }

            @Override
            public int getIconWidth() {
                return 18;
            }

            @Override
            public int getIconHeight() {
                return 18;
            }
        };
    }

    /**
     * Aplica o selo colorido (ícone dentro de um quadrado verde
     * arredondado, no canto superior direito) nos 4 cartões de KPI do topo
     * da tela, no mesmo espírito do que já é feito para o seletor de
     * período: em vez de editar o initComponents() gerado pelo Form
     * Editor, o layout de cada cartão é reconstruído em tempo de execução.
     */
    private void adicionarBadgesKpi() {
        java.awt.Color verdeBadge = new java.awt.Color(25, 100, 25);

        // linha de comparação com o período anterior (ex.: "▲ 12,3% vs. agosto/2026")
        jLabelComparacaoFaturamento = criarLabelComparacao();
        jLabelComparacaoVendas = criarLabelComparacao();
        jLabelComparacaoTicket = criarLabelComparacao();

        configurarCartaoKpi(jPanel10, jLabel8, jLabel9, jLabelComparacaoFaturamento,
                criarIconeCifrao(java.awt.Color.WHITE), verdeBadge);
        configurarCartaoKpi(jPanel11, jLabel10, jLabel11, jLabelComparacaoVendas,
                criarIconeSacola(java.awt.Color.WHITE), verdeBadge);
        configurarCartaoKpi(jPanel12, jLabel12, jLabel13, jLabelComparacaoTicket,
                criarIconeTendencia(java.awt.Color.WHITE), verdeBadge);

        jLabelSubtituloPagamento = new javax.swing.JLabel(" ");
        jLabelSubtituloPagamento.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 11));
        jLabelSubtituloPagamento.setForeground(new java.awt.Color(140, 140, 140));

        configurarCartaoKpi(jPanel23, jLabel22, jLabel23, jLabelSubtituloPagamento,
                criarIconeCartao(java.awt.Color.WHITE), verdeBadge);
    }

    /**
     * Troca o GroupLayout gerado pelo Form Editor (título em cima, valor
     * embaixo, alinhados à esquerda) por um GridBagLayout equivalente,
     * acrescentando uma coluna à direita só para o selo. A coluna de texto
     * fica com weightx = 1 (ocupa todo o espaço sobrando) e a do selo com
     * weightx = 0 e âncora NORTHEAST, o que empurra o selo pro canto
     * superior direito do cartão.
     */
    private void configurarCartaoKpi(javax.swing.JPanel painel, javax.swing.JLabel rotulo,
            javax.swing.JLabel valor, javax.swing.JLabel subtitulo,
            javax.swing.Icon icone, java.awt.Color corBadge) {

        painel.removeAll();
        painel.setLayout(new java.awt.GridBagLayout());

        javax.swing.JPanel textoPanel = new javax.swing.JPanel();
        textoPanel.setOpaque(false);
        textoPanel.setLayout(new javax.swing.BoxLayout(textoPanel, javax.swing.BoxLayout.Y_AXIS));
        rotulo.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        valor.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        textoPanel.add(rotulo);
        textoPanel.add(javax.swing.Box.createVerticalStrut(4));
        textoPanel.add(valor);
        if (subtitulo != null) {
            subtitulo.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
            textoPanel.add(javax.swing.Box.createVerticalStrut(2));
            textoPanel.add(subtitulo);
        }

        java.awt.GridBagConstraints gbcTexto = new java.awt.GridBagConstraints();
        gbcTexto.gridx = 0;
        gbcTexto.gridy = 0;
        gbcTexto.weightx = 1;
        gbcTexto.weighty = 1;
        gbcTexto.anchor = java.awt.GridBagConstraints.WEST;
        gbcTexto.insets = new java.awt.Insets(14, 14, 14, 4);
        painel.add(textoPanel, gbcTexto);

        javax.swing.JPanel badge = criarBadgeIcone(icone, corBadge);
        java.awt.GridBagConstraints gbcBadge = new java.awt.GridBagConstraints();
        gbcBadge.gridx = 1;
        gbcBadge.gridy = 0;
        gbcBadge.weightx = 0;
        gbcBadge.weighty = 1;
        gbcBadge.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gbcBadge.insets = new java.awt.Insets(10, 4, 4, 12);
        painel.add(badge, gbcBadge);

        painel.revalidate();
        painel.repaint();
    }

    private javax.swing.JLabel criarLabelComparacao() {
        javax.swing.JLabel label = new javax.swing.JLabel(" ");
        label.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 11));
        label.setForeground(COR_NEUTRA);
        label.setIconTextGap(4);
        return label;
    }

    private javax.swing.JPanel criarBadgeIcone(javax.swing.Icon icone, java.awt.Color corFundo) {
        int tamanho = 32;
        PanelArredondado badge = new PanelArredondado(9);
        badge.setBackground(corFundo);
        badge.setPreferredSize(new java.awt.Dimension(tamanho, tamanho));
        badge.setMinimumSize(new java.awt.Dimension(tamanho, tamanho));
        badge.setLayout(new java.awt.GridBagLayout());
        badge.add(new javax.swing.JLabel(icone), new java.awt.GridBagConstraints());
        return badge;
    }

    private javax.swing.Icon criarIconeCifrao(java.awt.Color cor) {
        return new javax.swing.Icon() {
            @Override
            public void paintIcon(java.awt.Component c, java.awt.Graphics g, int x, int y) {
                java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                g2.translate(x, y);
                g2.setColor(cor);
                g2.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 15));
                java.awt.FontMetrics fm = g2.getFontMetrics();
                String texto = "$";
                int tx = (getIconWidth() - fm.stringWidth(texto)) / 2;
                int ty = (getIconHeight() + fm.getAscent() - fm.getDescent()) / 2 - 1;
                g2.drawString(texto, tx, ty);
                g2.dispose();
            }

            @Override
            public int getIconWidth() {
                return 18;
            }

            @Override
            public int getIconHeight() {
                return 18;
            }
        };
    }

    private javax.swing.Icon criarIconeSacola(java.awt.Color cor) {
        return new javax.swing.Icon() {
            @Override
            public void paintIcon(java.awt.Component c, java.awt.Graphics g, int x, int y) {
                java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                g2.translate(x, y);
                g2.setColor(cor);
                g2.setStroke(new java.awt.BasicStroke(1.6f, java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_ROUND));
                int[] xsCorpo = {3, 15, 14, 4};
                int[] ysCorpo = {6, 6, 16, 16};
                g2.drawPolygon(xsCorpo, ysCorpo, 4);
                g2.drawArc(6, 1, 6, 8, 0, 180);
                g2.dispose();
            }

            @Override
            public int getIconWidth() {
                return 18;
            }

            @Override
            public int getIconHeight() {
                return 18;
            }
        };
    }

    private javax.swing.Icon criarIconeCartao(java.awt.Color cor) {
        return new javax.swing.Icon() {
            @Override
            public void paintIcon(java.awt.Component c, java.awt.Graphics g, int x, int y) {
                java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                g2.translate(x, y);
                g2.setColor(cor);
                g2.setStroke(new java.awt.BasicStroke(1.5f, java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_ROUND));
                g2.drawRoundRect(2, 4, 14, 10, 3, 3);
                g2.fillRect(2, 7, 14, 3);
                g2.dispose();
            }

            @Override
            public int getIconWidth() {
                return 18;
            }

            @Override
            public int getIconHeight() {
                return 18;
            }
        };
    }

    private void estilizarComboPeriodo(javax.swing.JComboBox<?> combo, java.awt.Font fonte, java.awt.Color corTexto) {
        combo.setFont(fonte);
        combo.setForeground(corTexto);
        combo.setBackground(java.awt.Color.WHITE);
        combo.putClientProperty("JComponent.arc", 12);
        combo.setBorder(javax.swing.BorderFactory.createEmptyBorder(2, 6, 2, 6));
    }

    // =====================================================================
    // Exportação (PDF e Excel)
    // =====================================================================

    /** Formatos de arquivo oferecidos no botão "Exportar". */
    private enum FormatoExportacao {
        PDF("PDF", "pdf", "Arquivo PDF (*.pdf)", "Abrir PDF"),
        EXCEL("Excel", "xlsx", "Planilha do Excel (*.xlsx)", "Abrir planilha");

        private final String nome;
        private final String extensao;
        private final String descricaoFiltro;
        private final String textoAbrir;

        FormatoExportacao(String nome, String extensao, String descricaoFiltro, String textoAbrir) {
            this.nome = nome;
            this.extensao = extensao;
            this.descricaoFiltro = descricaoFiltro;
            this.textoAbrir = textoAbrir;
        }
    }

    private static final String TEXTO_BOTAO_EXPORTAR = "Exportar";

    /** Pasta usada na última exportação (o seletor de arquivo reabre nela). */
    private java.io.File ultimaPastaExportacao;

    /**
     * Botão "Exportar" que fica na barra de filtros, logo depois do seletor
     * de período. Ao clicar, abre um menu com as opções PDF e Excel. Criado
     * em tempo de execução (como o seletor), pra não mexer no
     * initComponents() gerado pelo Form Editor.
     */
    private javax.swing.JButton criarBotaoExportar() {
        java.awt.Color verdeEscuro = new java.awt.Color(25, 100, 25);

        btnExportar = new javax.swing.JButton(TEXTO_BOTAO_EXPORTAR, criarIconeDownload(java.awt.Color.WHITE));
        btnExportar.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14));
        btnExportar.setForeground(java.awt.Color.WHITE);
        btnExportar.setBackground(new java.awt.Color(210, 84, 43));
        btnExportar.setIconTextGap(8);
        btnExportar.setMargin(new java.awt.Insets(6, 16, 6, 16));
        btnExportar.setFocusPainted(false);
        btnExportar.setRequestFocusEnabled(false);
        btnExportar.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
        btnExportar.setToolTipText("Exporta o relatório do período selecionado em PDF ou Excel");
        btnExportar.putClientProperty("JButton.buttonType", "roundRect");

        java.awt.Font fonteMenu = new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 13);

        javax.swing.JMenuItem itemPdf = new javax.swing.JMenuItem(
                "PDF — relatório com gráficos (.pdf)", criarIconeDocumento(verdeEscuro));
        itemPdf.setFont(fonteMenu);
        itemPdf.addActionListener(evento -> exportar(FormatoExportacao.PDF));

        javax.swing.JMenuItem itemExcel = new javax.swing.JMenuItem(
                "Excel — planilha com os dados (.xlsx)", criarIconeTabela(verdeEscuro));
        itemExcel.setFont(fonteMenu);
        itemExcel.addActionListener(evento -> exportar(FormatoExportacao.EXCEL));

        javax.swing.JPopupMenu menuExportar = new javax.swing.JPopupMenu();
        menuExportar.add(itemPdf);
        menuExportar.add(itemExcel);

        btnExportar.addActionListener(evento ->
                menuExportar.show(btnExportar, 0, btnExportar.getHeight() + 4));
        return btnExportar;
    }

    /**
     * Fluxo da exportação:
     * 1) confere se há dados carregados; 2) pergunta onde salvar;
     * 3) na thread do Swing, copia KPIs, comparação e gráficos que estão na
     *    tela; 4) em segundo plano (SwingWorker), busca as tabelas de
     *    detalhamento no banco e grava o arquivo, sem travar a tela;
     * 5) oferece abrir o arquivo.
     */
    private void exportar(FormatoExportacao formato) {
        if (ultimoResumo == null || ultimoPeriodo == null) {
            javax.swing.JOptionPane.showMessageDialog(this,
                    "Não foi possível carregar os dados do relatório (sem conexão com o banco).\n"
                    + "Verifique a conexão e tente novamente.",
                    "Exportar " + formato.nome, javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (ultimoResumo.getQuantidadeVendas() == 0) {
            int opcao = javax.swing.JOptionPane.showConfirmDialog(this,
                    "Não há vendas no período selecionado.\nDeseja gerar o arquivo mesmo assim?",
                    "Exportar " + formato.nome, javax.swing.JOptionPane.YES_NO_OPTION,
                    javax.swing.JOptionPane.QUESTION_MESSAGE);
            if (opcao != javax.swing.JOptionPane.YES_OPTION) {
                return;
            }
        }

        java.io.File arquivo = escolherArquivo(formato);
        if (arquivo == null) {
            return;
        }

        final DadosRelatorio dados = prepararDados();
        final RelatorioPdfExporter pdf = formato == FormatoExportacao.PDF ? prepararPdf(dados) : null;

        btnExportar.setEnabled(false);
        btnExportar.setText("Gerando " + formato.nome + "...");
        setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.WAIT_CURSOR));

        new javax.swing.SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                carregarDetalhamento(dados, formato);
                if (pdf != null) {
                    pdf.exportar(arquivo);
                } else {
                    new RelatorioExcelExporter(dados).exportar(arquivo);
                }
                return null;
            }

            @Override
            protected void done() {
                btnExportar.setEnabled(true);
                btnExportar.setText(TEXTO_BOTAO_EXPORTAR);
                setCursor(java.awt.Cursor.getDefaultCursor());

                try {
                    get();
                    oferecerAbrirArquivo(arquivo, formato);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (java.util.concurrent.ExecutionException e) {
                    Throwable causa = e.getCause() != null ? e.getCause() : e;
                    java.util.logging.Logger.getLogger(Relatorio.class.getName())
                            .log(java.util.logging.Level.SEVERE, "Falha ao exportar " + formato.nome + ".", causa);

                    String mensagem = causa instanceof java.io.FileNotFoundException
                            ? "Não foi possível salvar o arquivo.\n"
                              + "Se ele estiver aberto em outro programa (ex.: Excel ou leitor de PDF), feche-o e tente novamente."
                            : "Erro ao gerar o arquivo:\n" + causa.getMessage();
                    javax.swing.JOptionPane.showMessageDialog(Relatorio.this, mensagem,
                            "Exportar " + formato.nome, javax.swing.JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    /**
     * Copia para o DadosRelatorio o que está na tela agora (período,
     * comparação, KPIs, formas de pagamento, categorias e evolução).
     */
    private DadosRelatorio prepararDados() {
        DadosRelatorio dados = new DadosRelatorio(ultimoPeriodo);
        if (SessaoUsuario.getUsuarioLogado() != null) {
            dados.setGeradoPor(SessaoUsuario.getUsuarioLogado().getNome());
        }
        dados.setResumo(ultimoResumo);
        dados.setPeriodoAnterior(ultimoPeriodoAnterior);
        dados.setResumoAnterior(ultimoResumoAnterior);
        dados.setTotaisPorFormaPagamento(new LinkedHashMap<>(ultimosTotaisPagamento));
        dados.setVendasPorCategoria(new LinkedHashMap<>(ultimasVendasPorCategoria));
        dados.setEvolucao(new LinkedHashMap<>(ultimaEvolucao));
        dados.setEvolucaoAnterior(new ArrayList<>(ultimosRotulosEvolucaoAnterior),
                new ArrayList<>(ultimaEvolucaoAnterior));
        return dados;
    }

    /**
     * Cria o exportador de PDF já com os 4 gráficos da tela. Precisa rodar
     * na thread do Swing, porque os gráficos são desenhados em imagem neste
     * momento.
     */
    private RelatorioPdfExporter prepararPdf(DadosRelatorio dados) {
        RelatorioPdfExporter pdf = new RelatorioPdfExporter(dados);
        pdf.setGraficoRanking(graficoRanking);
        pdf.setGraficoCategoria(graficoCategoria);
        pdf.setGraficoPagamento(graficoPagamento);
        pdf.setGraficoEvolucao(graficoEvolucao);
        return pdf;
    }

    /**
     * Busca no banco as tabelas de detalhamento, que mudam conforme o
     * tamanho do período. Roda em segundo plano (dentro do SwingWorker).
     * No Excel a lista de produtos vai completa; no PDF, só os 15 primeiros
     * (exceto no relatório de um dia, que lista todos).
     */
    private void carregarDetalhamento(DadosRelatorio dados, FormatoExportacao formato) {
        LocalDateTime inicio = dados.getPeriodo().getInicio();
        LocalDateTime fim = dados.getPeriodo().getFim();
        int limiteProdutos = formato == FormatoExportacao.EXCEL ? 1000 : 15;

        switch (dados.getDetalhamento()) {
            case POR_VENDA -> {
                // um dia: cada venda + todos os produtos vendidos
                dados.setVendas(relatorioDAO.listarVendasDetalhadas(inicio, fim));
                dados.setProdutos(relatorioDAO.produtosVendidos(inicio, fim, 1000));
            }
            case POR_SEMANA -> {
                dados.setResumoPeriodos(relatorioDAO.resumoSemanal(inicio, fim));
                dados.setProdutos(relatorioDAO.produtosVendidos(inicio, fim, limiteProdutos));
            }
            case POR_MES -> {
                dados.setResumoPeriodos(relatorioDAO.resumoMensal(inicio, fim));
                dados.setProdutos(relatorioDAO.produtosVendidos(inicio, fim, limiteProdutos));
            }
        }
    }

    private java.io.File escolherArquivo(FormatoExportacao formato) {
        javax.swing.JFileChooser seletor = new javax.swing.JFileChooser();
        seletor.setDialogTitle("Salvar relatório em " + formato.nome);
        seletor.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                formato.descricaoFiltro, formato.extensao));
        seletor.setAcceptAllFileFilterUsed(false);
        if (ultimaPastaExportacao != null && ultimaPastaExportacao.isDirectory()) {
            seletor.setCurrentDirectory(ultimaPastaExportacao);
        }
        seletor.setSelectedFile(new java.io.File(seletor.getCurrentDirectory(),
                ultimoPeriodo.getNomeArquivo(formato.extensao)));

        while (true) {
            if (seletor.showSaveDialog(this) != javax.swing.JFileChooser.APPROVE_OPTION) {
                return null;
            }

            java.io.File arquivo = seletor.getSelectedFile();
            if (!arquivo.getName().toLowerCase(Locale.ROOT).endsWith("." + formato.extensao)) {
                arquivo = new java.io.File(arquivo.getParentFile(), arquivo.getName() + "." + formato.extensao);
            }

            if (arquivo.exists()) {
                int opcao = javax.swing.JOptionPane.showConfirmDialog(this,
                        "O arquivo \"" + arquivo.getName() + "\" já existe.\nDeseja substituí-lo?",
                        "Exportar " + formato.nome, javax.swing.JOptionPane.YES_NO_OPTION,
                        javax.swing.JOptionPane.WARNING_MESSAGE);
                if (opcao != javax.swing.JOptionPane.YES_OPTION) {
                    continue;
                }
            }

            ultimaPastaExportacao = arquivo.getParentFile();
            return arquivo;
        }
    }

    private void oferecerAbrirArquivo(java.io.File arquivo, FormatoExportacao formato) {
        Object[] opcoes = {formato.textoAbrir, "Abrir pasta", "Fechar"};
        int escolha = javax.swing.JOptionPane.showOptionDialog(this,
                "Relatório salvo com sucesso em:\n" + arquivo.getAbsolutePath(),
                "Exportar " + formato.nome, javax.swing.JOptionPane.DEFAULT_OPTION,
                javax.swing.JOptionPane.INFORMATION_MESSAGE, null, opcoes, opcoes[0]);

        if (escolha != 0 && escolha != 1) {
            return;
        }
        try {
            java.io.File alvo = escolha == 0 ? arquivo : arquivo.getParentFile();
            if (java.awt.Desktop.isDesktopSupported()) {
                java.awt.Desktop.getDesktop().open(alvo);
            }
        } catch (java.io.IOException | UnsupportedOperationException | IllegalArgumentException e) {
            javax.swing.JOptionPane.showMessageDialog(this,
                    "Não foi possível abrir automaticamente. O arquivo está em:\n" + arquivo.getAbsolutePath(),
                    "Exportar " + formato.nome, javax.swing.JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /** Ícone de planilha (grade) do item "Excel" no menu Exportar. */
    private javax.swing.Icon criarIconeTabela(java.awt.Color cor) {
        return new javax.swing.Icon() {
            @Override
            public void paintIcon(java.awt.Component c, java.awt.Graphics g, int x, int y) {
                java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                g2.translate(x, y);
                g2.setColor(cor);
                g2.setStroke(new java.awt.BasicStroke(1.4f));
                g2.drawRoundRect(1, 2, 15, 14, 3, 3);
                g2.drawLine(1, 7, 16, 7);
                g2.drawLine(1, 11, 16, 11);
                g2.drawLine(6, 2, 6, 16);
                g2.dispose();
            }

            @Override
            public int getIconWidth() {
                return 18;
            }

            @Override
            public int getIconHeight() {
                return 18;
            }
        };
    }

    private javax.swing.Icon criarIconeDownload(java.awt.Color cor) {
        return new javax.swing.Icon() {
            @Override
            public void paintIcon(java.awt.Component c, java.awt.Graphics g, int x, int y) {
                java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                g2.translate(x, y);
                g2.setColor(cor);
                g2.setStroke(new java.awt.BasicStroke(1.7f, java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_ROUND));
                g2.drawLine(9, 2, 9, 11);
                g2.drawPolyline(new int[] {5, 9, 13}, new int[] {7, 11, 7}, 3);
                g2.drawPolyline(new int[] {2, 2, 16, 16}, new int[] {12, 16, 16, 12}, 4);
                g2.dispose();
            }

            @Override
            public int getIconWidth() {
                return 18;
            }

            @Override
            public int getIconHeight() {
                return 18;
            }
        };
    }

    private void destacarBotaoPeriodo(javax.swing.JButton botaoAtivo) {
        java.awt.Color verdeEscuro = new java.awt.Color(25, 100, 25);
        javax.swing.JButton[] botoesPeriodo = {btnDiario, btnMensal, btnAnual, btnPersonalizado};

        for (javax.swing.JButton botao : botoesPeriodo) {
            boolean ativo = botao == botaoAtivo;
            botao.setForeground(ativo ? java.awt.Color.WHITE : verdeEscuro);
            botao.setBackground(ativo ? verdeEscuro : java.awt.Color.WHITE);
            botao.setContentAreaFilled(true);
            botao.setOpaque(true);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        btnDiario = new javax.swing.JButton();
        btnAnual = new javax.swing.JButton();
        btnPersonalizado = new javax.swing.JButton();
        btnMensal = new javax.swing.JButton();
        jPanel14 = new javax.swing.JPanel();
        chartRankingPanel = new PanelArredondado(14);
        chartCategoriaPanel = new PanelArredondado(14);
        chartPagamentoPanel = new PanelArredondado(14);
        jPanel18 = new PanelArredondado(14);
        jPanel19 = new javax.swing.JPanel();
        jPanel10 = new PanelArredondado(14);
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jPanel11 = new PanelArredondado(14);
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jPanel12 = new PanelArredondado(14);
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jPanel23 = new PanelArredondado(14);
        jLabel22 = new javax.swing.JLabel();
        jLabel23 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();

        setBackground(new java.awt.Color(238, 232, 227));
        setMaximumSize(new java.awt.Dimension(1300, 1080));
        setMinimumSize(new java.awt.Dimension(1300, 1080));
        setPreferredSize(new java.awt.Dimension(1300, 1080));

        jScrollPane1.setBorder(null);

        jPanel1.setBackground(new java.awt.Color(238, 232, 227));
        jPanel1.setPreferredSize(new java.awt.Dimension(1300, 1080));

        jPanel2.setBackground(new java.awt.Color(238, 232, 227));
        jPanel2.setAlignmentX(0.0F);
        jPanel2.setAlignmentY(0.0F);

        btnDiario.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnDiario.setForeground(new java.awt.Color(25, 100, 25));
        btnDiario.setText("Diário");
        btnDiario.setActionCommand("");
        btnDiario.setAlignmentY(0.0F);
        btnDiario.setMargin(new java.awt.Insets(2, 15, 2, 15));
        btnDiario.setRequestFocusEnabled(false);
        btnDiario.addActionListener(this::btnDiarioActionPerformed);

        btnAnual.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnAnual.setForeground(new java.awt.Color(25, 100, 25));
        btnAnual.setText("Anual");
        btnAnual.setActionCommand("");
        btnAnual.setMargin(new java.awt.Insets(2, 15, 2, 15));
        btnAnual.setRequestFocusEnabled(false);
        btnAnual.addActionListener(this::btnAnualActionPerformed);

        btnPersonalizado.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnPersonalizado.setForeground(new java.awt.Color(25, 100, 25));
        btnPersonalizado.setText("Personalizado");
        btnPersonalizado.setActionCommand("");
        btnPersonalizado.setMargin(new java.awt.Insets(2, 15, 2, 15));
        btnPersonalizado.setRequestFocusEnabled(false);
        btnPersonalizado.addActionListener(this::btnPersonalizadoActionPerformed);

        btnMensal.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnMensal.setForeground(new java.awt.Color(25, 100, 25));
        btnMensal.setText("Mensal");
        btnMensal.setActionCommand("");
        btnMensal.setMargin(new java.awt.Insets(2, 15, 2, 15));
        btnMensal.setRequestFocusEnabled(false);
        btnMensal.addActionListener(this::btnMensalActionPerformed);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(btnDiario, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnMensal, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnAnual, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnPersonalizado, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(716, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnDiario, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnAnual, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnPersonalizado, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnMensal, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(9, Short.MAX_VALUE))
        );

        jPanel14.setBackground(new java.awt.Color(238, 232, 227));
        jPanel14.setPreferredSize(new java.awt.Dimension(1208, 819));

        chartRankingPanel.setBackground(new java.awt.Color(255, 255, 255));
        chartRankingPanel.setPreferredSize(new java.awt.Dimension(589, 0));

        javax.swing.GroupLayout chartRankingPanelLayout = new javax.swing.GroupLayout(chartRankingPanel);
        chartRankingPanel.setLayout(chartRankingPanelLayout);
        chartRankingPanelLayout.setHorizontalGroup(
            chartRankingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 589, Short.MAX_VALUE)
        );
        chartRankingPanelLayout.setVerticalGroup(
            chartRankingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 340, Short.MAX_VALUE)
        );

        chartCategoriaPanel.setBackground(new java.awt.Color(255, 255, 255));
        chartCategoriaPanel.setPreferredSize(new java.awt.Dimension(589, 340));

        javax.swing.GroupLayout chartCategoriaPanelLayout = new javax.swing.GroupLayout(chartCategoriaPanel);
        chartCategoriaPanel.setLayout(chartCategoriaPanelLayout);
        chartCategoriaPanelLayout.setHorizontalGroup(
            chartCategoriaPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 589, Short.MAX_VALUE)
        );
        chartCategoriaPanelLayout.setVerticalGroup(
            chartCategoriaPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 340, Short.MAX_VALUE)
        );

        chartPagamentoPanel.setBackground(new java.awt.Color(255, 255, 255));
        chartPagamentoPanel.setPreferredSize(new java.awt.Dimension(589, 340));
        chartPagamentoPanel.setRequestFocusEnabled(false);

        javax.swing.GroupLayout chartPagamentoPanelLayout = new javax.swing.GroupLayout(chartPagamentoPanel);
        chartPagamentoPanel.setLayout(chartPagamentoPanelLayout);
        chartPagamentoPanelLayout.setHorizontalGroup(
            chartPagamentoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 589, Short.MAX_VALUE)
        );
        chartPagamentoPanelLayout.setVerticalGroup(
            chartPagamentoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 340, Short.MAX_VALUE)
        );

        jPanel18.setBackground(new java.awt.Color(255, 255, 255));
        jPanel18.setPreferredSize(new java.awt.Dimension(589, 340));

        javax.swing.GroupLayout jPanel18Layout = new javax.swing.GroupLayout(jPanel18);
        jPanel18.setLayout(jPanel18Layout);
        jPanel18Layout.setHorizontalGroup(
            jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 589, Short.MAX_VALUE)
        );
        jPanel18Layout.setVerticalGroup(
            jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 340, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jPanel14Layout = new javax.swing.GroupLayout(jPanel14);
        jPanel14.setLayout(jPanel14Layout);
        jPanel14Layout.setHorizontalGroup(
            jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel14Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(chartPagamentoPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(chartRankingPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(chartCategoriaPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel18, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(35, Short.MAX_VALUE))
        );
        jPanel14Layout.setVerticalGroup(
            jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel14Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(chartCategoriaPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(chartRankingPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 340, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(24, 24, 24)
                .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(chartPagamentoPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel18, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(109, Short.MAX_VALUE))
        );

        jPanel19.setBackground(new java.awt.Color(238, 232, 227));

        jPanel10.setBackground(new java.awt.Color(255, 255, 255));
        jPanel10.setForeground(new java.awt.Color(255, 255, 255));
        jPanel10.setPreferredSize(new java.awt.Dimension(285, 82));
        jPanel10.setRequestFocusEnabled(false);
        jPanel10.setVerifyInputWhenFocusTarget(false);

        jLabel8.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel8.setForeground(new java.awt.Color(102, 102, 102));
        jLabel8.setText("Faturamento Total");

        jLabel9.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel9.setText("R$ 114,00");

        javax.swing.GroupLayout jPanel10Layout = new javax.swing.GroupLayout(jPanel10);
        jPanel10.setLayout(jPanel10Layout);
        jPanel10Layout.setHorizontalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel8)
                    .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, 195, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(76, Short.MAX_VALUE))
        );
        jPanel10Layout.setVerticalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addComponent(jLabel8)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(34, Short.MAX_VALUE))
        );

        jPanel11.setBackground(new java.awt.Color(255, 255, 255));
        jPanel11.setForeground(new java.awt.Color(255, 255, 255));
        jPanel11.setPreferredSize(new java.awt.Dimension(285, 82));
        jPanel11.setRequestFocusEnabled(false);
        jPanel11.setVerifyInputWhenFocusTarget(false);

        jLabel10.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel10.setForeground(new java.awt.Color(102, 102, 102));
        jLabel10.setText("Quantidade de Vendas");

        jLabel11.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel11.setText("10");

        javax.swing.GroupLayout jPanel11Layout = new javax.swing.GroupLayout(jPanel11);
        jPanel11.setLayout(jPanel11Layout);
        jPanel11Layout.setHorizontalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel11Layout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, 195, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, 164, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(76, Short.MAX_VALUE))
        );
        jPanel11Layout.setVerticalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel11Layout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addComponent(jLabel10)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel12.setBackground(new java.awt.Color(255, 255, 255));
        jPanel12.setForeground(new java.awt.Color(255, 255, 255));
        jPanel12.setPreferredSize(new java.awt.Dimension(285, 82));
        jPanel12.setRequestFocusEnabled(false);
        jPanel12.setVerifyInputWhenFocusTarget(false);

        jLabel12.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel12.setForeground(new java.awt.Color(102, 102, 102));
        jLabel12.setText("Ticket Médio");

        jLabel13.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel13.setText("R$ 28,00");

        javax.swing.GroupLayout jPanel12Layout = new javax.swing.GroupLayout(jPanel12);
        jPanel12.setLayout(jPanel12Layout);
        jPanel12Layout.setHorizontalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel12Layout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, 195, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel12))
                .addContainerGap(76, Short.MAX_VALUE))
        );
        jPanel12Layout.setVerticalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel12Layout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addComponent(jLabel12)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(34, Short.MAX_VALUE))
        );

        jPanel23.setBackground(new java.awt.Color(255, 255, 255));
        jPanel23.setForeground(new java.awt.Color(255, 255, 255));
        jPanel23.setPreferredSize(new java.awt.Dimension(285, 82));
        jPanel23.setRequestFocusEnabled(false);
        jPanel23.setVerifyInputWhenFocusTarget(false);

        jLabel22.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel22.setForeground(new java.awt.Color(102, 102, 102));
        jLabel22.setText("Pagamento Principal");

        jLabel23.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel23.setText("Cartão de Crédito");

        javax.swing.GroupLayout jPanel23Layout = new javax.swing.GroupLayout(jPanel23);
        jPanel23.setLayout(jPanel23Layout);
        jPanel23Layout.setHorizontalGroup(
            jPanel23Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel23Layout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addGroup(jPanel23Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel23, javax.swing.GroupLayout.PREFERRED_SIZE, 195, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel22))
                .addContainerGap(78, Short.MAX_VALUE))
        );
        jPanel23Layout.setVerticalGroup(
            jPanel23Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel23Layout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addComponent(jLabel22)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel23, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel19Layout = new javax.swing.GroupLayout(jPanel19);
        jPanel19.setLayout(jPanel19Layout);
        jPanel19Layout.setHorizontalGroup(
            jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel19Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jPanel11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jPanel12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jPanel23, javax.swing.GroupLayout.PREFERRED_SIZE, 287, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel19Layout.setVerticalGroup(
            jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel19Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel23, javax.swing.GroupLayout.DEFAULT_SIZE, 110, Short.MAX_VALUE)
                    .addComponent(jPanel11, javax.swing.GroupLayout.DEFAULT_SIZE, 110, Short.MAX_VALUE)
                    .addComponent(jPanel10, javax.swing.GroupLayout.DEFAULT_SIZE, 110, Short.MAX_VALUE)
                    .addComponent(jPanel12, javax.swing.GroupLayout.DEFAULT_SIZE, 110, Short.MAX_VALUE))
                .addGap(9, 9, 9))
        );

        jPanel3.setBackground(new java.awt.Color(238, 232, 227));

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 28)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(21, 97, 0));
        jLabel1.setText("RELATÓRIOS");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(28, 28, 28)
                .addComponent(jLabel1)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(63, 63, 63)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel19, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel14, javax.swing.GroupLayout.DEFAULT_SIZE, 1237, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addContainerGap())))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(23, 23, 23)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel19, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel14, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jScrollPane1.setViewportView(jPanel1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnDiarioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDiarioActionPerformed
        tipoPeriodoAtual = PeriodoRelatorio.Tipo.DIARIO;
        seletorCardLayout.show(painelPeriodo, "diario");
        destacarBotaoPeriodo(btnDiario);
        atualizarRelatorios();
    }//GEN-LAST:event_btnDiarioActionPerformed

    private void btnAnualActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAnualActionPerformed
        tipoPeriodoAtual = PeriodoRelatorio.Tipo.ANUAL;
        seletorCardLayout.show(painelPeriodo, "anual");
        destacarBotaoPeriodo(btnAnual);
        atualizarRelatorios();
    }//GEN-LAST:event_btnAnualActionPerformed

    private void btnPersonalizadoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPersonalizadoActionPerformed
        tipoPeriodoAtual = PeriodoRelatorio.Tipo.PERSONALIZADO;
        seletorCardLayout.show(painelPeriodo, "personalizado");
        destacarBotaoPeriodo(btnPersonalizado);
        atualizarRelatorios();
    }//GEN-LAST:event_btnPersonalizadoActionPerformed

    private void btnMensalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMensalActionPerformed
        tipoPeriodoAtual = PeriodoRelatorio.Tipo.MENSAL;
        seletorCardLayout.show(painelPeriodo, "mensal");
        destacarBotaoPeriodo(btnMensal);
        atualizarRelatorios();
    }//GEN-LAST:event_btnMensalActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAnual;
    private javax.swing.JButton btnDiario;
    private javax.swing.JButton btnPersonalizado;
    private javax.swing.JButton btnMensal;
    private javax.swing.JPanel chartCategoriaPanel;
    private javax.swing.JPanel chartPagamentoPanel;
    private javax.swing.JPanel chartRankingPanel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JPanel jPanel14;
    private javax.swing.JPanel jPanel18;
    private javax.swing.JPanel jPanel19;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel23;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables
}
