package br.com.trevizan.espetinhos.view;

import br.com.trevizan.espetinhos.PadraoJPanel;
import br.com.trevizan.espetinhos.dao.MesaDAO;
import br.com.trevizan.espetinhos.model.Mesa;
import br.com.trevizan.espetinhos.model.Usuario;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;
import br.com.trevizan.espetinhos.dao.ComandaDAO;
import br.com.trevizan.espetinhos.model.Comanda;

public class MesaPanel extends PadraoJPanel {

    private final Usuario usuarioLogado;
    private final MesaDAO mesaDAO;
    private final JPanel painelMesas;
    private final ComandaDAO comandaDAO = new ComandaDAO();

    public MesaPanel(Usuario usuarioLogado) {
        this.usuarioLogado = usuarioLogado;
        mesaDAO = new MesaDAO();

        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(30, 30, 30, 30));

        // Título
        JLabel lblTitulo = new JLabel("MESAS");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 28));

        JPanel painelTopo = new JPanel(new BorderLayout());
        painelTopo.setOpaque(false);
        painelTopo.add(lblTitulo, BorderLayout.WEST);

        add(painelTopo, BorderLayout.NORTH);

        // Área onde os cards das mesas serão exibidos
        painelMesas = new JPanel();
        painelMesas.setOpaque(false);
        painelMesas.setLayout(new GridLayout(0, 4, 20, 20));
        painelMesas.setBorder(new EmptyBorder(30, 0, 0, 0));

        JScrollPane scrollPane = new JScrollPane(painelMesas);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        add(scrollPane, BorderLayout.CENTER);

        carregarMesas();
    }

    private void carregarMesas() {

        painelMesas.removeAll();

        List<Mesa> mesas = mesaDAO.listarTodas();

        for (Mesa mesa : mesas) {
            painelMesas.add(criarCardMesa(mesa));
        }

        painelMesas.revalidate();
        painelMesas.repaint();
    }

    private JPanel criarCardMesa(Mesa mesa) {

        JPanel card = new JPanel();
        card.setLayout(new BorderLayout());
        card.setPreferredSize(new Dimension(180, 140));
        card.setBorder(BorderFactory.createLineBorder(
                new Color(120, 120, 120),
                1
        ));

        boolean livre = mesa.isLivre();

        if (livre) {
            card.setBackground(new Color(230, 245, 230));
        } else {
            card.setBackground(new Color(245, 220, 220));
        }

        JLabel lblMesa = new JLabel(
                "Mesa " + mesa.getNumero(),
                SwingConstants.CENTER
        );

        lblMesa.setFont(new Font("Arial", Font.BOLD, 22));

        JLabel lblStatus = new JLabel(
                mesa.getStatus(),
                SwingConstants.CENTER
        );

        lblStatus.setFont(new Font("Arial", Font.BOLD, 15));

        JButton btnAbrir = new JButton(
                livre ? "Abrir mesa" : "Ver comanda"
        );

        btnAbrir.setFocusPainted(false);

        btnAbrir.addActionListener(e -> abrirMesa(mesa));

        JPanel painelCentro = new JPanel();
        painelCentro.setOpaque(false);
        painelCentro.setLayout(new GridLayout(2, 1));

        painelCentro.add(lblMesa);
        painelCentro.add(lblStatus);

        JPanel painelBotao = new JPanel(new BorderLayout());
        painelBotao.setOpaque(false);
        painelBotao.setBorder(new EmptyBorder(10, 15, 15, 15));
        painelBotao.add(btnAbrir, BorderLayout.CENTER);

        card.add(painelCentro, BorderLayout.CENTER);
        card.add(painelBotao, BorderLayout.SOUTH);

        return card;
    }

    private void abrirMesa(Mesa mesa) {

        if (mesa.isLivre()) {

            int resposta = JOptionPane.showConfirmDialog(
                    this,
                    "Deseja abrir a Mesa " + mesa.getNumero() + "?",
                    "Abrir mesa",
                    JOptionPane.YES_NO_OPTION
            );

            if (resposta == JOptionPane.YES_OPTION) {

                mesaDAO.atualizarStatus(
                        mesa.getIdMesa(),
                        "OCUPADA"
                );

                carregarMesas();
            }

        } else {

            JOptionPane.showMessageDialog(
                    this,
                    "Mesa " + mesa.getNumero()
                            + " está ocupada.\n"
                            + "Aqui será aberta a comanda desta mesa."
            );
        }
    }

    public void atualizarMesas() {
        carregarMesas();
    }
}