package br.com.trevizan.espetinhos.view;

import br.com.trevizan.espetinhos.dao.ComandaDAO;
import br.com.trevizan.espetinhos.dao.MesaDAO;
import br.com.trevizan.espetinhos.dao.UsuarioDAO;
import br.com.trevizan.espetinhos.model.Comanda;
import br.com.trevizan.espetinhos.model.Mesa;
import br.com.trevizan.espetinhos.model.Usuario;
import br.com.trevizan.espetinhos.util.FiltroPeriodo;
import br.com.trevizan.espetinhos.util.PadraoComponentes;
import br.com.trevizan.espetinhos.util.PadraoTela;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Painel responsável por exibir o Histórico de Comandas encerradas.
 *
 * <p>O layout gerado pelo NetBeans (initComponents) é reorganizado em
 * tempo de execução por {@link #montarLayout()}:</p>
 * <ul>
 *   <li>filtro de datas "De ... até ..." (em branco mostra tudo);</li>
 *   <li>pesquisa com o texto alinhado à esquerda;</li>
 *   <li>tabela "clean" com linhas zebradas dentro de um cartão branco.</li>
 * </ul>
 */
public class HistoricoPanel extends br.com.trevizan.espetinhos.PadraoJPanel {

    private static final int COL_COMANDA = 0;
    private static final int COL_ITENS = 4;
    private static final int COL_VALOR = 5;

    private javax.swing.table.DefaultTableModel modeloTabela;
    private javax.swing.table.TableRowSorter<javax.swing.table.DefaultTableModel> sorter;
    private java.util.List<Comanda> comandas = new ArrayList<>();
    /** Valor de cada linha do modelo (para somar o total do que está visível). */
    private final List<BigDecimal> valoresLinhas = new ArrayList<>();
    private ComandaDAO comandaDAO;
    private FiltroPeriodo filtroPeriodo;
    private javax.swing.JLabel lblResumo;

    public HistoricoPanel() {
        initComponents();
        comandaDAO = new ComandaDAO();
        PadraoTela.aplicarTitulo(jLabel1);
        PadraoComponentes.aplicarCampoPesquisa(txtPesquisar,
                "Pesquisar comanda, mesa, atendente ou item...");

        modeloTabela = (javax.swing.table.DefaultTableModel) tabelaHistorico.getModel();
        modeloTabela.setRowCount(0);
        sorter = new javax.swing.table.TableRowSorter<>(modeloTabela);
        sorter.setSortable(COL_VALOR, false);
        tabelaHistorico.setRowSorter(sorter);

        filtroPeriodo = new FiltroPeriodo(this::carregarHistorico);

        estilizarTabela();
        configurarTabela();
        montarLayout();
        configurarPesquisa();
        carregarHistorico();

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

    /**
     * Reorganiza a tela: título, filtro de datas, pesquisa e o cartão com a
     * tabela (que ocupa o restante da altura).
     */
    private void montarLayout() {
        removeAll();
        setLayout(new java.awt.BorderLayout(0, 14));
        setBorder(javax.swing.BorderFactory.createEmptyBorder(
                PadraoTela.MARGEM_TOPO, PadraoTela.MARGEM_LATERAL, 30, PadraoTela.MARGEM_LATERAL));

        javax.swing.JPanel topo = new javax.swing.JPanel();
        topo.setOpaque(false);
        topo.setLayout(new javax.swing.BoxLayout(topo, javax.swing.BoxLayout.Y_AXIS));

        jLabel1.setAlignmentX(LEFT_ALIGNMENT);
        topo.add(jLabel1);
        topo.add(javax.swing.Box.createVerticalStrut(14));

        // filtro "De ... até ..." (datas em branco = todas as comandas)
        javax.swing.JPanel linhaFiltro = new javax.swing.JPanel(new java.awt.BorderLayout());
        linhaFiltro.setOpaque(false);
        linhaFiltro.setAlignmentX(LEFT_ALIGNMENT);
        linhaFiltro.add(filtroPeriodo, java.awt.BorderLayout.WEST);
        topo.add(linhaFiltro);
        topo.add(javax.swing.Box.createVerticalStrut(10));

        txtPesquisar.setAlignmentX(LEFT_ALIGNMENT);
        txtPesquisar.setPreferredSize(new java.awt.Dimension(400, PadraoComponentes.ALTURA_PESQUISA));
        txtPesquisar.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, PadraoComponentes.ALTURA_PESQUISA));
        topo.add(txtPesquisar);

        lblResumo = new javax.swing.JLabel(" ");
        lblResumo.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 13));
        lblResumo.setForeground(PadraoComponentes.COR_TEXTO_SUAVE);

        add(topo, java.awt.BorderLayout.NORTH);
        add(PadraoComponentes.criarCartaoTabela("Comandas encerradas", tabelaHistorico, lblResumo),
                java.awt.BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    private void configurarTabela() {
        // {largura preferida, largura mínima} de cada coluna
        int[][] larguras = {
            {100, 95},  // Comanda
            {100, 80},  // Mesa
            {165, 155}, // Data
            {140, 110}, // Atendente
            {480, 160}, // Itens
            {120, 100}  // Valor Total
        };
        for (int i = 0; i < larguras.length; i++) {
            tabelaHistorico.getColumnModel().getColumn(i).setPreferredWidth(larguras[i][0]);
            tabelaHistorico.getColumnModel().getColumn(i).setMinWidth(larguras[i][1]);
        }
    }

    /*
     * ==========================
     * CARREGAR HISTÓRICO
     * ==========================
     */
    private void carregarHistorico() {
        try {
            comandas = comandaDAO.listarFechadas();

            List<Comanda> doPeriodo = new ArrayList<>();
            for (Comanda comanda : comandas) {
                if (filtroPeriodo.contem(comanda.getDataFechamento())) {
                    doPeriodo.add(comanda);
                }
            }
            preencherTabela(doPeriodo);
        } catch (Exception e) {
            javax.swing.JOptionPane.showMessageDialog(this,
                "Erro ao carregar o histórico do banco de dados: " + e.getMessage(),
                "Erro",
                javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }

    private void preencherTabela(java.util.List<Comanda> lista) {
        modeloTabela.setRowCount(0);
        valoresLinhas.clear();

        java.text.NumberFormat moeda = java.text.NumberFormat.getCurrencyInstance(new java.util.Locale("pt", "BR"));
        java.time.format.DateTimeFormatter formatoData = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        Map<Integer, Integer> numerosMesas = carregarNumerosMesas();
        Map<Integer, String> nomesAtendentes = carregarNomesAtendentes();

        for (Comanda comanda : lista) {
            // Formata a data de fechamento, caso exista
            String dataFechamento = (comanda.getDataFechamento() != null)
                    ? comanda.getDataFechamento().format(formatoData)
                    : "Sem registro";

            // Busca os itens concatenados direto do banco de dados
            String itensConsumidos = comandaDAO.obterItensFormatados(comanda.getIdComanda());

            BigDecimal valor = comanda.getValorTotal() != null ? comanda.getValorTotal() : BigDecimal.ZERO;
            valoresLinhas.add(valor);

            modeloTabela.addRow(new Object[]{
                String.valueOf(comanda.getIdComanda()),
                descreverAtendimento(comanda, numerosMesas),
                dataFechamento,
                nomesAtendentes.getOrDefault(comanda.getIdUsuario(), "Cód. " + comanda.getIdUsuario()),
                itensConsumidos,
                moeda.format(valor)
            });
        }
        atualizarResumo();
    }

    /** "Mesa 4" para comandas de mesa; "Delivery" / "Retirada" para as demais. */
    private String descreverAtendimento(Comanda comanda, Map<Integer, Integer> numerosMesas) {
        String tipo = comanda.getTipoAtendimento();
        if (tipo != null && !"MESA".equalsIgnoreCase(tipo)) {
            return tipo.substring(0, 1).toUpperCase() + tipo.substring(1).toLowerCase();
        }
        Integer numero = numerosMesas.get(comanda.getIdMesa());
        return "Mesa " + (numero != null ? numero : comanda.getIdMesa());
    }

    private Map<Integer, Integer> carregarNumerosMesas() {
        Map<Integer, Integer> mapa = new HashMap<>();
        try {
            for (Mesa mesa : new MesaDAO().listarTodas()) {
                mapa.put(mesa.getIdMesa(), mesa.getNumero());
            }
        } catch (RuntimeException e) {
            // sem o número da mesa, a tabela mostra o código dela
        }
        return mapa;
    }

    private Map<Integer, String> carregarNomesAtendentes() {
        Map<Integer, String> mapa = new HashMap<>();
        try {
            List<Usuario> usuarios = new UsuarioDAO().listar();
            if (usuarios != null) {
                for (Usuario usuario : usuarios) {
                    mapa.put(usuario.getIdUsuario(), usuario.getNome());
                }
            }
        } catch (RuntimeException e) {
            // sem o nome, a tabela mostra o código do atendente
        }
        return mapa;
    }

    /** Quantidade e soma das comandas que estão aparecendo na tabela. */
    private void atualizarResumo() {
        if (lblResumo == null) {
            return;
        }
        BigDecimal total = BigDecimal.ZERO;
        int quantidade = tabelaHistorico.getRowCount();
        for (int i = 0; i < quantidade; i++) {
            int linhaModelo = tabelaHistorico.convertRowIndexToModel(i);
            if (linhaModelo >= 0 && linhaModelo < valoresLinhas.size()) {
                total = total.add(valoresLinhas.get(linhaModelo));
            }
        }
        java.text.NumberFormat moeda = java.text.NumberFormat.getCurrencyInstance(new java.util.Locale("pt", "BR"));
        lblResumo.setText(quantidade + (quantidade == 1 ? " comanda" : " comandas") + "  •  Total " + moeda.format(total));
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

        if (texto.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            // (?i) ignora maiúsculas/minúsculas; quote evita erro com caracteres como "(" ou "+"
            sorter.setRowFilter(javax.swing.RowFilter.regexFilter(
                    "(?i)" + java.util.regex.Pattern.quote(texto)));
        }
        atualizarResumo();
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tabelaHistorico = new javax.swing.JTable();
        txtPesquisar = new javax.swing.JTextField();

        setBackground(new java.awt.Color(238, 232, 227));

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
        PadraoComponentes.estilizarTabela(tabelaHistorico);
        PadraoComponentes.alinharColuna(tabelaHistorico, COL_COMANDA, javax.swing.SwingConstants.CENTER);
        PadraoComponentes.alinharColuna(tabelaHistorico, COL_ITENS, javax.swing.SwingConstants.LEFT);
        PadraoComponentes.alinharColuna(tabelaHistorico, COL_VALOR, javax.swing.SwingConstants.RIGHT);
    }
}
