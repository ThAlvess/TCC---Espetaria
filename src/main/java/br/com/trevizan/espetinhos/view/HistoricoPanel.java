package br.com.trevizan.espetinhos.view;

import javax.swing.table.DefaultTableModel;

/**
 * Painel responsável por exibir o Histórico de Comandas encerradas.
 * Possui filtro de busca em tempo real e herda o padrão visual do sistema.
 */
public class HistoricoPanel extends br.com.trevizan.espetinhos.PadraoJPanel {

    private final java.awt.Color VERDE = new java.awt.Color(20, 115, 10);
    private final java.awt.Color CINZA_TABELA = new java.awt.Color(224, 224, 224);
    private final java.awt.Color CINZA_CABECALHO = new java.awt.Color(135, 135, 132);
    private javax.swing.table.DefaultTableModel modeloTabela;
    private java.util.List<Comanda> comandas;
    
    public HistoricoPanel() {
        initComponents();
        txtPesquisar.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 16));
        txtPesquisar.putClientProperty("JTextField.placeholderText", "Pesquisar comanda, mesa, atendente ou item...");
        txtPesquisar.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jLabel1.setForeground(VERDE);
        modeloTabela = (javax.swing.table.DefaultTableModel) tabelaHistorico.getModel();
        configurarTabela();
        carregarHistorico();
        estilizarTabela();
        configurarPesquisa();
    }

private void configurarTabela() {
    tabelaHistorico.getTableHeader().setReorderingAllowed(false);
    
    tabelaHistorico.getColumnModel().getColumn(0).setPreferredWidth(80);  // Comanda
    tabelaHistorico.getColumnModel().getColumn(1).setPreferredWidth(80);  // Mesa
    tabelaHistorico.getColumnModel().getColumn(2).setPreferredWidth(150); // Data
    tabelaHistorico.getColumnModel().getColumn(3).setPreferredWidth(120); // Atendente
    tabelaHistorico.getColumnModel().getColumn(4).setPreferredWidth(350); // Itens
    tabelaHistorico.getColumnModel().getColumn(5).setPreferredWidth(100); // Valor Total
}
   /*
    * ==========================
    * CARREGAR HISTÓRICO
    * ==========================
    */
private void carregarHistorico() {
    // TODO: Substituir dados mockados pela chamada ao banco de dados (ex: comandaDAO.listarEncerradas())
    comandas = java.util.Arrays.asList(
        new Comanda("1001", "Mesa 04", "08/09/2026 19:30", "João", "2x Espeto Carne, 1x Cerveja, 1x Coca-Cola", 45.90),
        new Comanda("1002", "Mesa 12", "08/09/2026 20:15", "Maria", "4x Espeto Frango, 2x Suco Laranja", 52.00),
        new Comanda("1003", "Mesa 01", "08/09/2026 21:00", "Carlos", "1x Porção Fritas, 3x Cerveja", 65.50)
    );

    preencherTabela(comandas);
}

private void preencherTabela(java.util.List<Comanda> lista) {
    modeloTabela.setRowCount(0);
    
    // Cria o formatador para transformar os números em formato R$ 0,00
    java.text.NumberFormat moeda = java.text.NumberFormat.getCurrencyInstance(new java.util.Locale("pt", "BR"));

    for (Comanda comanda : lista) {
        modeloTabela.addRow(new Object[]{
            comanda.getNumero(),
            comanda.getMesa(),
            comanda.getData(),
            comanda.getAtendente(),
            comanda.getItens(),
            moeda.format(comanda.getValorTotal())
            // TODO no futuro: Aplicar formatação NumberFormat.getCurrencyInstance() 
            // caso o modelo oficial inclua o "Valor Total" da comanda.
        });
    }
}

/*
 * ==========================
 * PESQUISA
 * ==========================
 */
private void configurarPesquisa() {
    txtPesquisar.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
        @Override
        public void insertUpdate(javax.swing.event.DocumentEvent e) { pesquisar(); }
        
        @Override
        public void removeUpdate(javax.swing.event.DocumentEvent e) { pesquisar(); }
        
        @Override
        public void changedUpdate(javax.swing.event.DocumentEvent e) { pesquisar(); }
    });
}

private void pesquisar() {
    String texto = txtPesquisar.getText().trim().toLowerCase();

    // Se o campo estiver vazio, devolve a lista completa
    if (texto.isEmpty()) {
        preencherTabela(comandas);
        return;
    }

    // Filtra a lista verificando se o texto de pesquisa existe em alguma das colunas
    java.util.List<Comanda> filtrados = comandas.stream()
        .filter(comanda -> 
            comanda.getNumero().toLowerCase().contains(texto) ||
            comanda.getMesa().toLowerCase().contains(texto) ||
            comanda.getAtendente().toLowerCase().contains(texto) ||
            comanda.getData().toLowerCase().contains(texto) ||
            comanda.getItens().toLowerCase().contains(texto)
        ).toList();

    preencherTabela(filtrados);
}
    
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tabelaHistorico = new javax.swing.JTable();
        txtPesquisar = new javax.swing.JTextField();

        setBackground(new java.awt.Color(240, 235, 235));

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 28)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(21, 97, 0));
        jLabel1.setText("HISTÓRICO DE COMANDAS");

        tabelaHistorico.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null}
            },
            new String [] {
                "Comanda", "Mesa", "Data", "Atendente", "Itens Consumidos", "Valor Total"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane1.setViewportView(tabelaHistorico);

        txtPesquisar.setPreferredSize(new java.awt.Dimension(64, 48));
        txtPesquisar.addActionListener(this::txtPesquisarActionPerformed);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(38, 38, 38)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtPesquisar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 1218, Short.MAX_VALUE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(38, 38, 38))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(42, 42, 42)
                .addComponent(jLabel1)
                .addGap(18, 18, 18)
                .addComponent(txtPesquisar, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 900, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void txtPesquisarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtPesquisarActionPerformed
    // Mantido vazio intencionalmente. 
    // A pesquisa já é disparada em tempo real pelo DocumentListener.
    // NÃO APAGUE este método para não quebrar o gerador visual do NetBeans.
    }//GEN-LAST:event_txtPesquisarActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable tabelaHistorico;
    private javax.swing.JTextField txtPesquisar;
    // End of variables declaration//GEN-END:variables

private void estilizarTabela() {
    // 1. Estilo base da Tabela
    tabelaHistorico.setRowHeight(66);
    tabelaHistorico.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 15));
    tabelaHistorico.setForeground(VERDE);
    tabelaHistorico.setBackground(CINZA_TABELA);
    tabelaHistorico.setGridColor(new java.awt.Color(100, 100, 100));
    tabelaHistorico.setShowVerticalLines(true);
    tabelaHistorico.setShowHorizontalLines(true);
    tabelaHistorico.setSelectionBackground(new java.awt.Color(210, 225, 210));
    tabelaHistorico.setSelectionForeground(VERDE);

    // 2. Estilo do Cabeçalho
    tabelaHistorico.getTableHeader().setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 15));
    tabelaHistorico.getTableHeader().setForeground(java.awt.Color.WHITE);
    tabelaHistorico.getTableHeader().setBackground(CINZA_CABECALHO);
    tabelaHistorico.getTableHeader().setPreferredSize(new java.awt.Dimension(0, 55));

    // 3. Centralizar o conteúdo das células
    javax.swing.table.DefaultTableCellRenderer centralizado = new javax.swing.table.DefaultTableCellRenderer();
    centralizado.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    for (int i = 0; i < tabelaHistorico.getColumnCount(); i++) {
        tabelaHistorico.getColumnModel().getColumn(i).setCellRenderer(centralizado);
    }

    // 4. Estilo do fundo e borda do painel de rolagem
    jScrollPane1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(150, 150, 150)));
    jScrollPane1.getViewport().setBackground(CINZA_TABELA);
}

// TODO: Remover esta classe interna e importar a oficial assim que 'Comanda' for implementada no pacote 'model'
private class Comanda {
    private String numero;
    private String mesa;
    private String data;
    private String atendente;
    private String itens;
    private double valorTotal;

    public Comanda(String numero, String mesa, String data, String atendente, String itens, double valorTotal) {
        this.numero = numero;
        this.mesa = mesa;
        this.data = data;
        this.atendente = atendente;
        this.itens = itens;
        this.valorTotal = valorTotal;
    }

    public String getNumero() { return numero; }
    public String getMesa() { return mesa; }
    public String getData() { return data; }
    public String getAtendente() { return atendente; }
    public String getItens() { return itens; }
    public double getValorTotal() { return valorTotal; }
}

}
