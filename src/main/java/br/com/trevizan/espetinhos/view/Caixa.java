package br.com.trevizan.espetinhos.view;

import br.com.trevizan.espetinhos.dao.CaixaDAO;
import br.com.trevizan.espetinhos.model.Usuario;
import br.com.trevizan.espetinhos.util.SessaoUsuario;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

/**
 *
 * @author thiag
 */
public class Caixa extends javax.swing.JPanel {

    private final CaixaDAO caixaDAO = new CaixaDAO();
    private br.com.trevizan.espetinhos.model.Caixa caixaAtual;

    private JButton btnAbrirCaixa;
    private JButton btnFecharCaixa;
    private JLabel lblValorInicial;
    private JLabel lblTotalVendas;
    private JLabel lblSangrias;
    private JLabel lblStatus;
    private DefaultTableModel modeloTabelaComandas;
    private JTable tabelaComandas;

    public Caixa() {
        initComponents();
        carregarCaixaAberto();
    }

    private void initComponents() {
        this.setLayout(new BorderLayout(20, 20));
        this.setBackground(new Color(237, 231, 226));
        this.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // ---------- Cabeçalho (título + ícone) ----------
        JPanel painelCabecalho = new JPanel(new BorderLayout());
        painelCabecalho.setOpaque(false);

        JLabel lblTitulo = new JLabel("CAIXA");
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 22));
        lblTitulo.setForeground(new Color(34, 102, 51));

        JLabel lblIconePerfil = new JLabel("👤");
        lblIconePerfil.setFont(new Font("SansSerif", Font.PLAIN, 24));
        lblIconePerfil.setForeground(new Color(34, 102, 51));

        painelCabecalho.add(lblTitulo, BorderLayout.WEST);
        painelCabecalho.add(lblIconePerfil, BorderLayout.EAST);

        // ---------- Corpo (botões + cards + tabela) ----------
        JPanel painelCorpo = new JPanel();
        painelCorpo.setOpaque(false);
        painelCorpo.setLayout(new BoxLayout(painelCorpo, BoxLayout.Y_AXIS));

        // Linha de botões
        JPanel painelBotoes = new JPanel(new GridLayout(1, 2, 15, 0));
        painelBotoes.setOpaque(false);
        painelBotoes.setAlignmentX(Component.LEFT_ALIGNMENT);
        painelBotoes.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        btnAbrirCaixa = new JButton("Abrir caixa");
        btnAbrirCaixa.setBackground(new Color(27, 94, 32));
        btnAbrirCaixa.setForeground(Color.WHITE);
        btnAbrirCaixa.setFont(new Font("SansSerif", Font.BOLD, 14));
        btnAbrirCaixa.setFocusPainted(false);
        btnAbrirCaixa.addActionListener(e -> abrirCaixaClicado());

        btnFecharCaixa = new JButton("Fechar caixa");
        btnFecharCaixa.setBackground(new Color(153, 0, 0));
        btnFecharCaixa.setForeground(Color.WHITE);
        btnFecharCaixa.setFont(new Font("SansSerif", Font.BOLD, 14));
        btnFecharCaixa.setFocusPainted(false);
        btnFecharCaixa.addActionListener(e -> fecharCaixaClicado());

        painelBotoes.add(btnAbrirCaixa);
        painelBotoes.add(btnFecharCaixa);

        // Linha de 4 cards
        JPanel painelCards = new JPanel(new GridLayout(1, 4, 15, 0));
        painelCards.setOpaque(false);
        painelCards.setAlignmentX(Component.LEFT_ALIGNMENT);
        painelCards.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));

        lblValorInicial = new JLabel("R$ -");
        lblTotalVendas = new JLabel("-");
        lblSangrias = new JLabel("R$ -");
        lblStatus = new JLabel("Pendente abertura");

        painelCards.add(criarCard("Valor Inicial", lblValorInicial));
        painelCards.add(criarCard("Total de vendas", lblTotalVendas));
        painelCards.add(criarCard("Sangrias", lblSangrias));
        painelCards.add(criarCard("Status", lblStatus));

        // Título da lista de comandas
        JLabel lblComandas = new JLabel("Comandas deste caixa");
        lblComandas.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblComandas.setForeground(new Color(34, 102, 51));
        lblComandas.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblComandas.setBorder(BorderFactory.createEmptyBorder(10, 0, 5, 0));

        // Tabela de comandas
        modeloTabelaComandas = new DefaultTableModel(
                new Object[]{"Mesa", "Cliente", "Status", "Valor"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // tabela só leitura
            }
        };

        tabelaComandas = new JTable(modeloTabelaComandas);
        tabelaComandas.setRowHeight(28);
        tabelaComandas.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));

        JScrollPane scrollTabela = new JScrollPane(tabelaComandas);
        scrollTabela.setAlignmentX(Component.LEFT_ALIGNMENT);
        scrollTabela.setPreferredSize(new Dimension(0, 200));
        scrollTabela.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));

        painelCorpo.add(painelBotoes);
        painelCorpo.add(Box.createRigidArea(new Dimension(0, 15)));
        painelCorpo.add(painelCards);
        painelCorpo.add(lblComandas);
        painelCorpo.add(scrollTabela);

        this.add(painelCabecalho, BorderLayout.NORTH);
        this.add(painelCorpo, BorderLayout.CENTER);
    }

    private JPanel criarCard(String titulo, JLabel lblValor) {
        JPanel card = new JPanel();
        card.setBackground(new Color(224, 224, 224));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblTitulo.setForeground(new Color(80, 80, 80));
        lblTitulo.setAlignmentX(Component.LEFT_ALIGNMENT);

        lblValor.setFont(new Font("SansSerif", Font.BOLD, 16));
        lblValor.setForeground(new Color(40, 40, 40));
        lblValor.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(lblTitulo);
        card.add(Box.createRigidArea(new Dimension(0, 6)));
        card.add(lblValor);

        return card;
    }

    /**
     * Verifica no banco se já existe um caixa aberto e atualiza a tela.
     */
    private void carregarCaixaAberto() {
        try {
            caixaAtual = caixaDAO.buscarCaixaAberto();

            if (caixaAtual != null) {
                lblValorInicial.setText("R$ " + caixaAtual.getValorInicial());

                BigDecimal totalVendas = caixaDAO.calcularTotalVendas(caixaAtual.getIdCaixa());
                lblTotalVendas.setText("R$ " + totalVendas);

                BigDecimal sangrias = caixaDAO.calcularSangrias(caixaAtual.getIdCaixa());
                lblSangrias.setText("R$ " + sangrias);

                lblStatus.setText("Aberto");

                btnAbrirCaixa.setEnabled(false);
                btnAbrirCaixa.setText("Caixa já aberto");
                btnFecharCaixa.setEnabled(true);

                carregarComandas(caixaAtual.getIdCaixa());

            } else {
                lblValorInicial.setText("R$ -");
                lblTotalVendas.setText("-");
                lblSangrias.setText("R$ -");
                lblStatus.setText("Pendente abertura");

                btnAbrirCaixa.setEnabled(true);
                btnAbrirCaixa.setText("Abrir caixa");
                btnFecharCaixa.setEnabled(false);

                modeloTabelaComandas.setRowCount(0); // limpa a tabela
            }

        } catch (RuntimeException e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Não foi possível verificar o caixa no banco de dados.",
                    "Erro de conexão",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    /**
     * Busca as comandas do caixa atual e preenche a tabela.
     */
    private void carregarComandas(int idCaixa) {
        try {
            modeloTabelaComandas.setRowCount(0); // limpa antes de preencher

            List<CaixaDAO.ComandaResumo> comandas = caixaDAO.listarComandasDoCaixa(idCaixa);

            for (CaixaDAO.ComandaResumo c : comandas) {
                modeloTabelaComandas.addRow(new Object[]{
                        "Mesa " + c.numeroMesa,
                        c.nomeCliente,
                        c.status,
                        "R$ " + c.valorTotal
                });
            }

        } catch (RuntimeException e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Não foi possível carregar as comandas.",
                    "Erro",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    /**
     * Ação do botão "Abrir caixa": pede o valor inicial e salva no banco.
     */
    private void abrirCaixaClicado() {

        Usuario usuarioLogado = SessaoUsuario.getUsuarioLogado();

        if (usuarioLogado == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Nenhum usuário logado. Faça login novamente.",
                    "Erro",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        String valorDigitado = JOptionPane.showInputDialog(
                this,
                "Informe o valor inicial do caixa (ex: 100.00):",
                "Abrir caixa",
                JOptionPane.QUESTION_MESSAGE
        );

        if (valorDigitado == null || valorDigitado.isBlank()) {
            return; // usuário cancelou
        }

        BigDecimal valorInicial;
        try {
            valorInicial = new BigDecimal(valorDigitado.trim().replace(",", "."));
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Valor inválido. Use apenas números (ex: 100.00).",
                    "Erro",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        try {
            br.com.trevizan.espetinhos.model.Caixa novoCaixa =
                    new br.com.trevizan.espetinhos.model.Caixa();
            novoCaixa.setUsuarioAbertura(usuarioLogado);
            novoCaixa.setValorInicial(valorInicial);
            novoCaixa.setStatus("ABERTO");

            caixaDAO.abrirCaixa(novoCaixa);

            JOptionPane.showMessageDialog(
                    this,
                    "Caixa aberto com sucesso!",
                    "Sucesso",
                    JOptionPane.INFORMATION_MESSAGE
            );

            carregarCaixaAberto();

        } catch (RuntimeException e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Erro ao abrir o caixa no banco de dados.",
                    "Erro",
                    JOptionPane.ERROR_MESSAGE
            );
            e.printStackTrace();
        }
    }

    /**
     * Ação do botão "Fechar caixa": confirma, calcula o valor final e salva.
     */
    private void fecharCaixaClicado() {

        if (caixaAtual == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Não há caixa aberto para fechar.",
                    "Erro",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        BigDecimal totalVendas = caixaDAO.calcularTotalVendas(caixaAtual.getIdCaixa());
        BigDecimal sangrias = caixaDAO.calcularSangrias(caixaAtual.getIdCaixa());
        BigDecimal valorFinal = caixaAtual.getValorInicial()
                .add(totalVendas)
                .subtract(sangrias);

        int confirmacao = JOptionPane.showConfirmDialog(
                this,
                "Fechar o caixa com os seguintes valores?\n\n"
                        + "Valor inicial: R$ " + caixaAtual.getValorInicial() + "\n"
                        + "Total de vendas: R$ " + totalVendas + "\n"
                        + "Sangrias: R$ " + sangrias + "\n"
                        + "Valor final: R$ " + valorFinal,
                "Confirmar fechamento",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (confirmacao != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            caixaDAO.fecharCaixa(caixaAtual.getIdCaixa(), valorFinal);

            JOptionPane.showMessageDialog(
                    this,
                    "Caixa fechado com sucesso!",
                    "Sucesso",
                    JOptionPane.INFORMATION_MESSAGE
            );

            carregarCaixaAberto();

        } catch (RuntimeException e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Erro ao fechar o caixa no banco de dados.",
                    "Erro",
                    JOptionPane.ERROR_MESSAGE
            );
            e.printStackTrace();
        }
    }
}