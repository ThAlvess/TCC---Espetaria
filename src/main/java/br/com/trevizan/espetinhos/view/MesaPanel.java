package br.com.trevizan.espetinhos.view;

import br.com.trevizan.espetinhos.PadraoJPanel;
import br.com.trevizan.espetinhos.dao.ComandaDAO;
import br.com.trevizan.espetinhos.dao.MesaDAO;
import br.com.trevizan.espetinhos.model.Comanda;
import br.com.trevizan.espetinhos.model.Mesa;
import br.com.trevizan.espetinhos.model.Usuario;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class MesaPanel extends PadraoJPanel {

    private static final Color COR_FUNDO = new Color(238, 232, 227);
    private static final Color COR_CARD = Color.WHITE;
    private static final Color COR_VERDE = new Color(18, 115, 36);
    private static final Color COR_VERDE_ESCURO = new Color(18, 115, 36);
    private static final Color COR_VERDE_CLARO = new Color(231, 240, 235);
    private static final Color COR_VERMELHO = new Color(179, 82, 67);
    private static final Color COR_VERMELHO_CLARO = new Color(248, 233, 230);
    private static final Color COR_TEXTO = new Color(43, 43, 43);
    private static final Color COR_SECUNDARIO = new Color(112, 112, 112);
    private static final Color COR_BORDA = new Color(222, 216, 211);

    private final Usuario usuarioLogado;
    private final MesaDAO mesaDAO;
    private final ComandaDAO comandaDAO;

    private final CardLayout cardLayout;
    private final JPanel painelPrincipal;
    private final JPanel painelMesas;
    private final JLabel lblResumo;

    public MesaPanel(Usuario usuarioLogado) {
        this.usuarioLogado = usuarioLogado;
        this.mesaDAO = new MesaDAO();
        this.comandaDAO = new ComandaDAO();

        setBackground(COR_FUNDO);
        setLayout(new BorderLayout());

        cardLayout = new CardLayout();
        painelPrincipal = new JPanel(cardLayout);
        painelPrincipal.setOpaque(false);
        add(painelPrincipal, BorderLayout.CENTER);

        JPanel telaMesas = new JPanel(new BorderLayout(0, 22));
        telaMesas.setOpaque(false);
        telaMesas.setBorder(new EmptyBorder(28, 30, 28, 30));

        // ============================================================
        // CABEÇALHO
        // ============================================================
        JPanel cabecalho = new JPanel(new BorderLayout());
        cabecalho.setOpaque(false);

        JPanel blocoTitulo = new JPanel();
        blocoTitulo.setOpaque(false);
        blocoTitulo.setLayout(new BoxLayout(blocoTitulo, BoxLayout.Y_AXIS));

        JLabel titulo = new JLabel("MESAS");
        titulo.setFont(new Font("Arial", Font.BOLD, 30));
        titulo.setForeground(COR_TEXTO);
        titulo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitulo = new JLabel("Acompanhe as mesas e acesse as comandas em andamento.");
        subtitulo.setFont(new Font("Arial", Font.PLAIN, 13));
        subtitulo.setForeground(COR_SECUNDARIO);
        subtitulo.setAlignmentX(Component.LEFT_ALIGNMENT);

        blocoTitulo.add(titulo);
        blocoTitulo.add(Box.createVerticalStrut(5));
        blocoTitulo.add(subtitulo);

        lblResumo = new JLabel();
        lblResumo.setFont(new Font("Arial", Font.BOLD, 12));
        lblResumo.setForeground(COR_SECUNDARIO);

        RoundedPanel resumo = new RoundedPanel(16, COR_CARD);
        resumo.setLayout(new FlowLayout(FlowLayout.CENTER, 16, 10));
        resumo.setBorder(new EmptyBorder(0, 10, 0, 10));
        resumo.add(lblResumo);

        cabecalho.add(blocoTitulo, BorderLayout.WEST);
        cabecalho.add(resumo, BorderLayout.EAST);
        telaMesas.add(cabecalho, BorderLayout.NORTH);

        // ============================================================
        // GRADE DE MESAS
        // ============================================================
        painelMesas = new JPanel(new GridLayout(0, 4, 18, 18));
        painelMesas.setOpaque(false);

        JPanel gradeWrapper = new JPanel(new BorderLayout());
        gradeWrapper.setOpaque(false);
        gradeWrapper.add(painelMesas, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(gradeWrapper);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        telaMesas.add(scroll, BorderLayout.CENTER);

        painelPrincipal.add(telaMesas, "lista");

        carregarMesas();
        cardLayout.show(painelPrincipal, "lista");
    }

    private void carregarMesas() {
        painelMesas.removeAll();

        List<Mesa> mesas = mesaDAO.listarTodas();
        int livres = 0;
        int ocupadas = 0;

        for (Mesa mesa : mesas) {
            if (mesa.isLivre()) {
                livres++;
            } else {
                ocupadas++;
            }
            painelMesas.add(criarCardMesa(mesa));
        }

        lblResumo.setText("LIVRES  " + livres + "     •     OCUPADAS  " + ocupadas);

        painelMesas.revalidate();
        painelMesas.repaint();
    }

    private JPanel criarCardMesa(Mesa mesa) {
        boolean livre = mesa.isLivre();

        RoundedPanel card = new RoundedPanel(22, COR_CARD);
        card.setLayout(new BorderLayout(0, 12));
        card.setBorder(new EmptyBorder(16, 16, 16, 16));
        card.setPreferredSize(new Dimension(230, 200));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JPanel topo = new JPanel(new BorderLayout());
        topo.setOpaque(false);

        JLabel numeroPequeno = new JLabel("MESA");
        numeroPequeno.setFont(new Font("Arial", Font.BOLD, 11));
        numeroPequeno.setForeground(COR_SECUNDARIO);

        StatusBadge badge = new StatusBadge(
                livre ? "LIVRE" : "OCUPADA",
                livre ? COR_VERDE_CLARO : COR_VERMELHO_CLARO,
                livre ? COR_VERDE_ESCURO : COR_VERMELHO
        );

        topo.add(numeroPequeno, BorderLayout.WEST);
        topo.add(badge, BorderLayout.EAST);

        JPanel centro = new JPanel();
        centro.setOpaque(false);
        centro.setLayout(new BoxLayout(centro, BoxLayout.Y_AXIS));

        JLabel numero = new JLabel(String.valueOf(mesa.getNumero()));
        numero.setFont(new Font("Arial", Font.BOLD, 42));
        numero.setForeground(COR_TEXTO);
        numero.setAlignmentX(Component.CENTER_ALIGNMENT);



        centro.add(Box.createVerticalGlue());
        centro.add(numero);
        centro.add(Box.createVerticalStrut(3));

        centro.add(Box.createVerticalGlue());

        RoundedButton botao = new RoundedButton(livre ? "ABRIR MESA" : "VER COMANDA", 14);
        botao.setFont(new Font("Arial", Font.BOLD, 12));
        botao.setPreferredSize(new Dimension(0, 38));
        botao.setBackground(livre ? COR_VERDE : COR_VERMELHO);
        botao.setForeground(Color.WHITE);
        botao.setHoverColor(livre ? COR_VERDE_ESCURO : new Color(150, 66, 54));
        botao.addActionListener(e -> abrirMesa(mesa));

        card.add(topo, BorderLayout.NORTH);
        card.add(centro, BorderLayout.CENTER);
        card.add(botao, BorderLayout.SOUTH);

        MouseAdapter abrirAoClicar = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                abrirMesa(mesa);
            }
        };
        centro.addMouseListener(abrirAoClicar);
        numero.addMouseListener(abrirAoClicar);


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

            if (resposta != JOptionPane.YES_OPTION) {
                return;
            }

            try {
                int idComanda = comandaDAO.abrirComanda(
                        mesa.getIdMesa(),
                        usuarioLogado.getIdUsuario(),
                        null
                );

                Comanda comanda = comandaDAO.buscarPorId(idComanda);
                mesaDAO.atualizarStatus(mesa.getIdMesa(), "OCUPADA");
                mesa.setStatus("OCUPADA");
                mostrarComanda(mesa, comanda);

            } catch (RuntimeException e) {
                JOptionPane.showMessageDialog(
                        this,
                        "Erro ao abrir a mesa:\n" + e.getMessage(),
                        "Erro",
                        JOptionPane.ERROR_MESSAGE
                );
                e.printStackTrace();
            }

        } else {
            try {
                Comanda comanda = comandaDAO.buscarComandaAbertaPorMesa(mesa.getIdMesa());

                if (comanda == null) {
                    JOptionPane.showMessageDialog(
                            this,
                            "A mesa está marcada como ocupada, mas não existe uma comanda aberta para ela.",
                            "Comanda não encontrada",
                            JOptionPane.WARNING_MESSAGE
                    );
                    return;
                }

                mostrarComanda(mesa, comanda);

            } catch (RuntimeException e) {
                JOptionPane.showMessageDialog(
                        this,
                        "Erro ao carregar a comanda:\n" + e.getMessage(),
                        "Erro",
                        JOptionPane.ERROR_MESSAGE
                );
                e.printStackTrace();
            }
        }
    }

    private void mostrarComanda(Mesa mesa, Comanda comanda) {
        Component[] componentes = painelPrincipal.getComponents();
        for (Component componente : componentes) {
            if (componente instanceof ComandaPanel) {
                painelPrincipal.remove(componente);
            }
        }

        ComandaPanel comandaPanel = new ComandaPanel(
                mesa,
                comanda,
                usuarioLogado,
                this::voltarParaMesas
        );

        painelPrincipal.add(comandaPanel, "comanda");
        cardLayout.show(painelPrincipal, "comanda");
        painelPrincipal.revalidate();
        painelPrincipal.repaint();
    }

    public void voltarParaMesas() {
        carregarMesas();
        cardLayout.show(painelPrincipal, "lista");
    }

    public void atualizarMesas() {
        carregarMesas();
    }

    private static class RoundedPanel extends JPanel {
        private final int radius;
        private final Color backgroundColor;

        public RoundedPanel(int radius, Color backgroundColor) {
            this.radius = radius;
            this.backgroundColor = backgroundColor;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(backgroundColor);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static class StatusBadge extends JPanel {
        private final String texto;
        private final Color fundo;
        private final Color frente;

        public StatusBadge(String texto, Color fundo, Color frente) {
            this.texto = texto;
            this.fundo = fundo;
            this.frente = frente;
            setOpaque(false);
            setPreferredSize(new Dimension(82, 28));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(fundo);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
            g2.setFont(new Font("Arial", Font.BOLD, 10));
            g2.setColor(frente);
            FontMetrics fm = g2.getFontMetrics();
            int x = (getWidth() - fm.stringWidth(texto)) / 2;
            int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
            g2.drawString(texto, x, y);
            g2.dispose();
        }
    }

    private static class RoundedButton extends JButton {
        private final int radius;
        private Color hoverColor;
        private boolean hover;

        public RoundedButton(String text, int radius) {
            super(text);
            this.radius = radius;
            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    hover = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hover = false;
                    repaint();
                }
            });
        }

        public void setHoverColor(Color hoverColor) {
            this.hoverColor = hoverColor;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(hover && hoverColor != null ? hoverColor : getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
            g2.dispose();
            super.paintComponent(g);
        }
    }
}
