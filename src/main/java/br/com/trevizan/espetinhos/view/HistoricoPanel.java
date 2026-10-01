package br.com.trevizan.espetinhos.view;

import javax.swing.table.DefaultTableModel;
import br.com.trevizan.espetinhos.model.Comanda;
import br.com.trevizan.espetinhos.dao.ComandaDAO;
import java.util.List;

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
    private ComandaDAO comandaDAO;
    
    public HistoricoPanel() {
        initComponents();
        comandaDAO = new ComandaDAO();
        txtPesquisar.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 16));
        txtPesquisar.putClientProperty("JTextField.placeholderText", "Pesquisar comanda, mesa, atendente ou item...");
        txtPesquisar.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        br.com.trevizan.espetinhos.util.PadraoTela.aplicarTitulo(jLabel1);
        modeloTabela = (javax.swing.table.DefaultTableModel) tabelaHistorico.getModel();
        configurarTabela();
        carregarHistorico();
        estilizarTabela();
        configurarPesquisa();
        
    // Atualiza a lista no banco de dados sempre que a tela for exibida pelo CardLayout
    this.addComponentListener(new java.awt.event.ComponentAdapter() {
        @Override
        public void componentShown(java.awt.event.ComponentEvent evt) {
            // Limpa o texto da pesquisa ao entrar na tela
            txtPesquisar.setText(""); 
            
            // Faz a busca atualizada no banco de dados
            carregarHistorico();
        }
    });
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
    try {
        // Requer a inclusão do método listarFechadas() no ComandaDAO
        comandas = comandaDAO.listarFechadas(); 
        preencherTabela(comandas);
    } catch (Exception e) {
        javax.swing.JOptionPane.showMessageDialog(this, 
            "Erro ao carregar o histórico do banco de dados: " + e.getMessage(), 
            "Erro", 
            javax.swing.JOptionPane.ERROR_MESSAGE);
    }
}

private void preencherTabela(java.util.List<Comanda> lista) {
    modeloTabela.setRowCount(0);
    
    java.text.NumberFormat moeda = java.text.NumberFormat.getCurrencyInstance(new java.util.Locale("pt", "BR"));
    java.time.format.DateTimeFormatter formatoData = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    for (Comanda comanda : lista) {
        // Formata a data de fechamento, caso exista
        String dataFechamento = (comanda.getDataFechamento() != null) 
                ? comanda.getDataFechamento().format(formatoData) 
                : "Sem registro";

        // Busca os itens concatenados direto do banco de dados
        String itensConsumidos = comandaDAO.obterItensFormatados(comanda.getIdComanda());

        modeloTabela.addRow(new Object[]{
            comanda.getIdComanda(),
            "Mesa " + comanda.getIdMesa(),
            dataFechamento,
            "Cód. " + comanda.getIdUsuario(),
            itensConsumidos,
            moeda.format(comanda.getValorTotal())
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
    String texto = txtPesquisar.getText().trim();
    javax.swing.table.TableRowSorter<javax.swing.table.DefaultTableModel> sorter = 
        new javax.swing.table.TableRowSorter<>(modeloTabela);
        
    tabelaHistorico.setRowSorter(sorter);

    if (texto.isEmpty()) {
        sorter.setRowFilter(null);
    } else {
        // (?i) torna a pesquisa case-insensitive (ignora maiúsculas/minúsculas)
        sorter.setRowFilter(javax.swing.RowFilter.regexFilter("(?i)" + texto));
    }
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
                .addGap(69, 69, 69)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtPesquisar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 1218, Short.MAX_VALUE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(69, 69, 69))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(51, 51, 51)
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
}