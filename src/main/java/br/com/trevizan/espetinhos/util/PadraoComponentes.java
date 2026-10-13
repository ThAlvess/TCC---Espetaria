package br.com.trevizan.espetinhos.util;

import br.com.trevizan.espetinhos.PanelArredondado;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;

/**
 * Cria e estiliza os componentes visuais usados pelas telas do sistema:
 * botões, cartões, campos de pesquisa e senha, tabelas e ícones.
 *
 * <p>As cores, fontes e medidas ficam nas constantes desta classe.</p>
 *
 * <ul>
 *   <li><b>Botões de ação</b>: cantos com curvatura {@link #ARCO}, Segoe UI
 *       negrito 14, 38 px de altura e ícone opcional.</li>
 *   <li><b>Cartões</b>: brancos, com cantos arredondados (raio 14), título
 *       cinza e selo verde com ícone no canto superior direito.</li>
 *   <li><b>Tabelas</b>: fundo branco, linhas com cores alternadas, sem
 *       grade, cabeçalho com linha inferior, dentro de um cartão branco.</li>
 * </ul>
 */
public final class PadraoComponentes {

    // =====================================================================
    // Cores
    // =====================================================================

    /** Fundo das telas. */
    public static final Color COR_FUNDO = new Color(238, 232, 227);

    /** Verde principal (botões principais, selos, seleção de período). */
    public static final Color VERDE = new Color(25, 100, 25);
    public static final Color VERDE_HOVER = new Color(18, 80, 18);
    public static final Color VERDE_CLARO = new Color(222, 235, 222);

    /** Laranja de destaque (sangria, exportar). */
    public static final Color LARANJA = new Color(210, 84, 43);

    /** Vermelho (ações críticas e status negativos). */
    public static final Color VERMELHO = new Color(190, 55, 35);

    public static final Color COR_TEXTO = new Color(45, 45, 45);
    public static final Color COR_TEXTO_SECUNDARIO = new Color(102, 102, 102);
    /** Texto de apoio (linhas pequenas abaixo dos valores). */
    public static final Color COR_TEXTO_SUAVE = new Color(105, 105, 105);
    public static final Color COR_BORDA = new Color(225, 220, 213);

    /** Linhas da tabela: brancas e "zebradas" com um bege bem claro. */
    public static final Color COR_LINHA_PAR = Color.WHITE;
    public static final Color COR_LINHA_IMPAR = new Color(246, 242, 237);
    public static final Color COR_SELECAO = new Color(222, 235, 222);

    // =====================================================================
    // Fontes e medidas
    // =====================================================================

    public static final Font FONTE_BOTAO = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONTE_TITULO_CARTAO = new Font("Segoe UI", Font.BOLD, 15);
    public static final Font FONTE_ROTULO_KPI = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONTE_VALOR_KPI = new Font("Segoe UI", Font.BOLD, 18);
    public static final Font FONTE_SUBTITULO_KPI = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font FONTE_PESQUISA = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font FONTE_TABELA = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font FONTE_CABECALHO_TABELA = new Font("Segoe UI", Font.BOLD, 13);

    public static final int ALTURA_BOTAO = 38;
    public static final int ALTURA_PESQUISA = 40;
    public static final int ALTURA_LINHA_TABELA = 40;
    public static final int ALTURA_CABECALHO_TABELA = 42;
    public static final int RAIO_CARTAO = 14;

    /** Curvatura dos cantos de botões e campos (igual ao raio dos cartões). */
    public static final int ARCO = RAIO_CARTAO;

    private PadraoComponentes() {
    }

    // =====================================================================
    // Botões
    // =====================================================================

    /** Botão principal (verde, texto branco). */
    public static JButton criarBotaoPrimario(String texto, Icon icone) {
        return criarBotaoPreenchido(texto, icone, VERDE);
    }

    /** Botão de destaque (laranja). */
    public static JButton criarBotaoDestaque(String texto, Icon icone) {
        return criarBotaoPreenchido(texto, icone, LARANJA);
    }

    /** Botão para ações críticas (vermelho), ex.: "Fechar caixa". */
    public static JButton criarBotaoPerigo(String texto, Icon icone) {
        return criarBotaoPreenchido(texto, icone, VERMELHO);
    }

    /**
     * Botão secundário: fundo branco, texto e borda verdes. Usado para
     * navegação ("Voltar", "Histórico de caixas", "Ver detalhes").
     */
    public static JButton criarBotaoSecundario(String texto, Icon icone) {
        JButton botao = criarBotaoBase(texto, icone);
        botao.setBackground(Color.WHITE);
        botao.setForeground(VERDE);
        botao.putClientProperty("FlatLaf.style", "arc: " + ARCO + "; borderColor: " + hex(VERDE)
                + "; focusedBorderColor: " + hex(VERDE)
                + "; hoverBorderColor: " + hex(VERDE_HOVER)
                + "; hoverBackground: " + hex(VERDE_CLARO)
                + "; pressedBackground: " + hex(VERDE_CLARO.darker()));
        return botao;
    }

    private static JButton criarBotaoPreenchido(String texto, Icon icone, Color cor) {
        JButton botao = criarBotaoBase(texto, icone);
        aplicarCoresPreenchido(botao, cor);
        return botao;
    }

    /** Aplica o visual de botão principal (verde) num botão que já existe. */
    public static void aplicarEstiloPrimario(JButton botao) {
        aplicarEstiloBase(botao);
        aplicarCoresPreenchido(botao, VERDE);
    }

    /** Aplica o visual de botão secundário (branco, borda verde) num botão que já existe. */
    public static void aplicarEstiloSecundario(JButton botao) {
        aplicarEstiloBase(botao);
        botao.setBackground(Color.WHITE);
        botao.setForeground(VERDE);
        botao.putClientProperty("FlatLaf.style", "arc: " + ARCO + "; borderColor: " + hex(VERDE)
                + "; focusedBorderColor: " + hex(VERDE)
                + "; hoverBorderColor: " + hex(VERDE_HOVER)
                + "; hoverBackground: " + hex(VERDE_CLARO)
                + "; pressedBackground: " + hex(VERDE_CLARO.darker()));
    }

    /** Estiliza um campo de texto/senha: fundo branco, cantos arredondados e borda verde no foco. */
    public static void aplicarEstiloCampo(JTextField campo) {
        campo.setBackground(Color.WHITE);
        campo.setForeground(COR_TEXTO);
        campo.setFont(FONTE_PESQUISA);
        campo.setMargin(new Insets(2, 8, 2, 8));
        Object estiloAtual = campo.getClientProperty("FlatLaf.style");
        String estilo = "arc: " + ARCO + "; focusedBorderColor: " + hex(VERDE);
        if (estiloAtual instanceof String texto && !texto.isBlank()) {
            estilo = texto + "; " + estilo;
        }
        campo.putClientProperty("FlatLaf.style", estilo);
    }

    private static void aplicarEstiloBase(JButton botao) {
        botao.setFont(FONTE_BOTAO);
        botao.setFocusPainted(false);
        botao.setRequestFocusEnabled(false);
        botao.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private static void aplicarCoresPreenchido(JButton botao, Color cor) {
        botao.setBackground(cor);
        botao.setForeground(Color.WHITE);
        Color escura = escurecer(cor, 0.85f);
        botao.putClientProperty("FlatLaf.style", "arc: " + ARCO + "; borderColor: " + hex(cor)
                + "; focusedBorderColor: " + hex(cor)
                + "; hoverBorderColor: " + hex(escura)
                + "; hoverBackground: " + hex(escura)
                + "; pressedBackground: " + hex(escurecer(cor, 0.75f)));
    }

    /** Cor em hexadecimal (#rrggbb), usada nos estilos do FlatLaf. */
    private static String hex(Color cor) {
        return String.format("#%02x%02x%02x", cor.getRed(), cor.getGreen(), cor.getBlue());
    }

    private static Color escurecer(Color cor, float fator) {
        return new Color(
                Math.round(cor.getRed() * fator),
                Math.round(cor.getGreen() * fator),
                Math.round(cor.getBlue() * fator));
    }

    private static JButton criarBotaoBase(String texto, Icon icone) {
        JButton botao = new JButton(texto, icone) {
            /* Altura fixa; largura calculada pelo texto com uma folga de 8 px. */
            @Override
            public Dimension getPreferredSize() {
                Dimension tamanho = super.getPreferredSize();
                return new Dimension(tamanho.width + 8, ALTURA_BOTAO);
            }

            @Override
            public Dimension getMaximumSize() {
                return getPreferredSize();
            }
        };
        botao.setFont(FONTE_BOTAO);
        botao.setIconTextGap(8);
        botao.setMargin(new Insets(4, 18, 4, 18));
        botao.setFocusPainted(false);
        botao.setRequestFocusEnabled(false);
        botao.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return botao;
    }

    // =====================================================================
    // Cartões
    // =====================================================================

    /** Cartão branco com cantos arredondados. */
    public static PanelArredondado criarCartao() {
        PanelArredondado cartao = new PanelArredondado(RAIO_CARTAO);
        cartao.setBackground(Color.WHITE);
        return cartao;
    }

    /**
     * Cartão de indicador (KPI): rótulo cinza, valor em destaque, linha de
     * apoio opcional e selo verde com ícone no canto superior direito.
     *
     * @param titulo    texto do rótulo (ex.: "Total de vendas")
     * @param lblValor  label que mostra o valor (a tela atualiza o texto dela)
     * @param lblApoio  linha pequena abaixo do valor; pode ser {@code null}
     * @param icone     ícone branco desenhado dentro do selo
     */
    public static PanelArredondado criarCartaoKpi(String titulo, JLabel lblValor, JLabel lblApoio, Icon icone) {
        JLabel lblTitulo = new JLabel(titulo);
        return criarCartaoKpi(lblTitulo, lblValor, lblApoio, icone);
    }

    /** Variante que recebe o label do título (para telas que trocam o texto dele). */
    public static PanelArredondado criarCartaoKpi(JLabel lblTitulo, JLabel lblValor, JLabel lblApoio, Icon icone) {
        PanelArredondado cartao = criarCartao();
        cartao.setLayout(new GridBagLayout());
        cartao.setPreferredSize(new Dimension(220, 96));
        cartao.setMinimumSize(new Dimension(160, 96));

        lblTitulo.setFont(FONTE_ROTULO_KPI);
        lblTitulo.setForeground(COR_TEXTO_SECUNDARIO);
        lblTitulo.setAlignmentX(Component.LEFT_ALIGNMENT);

        lblValor.setFont(FONTE_VALOR_KPI);
        lblValor.setForeground(Color.BLACK);
        lblValor.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel texto = new JPanel();
        texto.setOpaque(false);
        texto.setLayout(new BoxLayout(texto, BoxLayout.Y_AXIS));
        texto.add(lblTitulo);
        texto.add(Box.createVerticalStrut(4));
        texto.add(lblValor);

        if (lblApoio != null) {
            lblApoio.setFont(FONTE_SUBTITULO_KPI);
            lblApoio.setForeground(COR_TEXTO_SUAVE);
            lblApoio.setAlignmentX(Component.LEFT_ALIGNMENT);
            texto.add(Box.createVerticalStrut(2));
            texto.add(lblApoio);
        }

        GridBagConstraints gbcTexto = new GridBagConstraints();
        gbcTexto.gridx = 0;
        gbcTexto.gridy = 0;
        gbcTexto.weightx = 1;
        gbcTexto.weighty = 1;
        gbcTexto.anchor = GridBagConstraints.WEST;
        gbcTexto.fill = GridBagConstraints.HORIZONTAL;
        gbcTexto.insets = new Insets(14, 14, 14, 4);
        cartao.add(texto, gbcTexto);

        GridBagConstraints gbcSelo = new GridBagConstraints();
        gbcSelo.gridx = 1;
        gbcSelo.gridy = 0;
        gbcSelo.weighty = 1;
        gbcSelo.anchor = GridBagConstraints.NORTHEAST;
        gbcSelo.insets = new Insets(10, 4, 4, 12);
        cartao.add(criarSelo(icone, VERDE), gbcSelo);

        return cartao;
    }

    /** Selo arredondado (quadrado verde com o ícone branco no centro). */
    public static JPanel criarSelo(Icon icone, Color corFundo) {
        int tamanho = 32;
        PanelArredondado selo = new PanelArredondado(9);
        selo.setBackground(corFundo);
        selo.setPreferredSize(new Dimension(tamanho, tamanho));
        selo.setMinimumSize(new Dimension(tamanho, tamanho));
        selo.setMaximumSize(new Dimension(tamanho, tamanho));
        selo.setLayout(new GridBagLayout());
        selo.add(new JLabel(icone), new GridBagConstraints());
        return selo;
    }

    /**
     * Cartão branco com título no topo e a tabela embaixo. O cabeçalho pode
     * receber componentes extras à direita (ex.: um botão), passando
     * {@code acoes}.
     */
    public static PanelArredondado criarCartaoTabela(String titulo, JTable tabela, JComponent acoes) {
        PanelArredondado cartao = criarCartao();
        cartao.setLayout(new BorderLayout());

        JPanel cabecalho = new JPanel(new BorderLayout(12, 0));
        cabecalho.setOpaque(false);

        if (titulo != null) {
            JLabel lblTitulo = criarTituloCartao(titulo);
            cabecalho.add(lblTitulo, BorderLayout.WEST);
        }
        if (acoes != null) {
            cabecalho.add(acoes, BorderLayout.EAST);
        }
        cabecalho.setBorder(BorderFactory.createEmptyBorder(
                titulo != null || acoes != null ? 14 : 6, 20, 8, 16));

        if (titulo != null || acoes != null) {
            cartao.add(cabecalho, BorderLayout.NORTH);
        }

        JScrollPane scroll = criarScrollTabela(tabela);
        JPanel corpo = new JPanel(new BorderLayout());
        corpo.setOpaque(false);
        corpo.setBorder(BorderFactory.createEmptyBorder(0, 8, RAIO_CARTAO / 2 + 4, 8));
        corpo.add(scroll, BorderLayout.CENTER);
        cartao.add(corpo, BorderLayout.CENTER);
        return cartao;
    }

    /** Título usado dentro dos cartões (Segoe UI negrito 15, cinza escuro). */
    public static JLabel criarTituloCartao(String texto) {
        JLabel titulo = new JLabel(texto);
        titulo.setFont(FONTE_TITULO_CARTAO);
        titulo.setForeground(COR_TEXTO);
        return titulo;
    }

    // =====================================================================
    // Campo de pesquisa
    // =====================================================================

    /**
     * Campo de pesquisa: texto alinhado à esquerda, ícone de lupa, botão
     * para limpar e cantos arredondados.
     */
    public static JTextField criarCampoPesquisa(String placeholder) {
        JTextField campo = new JTextField() {
            @Override
            public Dimension getPreferredSize() {
                Dimension tamanho = super.getPreferredSize();
                return new Dimension(tamanho.width, ALTURA_PESQUISA);
            }
        };
        aplicarCampoPesquisa(campo, placeholder);
        return campo;
    }

    /** Aplica o estilo de pesquisa em um campo já existente. */
    public static void aplicarCampoPesquisa(JTextField campo, String placeholder) {
        campo.setFont(FONTE_PESQUISA);
        campo.setHorizontalAlignment(JTextField.LEFT);
        campo.setBackground(Color.WHITE);
        campo.setForeground(COR_TEXTO);
        campo.setColumns(24);
        campo.setMargin(new Insets(2, 6, 2, 6));
        campo.putClientProperty("JTextField.placeholderText", placeholder);
        campo.putClientProperty("JTextField.leadingIcon", comEspacoEsquerdo(iconeLupa(COR_TEXTO_SUAVE), 8));
        campo.putClientProperty("JTextField.showClearButton", true);
        campo.putClientProperty("FlatLaf.style", "arc: " + ARCO + "; focusedBorderColor: " + hex(VERDE));
    }

    // =====================================================================
    // Campo de senha
    // =====================================================================

    /**
     * Coloca um botão de "olho" dentro do campo de senha: clicando nele a
     * senha aparece; clicando de novo, volta a ficar oculta.
     */
    public static void habilitarMostrarSenha(javax.swing.JPasswordField campo) {
        // botão de alternância com ícone de 22 px e área de clique de 34 x 30 px
        char mascara = campo.getEchoChar() != 0 ? campo.getEchoChar() : '\u2022';

        javax.swing.JToggleButton botao = new javax.swing.JToggleButton(iconeOlho(COR_TEXTO_SECUNDARIO, false));
        botao.setSelectedIcon(iconeOlho(VERDE, true));
        botao.putClientProperty("JButton.buttonType", "toolBarButton");
        botao.setFocusable(false);
        botao.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        botao.setToolTipText("Mostrar senha");
        Dimension tamanho = new Dimension(34, 30);
        botao.setPreferredSize(tamanho);
        botao.setMinimumSize(tamanho);
        botao.setMaximumSize(tamanho);
        botao.addActionListener(e -> {
            boolean mostrar = botao.isSelected();
            campo.setEchoChar(mostrar ? (char) 0 : mascara);
            botao.setToolTipText(mostrar ? "Ocultar senha" : "Mostrar senha");
            campo.requestFocusInWindow();
        });

        campo.putClientProperty("JTextField.trailingComponent", botao);
    }

    /**
     * Olho grande (22 px) para o botão de mostrar/ocultar senha. Quando a
     * senha está oculta, o olho aparece riscado.
     */
    private static Icon iconeOlho(Color cor, boolean aberto) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
                g2.translate(x, y);
                g2.setColor(cor);
                g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                Path2D olho = new Path2D.Float();
                olho.moveTo(1, 11);
                olho.quadTo(11, 1, 21, 11);
                olho.quadTo(11, 21, 1, 11);
                g2.draw(olho);
                g2.drawOval(7, 7, 8, 8);
                g2.fillOval(9, 9, 4, 4);
                if (!aberto) {
                    g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.drawLine(3, 19, 19, 3);
                }
                g2.dispose();
            }

            @Override
            public int getIconWidth() {
                return 22;
            }

            @Override
            public int getIconHeight() {
                return 22;
            }
        };
    }

    // =====================================================================
    // Tabelas
    // =====================================================================

    /**
     * Aplica o visual padrão (clean e zebrado) em uma tabela. Todas as
     * colunas ficam alinhadas à esquerda; use {@link #alinharColuna} para as
     * colunas de valores (direita) ou códigos curtos (centro).
     */
    public static void estilizarTabela(JTable tabela) {
        tabela.setFont(FONTE_TABELA);
        tabela.setForeground(COR_TEXTO);
        tabela.setBackground(COR_LINHA_PAR);
        tabela.setRowHeight(ALTURA_LINHA_TABELA);
        tabela.setShowGrid(false);
        tabela.setShowHorizontalLines(false);
        tabela.setShowVerticalLines(false);
        tabela.setIntercellSpacing(new Dimension(0, 0));
        tabela.setSelectionBackground(COR_SELECAO);
        tabela.setSelectionForeground(COR_TEXTO);
        tabela.setFillsViewportHeight(true);
        tabela.setFocusable(false);
        tabela.setRowSelectionAllowed(true);

        JTableHeader cabecalho = tabela.getTableHeader();
        cabecalho.setReorderingAllowed(false);
        cabecalho.setFont(FONTE_CABECALHO_TABELA);
        cabecalho.setBackground(Color.WHITE);
        cabecalho.setForeground(COR_TEXTO_SECUNDARIO);
        cabecalho.setPreferredSize(new Dimension(0, ALTURA_CABECALHO_TABELA));
        cabecalho.setDefaultRenderer(new RendererCabecalho(tabela));

        for (int i = 0; i < tabela.getColumnCount(); i++) {
            tabela.getColumnModel().getColumn(i).setCellRenderer(new RendererCelula(SwingConstants.LEFT));
        }
    }

    /** Muda o alinhamento de uma coluna (célula e cabeçalho juntos). */
    public static void alinharColuna(JTable tabela, int coluna, int alinhamento) {
        tabela.getColumnModel().getColumn(coluna).setCellRenderer(new RendererCelula(alinhamento));
    }

    /**
     * Pinta as cores das linhas (zebrado/seleção) em um componente devolvido
     * por um renderer próprio de coluna (status, ações...).
     */
    public static void aplicarCoresLinha(Component componente, JTable tabela, boolean selecionada, int linha) {
        componente.setBackground(selecionada ? COR_SELECAO : (linha % 2 == 0 ? COR_LINHA_PAR : COR_LINHA_IMPAR));
        if (componente instanceof JComponent jc) {
            jc.setOpaque(true);
        }
    }

    /** Scroll da tabela sem bordas e com fundo branco também na área vazia. */
    public static JScrollPane criarScrollTabela(JTable tabela) {
        JScrollPane scroll = new JScrollPane(tabela);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setViewportBorder(null);
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.setBackground(Color.WHITE);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        return scroll;
    }

    /** Renderer das células: zebrado, espaçamento lateral e alinhamento. */
    public static class RendererCelula extends DefaultTableCellRenderer {

        private final int alinhamento;

        public RendererCelula(int alinhamento) {
            this.alinhamento = alinhamento;
        }

        @Override
        public Component getTableCellRendererComponent(JTable tabela, Object valor, boolean selecionada,
                boolean foco, int linha, int coluna) {
            super.getTableCellRendererComponent(tabela, valor, selecionada, false, linha, coluna);
            setHorizontalAlignment(alinhamento);
            setFont(FONTE_TABELA);
            setForeground(selecionada ? VERDE : COR_TEXTO);
            setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 14));
            aplicarCoresLinha(this, tabela, selecionada, linha);
            setToolTipText(valor != null && valor.toString().length() > 40 ? valor.toString() : null);
            return this;
        }
    }

    /**
     * Cabeçalho: fundo branco, texto cinza em negrito e uma linha fina
     * embaixo, com o mesmo alinhamento da coluna.
     */
    private static class RendererCabecalho extends DefaultTableCellRenderer {

        private final JTable tabela;

        RendererCabecalho(JTable tabela) {
            this.tabela = tabela;
        }

        @Override
        public Component getTableCellRendererComponent(JTable t, Object valor, boolean selecionada,
                boolean foco, int linha, int coluna) {
            super.getTableCellRendererComponent(t, valor, false, false, linha, coluna);
            int alinhamento = SwingConstants.LEFT;
            if (coluna >= 0 && coluna < tabela.getColumnCount()) {
                TableCellRenderer renderer = tabela.getColumnModel().getColumn(coluna).getCellRenderer();
                if (renderer instanceof RendererCelula celula) {
                    alinhamento = celula.alinhamento;
                } else if (renderer instanceof DefaultTableCellRenderer padrao) {
                    alinhamento = padrao.getHorizontalAlignment();
                }
            }
            setHorizontalAlignment(alinhamento);
            setFont(FONTE_CABECALHO_TABELA);
            setForeground(COR_TEXTO_SECUNDARIO);
            setBackground(Color.WHITE);
            setOpaque(true);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 2, 0, COR_BORDA),
                    BorderFactory.createEmptyBorder(0, 14, 0, 14)));
            return this;
        }
    }

    // =====================================================================
    // Ícones (desenhados com Graphics2D)
    // =====================================================================

    /** Base dos ícones 18x18 com antialiasing e traço arredondado. */
    private abstract static class IconeDesenhado implements Icon {

        protected final Color cor;

        IconeDesenhado(Color cor) {
            this.cor = cor;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
            g2.translate(x, y);
            // sem cor fixa, usa a cor do texto do componente que desenha o ícone
            g2.setColor(cor != null ? cor : c.getForeground());
            g2.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            desenhar(g2);
            g2.dispose();
        }

        protected abstract void desenhar(Graphics2D g2);

        @Override
        public int getIconWidth() {
            return 18;
        }

        @Override
        public int getIconHeight() {
            return 18;
        }
    }

    public static Icon iconeCifrao(Color cor) {
        return new IconeDesenhado(cor) {
            @Override
            protected void desenhar(Graphics2D g2) {
                g2.setFont(new Font("Segoe UI", Font.BOLD, 15));
                FontMetrics fm = g2.getFontMetrics();
                String texto = "$";
                int tx = (getIconWidth() - fm.stringWidth(texto)) / 2;
                int ty = (getIconHeight() + fm.getAscent() - fm.getDescent()) / 2 - 1;
                g2.drawString(texto, tx, ty);
            }
        };
    }

    public static Icon iconeSacola(Color cor) {
        return new IconeDesenhado(cor) {
            @Override
            protected void desenhar(Graphics2D g2) {
                g2.drawPolygon(new int[]{3, 15, 14, 4}, new int[]{6, 6, 16, 16}, 4);
                g2.drawArc(6, 1, 6, 8, 0, 180);
            }
        };
    }

    public static Icon iconeTendencia(Color cor) {
        return new IconeDesenhado(cor) {
            @Override
            protected void desenhar(Graphics2D g2) {
                int[] xs = {1, 6, 10, 16};
                int[] ys = {13, 8, 11, 2};
                g2.drawPolyline(xs, ys, xs.length);
                g2.fillOval(xs[3] - 2, ys[3] - 2, 4, 4);
            }
        };
    }

    public static Icon iconeCartao(Color cor) {
        return new IconeDesenhado(cor) {
            @Override
            protected void desenhar(Graphics2D g2) {
                g2.drawRoundRect(2, 4, 14, 10, 3, 3);
                g2.fillRect(2, 7, 14, 3);
            }
        };
    }

    public static Icon iconeCalendario(Color cor) {
        return new IconeDesenhado(cor) {
            @Override
            protected void desenhar(Graphics2D g2) {
                g2.setStroke(new BasicStroke(1.4f));
                g2.drawRoundRect(1, 3, 15, 13, 3, 3);
                g2.drawLine(1, 7, 16, 7);
                g2.drawLine(5, 1, 5, 4);
                g2.drawLine(12, 1, 12, 4);
                g2.fillRect(10, 10, 3, 3);
            }
        };
    }

    /** Carteira (valor inicial / saldo). */
    public static Icon iconeCarteira(Color cor) {
        return new IconeDesenhado(cor) {
            @Override
            protected void desenhar(Graphics2D g2) {
                g2.drawRoundRect(1, 4, 15, 12, 3, 3);
                g2.drawLine(3, 4, 12, 1);
                g2.drawRoundRect(10, 8, 6, 4, 2, 2);
                g2.fillOval(12, 9, 2, 2);
            }
        };
    }

    /** Seta saindo de uma bandeja (sangria / retirada). */
    public static Icon iconeSaida(Color cor) {
        return new IconeDesenhado(cor) {
            @Override
            protected void desenhar(Graphics2D g2) {
                g2.drawLine(9, 11, 9, 1);
                g2.drawPolyline(new int[]{5, 9, 13}, new int[]{5, 1, 5}, 3);
                g2.drawPolyline(new int[]{2, 2, 16, 16}, new int[]{11, 16, 16, 11}, 4);
            }
        };
    }

    /** Cadeado aberto (status do caixa / abrir caixa). */
    public static Icon iconeCadeadoAberto(Color cor) {
        return new IconeDesenhado(cor) {
            @Override
            protected void desenhar(Graphics2D g2) {
                g2.drawRoundRect(3, 8, 12, 9, 3, 3);
                g2.drawArc(4, 1, 8, 10, 10, 170);
                g2.fillOval(8, 11, 3, 3);
            }
        };
    }

    /** Cadeado fechado (fechar caixa / caixa fechado). */
    public static Icon iconeCadeadoFechado(Color cor) {
        return new IconeDesenhado(cor) {
            @Override
            protected void desenhar(Graphics2D g2) {
                g2.drawRoundRect(3, 8, 12, 9, 3, 3);
                g2.drawArc(5, 1, 8, 12, 0, 180);
                g2.drawLine(5, 7, 5, 8);
                g2.drawLine(13, 7, 13, 8);
                g2.fillOval(8, 11, 3, 3);
            }
        };
    }

    /** Relógio (histórico). */
    public static Icon iconeHistorico(Color cor) {
        return new IconeDesenhado(cor) {
            @Override
            protected void desenhar(Graphics2D g2) {
                g2.drawOval(2, 2, 14, 14);
                g2.drawPolyline(new int[]{9, 9, 12}, new int[]{5, 9, 11}, 3);
            }
        };
    }

    /** Olho (ver detalhes). */
    public static Icon iconeOlho(Color cor) {
        return new IconeDesenhado(cor) {
            @Override
            protected void desenhar(Graphics2D g2) {
                Path2D olho = new Path2D.Float();
                olho.moveTo(1, 9);
                olho.quadTo(9, 1, 17, 9);
                olho.quadTo(9, 17, 1, 9);
                g2.draw(olho);
                g2.drawOval(6, 6, 6, 6);
                g2.fillOval(8, 8, 2, 2);
            }
        };
    }

    /** Seta para a esquerda (voltar). */
    public static Icon iconeVoltar(Color cor) {
        return new IconeDesenhado(cor) {
            @Override
            protected void desenhar(Graphics2D g2) {
                g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawLine(3, 9, 15, 9);
                g2.drawPolyline(new int[]{8, 3, 8}, new int[]{4, 9, 14}, 3);
            }
        };
    }

    /** Sinal de mais (novo / cadastrar). */
    public static Icon iconeMais(Color cor) {
        return new IconeDesenhado(cor) {
            @Override
            protected void desenhar(Graphics2D g2) {
                g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawLine(9, 3, 9, 15);
                g2.drawLine(3, 9, 15, 9);
            }
        };
    }

    /** Lápis (editar). */
    public static Icon iconeEditar(Color cor) {
        return new IconeDesenhado(cor) {
            @Override
            protected void desenhar(Graphics2D g2) {
                g2.drawPolygon(new int[]{3, 12, 15, 6, 2}, new int[]{12, 3, 6, 15, 16}, 5);
                g2.drawLine(10, 5, 13, 8);
            }
        };
    }

    /** Devolve o mesmo ícone com um espaço vazio à esquerda. */
    public static Icon comEspacoEsquerdo(Icon icone, int espaco) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                icone.paintIcon(c, g, x + espaco, y);
            }

            @Override
            public int getIconWidth() {
                return icone.getIconWidth() + espaco;
            }

            @Override
            public int getIconHeight() {
                return icone.getIconHeight();
            }
        };
    }

    /** Lupa (pesquisa). */
    public static Icon iconeLupa(Color cor) {
        return new IconeDesenhado(cor) {
            @Override
            protected void desenhar(Graphics2D g2) {
                g2.drawOval(2, 2, 10, 10);
                g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawLine(11, 11, 15, 15);
            }
        };
    }

    /** Chave (senha). */
    public static Icon iconeChave(Color cor) {
        return new IconeDesenhado(cor) {
            @Override
            protected void desenhar(Graphics2D g2) {
                g2.drawOval(1, 5, 8, 8);
                g2.drawLine(9, 9, 17, 9);
                g2.drawLine(14, 9, 14, 12);
                g2.drawLine(16, 9, 16, 11);
            }
        };
    }

    /** Engrenagem (configurações). */
    public static Icon iconeEngrenagem(Color cor) {
        return new IconeDesenhado(cor) {
            @Override
            protected void desenhar(Graphics2D g2) {
                double cx = 9, cy = 9;
                Path2D dentes = new Path2D.Double();
                int pontas = 8;
                for (int i = 0; i < pontas * 2; i++) {
                    // alterna raio externo (dente) e interno, 4 pontos por dente
                    double base = Math.PI * 2 * i / (pontas * 2);
                    double raio = (i % 2 == 0) ? 8 : 6;
                    double a1 = base - Math.PI / (pontas * 4);
                    double a2 = base + Math.PI / (pontas * 4);
                    double x1 = cx + raio * Math.cos(a1), y1 = cy + raio * Math.sin(a1);
                    double x2 = cx + raio * Math.cos(a2), y2 = cy + raio * Math.sin(a2);
                    if (i == 0) {
                        dentes.moveTo(x1, y1);
                    } else {
                        dentes.lineTo(x1, y1);
                    }
                    dentes.lineTo(x2, y2);
                }
                dentes.closePath();
                g2.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.draw(dentes);
                g2.drawOval(6, 6, 6, 6);
            }
        };
    }

    /** Porta com seta saindo (sair / logoff). */
    public static Icon iconeSair(Color cor) {
        return new IconeDesenhado(cor) {
            @Override
            protected void desenhar(Graphics2D g2) {
                g2.drawPolyline(new int[]{9, 3, 3, 9}, new int[]{2, 2, 16, 16}, 4);
                g2.drawLine(7, 9, 16, 9);
                g2.drawPolyline(new int[]{12, 16, 12}, new int[]{5, 9, 13}, 3);
            }
        };
    }

    /** Livro aberto (manual). */
    public static Icon iconeManual(Color cor) {
        return new IconeDesenhado(cor) {
            @Override
            protected void desenhar(Graphics2D g2) {
                g2.drawPolygon(new int[]{1, 9, 9, 1}, new int[]{3, 5, 16, 14}, 4);
                g2.drawPolygon(new int[]{17, 9, 9, 17}, new int[]{3, 5, 16, 14}, 4);
            }
        };
    }

    // ---------------------------------------------------------------------
    // Ícones do menu lateral. Com cor null, o ícone usa a cor do texto do
    // botão em que está.
    // ---------------------------------------------------------------------

    /** Prancheta com itens (Pedidos). */
    public static Icon iconePedidos(Color cor) {
        return new IconeDesenhado(cor) {
            @Override
            protected void desenhar(Graphics2D g2) {
                g2.drawRoundRect(3, 3, 12, 14, 3, 3);
                g2.drawRoundRect(6, 1, 6, 3, 2, 2);
                g2.drawLine(6, 8, 12, 8);
                g2.drawLine(6, 11, 12, 11);
                g2.drawLine(6, 14, 10, 14);
            }
        };
    }

    /** Caixa registradora (Caixa). */
    public static Icon iconeCaixaRegistradora(Color cor) {
        return new IconeDesenhado(cor) {
            @Override
            protected void desenhar(Graphics2D g2) {
                g2.drawRoundRect(4, 1, 10, 5, 2, 2);
                g2.drawLine(9, 6, 9, 8);
                g2.drawRoundRect(1, 8, 16, 9, 3, 3);
                g2.drawLine(5, 12, 6, 12);
                g2.drawLine(9, 12, 10, 12);
                g2.drawLine(13, 12, 14, 12);
            }
        };
    }

    /** Espetinho (Produtos). */
    public static Icon iconeEspeto(Color cor) {
        return new IconeDesenhado(cor) {
            @Override
            protected void desenhar(Graphics2D g2) {
                g2.drawLine(2, 16, 16, 2);
                java.awt.geom.AffineTransform original = g2.getTransform();
                int[][] pedacos = {{5, 13}, {9, 9}, {13, 5}};
                for (int[] p : pedacos) {
                    g2.setTransform(original);
                    g2.rotate(-Math.PI / 4, p[0], p[1]);
                    g2.fillRoundRect(p[0] - 3, p[1] - 4, 5, 8, 3, 3);
                }
                g2.setTransform(original);
            }
        };
    }

    /** Gráfico de barras (Relatórios). */
    public static Icon iconeRelatorios(Color cor) {
        return new IconeDesenhado(cor) {
            @Override
            protected void desenhar(Graphics2D g2) {
                g2.drawLine(2, 16, 16, 16);
                g2.fillRoundRect(3, 9, 3, 6, 1, 1);
                g2.fillRoundRect(8, 4, 3, 11, 1, 1);
                g2.fillRoundRect(13, 7, 3, 8, 1, 1);
            }
        };
    }

    /** Duas pessoas (Usuários). */
    public static Icon iconeUsuarios(Color cor) {
        return new IconeDesenhado(cor) {
            @Override
            protected void desenhar(Graphics2D g2) {
                g2.drawOval(4, 2, 5, 5);
                g2.drawArc(1, 9, 11, 10, 0, 180);
                g2.drawOval(11, 4, 4, 4);
                g2.drawArc(10, 10, 8, 8, 0, 120);
            }
        };
    }

    /** Troféu (maior faturamento). */
    public static Icon iconeTrofeu(Color cor) {
        return new IconeDesenhado(cor) {
            @Override
            protected void desenhar(Graphics2D g2) {
                g2.drawPolyline(new int[]{5, 5, 13, 13}, new int[]{2, 8, 8, 2}, 4);
                g2.drawArc(5, 3, 8, 9, 180, 180);
                g2.drawLine(5, 2, 13, 2);
                g2.drawArc(1, 3, 5, 5, 90, 180);
                g2.drawArc(12, 3, 5, 5, 270, 180);
                g2.drawLine(9, 12, 9, 15);
                g2.drawLine(5, 16, 13, 16);
            }
        };
    }
}
