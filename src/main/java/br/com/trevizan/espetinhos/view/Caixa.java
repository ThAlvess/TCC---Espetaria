package br.com.trevizan.espetinhos.view;

import br.com.trevizan.espetinhos.dao.CaixaDAO;
import br.com.trevizan.espetinhos.model.Usuario;
import br.com.trevizan.espetinhos.util.CampoMoeda;
import br.com.trevizan.espetinhos.util.FiltroPeriodo;
import br.com.trevizan.espetinhos.util.PadraoComponentes;
import br.com.trevizan.espetinhos.util.PadraoTela;
import br.com.trevizan.espetinhos.util.SessaoUsuario;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;
import br.com.trevizan.espetinhos.dao.PagamentoDAO;
import br.com.trevizan.espetinhos.dao.MovimentacaoCaixaDAO;

/**
 * Tela de Caixa: caixa atual, histórico de caixas e detalhes de um caixa.
 *
 * <p>Os componentes visuais (botões, cartões de indicadores, tabelas e
 * filtro de datas) são criados com {@link PadraoComponentes} e
 * {@link br.com.trevizan.espetinhos.util.FiltroPeriodo}.</p>
 *
 * @author thiag
 */
public class Caixa extends javax.swing.JPanel {

    private final MovimentacaoCaixaDAO movimentacaoCaixaDAO =
            new MovimentacaoCaixaDAO();

    private JButton btnSangria;

    private final PagamentoDAO pagamentoDAO = new PagamentoDAO();

    private JPanel painelDetalhes;

    private JLabel lblDetalheTitulo;
    private JLabel lblDetalhePeriodo;
    private JLabel lblDetalheOperador;

    private JLabel lblDetalheInicial;
    private JLabel lblDetalheVendas;
    private JLabel lblDetalheSangrias;
    private JLabel lblDetalheFinal;
    private JLabel lblApoioDetalheVendas;
    private JLabel lblApoioDetalheSangrias;
    private JLabel lblApoioDetalheFinal;

    private DefaultTableModel modeloTabelaPagamentos;
    private JTable tabelaPagamentos;

    private DefaultTableModel modeloTabelaComandasDetalhes;
    private JTable tabelaComandasDetalhes;

    private JButton btnVerDetalhes;
    private JButton btnVoltarHistorico;


    private final CaixaDAO caixaDAO = new CaixaDAO();
    private br.com.trevizan.espetinhos.model.Caixa caixaAtual;

    private JButton btnAbrirCaixa;
    private JButton btnFecharCaixa;
    private JLabel lblValorInicial;
    private JLabel lblTotalVendas;
    private JLabel lblSangrias;
    private JLabel lblStatus;
    private JLabel lblApoioValorInicial;
    private JLabel lblApoioVendas;
    private JLabel lblApoioSangrias;
    private JLabel lblApoioStatus;
    private JLabel lblNumeroCaixa;
    private JLabel lblDataCaixa;
    private DefaultTableModel modeloTabelaComandas;
    private JTable tabelaComandas;
    private JPanel painelAtual;
    private JPanel painelHistorico;

    private DefaultTableModel modeloTabelaHistorico;
    private JTable tabelaHistorico;

    private JButton btnVerHistorico;
    private JButton btnVoltarCaixa;
    private JPanel painelCentral;
    private CardLayout cardLayoutCaixa;
    private FiltroPeriodo filtroHistorico;

    private JLabel lblQtdCaixas;
    private JLabel lblFaturamentoPeriodo;
    private JLabel lblApoioFaturamentoPeriodo;
    private JLabel lblMaiorFaturamento;
    private JLabel lblApoioMaiorFaturamento;
    private JLabel lblSangriasPeriodo;
    private JLabel lblResumoHistorico;
    private DefaultTableModel modeloTabelaMovimentacoes;
    private JTable tabelaMovimentacoes;
    private JLabel lblTituloDetalheFinal;

    /** Subtítulo das seções (ex.: "Caixa #3", "Histórico de caixas"). */
    private static final Font FONTE_SECAO = new Font("Segoe UI", Font.BOLD, 20);
    private static final Font FONTE_INFO = new Font("Segoe UI", Font.PLAIN, 14);
    /** Cor das informações do caixa (data, horário, operador). */
    private static final Color COR_INFO = new Color(60, 60, 60);


    public Caixa() {
        initComponents();
        carregarCaixaAberto();
    }

    /**
     * Atualiza os dados da tela consultando o banco novamente.
     * Deve ser chamado toda vez que a tela de Caixa for exibida.
     */
    public void atualizarDados() {
        carregarCaixaAberto();
    }


    private void initComponents() {
        this.setLayout(new BorderLayout(0, 18));
        this.setBackground(PadraoComponentes.COR_FUNDO);
        // margens do padrão de telas (título na mesma posição da tela de Relatórios)
        this.setBorder(BorderFactory.createEmptyBorder(
                PadraoTela.MARGEM_TOPO, PadraoTela.MARGEM_LATERAL, 30, PadraoTela.MARGEM_LATERAL));

        // ---------- Cabeçalho ----------
        JLabel lblTitulo = PadraoTela.criarTitulo("CAIXA");

        // ---------- Corpo do caixa atual ----------
        JPanel painelCorpo = new JPanel(new BorderLayout(0, 18));
        painelCorpo.setOpaque(false);

        JPanel topoCorpo = new JPanel();
        topoCorpo.setOpaque(false);
        topoCorpo.setLayout(new BoxLayout(topoCorpo, BoxLayout.Y_AXIS));

        // Identificação do caixa atual
        lblNumeroCaixa = criarTituloSecao("Nenhum caixa aberto");
        lblDataCaixa = criarInfo("Aguardando abertura");

        JPanel painelIdentificacao = new JPanel();
        painelIdentificacao.setOpaque(false);
        painelIdentificacao.setLayout(new BoxLayout(painelIdentificacao, BoxLayout.Y_AXIS));
        painelIdentificacao.add(lblNumeroCaixa);
        painelIdentificacao.add(Box.createVerticalStrut(4));
        painelIdentificacao.add(lblDataCaixa);

        // Botões de ação
        btnAbrirCaixa = PadraoComponentes.criarBotaoPrimario(
                "Abrir caixa", PadraoComponentes.iconeCadeadoAberto(Color.WHITE));
        btnAbrirCaixa.addActionListener(e -> abrirCaixaClicado());

        btnSangria = PadraoComponentes.criarBotaoDestaque(
                "Registrar sangria", PadraoComponentes.iconeSaida(Color.WHITE));
        btnSangria.addActionListener(
                e -> registrarSangriaClicado()
        );

        btnFecharCaixa = PadraoComponentes.criarBotaoPerigo(
                "Fechar caixa", PadraoComponentes.iconeCadeadoFechado(Color.WHITE));
        btnFecharCaixa.addActionListener(e -> fecharCaixaClicado());

        btnVerHistorico = PadraoComponentes.criarBotaoSecundario(
                "Histórico de caixas", PadraoComponentes.iconeHistorico(PadraoComponentes.VERDE));
        btnVerHistorico.addActionListener(e -> mostrarHistorico());

        JButton btnResumoFinanceiro = PadraoComponentes.criarBotaoSecundario(
                "Resumo financeiro", PadraoComponentes.iconeCifrao(PadraoComponentes.VERDE));
        btnResumoFinanceiro.addActionListener(e -> mostrarResumoFinanceiro());

        JPanel painelConsultas = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        painelConsultas.setOpaque(false);
        painelConsultas.add(btnResumoFinanceiro);
        painelConsultas.add(Box.createHorizontalStrut(12));
        painelConsultas.add(btnVerHistorico);

        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        painelBotoes.setOpaque(false);
        painelBotoes.add(btnAbrirCaixa);
        painelBotoes.add(Box.createHorizontalStrut(12));
        painelBotoes.add(btnSangria);
        painelBotoes.add(Box.createHorizontalStrut(12));
        painelBotoes.add(btnFecharCaixa);

        JPanel linhaBotoes = new JPanel(new BorderLayout());
        linhaBotoes.setOpaque(false);
        linhaBotoes.setAlignmentX(Component.LEFT_ALIGNMENT);
        linhaBotoes.add(painelBotoes, BorderLayout.WEST);
        linhaBotoes.add(painelConsultas, BorderLayout.EAST);
        linhaBotoes.setMaximumSize(new Dimension(Integer.MAX_VALUE, PadraoComponentes.ALTURA_BOTAO));

        painelIdentificacao.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Linha de 4 cartões de indicadores
        lblValorInicial = new JLabel("R$ -");
        lblTotalVendas = new JLabel("-");
        lblSangrias = new JLabel("R$ -");
        lblStatus = new JLabel("Pendente abertura");
        lblApoioValorInicial = new JLabel(" ");
        lblApoioVendas = new JLabel(" ");
        lblApoioSangrias = new JLabel(" ");
        lblApoioStatus = new JLabel(" ");

        JPanel painelCards = criarLinhaCartoes(
                PadraoComponentes.criarCartaoKpi("Valor inicial", lblValorInicial, lblApoioValorInicial,
                        PadraoComponentes.iconeCarteira(Color.WHITE)),
                PadraoComponentes.criarCartaoKpi("Total de vendas", lblTotalVendas, lblApoioVendas,
                        PadraoComponentes.iconeSacola(Color.WHITE)),
                PadraoComponentes.criarCartaoKpi("Sangrias", lblSangrias, lblApoioSangrias,
                        PadraoComponentes.iconeSaida(Color.WHITE)),
                PadraoComponentes.criarCartaoKpi("Status", lblStatus, lblApoioStatus,
                        PadraoComponentes.iconeCadeadoAberto(Color.WHITE)));

        topoCorpo.add(painelIdentificacao);
        topoCorpo.add(Box.createVerticalStrut(16));
        topoCorpo.add(linhaBotoes);
        topoCorpo.add(Box.createVerticalStrut(18));
        topoCorpo.add(painelCards);

        // Tabela de comandas
        modeloTabelaComandas = new DefaultTableModel(
                new Object[]{"Atendimento", "Cliente", "Status", "Valor"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // tabela só leitura
            }
        };

        tabelaComandas = new JTable(modeloTabelaComandas);
        estilizarTabelaComandas(tabelaComandas);

        painelCorpo.add(topoCorpo, BorderLayout.NORTH);
        painelCorpo.add(PadraoComponentes.criarCartaoTabela("Comandas deste caixa", tabelaComandas, null),
                BorderLayout.CENTER);

        // Painel do caixa atual
        painelAtual = painelCorpo;

        criarPainelHistorico();
        criarPainelDetalhes();

        cardLayoutCaixa = new CardLayout();

        painelCentral = new JPanel(cardLayoutCaixa);
        painelCentral.setOpaque(false);

        painelCentral.add(painelAtual, "ATUAL");
        painelCentral.add(painelHistorico, "HISTORICO");
        painelCentral.add(painelDetalhes, "DETALHES");

        this.add(lblTitulo, BorderLayout.NORTH);
        this.add(painelCentral, BorderLayout.CENTER);
    }

    // ------------------------------------------------------------------
    // Auxiliares de layout
    // ------------------------------------------------------------------

    private JLabel criarTituloSecao(String texto) {
        JLabel label = new JLabel(texto);
        label.setFont(FONTE_SECAO);
        label.setForeground(PadraoComponentes.COR_TEXTO);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    /** Separador entre as informações (data • horário • operador). */
    private static final String SEPARADOR_INFO = "&nbsp;&nbsp;•&nbsp;&nbsp;";

    /** Texto em negrito (HTML), escapando caracteres especiais do nome. */
    private static String negrito(String texto) {
        String seguro = texto == null ? "-" : texto
                .replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
        return "<b>" + seguro + "</b>";
    }

    private JLabel criarInfo(String texto) {
        JLabel label = new JLabel(texto);
        label.setFont(FONTE_INFO);
        label.setForeground(COR_INFO);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    /** Bloco com o botão de voltar e, embaixo dele, o título da seção. */
    private JPanel criarLinhaVoltar(JButton botaoVoltar, JLabel titulo) {
        JPanel bloco = new JPanel();
        bloco.setOpaque(false);
        bloco.setLayout(new BoxLayout(bloco, BoxLayout.Y_AXIS));
        bloco.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel linhaBotao = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        linhaBotao.setOpaque(false);
        linhaBotao.setAlignmentX(Component.LEFT_ALIGNMENT);
        linhaBotao.add(botaoVoltar);
        linhaBotao.setMaximumSize(new Dimension(Integer.MAX_VALUE, PadraoComponentes.ALTURA_BOTAO));

        titulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        bloco.add(linhaBotao);
        bloco.add(Box.createVerticalStrut(14));
        bloco.add(titulo);
        return bloco;
    }

    /** Linha com os cartões de indicadores, todos com a mesma largura. */
    private JPanel criarLinhaCartoes(JComponent... cartoes) {
        JPanel linha = new JPanel(new GridLayout(1, cartoes.length, 18, 0));
        linha.setOpaque(false);
        linha.setAlignmentX(Component.LEFT_ALIGNMENT);
        for (JComponent cartao : cartoes) {
            linha.add(cartao);
        }
        linha.setPreferredSize(new Dimension(0, 96));
        linha.setMaximumSize(new Dimension(Integer.MAX_VALUE, 96));
        return linha;
    }

    /** Colunas: Atendimento, Cliente, Status, Valor. */
    private void estilizarTabelaComandas(JTable tabela) {
        PadraoComponentes.estilizarTabela(tabela);
        tabela.getColumnModel().getColumn(2).setCellRenderer(new RendererStatus());
        PadraoComponentes.alinharColuna(tabela, 3, SwingConstants.RIGHT);
    }

    /**
     * Status com a primeira letra maiúscula e colorido:
     * verde (fechada / aberto), laranja (aberta) e vermelho (cancelada).
     */
    private static class RendererStatus extends PadraoComponentes.RendererCelula {

        RendererStatus() {
            super(SwingConstants.LEFT);
        }

        @Override
        public Component getTableCellRendererComponent(JTable tabela, Object valor, boolean selecionada,
                boolean foco, int linha, int coluna) {
            String texto = valor != null ? valor.toString() : "";
            String exibicao = texto.isEmpty() ? texto
                    : texto.substring(0, 1).toUpperCase() + texto.substring(1).toLowerCase();
            super.getTableCellRendererComponent(tabela, exibicao, selecionada, foco, linha, coluna);
            setFont(getFont().deriveFont(Font.BOLD));
            switch (texto.toUpperCase()) {
                case "ABERTA" -> setForeground(PadraoComponentes.LARANJA);
                case "CANCELADA" -> setForeground(PadraoComponentes.VERMELHO);
                case "FECHADO" -> setForeground(PadraoComponentes.COR_TEXTO_SECUNDARIO);
                default -> setForeground(PadraoComponentes.VERDE);
            }
            return this;
        }
    }

    /** Tipo da movimentação: entrada em verde, saída em vermelho. */
    private static class RendererTipoMovimentacao extends PadraoComponentes.RendererCelula {

        RendererTipoMovimentacao() {
            super(SwingConstants.LEFT);
        }

        @Override
        public Component getTableCellRendererComponent(JTable tabela, Object valor, boolean selecionada,
                boolean foco, int linha, int coluna) {
            super.getTableCellRendererComponent(tabela, valor, selecionada, foco, linha, coluna);
            setFont(getFont().deriveFont(Font.BOLD));
            setForeground("Saída".equals(valor) ? PadraoComponentes.VERMELHO : PadraoComponentes.VERDE);
            return this;
        }
    }

    /** Nome de exibição da forma de pagamento. */
    private static String nomeFormaPagamento(String forma) {
        if (forma == null) {
            return "-";
        }
        return switch (forma.toUpperCase()) {
            case "DINHEIRO" -> "Dinheiro";
            case "PIX" -> "Pix";
            case "DEBITO" -> "Cartão de Débito";
            case "CREDITO" -> "Cartão de Crédito";
            default -> forma;
        };
    }

    private static String descreverAtendimento(CaixaDAO.ComandaResumo comanda) {
        if ("MESA".equalsIgnoreCase(comanda.tipoAtendimento)) {
            return "Mesa " + comanda.numeroMesa;
        }
        String tipo = comanda.tipoAtendimento != null ? comanda.tipoAtendimento : "-";
        return tipo.isEmpty() ? tipo : tipo.substring(0, 1).toUpperCase() + tipo.substring(1).toLowerCase();
    }



    private void criarPainelHistorico() {

        painelHistorico = new JPanel(new BorderLayout(0, 16));
        painelHistorico.setOpaque(false);

        JPanel topo = new JPanel();
        topo.setOpaque(false);
        topo.setLayout(new BoxLayout(topo, BoxLayout.Y_AXIS));

        // Cabeçalho do histórico: botão de voltar e título
        btnVoltarCaixa = PadraoComponentes.criarBotaoSecundario(
                "Voltar ao caixa atual", PadraoComponentes.iconeVoltar(PadraoComponentes.VERDE));

        btnVoltarCaixa.addActionListener(e -> {
            cardLayoutCaixa.show(
                    painelCentral,
                    "ATUAL"
            );

            carregarCaixaAberto();
        });

        JLabel titulo = criarTituloSecao("Histórico de caixas");

        JPanel linhaTitulo = criarLinhaVoltar(btnVoltarCaixa, titulo);

        // ---------- FILTRO DE DATAS ----------
        filtroHistorico = new FiltroPeriodo(this::carregarHistorico);

        // filtro "De ... até ..." (datas em branco = todos os caixas)
        JPanel linhaFiltro = new JPanel(new BorderLayout());
        linhaFiltro.setOpaque(false);
        linhaFiltro.setAlignmentX(Component.LEFT_ALIGNMENT);
        linhaFiltro.add(filtroHistorico, BorderLayout.WEST);

        // ---------- CARTÕES DO PERÍODO ----------
        lblQtdCaixas = new JLabel("-");
        lblFaturamentoPeriodo = new JLabel("-");
        lblApoioFaturamentoPeriodo = new JLabel(" ");
        lblMaiorFaturamento = new JLabel("-");
        lblApoioMaiorFaturamento = new JLabel(" ");
        lblSangriasPeriodo = new JLabel("-");

        JPanel cartoes = criarLinhaCartoes(
                PadraoComponentes.criarCartaoKpi("Caixas no período", lblQtdCaixas, new JLabel(" "),
                        PadraoComponentes.iconeCalendario(Color.WHITE)),
                PadraoComponentes.criarCartaoKpi("Faturamento do período", lblFaturamentoPeriodo,
                        lblApoioFaturamentoPeriodo, PadraoComponentes.iconeCifrao(Color.WHITE)),
                PadraoComponentes.criarCartaoKpi("Maior faturamento", lblMaiorFaturamento,
                        lblApoioMaiorFaturamento, PadraoComponentes.iconeTrofeu(Color.WHITE)),
                PadraoComponentes.criarCartaoKpi("Sangrias do período", lblSangriasPeriodo, new JLabel(" "),
                        PadraoComponentes.iconeSaida(Color.WHITE)));

        topo.add(linhaTitulo);
        topo.add(Box.createVerticalStrut(12));
        topo.add(linhaFiltro);
        topo.add(Box.createVerticalStrut(14));
        topo.add(cartoes);

        // Tabela
        modeloTabelaHistorico = new DefaultTableModel(
                new Object[]{
                        "Caixa",
                        "Data",
                        "Abertura",
                        "Fechamento",
                        "Operador",
                        "Valor inicial",
                        "Vendas",
                        "Sangrias",
                        "Valor final",
                        "Status"
                },
                0
        ) {
            @Override
            public boolean isCellEditable(
                    int row,
                    int column
            ) {
                return false;
            }
        };

        tabelaHistorico = new JTable(modeloTabelaHistorico);
        tabelaHistorico.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        PadraoComponentes.estilizarTabela(tabelaHistorico);
        PadraoComponentes.alinharColuna(tabelaHistorico, 0, SwingConstants.CENTER);
        for (int coluna = 5; coluna <= 8; coluna++) {
            PadraoComponentes.alinharColuna(tabelaHistorico, coluna, SwingConstants.RIGHT);
        }
        tabelaHistorico.getColumnModel().getColumn(9).setCellRenderer(new RendererStatus());
        tabelaHistorico.getColumnModel().getColumn(0).setPreferredWidth(70);
        tabelaHistorico.getColumnModel().getColumn(4).setPreferredWidth(150);

        // "Ver detalhes" no cabeçalho do cartão da tabela, ao lado do resumo
        btnVerDetalhes = PadraoComponentes.criarBotaoPrimario(
                "Ver detalhes", PadraoComponentes.iconeOlho(Color.WHITE));
        btnVerDetalhes.setEnabled(false);
        btnVerDetalhes.setToolTipText("Selecione um caixa na tabela para ver os detalhes");
        btnVerDetalhes.addActionListener(e -> abrirDetalhesCaixaSelecionado());

        lblResumoHistorico = new JLabel(" ");
        lblResumoHistorico.setFont(FONTE_INFO);
        lblResumoHistorico.setForeground(PadraoComponentes.COR_TEXTO_SUAVE);

        JPanel acoesTabela = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        acoesTabela.setOpaque(false);
        acoesTabela.add(lblResumoHistorico);
        acoesTabela.add(Box.createHorizontalStrut(16));
        acoesTabela.add(btnVerDetalhes);

        tabelaHistorico.getSelectionModel().addListSelectionListener(e -> {
            btnVerDetalhes.setEnabled(
                    tabelaHistorico.getSelectedRow() != -1
            );
        });

        painelHistorico.add(topo, BorderLayout.NORTH);
        painelHistorico.add(PadraoComponentes.criarCartaoTabela("Caixas", tabelaHistorico, acoesTabela),
                BorderLayout.CENTER);
    }

    private void carregarHistorico() {


        try {

            modeloTabelaHistorico.setRowCount(0);

            List<br.com.trevizan.espetinhos.model.Caixa> caixas =
                    caixaDAO.listarCaixas();

            BigDecimal maiorFaturamento = null;

            br.com.trevizan.espetinhos.model.Caixa
                    caixaMaiorFaturamento = null;

            BigDecimal totalVendasPeriodo = BigDecimal.ZERO;
            BigDecimal totalSangriasPeriodo = BigDecimal.ZERO;
            int quantidadeCaixas = 0;

            java.time.format.DateTimeFormatter formatoData =
                    java.time.format.DateTimeFormatter.ofPattern(
                            "dd/MM/yyyy"
                    );

            java.time.format.DateTimeFormatter formatoHora =
                    java.time.format.DateTimeFormatter.ofPattern(
                            "HH:mm"
                    );

            for (br.com.trevizan.espetinhos.model.Caixa caixa : caixas) {

                if (caixa.getDataHoraAbertura() == null) {
                    continue;
                }

                // filtra pelo período escolhido (o caixa conta pela data de abertura)
                if (!filtroHistorico.contem(caixa.getDataHoraAbertura())) {
                    continue;
                }

                BigDecimal vendas =
                        caixaDAO.calcularTotalVendas(
                                caixa.getIdCaixa()
                        );

                if (maiorFaturamento == null
                        || vendas.compareTo(maiorFaturamento) > 0) {

                    maiorFaturamento = vendas;
                    caixaMaiorFaturamento = caixa;
                }

                BigDecimal sangrias =
                        caixaDAO.calcularSangrias(
                                caixa.getIdCaixa()
                        );

                quantidadeCaixas++;
                totalVendasPeriodo = totalVendasPeriodo.add(vendas);
                totalSangriasPeriodo = totalSangriasPeriodo.add(sangrias);

                String data =
                        caixa.getDataHoraAbertura()
                                .format(formatoData);

                String abertura =
                        caixa.getDataHoraAbertura()
                                .format(formatoHora);

                String fechamento =
                        caixa.getDataHoraFechamento() != null
                                ? caixa.getDataHoraFechamento()
                                .format(formatoHora)
                                : "-";

                String operador =
                        caixa.getUsuarioAbertura() != null
                                ? caixa.getUsuarioAbertura().getNome()
                                : "-";

                String valorFinal =
                        caixa.getValorFinal() != null
                                ? formatarMoeda(caixa.getValorFinal())
                                : "-";

                modeloTabelaHistorico.addRow(
                        new Object[]{
                                "#" + caixa.getIdCaixa(),
                                data,
                                abertura,
                                fechamento,
                                operador,
                                formatarMoeda(caixa.getValorInicial()),
                                formatarMoeda(vendas),
                                formatarMoeda(sangrias),
                                valorFinal,
                                caixa.getStatus()
                        }
                );
            }

            lblQtdCaixas.setText(String.valueOf(quantidadeCaixas));
            lblFaturamentoPeriodo.setText(formatarMoeda(totalVendasPeriodo));
            lblApoioFaturamentoPeriodo.setText(quantidadeCaixas > 0
                    ? "Média por caixa: " + formatarMoeda(totalVendasPeriodo.divide(
                            BigDecimal.valueOf(quantidadeCaixas), 2, java.math.RoundingMode.HALF_UP))
                    : " ");
            lblSangriasPeriodo.setText(formatarMoeda(totalSangriasPeriodo));
            lblResumoHistorico.setText(quantidadeCaixas + (quantidadeCaixas == 1 ? " caixa" : " caixas"));

            if (caixaMaiorFaturamento != null) {

                String dataMaior =
                        caixaMaiorFaturamento
                                .getDataHoraAbertura()
                                .format(
                                        java.time.format.DateTimeFormatter
                                                .ofPattern("dd/MM/yyyy")
                                );

                lblMaiorFaturamento.setText(formatarMoeda(maiorFaturamento));
                lblApoioMaiorFaturamento.setText(
                        "Caixa #"
                                + caixaMaiorFaturamento.getIdCaixa()
                                + " • "
                                + dataMaior
                );

            } else {

                lblMaiorFaturamento.setText("-");
                lblApoioMaiorFaturamento.setText(
                        "Nenhum caixa no período"
                );
            }

            btnVerDetalhes.setEnabled(false);

        } catch (RuntimeException e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Não foi possível carregar o histórico de caixas.",
                    "Erro",
                    JOptionPane.ERROR_MESSAGE
            );

            e.printStackTrace();
        }
    }

    private void mostrarHistorico() {

        carregarHistorico();

        cardLayoutCaixa.show(
                painelCentral,
                "HISTORICO"
        );
    }

    /**
     * Verifica no banco se já existe um caixa aberto e atualiza a tela.
     */
    private void carregarCaixaAberto() {
        try {
            caixaAtual = caixaDAO.buscarCaixaAberto();

            if (caixaAtual != null) {
                java.time.format.DateTimeFormatter formatoData =
                        java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");

                java.time.format.DateTimeFormatter formatoHora =
                        java.time.format.DateTimeFormatter.ofPattern("HH:mm");

                lblNumeroCaixa.setText(
                        "Caixa #" + caixaAtual.getIdCaixa()
                );

                String operador = caixaAtual.getUsuarioAbertura() != null
                        && caixaAtual.getUsuarioAbertura().getNome() != null
                        ? SEPARADOR_INFO + "Operador: " + negrito(caixaAtual.getUsuarioAbertura().getNome())
                        : "";

                // valores em negrito
                lblDataCaixa.setText("<html>"
                        + negrito(caixaAtual.getDataHoraAbertura().format(formatoData))
                        + SEPARADOR_INFO + "Aberto às "
                        + negrito(caixaAtual.getDataHoraAbertura().format(formatoHora))
                        + operador + "</html>"
                );
                lblValorInicial.setText(
                        formatarMoeda(caixaAtual.getValorInicial())
                );
                lblApoioValorInicial.setText("Troco na abertura");

                BigDecimal totalVendas = caixaDAO.calcularTotalVendas(caixaAtual.getIdCaixa());
                lblTotalVendas.setText(
                        formatarMoeda(totalVendas)
                );

                BigDecimal sangrias = caixaDAO.calcularSangrias(caixaAtual.getIdCaixa());
                lblSangrias.setText(
                        formatarMoeda(sangrias)
                );
                lblApoioSangrias.setText("Retiradas do caixa");

                BigDecimal saldo = caixaAtual.getValorInicial() != null
                        ? caixaAtual.getValorInicial().add(totalVendas).subtract(sangrias)
                        : totalVendas.subtract(sangrias);

                lblStatus.setText("Aberto");
                lblApoioStatus.setText("Saldo atual: " + formatarMoeda(saldo));

                btnAbrirCaixa.setEnabled(false);
                btnAbrirCaixa.setText("Caixa já aberto");
                btnFecharCaixa.setEnabled(true);
                btnSangria.setEnabled(true);

                carregarComandas(caixaAtual.getIdCaixa());

            } else {
                lblNumeroCaixa.setText("Nenhum caixa aberto");
                lblDataCaixa.setText("Aguardando abertura");
                lblValorInicial.setText("R$ -");
                lblTotalVendas.setText("-");
                lblSangrias.setText("R$ -");
                lblStatus.setText("Pendente abertura");
                lblApoioValorInicial.setText(" ");
                lblApoioVendas.setText(" ");
                lblApoioSangrias.setText(" ");
                lblApoioStatus.setText("Abra o caixa para começar");

                btnAbrirCaixa.setEnabled(true);
                btnAbrirCaixa.setText("Abrir caixa");
                btnFecharCaixa.setEnabled(false);
                btnSangria.setEnabled(false);

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
                        descreverAtendimento(c),
                        c.nomeCliente,
                        c.status,
                        formatarMoeda(c.valorTotal)
                });
            }

            lblApoioVendas.setText(comandas.size() + (comandas.size() == 1 ? " comanda" : " comandas"));

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

        BigDecimal valorInicial = CampoMoeda.pedirValor(
                this,
                "Informe o valor inicial do caixa (troco):",
                "Abrir caixa",
                BigDecimal.ZERO
        );

        if (valorInicial == null) {
            return; // usuário cancelou
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

    private void registrarSangriaClicado() {

        if (caixaAtual == null) {

            JOptionPane.showMessageDialog(
                    this,
                    "Não existe caixa aberto.",
                    "Sangria",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        CampoMoeda campoValor = new CampoMoeda();
        JTextField campoMotivo = new JTextField();
        PadraoComponentes.aplicarEstiloCampo(campoMotivo);
        CampoMoeda.focarAoAbrir(campoValor);

        JPanel painel = new JPanel(
                new GridLayout(0, 1, 5, 5)
        );

        painel.add(new JLabel("Valor da sangria:"));
        painel.add(campoValor);

        painel.add(new JLabel("Motivo:"));
        painel.add(campoMotivo);

        int opcao = JOptionPane.showConfirmDialog(
                this,
                painel,
                "Registrar sangria - Caixa #"
                        + caixaAtual.getIdCaixa(),
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (opcao != JOptionPane.OK_OPTION) {
            return;
        }

        String motivo =
                campoMotivo.getText().trim();

        if (!campoValor.temValor()) {

            JOptionPane.showMessageDialog(
                    this,
                    "Informe o valor da sangria.",
                    "Sangria",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        if (motivo.isEmpty()) {

            JOptionPane.showMessageDialog(
                    this,
                    "Informe o motivo da sangria.",
                    "Sangria",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        BigDecimal valor = campoValor.getValor();

        if (valor.compareTo(BigDecimal.ZERO) <= 0) {

            JOptionPane.showMessageDialog(
                    this,
                    "O valor da sangria deve ser maior que zero.",
                    "Sangria",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        try {

            // a sangria não pode passar do dinheiro que deveria estar na gaveta
            java.util.Map<String, BigDecimal> totaisPorForma =
                    pagamentoDAO.listarTotaisPorFormaDoCaixa(
                            caixaAtual.getIdCaixa()
                    );

            BigDecimal totalEntradas =
                    totaisPorForma.getOrDefault(
                            "DINHEIRO",
                            BigDecimal.ZERO
                    );

            BigDecimal totalSangrias =
                    caixaDAO.calcularSangrias(caixaAtual.getIdCaixa());

            BigDecimal saldoDisponivel =
                    caixaAtual.getValorInicial()
                            .add(totalEntradas)
                            .subtract(totalSangrias);

            if (valor.compareTo(saldoDisponivel) > 0) {

                JOptionPane.showMessageDialog(
                        this,
                        "Saldo insuficiente para realizar a sangria!\n\n"
                                + "Saldo registrado: "
                                + formatarMoeda(saldoDisponivel)
                                + "\nValor solicitado: "
                                + formatarMoeda(valor),
                        "Sangria não permitida",
                        JOptionPane.WARNING_MESSAGE
                );

                return;
            }

            movimentacaoCaixaDAO.registrarSaida(
                    caixaAtual.getIdCaixa(),
                    motivo,
                    valor
            );

            JOptionPane.showMessageDialog(
                    this,
                    "Sangria registrada com sucesso!\n\n"
                            + "Valor: "
                            + formatarMoeda(valor)
                            + "\nMotivo: "
                            + motivo,
                    "Sangria",
                    JOptionPane.INFORMATION_MESSAGE
            );

            carregarCaixaAberto();

        } catch (RuntimeException e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Não foi possível registrar a sangria.",
                    "Erro",
                    JOptionPane.ERROR_MESSAGE
            );

            e.printStackTrace();
        }
    }


    private void criarPainelDetalhes() {

        painelDetalhes = new JPanel(new BorderLayout(0, 16));
        painelDetalhes.setOpaque(false);

        JPanel topo = new JPanel();
        topo.setOpaque(false);
        topo.setLayout(new BoxLayout(topo, BoxLayout.Y_AXIS));

        // ---------- TOPO ----------
        lblDetalheTitulo = criarTituloSecao("Detalhes do caixa");

        btnVoltarHistorico = PadraoComponentes.criarBotaoSecundario(
                "Voltar ao histórico", PadraoComponentes.iconeVoltar(PadraoComponentes.VERDE));

        btnVoltarHistorico.addActionListener(e ->
                cardLayoutCaixa.show(
                        painelCentral,
                        "HISTORICO"
                )
        );

        JPanel linhaTitulo = criarLinhaVoltar(btnVoltarHistorico, lblDetalheTitulo);

        // ---------- INFORMAÇÕES ----------
        lblDetalhePeriodo = criarInfo("-");
        lblDetalheOperador = criarInfo("-");

        // ---------- CARTÕES ----------
        lblDetalheInicial = new JLabel("-");
        lblDetalheVendas = new JLabel("-");
        lblDetalheSangrias = new JLabel("-");
        lblDetalheFinal = new JLabel("-");
        lblTituloDetalheFinal = new JLabel("Valor final");
        lblApoioDetalheVendas = new JLabel(" ");
        lblApoioDetalheSangrias = new JLabel(" ");
        lblApoioDetalheFinal = new JLabel(" ");

        JPanel cards = criarLinhaCartoes(
                PadraoComponentes.criarCartaoKpi("Valor inicial", lblDetalheInicial, new JLabel("Troco na abertura"),
                        PadraoComponentes.iconeCarteira(Color.WHITE)),
                PadraoComponentes.criarCartaoKpi("Total de vendas", lblDetalheVendas, lblApoioDetalheVendas,
                        PadraoComponentes.iconeSacola(Color.WHITE)),
                PadraoComponentes.criarCartaoKpi("Sangrias", lblDetalheSangrias, lblApoioDetalheSangrias,
                        PadraoComponentes.iconeSaida(Color.WHITE)),
                PadraoComponentes.criarCartaoKpi(lblTituloDetalheFinal, lblDetalheFinal, lblApoioDetalheFinal,
                        PadraoComponentes.iconeCifrao(Color.WHITE)));

        topo.add(linhaTitulo);
        topo.add(Box.createVerticalStrut(4));
        topo.add(lblDetalhePeriodo);
        topo.add(Box.createVerticalStrut(2));
        topo.add(lblDetalheOperador);
        topo.add(Box.createVerticalStrut(16));
        topo.add(cards);

        // ---------- PAGAMENTOS ----------
        modeloTabelaPagamentos =
                new DefaultTableModel(
                        new Object[]{
                                "Forma de pagamento",
                                "Valor"
                        },
                        0
                ) {

                    @Override
                    public boolean isCellEditable(
                            int row,
                            int column
                    ) {
                        return false;
                    }
                };

        tabelaPagamentos =
                new JTable(modeloTabelaPagamentos);
        PadraoComponentes.estilizarTabela(tabelaPagamentos);
        PadraoComponentes.alinharColuna(tabelaPagamentos, 1, SwingConstants.RIGHT);

        // ---------- MOVIMENTAÇÕES ----------
        modeloTabelaMovimentacoes =
                new DefaultTableModel(
                        new Object[]{
                                "Horário",
                                "Tipo",
                                "Descrição",
                                "Valor"
                        },
                        0
                ) {

                    @Override
                    public boolean isCellEditable(
                            int row,
                            int column
                    ) {
                        return false;
                    }
                };

        tabelaMovimentacoes =
                new JTable(modeloTabelaMovimentacoes);
        PadraoComponentes.estilizarTabela(tabelaMovimentacoes);
        tabelaMovimentacoes.getColumnModel().getColumn(1).setCellRenderer(new RendererTipoMovimentacao());
        PadraoComponentes.alinharColuna(tabelaMovimentacoes, 3, SwingConstants.RIGHT);
        tabelaMovimentacoes.getColumnModel().getColumn(0).setPreferredWidth(80);
        tabelaMovimentacoes.getColumnModel().getColumn(1).setPreferredWidth(90);
        tabelaMovimentacoes.getColumnModel().getColumn(2).setPreferredWidth(260);

        // ---------- COMANDAS ----------
        modeloTabelaComandasDetalhes =
                new DefaultTableModel(
                        new Object[]{
                                "Atendimento",
                                "Cliente",
                                "Status",
                                "Valor"
                        },
                        0
                ) {

                    @Override
                    public boolean isCellEditable(
                            int row,
                            int column
                    ) {
                        return false;
                    }
                };

        tabelaComandasDetalhes =
                new JTable(modeloTabelaComandasDetalhes);
        estilizarTabelaComandas(tabelaComandasDetalhes);

        // ---------- MONTAGEM ----------
        // Linha de cima: pagamentos (mais estreito) + movimentações; embaixo: comandas
        JPanel tabelas = new JPanel(new GridBagLayout());
        tabelas.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1;

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.4;
        gbc.insets = new Insets(0, 0, 18, 9);
        tabelas.add(PadraoComponentes.criarCartaoTabela("Formas de pagamento", tabelaPagamentos, null), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.6;
        gbc.insets = new Insets(0, 9, 18, 0);
        tabelas.add(PadraoComponentes.criarCartaoTabela("Movimentações do caixa", tabelaMovimentacoes, null), gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.weightx = 1;
        gbc.insets = new Insets(0, 0, 0, 0);
        tabelas.add(PadraoComponentes.criarCartaoTabela("Comandas deste caixa", tabelaComandasDetalhes, null), gbc);

        // cada cartão começa do mesmo tamanho; a altura é dividida igualmente
        for (Component cartao : tabelas.getComponents()) {
            cartao.setPreferredSize(new Dimension(100, 180));
            cartao.setMinimumSize(new Dimension(100, 150));
        }

        painelDetalhes.add(topo, BorderLayout.NORTH);
        painelDetalhes.add(tabelas, BorderLayout.CENTER);
    }

    private void abrirDetalhesCaixaSelecionado() {

        int linha = tabelaHistorico.getSelectedRow();

        if (linha == -1) {
            JOptionPane.showMessageDialog(
                    this,
                    "Selecione um caixa.",
                    "Histórico de caixas",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        String textoId =
                modeloTabelaHistorico
                        .getValueAt(tabelaHistorico.convertRowIndexToModel(linha), 0)
                        .toString();

        int idCaixa;

        try {

            idCaixa = Integer.parseInt(
                    textoId.replace("#", "")
            );

        } catch (NumberFormatException e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Não foi possível identificar o caixa selecionado.",
                    "Erro",
                    JOptionPane.ERROR_MESSAGE
            );

            return;
        }

        carregarDetalhesCaixa(idCaixa);
    }

    private void carregarDetalhesCaixa(int idCaixa) {

        try {

            br.com.trevizan.espetinhos.model.Caixa caixaSelecionado =
                    null;

            List<br.com.trevizan.espetinhos.model.Caixa> caixas =
                    caixaDAO.listarCaixas();

            for (br.com.trevizan.espetinhos.model.Caixa caixa : caixas) {

                if (caixa.getIdCaixa() == idCaixa) {
                    caixaSelecionado = caixa;
                    break;
                }
            }

            if (caixaSelecionado == null) {

                JOptionPane.showMessageDialog(
                        this,
                        "Caixa não encontrado.",
                        "Erro",
                        JOptionPane.ERROR_MESSAGE
                );

                return;
            }

            java.time.format.DateTimeFormatter data =
                    java.time.format.DateTimeFormatter
                            .ofPattern("dd/MM/yyyy");

            java.time.format.DateTimeFormatter hora =
                    java.time.format.DateTimeFormatter
                            .ofPattern("HH:mm");

            lblDetalheTitulo.setText(
                    "Caixa #" + caixaSelecionado.getIdCaixa()
            );

            String periodo =
                    negrito(caixaSelecionado
                            .getDataHoraAbertura()
                            .format(data))
                            + SEPARADOR_INFO + "Aberto às "
                            + negrito(caixaSelecionado
                            .getDataHoraAbertura()
                            .format(hora));

            if (caixaSelecionado.getDataHoraFechamento() != null) {

                periodo += SEPARADOR_INFO + "Fechado às "
                        + negrito(caixaSelecionado
                        .getDataHoraFechamento()
                        .format(hora));

            } else {

                periodo += SEPARADOR_INFO + negrito("Em andamento");
            }

            lblDetalhePeriodo.setText("<html>" + periodo + "</html>");

            String operador =
                    caixaSelecionado.getUsuarioAbertura() != null
                            ? caixaSelecionado
                            .getUsuarioAbertura()
                            .getNome()
                            : "-";

            lblDetalheOperador.setText(
                    "<html>Operador: " + negrito(operador) + "</html>"
            );

            lblApoioDetalheFinal.setText(
                    caixaSelecionado.getDataHoraFechamento() != null
                            ? "Fechado às " + caixaSelecionado.getDataHoraFechamento().format(hora)
                            : "Caixa em andamento"
            );

            BigDecimal vendas =
                    caixaDAO.calcularTotalVendas(idCaixa);

            BigDecimal sangrias =
                    caixaDAO.calcularSangrias(idCaixa);

            BigDecimal saldoAtual =
                    caixaSelecionado
                            .getValorInicial()
                            .add(vendas)
                            .subtract(sangrias);

            lblDetalheInicial.setText(
                    formatarMoeda(caixaSelecionado.getValorInicial())
            );

            lblDetalheVendas.setText(
                    formatarMoeda(vendas)
            );

            lblDetalheSangrias.setText(
                    formatarMoeda(sangrias)
            );

            if ("ABERTO".equalsIgnoreCase(
                    caixaSelecionado.getStatus()
            )) {

                lblTituloDetalheFinal.setText("Saldo atual");

                lblDetalheFinal.setText(
                        formatarMoeda(saldoAtual)
                );

            } else {

                lblTituloDetalheFinal.setText("Valor final");

                lblDetalheFinal.setText(
                        formatarMoeda(
                                caixaSelecionado.getValorFinal()
                        )
                );
            }


            // Formas de pagamento
            modeloTabelaPagamentos.setRowCount(0);

            java.util.Map<String, BigDecimal> pagamentos =
                    pagamentoDAO.listarTotaisPorFormaDoCaixa(
                            idCaixa
                    );

            for (java.util.Map.Entry<String, BigDecimal> entrada
                    : pagamentos.entrySet()) {

                modeloTabelaPagamentos.addRow(
                        new Object[]{
                                nomeFormaPagamento(entrada.getKey()),
                                formatarMoeda(entrada.getValue())
                        }
                );
            }

            // Movimentações
            modeloTabelaMovimentacoes.setRowCount(0);

            java.util.List<MovimentacaoCaixaDAO.MovimentacaoResumo>
                    movimentacoes =
                    movimentacaoCaixaDAO.listarPorCaixa(idCaixa);

            java.time.format.DateTimeFormatter formatoHora =
                    java.time.format.DateTimeFormatter
                            .ofPattern("HH:mm");

            int quantidadeSaidas = 0;

            for (MovimentacaoCaixaDAO.MovimentacaoResumo movimentacao
                    : movimentacoes) {

                if (!"ENTRADA".equalsIgnoreCase(movimentacao.tipo)) {
                    quantidadeSaidas++;
                }

                String tipoExibicao =
                        "ENTRADA".equalsIgnoreCase(movimentacao.tipo)
                                ? "Entrada"
                                : "Saída";

                modeloTabelaMovimentacoes.addRow(
                        new Object[]{
                                movimentacao.dataHora.format(formatoHora),
                                tipoExibicao,
                                movimentacao.descricao,
                                formatarMoeda(movimentacao.valor)
                        }
                );
            }

            lblApoioDetalheSangrias.setText(
                    quantidadeSaidas == 0 ? "Nenhuma retirada"
                            : quantidadeSaidas + (quantidadeSaidas == 1 ? " retirada" : " retiradas")
            );

            // Comandas
            modeloTabelaComandasDetalhes.setRowCount(0);

            List<CaixaDAO.ComandaResumo> comandas =
                    caixaDAO.listarComandasDoCaixa(
                            idCaixa
                    );

            lblApoioDetalheVendas.setText(
                    comandas.size() + (comandas.size() == 1 ? " comanda" : " comandas")
            );

            for (CaixaDAO.ComandaResumo comanda : comandas) {

                modeloTabelaComandasDetalhes.addRow(
                        new Object[]{
                                descreverAtendimento(comanda),
                                comanda.nomeCliente,
                                comanda.status,
                                formatarMoeda(comanda.valorTotal)
                        }
                );
            }

            cardLayoutCaixa.show(
                    painelCentral,
                    "DETALHES"
            );

        } catch (RuntimeException e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Não foi possível carregar os detalhes do caixa.",
                    "Erro",
                    JOptionPane.ERROR_MESSAGE
            );

            e.printStackTrace();
        }
    }

    private String formatarMoeda(BigDecimal valor) {

        if (valor == null) {
            return "-";
        }

        java.text.NumberFormat formato =
                java.text.NumberFormat.getCurrencyInstance(
                        new java.util.Locale("pt", "BR")
                );

        return formato.format(valor);
    }

    /**
     * Ação do botão "Fechar caixa": avisa se há comandas abertas, confirma,
     * calcula o valor final e salva.
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

        // Avisa se ainda existem comandas abertas
        int comandasAbertas;
        try {
            comandasAbertas = caixaDAO.contarComandasAbertas();
        } catch (RuntimeException e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Não foi possível verificar as comandas abertas.",
                    "Erro",
                    JOptionPane.ERROR_MESSAGE
            );
            e.printStackTrace();
            return;
        }

        if (comandasAbertas > 0) {
            Object[] opcoes = {"Fechar mesmo assim", "Cancelar"};

            int escolha = JOptionPane.showOptionDialog(
                    this,
                    "Ainda existem " + comandasAbertas + " comanda(s) aberta(s).\n"
                            + "Elas ainda não estão contabilizadas no total de vendas.\n\n"
                            + "Deseja fechar o caixa mesmo assim?",
                    "Comandas em aberto",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.WARNING_MESSAGE,
                    null,
                    opcoes,
                    opcoes[1]
            );

            if (escolha != 0) {
                return; // cancelou ou fechou a janela
            }
        }

        try {
            int idCaixa = caixaAtual.getIdCaixa();

            // Faturamento total: dinheiro, PIX e cartões
            BigDecimal totalVendas =
                    caixaDAO.calcularTotalVendas(idCaixa);

            // Somente recebimentos em dinheiro
            java.util.Map<String, BigDecimal> totaisPorForma =
                    pagamentoDAO.listarTotaisPorFormaDoCaixa(idCaixa);

            BigDecimal totalDinheiro =
                    totaisPorForma.getOrDefault(
                            "DINHEIRO",
                            BigDecimal.ZERO
                    );

            BigDecimal sangrias =
                    caixaDAO.calcularSangrias(idCaixa);

            // Dinheiro físico esperado na gaveta
            BigDecimal valorEsperado =
                    caixaAtual.getValorInicial()
                            .add(totalDinheiro)
                            .subtract(sangrias);

            BigDecimal valorContado = CampoMoeda.pedirValor(
                    this,
                    "CONFERÊNCIA DO CAIXA\n\n"
                            + "Valor inicial: "
                            + formatarMoeda(caixaAtual.getValorInicial())
                            + "\nFaturamento total: "
                            + formatarMoeda(totalVendas)
                            + "\nRecebido em dinheiro: "
                            + formatarMoeda(totalDinheiro)
                            + "\nSangrias: "
                            + formatarMoeda(sangrias)
                            + "\n\nSaldo esperado na gaveta: "
                            + formatarMoeda(valorEsperado)
                            + "\n\nInforme o dinheiro contado na gaveta:",
                    "Conferência de fechamento",
                    BigDecimal.ZERO
            );

            if (valorContado == null) {
                return;
            }

            BigDecimal diferenca =
                    valorContado.subtract(valorEsperado);

            String resultado;

            if (diferenca.compareTo(BigDecimal.ZERO) == 0) {
                resultado = "CAIXA CORRETO";
            } else if (diferenca.compareTo(BigDecimal.ZERO) < 0) {
                resultado = "FALTA DE DINHEIRO";
            } else {
                resultado = "SOBRA DE DINHEIRO";
            }

            int confirmacao = JOptionPane.showConfirmDialog(
                    this,
                    "CONFIRMAR FECHAMENTO\n\n"
                            + "Saldo esperado: "
                            + formatarMoeda(valorEsperado)
                            + "\nValor contado: "
                            + formatarMoeda(valorContado)
                            + "\n\nResultado: " + resultado
                            + "\nDiferença: "
                            + formatarMoeda(diferenca)
                            + "\n\nDeseja realmente fechar o caixa?",
                    "Confirmar fechamento",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
            );

            if (confirmacao != JOptionPane.YES_OPTION) {
                return;
            }

            caixaDAO.fecharCaixa(
                    idCaixa,
                    valorEsperado,
                    valorContado
            );

            JOptionPane.showMessageDialog(
                    this,
                    "Caixa fechado com sucesso!\n\n"
                            + "Resultado: " + resultado
                            + "\nDiferença: "
                            + formatarMoeda(diferenca),
                    "Fechamento concluído",
                    JOptionPane.INFORMATION_MESSAGE
            );

            carregarCaixaAberto();

        } catch (RuntimeException e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Erro ao realizar o fechamento do caixa.\n"
                            + e.getMessage(),
                    "Erro",
                    JOptionPane.ERROR_MESSAGE
            );
            e.printStackTrace();
        }
    }

    /** Mostra a soma dos valores finais de todos os caixas já fechados. */
    private void mostrarResumoFinanceiro() {
        try {
            BigDecimal totalValoresFinais =
                    caixaDAO.calcularTotalValoresFinais();

            JOptionPane.showMessageDialog(
                    this,
                    "RESUMO FINANCEIRO\n\n"
                            + "Total dos valores finais dos caixas fechados:\n"
                            + formatarMoeda(totalValoresFinais),
                    "Resumo financeiro",
                    JOptionPane.INFORMATION_MESSAGE
            );

        } catch (RuntimeException e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Não foi possível carregar o resumo financeiro.\n"
                            + e.getMessage(),
                    "Erro",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }
}