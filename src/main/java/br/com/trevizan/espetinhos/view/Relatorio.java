/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package br.com.trevizan.espetinhos.view;

import br.com.trevizan.espetinhos.PanelArredondado;

/**
 *
 * @author marlo
 */
public class Relatorio extends javax.swing.JPanel {

    /**
     * Creates new form Relatorio
     */
    public Relatorio() {
        initComponents();
        montarGraficoRanking();
        montarGraficoCategoria();
        montarGraficoPagamento();
    }

    private void montarGraficoRanking() {
        org.jfree.data.category.DefaultCategoryDataset dataset = new org.jfree.data.category.DefaultCategoryDataset();
        dataset.addValue(36, "Vendas", "Espetinho Misto");
        dataset.addValue(31, "Vendas", "Mandioca Frita");
        dataset.addValue(23, "Vendas", "Refrigerante Lata");
        dataset.addValue(20, "Vendas", "Suco Natural");
        dataset.addValue(19, "Vendas", "Espetinho de Queijo Coalho");
        dataset.addValue(9,  "Vendas", "Cerveja Long Neck");

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

        chartRankingPanel.setLayout(new java.awt.BorderLayout());
        chartRankingPanel.add(titulo, java.awt.BorderLayout.NORTH);
        chartRankingPanel.add(painel, java.awt.BorderLayout.CENTER);
    }
    
    private void montarGraficoCategoria() {
        org.jfree.data.general.DefaultPieDataset<String> dataset = new org.jfree.data.general.DefaultPieDataset<>();
        dataset.setValue("Espetinhos", 45);
        dataset.setValue("Bebidas", 40);
        dataset.setValue("Acompanhamentos", 15);

        org.jfree.chart.plot.RingPlot plot = new org.jfree.chart.plot.RingPlot(dataset);
        plot.setSectionDepth(0.35);
        plot.setBackgroundPaint(java.awt.Color.WHITE);
        plot.setOutlineVisible(false);
        plot.setSeparatorsVisible(false);
        plot.setShadowPaint(null);
        plot.setInteriorGap(0.12); // dá espaço pro rótulo + linha de chamada não serem cortados pela borda do card

        plot.setSectionPaint("Espetinhos", new java.awt.Color(25, 100, 25));
        plot.setSectionPaint("Bebidas", new java.awt.Color(230, 140, 60));
        plot.setSectionPaint("Acompanhamentos", new java.awt.Color(178, 58, 38));

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

        chartCategoriaPanel.setLayout(new java.awt.BorderLayout());
        chartCategoriaPanel.add(titulo, java.awt.BorderLayout.NORTH);
        chartCategoriaPanel.add(painel, java.awt.BorderLayout.CENTER);
    }
    
    private void montarGraficoPagamento() {
        org.jfree.data.general.DefaultPieDataset<String> dataset = new org.jfree.data.general.DefaultPieDataset<>();
        dataset.setValue("Crédito", 70);
        dataset.setValue("Débito", 20);
        dataset.setValue("Dinheiro", 10);

        org.jfree.chart.plot.RingPlot plot = new org.jfree.chart.plot.RingPlot(dataset);
        plot.setSectionDepth(0.35);
        plot.setBackgroundPaint(java.awt.Color.WHITE);
        plot.setOutlineVisible(false);
        plot.setSeparatorsVisible(false);
        plot.setShadowPaint(null);
        plot.setInteriorGap(0.12);

        plot.setSectionPaint("Crédito", new java.awt.Color(230, 140, 60));
        plot.setSectionPaint("Débito", new java.awt.Color(178, 58, 38));
        plot.setSectionPaint("Dinheiro", new java.awt.Color(25, 100, 25));

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

        chartPagamentoPanel.setLayout(new java.awt.BorderLayout());
        chartPagamentoPanel.add(titulo, java.awt.BorderLayout.NORTH);
        chartPagamentoPanel.add(painel, java.awt.BorderLayout.CENTER);
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
        // TODO add your handling code here:
    }//GEN-LAST:event_btnDiarioActionPerformed

    private void btnAnualActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAnualActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnAnualActionPerformed

    private void btnFiscalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnFiscalActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnFiscalActionPerformed

    private void btnMensalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMensalActionPerformed
        // TODO add your handling code here:
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
