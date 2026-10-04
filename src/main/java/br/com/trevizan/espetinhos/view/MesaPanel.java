package br.com.trevizan.espetinhos.view;

import br.com.trevizan.espetinhos.PadraoJPanel;
import br.com.trevizan.espetinhos.dao.ComandaDAO;
import br.com.trevizan.espetinhos.dao.MesaDAO;
import br.com.trevizan.espetinhos.model.Comanda;
import br.com.trevizan.espetinhos.model.Mesa;
import br.com.trevizan.espetinhos.model.Usuario;
import br.com.trevizan.espetinhos.util.PadraoTela;

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
        // margens do padrão de telas (título na mesma posição da tela de Relatórios)
        telaMesas.setBorder(new EmptyBorder(
                PadraoTela.MARGEM_TOPO, PadraoTela.MARGEM_LATERAL, 28, PadraoTela.MARGEM_LATERAL));

        // ============================================================
        // CABEÇALHO
        // ============================================================
        JPanel cabecalho = new JPanel(new BorderLayout());
        cabecalho.setOpaque(false);

        JPanel blocoTitulo = new JPanel();
        blocoTitulo.setOpaque(false);
        blocoTitulo.setLayout(new BoxLayout(blocoTitulo, BoxLayout.Y_AXIS));

        JLabel titulo = PadraoTela.criarTitulo("MESAS");
        JLabel subtitulo = PadraoTela.criarSubtitulo("Acompanhe as mesas e acesse as comandas em andamento.");

        blocoTitulo.add(titulo);
        blocoTitulo.add(Box.createVerticalStrut(PadraoTela.ESPACO_TITULO_SUBTITULO));
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
        // A grade ajusta a altura dos cards ao espaço disponível, para que
        // todas as mesas caibam na tela sem precisar rolar (ver GradeMesas).
        painelMesas = new GradeMesas();
        painelMesas.setOpaque(false);

        JScrollPane scroll = new JScrollPane(painelMesas);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        // Barra de rolagem invisível (largura 0): não aparece nem ocupa espaço.
        // Normalmente nem é usada, porque a grade encolhe os cards para caber
        // na tela; só entra em ação (pela roda do mouse) se houver tantas
        // mesas que nem os cards na altura mínima caibam.
        // (VERTICAL_SCROLLBAR_NEVER desligaria também a roda do mouse.)
        scroll.getVerticalScrollBar().setPreferredSize(new Dimension(0, 0));
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

            if (mesa.isAtivo()) {
                if (mesa.isLivre()) {
                    livres++;
                } else {
                    ocupadas++;
                }
            }

            painelMesas.add(criarCardMesa(mesa));
        }

        // Card "+" sempre aparece depois da última mesa
        painelMesas.add(criarCardAdicionarMesa());

        lblResumo.setText("LIVRES  " + livres + "     •     OCUPADAS  " + ocupadas);

        painelMesas.revalidate();
        painelMesas.repaint();
    }

    private JPanel criarCardMesa(Mesa mesa) {
        boolean livre = mesa.isLivre();
        boolean ativo = mesa.isAtivo();

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

        StatusBadge badge;

        if (!ativo) {
            badge = new StatusBadge(
                    "INATIVA",
                    new Color(235, 235, 235),
                    COR_SECUNDARIO
            );
        } else {
            badge = new StatusBadge(
                    livre ? "LIVRE" : "OCUPADA",
                    livre ? COR_VERDE_CLARO : COR_VERMELHO_CLARO,
                    livre ? COR_VERDE_ESCURO : COR_VERMELHO
            );
        }

        RoundedButton btnOpcoes = new RoundedButton(
                ativo ? "INATIVAR" : "REATIVAR",
                14
        );

        btnOpcoes.setFont(new Font("Arial", Font.BOLD, 9));
        btnOpcoes.setPreferredSize(new Dimension(75, 28));
        btnOpcoes.setFocusPainted(false);

        if (ativo) {
            // Inativar
            btnOpcoes.setForeground(new Color(185, 75, 60));
            btnOpcoes.setBackground(new Color(250, 230, 226));
            btnOpcoes.setHoverColor(new Color(245, 210, 204));
            btnOpcoes.setToolTipText("Inativar mesa");

        } else {
            // Reativar
            btnOpcoes.setForeground(COR_VERDE_ESCURO);
            btnOpcoes.setBackground(COR_VERDE_CLARO);
            btnOpcoes.setHoverColor(new Color(210, 235, 218));
            btnOpcoes.setToolTipText("Reativar mesa");
        }

        btnOpcoes.setCursor(
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        );

        btnOpcoes.addActionListener(e ->
                alterarSituacaoMesa(mesa)
        );


        JPanel ladoDireito = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        ladoDireito.setOpaque(false);

        ladoDireito.add(badge);

        if (!ativo || livre) {
            ladoDireito.add(btnOpcoes);
        }

        topo.add(numeroPequeno, BorderLayout.WEST);
        topo.add(ladoDireito, BorderLayout.EAST);

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

        String textoBotao;

        if (!ativo) {
            textoBotao = "REATIVAR";
        } else if (livre) {
            textoBotao = "ABRIR MESA";
        } else {
            textoBotao = "VER COMANDAS";
        }

        RoundedButton botao = new RoundedButton(textoBotao, 14);
        botao.setFont(new Font("Arial", Font.BOLD, 12));
        botao.setPreferredSize(new Dimension(0, 38));
        if (!ativo) {
            botao.setBackground(COR_SECUNDARIO);
        } else {
            botao.setBackground(livre ? COR_VERDE : COR_VERMELHO);
        }
        botao.setForeground(Color.WHITE);
        if (!ativo) {
            botao.setHoverColor(new Color(90, 90, 90));
        } else {
            botao.setHoverColor(
                    livre
                            ? COR_VERDE_ESCURO
                            : new Color(150, 66, 54)
            );
        }
        botao.addActionListener(e -> {

            if (!mesa.isAtivo()) {
                alterarSituacaoMesa(mesa);
            } else {
                abrirMesa(mesa);
            }

        });

        card.add(topo, BorderLayout.NORTH);
        card.add(centro, BorderLayout.CENTER);

        if (ativo) {
            card.add(botao, BorderLayout.SOUTH);
        }

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

    private void alterarSituacaoMesa(Mesa mesa) {

        // =============================================
        // REATIVAR
        // =============================================
        if (!mesa.isAtivo()) {

            int resposta = JOptionPane.showConfirmDialog(
                    this,
                    "Deseja reativar a Mesa " + mesa.getNumero() + "?",
                    "Reativar Mesa",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
            );

            if (resposta != JOptionPane.YES_OPTION) {
                return;
            }

            try {

                mesaDAO.atualizarAtivo(
                        mesa.getIdMesa(),
                        true
                );

                carregarMesas();

            } catch (RuntimeException e) {

                JOptionPane.showMessageDialog(
                        this,
                        "Erro ao reativar a mesa:\n" + e.getMessage(),
                        "Erro",
                        JOptionPane.ERROR_MESSAGE
                );
            }

            return;
        }

        // =============================================
        // NÃO PERMITE INATIVAR MESA OCUPADA
        // =============================================
        if (mesa.isOcupada()) {

            JOptionPane.showMessageDialog(
                    this,
                    "Não é possível inativar a Mesa "
                            + mesa.getNumero()
                            + " enquanto houver uma comanda aberta.",
                    "Mesa ocupada",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        // =============================================
        // INATIVAR
        // =============================================
        int resposta = JOptionPane.showConfirmDialog(
                this,
                "Deseja inativar a Mesa " + mesa.getNumero() + "?",
                "Inativar Mesa",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (resposta != JOptionPane.YES_OPTION) {
            return;
        }

        try {

            mesaDAO.atualizarAtivo(
                    mesa.getIdMesa(),
                    false
            );

            carregarMesas();

        } catch (RuntimeException e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Erro ao inativar a mesa:\n" + e.getMessage(),
                    "Erro",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private JPanel criarCardAdicionarMesa() {

        RoundedPanel card = new RoundedPanel(22, COR_CARD);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(16, 16, 16, 16));
        card.setPreferredSize(new Dimension(230, 200));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel mais = new JLabel("+", SwingConstants.CENTER);
        mais.setFont(new Font("Arial", Font.PLAIN, 72));
        mais.setForeground(COR_VERDE);

        JLabel texto = new JLabel(
                "ADICIONAR MESA",
                SwingConstants.CENTER
        );

        texto.setFont(new Font("Arial", Font.BOLD, 12));
        texto.setForeground(COR_VERDE_ESCURO);

        JPanel centro = new JPanel();
        centro.setOpaque(false);
        centro.setLayout(new BoxLayout(centro, BoxLayout.Y_AXIS));

        mais.setAlignmentX(Component.CENTER_ALIGNMENT);
        texto.setAlignmentX(Component.CENTER_ALIGNMENT);

        centro.add(Box.createVerticalGlue());
        centro.add(mais);
        centro.add(Box.createVerticalStrut(5));
        centro.add(texto);
        centro.add(Box.createVerticalGlue());

        card.add(centro, BorderLayout.CENTER);

        MouseAdapter adicionarAoClicar = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                adicionarMesa();
            }
        };

        card.addMouseListener(adicionarAoClicar);
        centro.addMouseListener(adicionarAoClicar);
        mais.addMouseListener(adicionarAoClicar);
        texto.addMouseListener(adicionarAoClicar);

        return card;
    }

    private void adicionarMesa() {

        int resposta = JOptionPane.showConfirmDialog(
                this,
                "Deseja adicionar uma nova mesa?",
                "Adicionar Mesa",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (resposta != JOptionPane.YES_OPTION) {
            return;
        }

        try {

            mesaDAO.adicionarMesa();

            carregarMesas();

            JOptionPane.showMessageDialog(
                    this,
                    "Mesa adicionada com sucesso!",
                    "Sucesso",
                    JOptionPane.INFORMATION_MESSAGE
            );

        } catch (RuntimeException e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Erro ao adicionar mesa:\n" + e.getMessage(),
                    "Erro",
                    JOptionPane.ERROR_MESSAGE
            );

            e.printStackTrace();
        }
    }

    private void abrirMesa(Mesa mesa) {

        if (mesa.isLivre()) {

            String nomeCliente = JOptionPane.showInputDialog(
                    this,
                    "Informe o nome do cliente:",
                    "Abrir Mesa " + mesa.getNumero(),
                    JOptionPane.QUESTION_MESSAGE
            );


            // Usuário clicou em Cancelar ou fechou a janela
            if (nomeCliente == null) {
                return;
            }

            nomeCliente = nomeCliente.trim();

            // Não permite nome vazio
            if (nomeCliente.isEmpty()) {
                JOptionPane.showMessageDialog(
                        this,
                        "Informe o nome do cliente para abrir a comanda.",
                        "Atenção",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            try {
                int idComanda = comandaDAO.abrirComanda(
                        mesa.getIdMesa(),
                        usuarioLogado.getIdUsuario(),
                        nomeCliente
                );

                Comanda comanda = comandaDAO.buscarPorId(idComanda);

                mesaDAO.atualizarStatus(
                        mesa.getIdMesa(),
                        "OCUPADA"
                );

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

        mostrarComandasDaMesa(mesa);

    }


    }

private void mostrarComandasDaMesa(Mesa mesa) {

    try {

        java.util.List<Comanda> comandas =
                comandaDAO.listarComandasAbertasPorMesa(
                        mesa.getIdMesa()
                );

        if (comandas.isEmpty()) {

            JOptionPane.showMessageDialog(
                    this,
                    "A mesa está marcada como ocupada, mas não possui comandas abertas.",
                    "Comandas não encontradas",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        JPanel painel = new JPanel();
        painel.setLayout(
                new BoxLayout(painel, BoxLayout.Y_AXIS)
        );

        JLabel titulo = new JLabel(
                "Comandas abertas - Mesa " + mesa.getNumero()
        );

        titulo.setFont(
                new Font("Arial", Font.BOLD, 16)
        );

        titulo.setAlignmentX(Component.LEFT_ALIGNMENT);

        painel.add(titulo);
        painel.add(Box.createVerticalStrut(10));

        for (Comanda comanda : comandas) {

            String nomeCliente = comanda.getNomeCliente();

            if (nomeCliente == null || nomeCliente.isBlank()) {
                nomeCliente = "Cliente sem nome";
            }

            JButton btnComanda = new JButton(
                    nomeCliente + " - Comanda #" + comanda.getIdComanda()
            );

            btnComanda.setAlignmentX(Component.LEFT_ALIGNMENT);

            btnComanda.setMaximumSize(
                    new Dimension(
                            Integer.MAX_VALUE,
                            40
                    )
            );

            btnComanda.addActionListener(e -> {

                Window janela =
                        SwingUtilities.getWindowAncestor(painel);

                if (janela != null) {
                    janela.dispose();
                }

                mostrarComanda(
                        mesa,
                        comanda
                );
            });

            painel.add(btnComanda);
            painel.add(Box.createVerticalStrut(5));
        }

        painel.add(Box.createVerticalStrut(10));

        JButton btnNovaComanda =
                new JButton("+ NOVA COMANDA");

        btnNovaComanda.setAlignmentX(
                Component.LEFT_ALIGNMENT
        );

        btnNovaComanda.setMaximumSize(
                new Dimension(
                        Integer.MAX_VALUE,
                        40
                )
        );

        JOptionPane optionPane =
                new JOptionPane(
                        painel,
                        JOptionPane.PLAIN_MESSAGE,
                        JOptionPane.DEFAULT_OPTION,
                        null,
                        new Object[]{btnNovaComanda, "Fechar"}
                );

        JDialog dialog =
                optionPane.createDialog(
                        this,
                        "Mesa " + mesa.getNumero()
                );

        btnNovaComanda.addActionListener(e -> {

            dialog.dispose();

            abrirNovaComanda(mesa);
        });

        dialog.setVisible(true);

    } catch (RuntimeException e) {

        JOptionPane.showMessageDialog(
                this,
                "Erro ao carregar comandas da mesa:\n"
                        + e.getMessage(),
                "Erro",
                JOptionPane.ERROR_MESSAGE
        );

        e.printStackTrace();
    }
}

private void abrirNovaComanda(Mesa mesa) {

    String nomeCliente =
            JOptionPane.showInputDialog(
                    this,
                    "Informe o nome do cliente:",
                    "Nova Comanda - Mesa " + mesa.getNumero(),
                    JOptionPane.QUESTION_MESSAGE
            );

    if (nomeCliente == null) {
        return;
    }

    nomeCliente = nomeCliente.trim();

    if (nomeCliente.isEmpty()) {

        JOptionPane.showMessageDialog(
                this,
                "Informe o nome do cliente.",
                "Atenção",
                JOptionPane.WARNING_MESSAGE
        );

        return;
    }

    try {

        int idComanda =
                comandaDAO.abrirComanda(
                        mesa.getIdMesa(),
                        usuarioLogado.getIdUsuario(),
                        nomeCliente
                );

        Comanda comanda =
                comandaDAO.buscarPorId(idComanda);

        mostrarComanda(
                mesa,
                comanda
        );

    } catch (RuntimeException e) {

        JOptionPane.showMessageDialog(
                this,
                "Erro ao abrir nova comanda:\n"
                        + e.getMessage(),
                "Erro",
                JOptionPane.ERROR_MESSAGE
        );

        e.printStackTrace();
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

    /**
     * Grade de mesas com 4 colunas cuja altura dos cards se adapta à tela:
     * os cards usam a altura normal (200) quando cabem e diminuem até
     * ALTURA_MIN_CARD para que todas as linhas fiquem visíveis sem rolagem.
     * Só se nem na altura mínima couber (muitas mesas / tela pequena) a
     * grade passa a rolar.
     */
    private static class GradeMesas extends JPanel implements Scrollable {

        private static final int COLUNAS = 4;
        private static final int ESPACO = 18;
        private static final int LARGURA_CARD = 230;
        private static final int ALTURA_CARD = 200;


        GradeMesas() {
            setLayout(new LayoutGrade());
        }

        private int linhas() {
            return (getComponentCount() + COLUNAS - 1) / COLUNAS;
        }

        private int alturaPara(int alturaCard) {
            int linhas = linhas();
            Insets in = getInsets();
            return linhas == 0 ? 0 : linhas * alturaCard + (linhas - 1) * ESPACO + in.top + in.bottom;
        }

        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle visivel, int orientacao, int direcao) {
            return 16;
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle visivel, int orientacao, int direcao) {
            return visivel.height;
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        /** Ocupa exatamente a altura visível sempre que os cards couberem na altura mínima. */
        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }

        private class LayoutGrade implements LayoutManager {

            @Override
            public void addLayoutComponent(String nome, Component componente) {
            }

            @Override
            public void removeLayoutComponent(Component componente) {
            }

            @Override
            public Dimension preferredLayoutSize(Container pai) {
                Insets in = pai.getInsets();
                // Altura mínima: só é usada quando a grade não cabe na tela e
                // precisa rolar — assim a rolagem necessária é a menor possível.
                return new Dimension(
                        COLUNAS * LARGURA_CARD + (COLUNAS - 1) * ESPACO + in.left + in.right,
                        alturaPara(ALTURA_CARD));
            }

            @Override
            public Dimension minimumLayoutSize(Container pai) {
                return preferredLayoutSize(pai);
            }

            @Override
            public void layoutContainer(Container pai) {
                int total = pai.getComponentCount();
                int linhas = linhas();
                if (linhas == 0) {
                    return;
                }

                Insets in = pai.getInsets();
                int largura = pai.getWidth() - in.left - in.right;
                int altura = pai.getHeight() - in.top - in.bottom;

                int larguraCard =
                        (largura - (COLUNAS - 1) * ESPACO) / COLUNAS;

                int alturaCard = ALTURA_CARD;

                for (int i = 0; i < total; i++) {
                    int coluna = i % COLUNAS;
                    int linha = i / COLUNAS;
                    pai.getComponent(i).setBounds(
                            in.left + coluna * (larguraCard + ESPACO),
                            in.top + linha * (alturaCard + ESPACO),
                            larguraCard,
                            alturaCard);
                }
            }
        }
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
