/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package br.com.trevizan.espetinhos.view;

import br.com.trevizan.espetinhos.PanelArredondado;
import br.com.trevizan.espetinhos.dao.RelatorioDAO;
import br.com.trevizan.espetinhos.model.ResumoRelatorio;

import com.github.lgooddatepicker.components.DatePicker;
import com.github.lgooddatepicker.components.DatePickerSettings;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.LinkedHashMap;
import java.util.Locale;

/**
 *
 * @author marlo
 */
public class Relatorio extends javax.swing.JPanel {

    private enum TipoPeriodo { DIARIO, MENSAL, ANUAL, FISCAL }

    private final RelatorioDAO relatorioDAO = new RelatorioDAO();

    private TipoPeriodo tipoPeriodoAtual = TipoPeriodo.DIARIO;

    private javax.swing.JPanel painelPeriodo;
    private java.awt.CardLayout seletorCardLayout;
    private DatePicker datePickerDiario;
    private javax.swing.JComboBox<String> comboMes;
    private javax.swing.JComboBox<Integer> comboAnoMensal;
    private javax.swing.JComboBox<Integer> comboAnoFiscal;
    private javax.swing.JLabel jLabelSubtituloPagamento;

    private static final java.text.NumberFormat FORMATO_MOEDA =
            java.text.NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

    /**
     * Creates new form Relatorio
     */
    public Relatorio() {
        initComponents();
        montarSeletorPeriodo();
        adicionarBadgesKpi();
        atualizarRelatorios();
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

        chart.setBackgroundPaint(java.awt.Color.WHITE);
        chart.setBorderVisible(false);

        org.jfree.chart.ChartPanel painel = new org.jfree.chart.ChartPanel(chart);
        painel.setPopupMenu(null);
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

        org.jfree.chart.ChartPanel painel = new org.jfree.chart.ChartPanel(chart);
        painel.setPopupMenu(null);
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

        org.jfree.chart.ChartPanel painel = new org.jfree.chart.ChartPanel(chart);
        painel.setPopupMenu(null);
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

    private void montarGraficoEvolucao(java.util.LinkedHashMap<String, java.math.BigDecimal> dados) {
        org.jfree.data.category.DefaultCategoryDataset dataset = new org.jfree.data.category.DefaultCategoryDataset();
        for (java.util.Map.Entry<String, java.math.BigDecimal> entrada : dados.entrySet()) {
            dataset.addValue(entrada.getValue(), "Faturamento", entrada.getKey());
        }

        org.jfree.chart.JFreeChart chart = org.jfree.chart.ChartFactory.createLineChart(
            null, null, null, dataset,
            org.jfree.chart.plot.PlotOrientation.VERTICAL,
            false, false, false);

        org.jfree.chart.plot.CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(java.awt.Color.WHITE);
        plot.setOutlineVisible(false);
        plot.setRangeGridlinePaint(new java.awt.Color(225, 220, 213));
        plot.setDomainGridlinesVisible(false);

        java.awt.Font fonteEixosEvolucao = new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 11);

        org.jfree.chart.axis.NumberAxis eixoValoresEvolucao = (org.jfree.chart.axis.NumberAxis) plot.getRangeAxis();
        eixoValoresEvolucao.setNumberFormatOverride(new java.text.DecimalFormat("'R$'#,##0"));
        eixoValoresEvolucao.setAxisLineVisible(false);
        eixoValoresEvolucao.setTickMarksVisible(false);
        eixoValoresEvolucao.setTickLabelFont(fonteEixosEvolucao);
        eixoValoresEvolucao.setUpperMargin(0.20);

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

        chart.setBackgroundPaint(java.awt.Color.WHITE);
        chart.setBorderVisible(false);

        org.jfree.chart.ChartPanel painelEvolucao = new org.jfree.chart.ChartPanel(chart);
        painelEvolucao.setPopupMenu(null);
        painelEvolucao.setBackground(java.awt.Color.WHITE);

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

    private void atualizarKpis(ResumoRelatorio resumo) {
        jLabel9.setText(FORMATO_MOEDA.format(resumo.getFaturamentoTotal()));
        jLabel11.setText(String.valueOf(resumo.getQuantidadeVendas()));
        jLabel13.setText(FORMATO_MOEDA.format(resumo.getTicketMedio()));
        jLabel23.setText(resumo.getFormaPagamentoPrincipal() != null
                ? resumo.getFormaPagamentoPrincipal() : "—");
        jLabelSubtituloPagamento.setText(resumo.getFormaPagamentoPrincipal() != null
                ? "Total: " + FORMATO_MOEDA.format(resumo.getValorFormaPagamentoPrincipal())
                : " ");
    }

    private LocalDateTime[] calcularIntervalo() {
        LocalDate hoje = LocalDate.now();

        return switch (tipoPeriodoAtual) {
            case DIARIO -> {
                LocalDate dia = datePickerDiario.getDate() != null ? datePickerDiario.getDate() : hoje;
                yield new LocalDateTime[] {
                    dia.atStartOfDay(),
                    dia.atTime(LocalTime.of(23, 59, 59))
                };
            }
            case MENSAL -> {
                int mes = comboMes.getSelectedIndex() + 1;
                int ano = (Integer) comboAnoMensal.getSelectedItem();
                YearMonth ym = YearMonth.of(ano, mes);
                yield new LocalDateTime[] {
                    ym.atDay(1).atStartOfDay(),
                    ym.atEndOfMonth().atTime(LocalTime.of(23, 59, 59))
                };
            }
            case FISCAL -> {
                int ano = (Integer) comboAnoFiscal.getSelectedItem();
                yield new LocalDateTime[] {
                    LocalDate.of(ano, 1, 1).atStartOfDay(),
                    LocalDate.of(ano, 12, 31).atTime(LocalTime.of(23, 59, 59))
                };
            }
            case ANUAL -> {
                LocalDate inicioJanela = hoje.minusMonths(11).withDayOfMonth(1);
                yield new LocalDateTime[] {
                    inicioJanela.atStartOfDay(),
                    hoje.atTime(LocalTime.of(23, 59, 59))
                };
            }
        };
    }

    private RelatorioDAO.Granularidade granularidadeAtual() {
        return switch (tipoPeriodoAtual) {
            case DIARIO -> RelatorioDAO.Granularidade.HORA;
            case MENSAL -> RelatorioDAO.Granularidade.DIA;
            case ANUAL, FISCAL -> RelatorioDAO.Granularidade.MES;
        };
    }

    private void atualizarRelatorios() {
        LocalDateTime[] intervalo = calcularIntervalo();
        LocalDateTime inicio = intervalo[0];
        LocalDateTime fim = intervalo[1];

        try {
            ResumoRelatorio resumo = relatorioDAO.buscarResumo(inicio, fim);
            atualizarKpis(resumo);

            montarGraficoRanking(relatorioDAO.rankingProdutos(inicio, fim, 6));
            montarGraficoCategoria(relatorioDAO.vendasPorCategoria(inicio, fim));
            montarGraficoPagamento(relatorioDAO.porFormaPagamento(inicio, fim));

            LinkedHashMap<String, BigDecimal> evolucaoBruta =
                    relatorioDAO.evolucaoFaturamento(inicio, fim, granularidadeAtual());
            montarGraficoEvolucao(formatarRotulosEvolucao(evolucaoBruta));

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

            montarGraficoRanking(new LinkedHashMap<>());
            montarGraficoCategoria(new LinkedHashMap<>());
            montarGraficoPagamento(new LinkedHashMap<>());
            montarGraficoEvolucao(new LinkedHashMap<>());
        }
    }

    /**
     * Deixa os rótulos do eixo X do gráfico de evolução mais legíveis (a
     * consulta traz "14" para hora, "05/09" para dia, "2026-09" para mês).
     */
    private LinkedHashMap<String, BigDecimal> formatarRotulosEvolucao(
            LinkedHashMap<String, BigDecimal> bruto) {

        LinkedHashMap<String, BigDecimal> formatado = new LinkedHashMap<>();

        for (java.util.Map.Entry<String, BigDecimal> entrada : bruto.entrySet()) {
            String chave = entrada.getKey();
            String rotulo;

            if (tipoPeriodoAtual == TipoPeriodo.DIARIO) {
                rotulo = chave + "h";
            } else if (tipoPeriodoAtual == TipoPeriodo.MENSAL) {
                rotulo = chave;
            } else {
                YearMonth ym = YearMonth.parse(chave);
                String mesAbrev = ym.getMonth().getDisplayName(TextStyle.SHORT, new Locale("pt", "BR"));
                rotulo = mesAbrev + "/" + ym.getYear();
            }

            formatado.put(rotulo, entrada.getValue());
        }

        return formatado;
    }

    private void montarSeletorPeriodo() {
        painelPeriodo = new javax.swing.JPanel();
        seletorCardLayout = new java.awt.CardLayout();
        painelPeriodo.setLayout(seletorCardLayout);
        painelPeriodo.setOpaque(false);

        java.awt.Color verdeEscuro = new java.awt.Color(25, 100, 25);
        java.awt.Color verdeClaro = new java.awt.Color(222, 235, 222);
        java.awt.Font fonteControle = new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 13);

        DatePickerSettings configuracaoData = new DatePickerSettings(new Locale("pt", "BR"));
        configuracaoData.setColor(DatePickerSettings.DateArea.CalendarBackgroundSelectedDate, verdeEscuro);
        configuracaoData.setColor(DatePickerSettings.DateArea.BackgroundTodayLabel, verdeClaro);
        configuracaoData.setColor(DatePickerSettings.DateArea.TextFieldBackgroundValidDate, java.awt.Color.WHITE);
        configuracaoData.setColor(DatePickerSettings.DateArea.BackgroundMonthAndYearNavigationButtons, java.awt.Color.WHITE);

        datePickerDiario = new DatePicker(configuracaoData);
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

        comboAnoFiscal = new javax.swing.JComboBox<>(anosDisponiveis);
        comboAnoFiscal.setSelectedItem(anoAtual);
        comboAnoFiscal.addActionListener(evento -> atualizarRelatorios());
        estilizarComboPeriodo(comboAnoFiscal, fonteControle, verdeEscuro);

        javax.swing.JPanel painelFiscal = criarCartaoSeletor(
                new javax.swing.JLabel(criarIconeDocumento(verdeEscuro)), comboAnoFiscal);

        painelPeriodo.add(painelDiario, "diario");
        painelPeriodo.add(painelMensal, "mensal");
        painelPeriodo.add(painelAnual, "anual");
        painelPeriodo.add(painelFiscal, "fiscal");

        jPanel2.removeAll();
        jPanel2.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 8, 4));
        jPanel2.add(btnDiario);
        jPanel2.add(btnMensal);
        jPanel2.add(btnAnual);
        jPanel2.add(btnFiscal);
        jPanel2.add(javax.swing.Box.createHorizontalStrut(20));
        jPanel2.add(painelPeriodo);
        jPanel2.revalidate();
        jPanel2.repaint();

        destacarBotaoPeriodo(btnDiario);
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

        configurarCartaoKpi(jPanel10, jLabel8, jLabel9, null,
                criarIconeCifrao(java.awt.Color.WHITE), verdeBadge);
        configurarCartaoKpi(jPanel11, jLabel10, jLabel11, null,
                criarIconeSacola(java.awt.Color.WHITE), verdeBadge);
        configurarCartaoKpi(jPanel12, jLabel12, jLabel13, null,
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

    private void destacarBotaoPeriodo(javax.swing.JButton botaoAtivo) {
        java.awt.Color verdeEscuro = new java.awt.Color(25, 100, 25);
        javax.swing.JButton[] botoesPeriodo = {btnDiario, btnMensal, btnAnual, btnFiscal};

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
        btnFiscal = new javax.swing.JButton();
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

        btnFiscal.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnFiscal.setForeground(new java.awt.Color(25, 100, 25));
        btnFiscal.setText("Fiscal");
        btnFiscal.setActionCommand("");
        btnFiscal.setMargin(new java.awt.Insets(2, 15, 2, 15));
        btnFiscal.setRequestFocusEnabled(false);
        btnFiscal.addActionListener(this::btnFiscalActionPerformed);

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
                .addComponent(btnFiscal, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(716, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnDiario, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnAnual, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnFiscal, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
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
        tipoPeriodoAtual = TipoPeriodo.DIARIO;
        seletorCardLayout.show(painelPeriodo, "diario");
        destacarBotaoPeriodo(btnDiario);
        atualizarRelatorios();
    }//GEN-LAST:event_btnDiarioActionPerformed

    private void btnAnualActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAnualActionPerformed
        tipoPeriodoAtual = TipoPeriodo.ANUAL;
        seletorCardLayout.show(painelPeriodo, "anual");
        destacarBotaoPeriodo(btnAnual);
        atualizarRelatorios();
    }//GEN-LAST:event_btnAnualActionPerformed

    private void btnFiscalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnFiscalActionPerformed
        tipoPeriodoAtual = TipoPeriodo.FISCAL;
        seletorCardLayout.show(painelPeriodo, "fiscal");
        destacarBotaoPeriodo(btnFiscal);
        atualizarRelatorios();
    }//GEN-LAST:event_btnFiscalActionPerformed

    private void btnMensalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMensalActionPerformed
        tipoPeriodoAtual = TipoPeriodo.MENSAL;
        seletorCardLayout.show(painelPeriodo, "mensal");
        destacarBotaoPeriodo(btnMensal);
        atualizarRelatorios();
    }//GEN-LAST:event_btnMensalActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAnual;
    private javax.swing.JButton btnDiario;
    private javax.swing.JButton btnFiscal;
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
