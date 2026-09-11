package br.com.trevizan.espetinhos.view;

import br.com.trevizan.espetinhos.dao.CaixaDAO;
import br.com.trevizan.espetinhos.model.Usuario;
import br.com.trevizan.espetinhos.util.SessaoUsuario;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;

/**
 *
 * @author thiag
 */
public class Caixa extends javax.swing.JPanel {

    private final CaixaDAO caixaDAO = new CaixaDAO();
    private br.com.trevizan.espetinhos.model.Caixa caixaAtual;

    private JButton btnAbrirCaixa;
    private JLabel lblValorInicial;
    private JLabel lblTotalVendas;
    private JLabel lblSangrias;
    private JLabel lblStatus;

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

        // ---------- Corpo (botão + cards) ----------
        JPanel painelCorpo = new JPanel();
        painelCorpo.setOpaque(false);
        painelCorpo.setLayout(new BoxLayout(painelCorpo, BoxLayout.Y_AXIS));

        // Botão "Abrir caixa"
        btnAbrirCaixa = new JButton("Abrir caixa");
        btnAbrirCaixa.setBackground(new Color(27, 94, 32));
        btnAbrirCaixa.setForeground(Color.WHITE);
        btnAbrirCaixa.setFont(new Font("SansSerif", Font.BOLD, 14));
        btnAbrirCaixa.setFocusPainted(false);
        btnAbrirCaixa.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnAbrirCaixa.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btnAbrirCaixa.addActionListener(e -> abrirCaixaClicado());

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

        painelCorpo.add(btnAbrirCaixa);
        painelCorpo.add(Box.createRigidArea(new Dimension(0, 15)));
        painelCorpo.add(painelCards);

        this.add(painelCabecalho, BorderLayout.NORTH);
        this.add(painelCorpo, BorderLayout.CENTER);
    }

    /**
     * Cria um "card" cinza claro com um rótulo em cima e um valor embaixo.
     * Agora recebe o JLabel de valor pronto, para podermos atualizá-lo depois.
     */
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
                lblStatus.setText("Aberto");
                btnAbrirCaixa.setEnabled(false);
                btnAbrirCaixa.setText("Caixa já aberto");
            } else {
                lblValorInicial.setText("R$ -");
                lblStatus.setText("Pendente abertura");
                btnAbrirCaixa.setEnabled(true);
                btnAbrirCaixa.setText("Abrir caixa");
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
}