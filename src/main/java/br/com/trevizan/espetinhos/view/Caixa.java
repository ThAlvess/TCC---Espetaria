package br.com.trevizan.espetinhos.view;

import javax.swing.*;
import java.awt.*;

/**
 *
 * @author thiag
 */
public class Caixa extends javax.swing.JPanel {

    public Caixa() {
        initComponents();
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
        JButton btnAbrirCaixa = new JButton("Abrir caixa");
        btnAbrirCaixa.setBackground(new Color(27, 94, 32));
        btnAbrirCaixa.setForeground(Color.WHITE);
        btnAbrirCaixa.setFont(new Font("SansSerif", Font.BOLD, 14));
        btnAbrirCaixa.setFocusPainted(false);
        btnAbrirCaixa.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnAbrirCaixa.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        // Linha de 4 cards
        JPanel painelCards = new JPanel(new GridLayout(1, 4, 15, 0));
        painelCards.setOpaque(false);
        painelCards.setAlignmentX(Component.LEFT_ALIGNMENT);
        painelCards.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));

        painelCards.add(criarCard("Valor Inicial", "R$ -"));
        painelCards.add(criarCard("Total de vendas", "-"));
        painelCards.add(criarCard("Sangrias", "R$ -"));
        painelCards.add(criarCard("Status", "Pendente abertura"));

        painelCorpo.add(btnAbrirCaixa);
        painelCorpo.add(Box.createRigidArea(new Dimension(0, 15)));
        painelCorpo.add(painelCards);

        this.add(painelCabecalho, BorderLayout.NORTH);
        this.add(painelCorpo, BorderLayout.CENTER);
    }

    /**
     * Cria um "card" cinza claro com um rótulo em cima e um valor embaixo.
     */
    private JPanel criarCard(String titulo, String valor) {
        JPanel card = new JPanel();
        card.setBackground(new Color(224, 224, 224));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblTitulo.setForeground(new Color(80, 80, 80));
        lblTitulo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblValor = new JLabel(valor);
        lblValor.setFont(new Font("SansSerif", Font.BOLD, 16));
        lblValor.setForeground(new Color(40, 40, 40));
        lblValor.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(lblTitulo);
        card.add(Box.createRigidArea(new Dimension(0, 6)));
        card.add(lblValor);

        return card;
    }
}