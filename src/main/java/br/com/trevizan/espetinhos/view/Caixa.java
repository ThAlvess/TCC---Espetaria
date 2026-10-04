package br.com.trevizan.espetinhos.view;

import br.com.trevizan.espetinhos.dao.CaixaDAO;
import br.com.trevizan.espetinhos.model.Usuario;
import br.com.trevizan.espetinhos.util.PadraoTela;
import br.com.trevizan.espetinhos.util.SessaoUsuario;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;
import br.com.trevizan.espetinhos.dao.PagamentoDAO;
import com.github.lgooddatepicker.components.DatePicker;
import com.github.lgooddatepicker.components.DatePickerSettings;
import br.com.trevizan.espetinhos.dao.MovimentacaoCaixaDAO;

import java.time.LocalDate;

/**
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
    private DatePicker dataInicioHistorico;
    private DatePicker dataFimHistorico;

    private JLabel lblMaiorFaturamento;
    private JButton btnFiltrarHistorico;
    private JButton btnLimparFiltroHistorico;
    private DefaultTableModel modeloTabelaMovimentacoes;
    private JTable tabelaMovimentacoes;
    private JLabel lblTituloDetalheFinal;


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
        this.setLayout(new BorderLayout(20, 20));
        this.setBackground(new Color(237, 231, 226));
        // margens do padrão de telas (título na mesma posição da tela de Relatórios)
        this.setBorder(BorderFactory.createEmptyBorder(
                PadraoTela.MARGEM_TOPO, PadraoTela.MARGEM_LATERAL, 20, PadraoTela.MARGEM_LATERAL));

        // ---------- Cabeçalho (título + ícone) ----------
        JPanel painelCabecalho = new JPanel(new BorderLayout());
        painelCabecalho.setOpaque(false);

        JLabel lblTitulo = PadraoTela.criarTitulo("CAIXA");

        JLabel lblIconePerfil = new JLabel("👤");
        lblIconePerfil.setFont(new Font("SansSerif", Font.PLAIN, 24));
        lblIconePerfil.setForeground(new Color(34, 102, 51));

        painelCabecalho.add(lblTitulo, BorderLayout.WEST);
        painelCabecalho.add(lblIconePerfil, BorderLayout.EAST);

        // ---------- Corpo (botões + cards + tabela) ----------
        JPanel painelCorpo = new JPanel();
        painelCorpo.setOpaque(false);
        painelCorpo.setLayout(new BoxLayout(painelCorpo, BoxLayout.Y_AXIS));

        // Identificação do caixa atual
        JPanel painelIdentificacao = new JPanel();
        painelIdentificacao.setOpaque(false);
        painelIdentificacao.setLayout(
                new BoxLayout(painelIdentificacao, BoxLayout.Y_AXIS)
        );
        painelIdentificacao.setAlignmentX(Component.LEFT_ALIGNMENT);

        lblNumeroCaixa = new JLabel("Nenhum caixa aberto");
        lblNumeroCaixa.setFont(
                new Font("SansSerif", Font.BOLD, 18)
        );
        lblNumeroCaixa.setForeground(
                new Color(40, 40, 40)
        );
        lblNumeroCaixa.setAlignmentX(Component.LEFT_ALIGNMENT);

        lblDataCaixa = new JLabel("Aguardando abertura");
        lblDataCaixa.setFont(
                new Font("SansSerif", Font.PLAIN, 13)
        );
        lblDataCaixa.setForeground(
                new Color(100, 100, 100)
        );
        lblDataCaixa.setAlignmentX(Component.LEFT_ALIGNMENT);

        painelIdentificacao.add(lblNumeroCaixa);
        painelIdentificacao.add(
                Box.createRigidArea(new Dimension(0, 3))
        );
        painelIdentificacao.add(lblDataCaixa);
        painelIdentificacao.add(
                Box.createRigidArea(new Dimension(0, 10))
        );

        btnVerHistorico = new JButton("Histórico de caixas");
        btnVerHistorico.setFont(
                new Font("SansSerif", Font.BOLD, 12)
        );
        btnVerHistorico.setFocusPainted(false);
        btnVerHistorico.setAlignmentX(Component.LEFT_ALIGNMENT);

        btnVerHistorico.addActionListener(e -> mostrarHistorico());

        painelIdentificacao.add(btnVerHistorico);

        // Linha de botões
        JPanel painelBotoes = new JPanel(new GridLayout(1, 3, 15, 0));
        painelBotoes.setOpaque(false);
        painelBotoes.setAlignmentX(Component.LEFT_ALIGNMENT);
        painelBotoes.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        btnAbrirCaixa = new JButton("Abrir caixa");
        btnAbrirCaixa.setBackground(new Color(27, 94, 32));
        btnAbrirCaixa.setForeground(Color.WHITE);
        btnAbrirCaixa.setFont(new Font("SansSerif", Font.BOLD, 14));
        btnAbrirCaixa.setFocusPainted(false);
        btnAbrirCaixa.addActionListener(e -> abrirCaixaClicado());

        btnSangria = new JButton("Registrar sangria");
        btnSangria.setBackground(new Color(204, 102, 0));
        btnSangria.setForeground(Color.WHITE);
        btnSangria.setFont(
                new Font("SansSerif", Font.BOLD, 14)
        );
        btnSangria.setFocusPainted(false);

        btnSangria.addActionListener(
                e -> registrarSangriaClicado()
        );

        btnFecharCaixa = new JButton("Fechar caixa");
        btnFecharCaixa.setBackground(new Color(153, 0, 0));
        btnFecharCaixa.setForeground(Color.WHITE);
        btnFecharCaixa.setFont(new Font("SansSerif", Font.BOLD, 14));
        btnFecharCaixa.setFocusPainted(false);
        btnFecharCaixa.addActionListener(e -> fecharCaixaClicado());

        painelBotoes.add(btnAbrirCaixa);
        painelBotoes.add(btnSangria);
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
                new Object[]{"Atendimento", "Cliente", "Status", "Valor"}, 0
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

        painelCorpo.add(painelIdentificacao);
        painelCorpo.add(Box.createRigidArea(new Dimension(0, 15)));

        painelCorpo.add(painelBotoes);
        painelCorpo.add(Box.createRigidArea(new Dimension(0, 15)));

        painelCorpo.add(painelCards);
        painelCorpo.add(lblComandas);
        painelCorpo.add(scrollTabela);

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

        this.add(painelCabecalho, BorderLayout.NORTH);
        this.add(painelCentral, BorderLayout.CENTER);
    }



    private void criarPainelHistorico() {

        painelHistorico = new JPanel();
        painelHistorico.setOpaque(false);
        painelHistorico.setLayout(
                new BoxLayout(painelHistorico, BoxLayout.Y_AXIS)
        );

        // Cabeçalho do histórico
        JPanel topo = new JPanel(new BorderLayout());
        topo.setOpaque(false);
        topo.setAlignmentX(Component.LEFT_ALIGNMENT);
        topo.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 45)
        );

        JLabel titulo = new JLabel("Histórico de caixas");
        titulo.setFont(
                new Font("SansSerif", Font.BOLD, 20)
        );
        titulo.setForeground(
                new Color(34, 102, 51)
        );

        btnVoltarCaixa = new JButton("← Voltar ao caixa atual");
        btnVoltarCaixa.setFocusPainted(false);

        btnVoltarCaixa.addActionListener(e -> {
            cardLayoutCaixa.show(
                    painelCentral,
                    "ATUAL"
            );

            carregarCaixaAberto();
        });

        topo.add(titulo, BorderLayout.WEST);
        topo.add(btnVoltarCaixa, BorderLayout.EAST);

        // ---------- FILTROS ----------
        JPanel painelFiltros = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 10, 5)
        );
        painelFiltros.setOpaque(false);
        painelFiltros.setAlignmentX(Component.LEFT_ALIGNMENT);
        painelFiltros.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 45)
        );

        DatePickerSettings configInicio = new DatePickerSettings();
        configInicio.setFormatForDatesCommonEra("dd/MM/yyyy");

        DatePickerSettings configFim = new DatePickerSettings();
        configFim.setFormatForDatesCommonEra("dd/MM/yyyy");

        dataInicioHistorico = new DatePicker(configInicio);
        dataFimHistorico = new DatePicker(configFim);

        btnFiltrarHistorico = new JButton("Filtrar");
        btnLimparFiltroHistorico = new JButton("Limpar");

        btnFiltrarHistorico.setFocusPainted(false);
        btnLimparFiltroHistorico.setFocusPainted(false);

        painelFiltros.add(new JLabel("De:"));
        painelFiltros.add(dataInicioHistorico);

        painelFiltros.add(new JLabel("Até:"));
        painelFiltros.add(dataFimHistorico);

        painelFiltros.add(btnFiltrarHistorico);
        painelFiltros.add(btnLimparFiltroHistorico);

        JPanel painelDestaque = new JPanel(
                new BorderLayout()
        );

        painelDestaque.setBackground(
                new Color(224, 224, 224)
        );

        painelDestaque.setBorder(
                BorderFactory.createEmptyBorder(
                        10, 12, 10, 12
                )
        );

        painelDestaque.setAlignmentX(
                Component.LEFT_ALIGNMENT
        );

        painelDestaque.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 55)
        );

        JLabel lblTituloMaior =
                new JLabel("Maior faturamento do período");

        lblTituloMaior.setFont(
                new Font("SansSerif", Font.PLAIN, 12)
        );

        lblMaiorFaturamento =
                new JLabel("Nenhum período selecionado");

        lblMaiorFaturamento.setFont(
                new Font("SansSerif", Font.BOLD, 15)
        );

        painelDestaque.add(
                lblTituloMaior,
                BorderLayout.WEST
        );

        painelDestaque.add(
                lblMaiorFaturamento,
                BorderLayout.EAST
        );

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
        tabelaHistorico.setRowHeight(30);

        tabelaHistorico
                .getTableHeader()
                .setFont(
                        new Font(
                                "SansSerif",
                                Font.BOLD,
                                12
                        )
                );

        JScrollPane scroll =
                new JScrollPane(tabelaHistorico);

        scroll.setAlignmentX(Component.LEFT_ALIGNMENT);

        btnVerDetalhes = new JButton("Ver detalhes");
        btnVerDetalhes.setFont(
                new Font("SansSerif", Font.BOLD, 12)
        );
        btnVerDetalhes.setFocusPainted(false);
        btnVerDetalhes.setEnabled(false);

        btnVerDetalhes.addActionListener(e -> abrirDetalhesCaixaSelecionado());

        JPanel painelAcoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        painelAcoes.setOpaque(false);
        painelAcoes.setAlignmentX(Component.LEFT_ALIGNMENT);
        painelAcoes.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 40)
        );

        painelAcoes.add(btnVerDetalhes);

        tabelaHistorico.getSelectionModel().addListSelectionListener(e -> {
            btnVerDetalhes.setEnabled(
                    tabelaHistorico.getSelectedRow() != -1
            );
        });

        painelHistorico.add(topo);
        painelHistorico.add(
                Box.createRigidArea(new Dimension(0, 8))
        );

        painelHistorico.add(painelFiltros);
        painelHistorico.add(
                Box.createRigidArea(new Dimension(0, 8))
        );

        painelHistorico.add(painelDestaque);
        painelHistorico.add(
                Box.createRigidArea(new Dimension(0, 8))
        );

        painelHistorico.add(painelAcoes);
        painelHistorico.add(
                Box.createRigidArea(new Dimension(0, 8))
        );

        painelHistorico.add(scroll);
        painelHistorico.add(
                Box.createRigidArea(new Dimension(0, 10))
        );

        painelHistorico.add(scroll);

        btnFiltrarHistorico.addActionListener(e -> {

            LocalDate inicio =
                    dataInicioHistorico.getDate();

            LocalDate fim =
                    dataFimHistorico.getDate();

            if (inicio != null
                    && fim != null
                    && inicio.isAfter(fim)) {

                JOptionPane.showMessageDialog(
                        this,
                        "A data inicial não pode ser posterior à data final.",
                        "Período inválido",
                        JOptionPane.WARNING_MESSAGE
                );

                return;
            }

            carregarHistorico(inicio, fim);
        });
        btnLimparFiltroHistorico.addActionListener(e -> {

            dataInicioHistorico.clear();
            dataFimHistorico.clear();

            carregarHistorico(null, null);
        });
    }

    private void carregarHistorico(LocalDate inicio,
                                   LocalDate fim) {


        try {

            modeloTabelaHistorico.setRowCount(0);

            List<br.com.trevizan.espetinhos.model.Caixa> caixas =
                    caixaDAO.listarCaixas();

            BigDecimal maiorFaturamento = null;

            br.com.trevizan.espetinhos.model.Caixa
                    caixaMaiorFaturamento = null;

            java.time.format.DateTimeFormatter formatoData =
                    java.time.format.DateTimeFormatter.ofPattern(
                            "dd/MM/yyyy"
                    );

            java.time.format.DateTimeFormatter formatoHora =
                    java.time.format.DateTimeFormatter.ofPattern(
                            "HH:mm"
                    );

            for (br.com.trevizan.espetinhos.model.Caixa caixa : caixas) {

                LocalDate dataCaixa =
                        caixa.getDataHoraAbertura() != null
                                ? caixa.getDataHoraAbertura().toLocalDate()
                                : null;

                if (dataCaixa == null) {
                    continue;
                }

                if (inicio != null && dataCaixa.isBefore(inicio)) {
                    continue;
                }

                if (fim != null && dataCaixa.isAfter(fim)) {
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

                String data =
                        caixa.getDataHoraAbertura() != null
                                ? caixa.getDataHoraAbertura()
                                .format(formatoData)
                                : "-";

                String abertura =
                        caixa.getDataHoraAbertura() != null
                                ? caixa.getDataHoraAbertura()
                                .format(formatoHora)
                                : "-";

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

            if (caixaMaiorFaturamento != null) {

                String dataMaior =
                        caixaMaiorFaturamento
                                .getDataHoraAbertura()
                                .format(
                                        java.time.format.DateTimeFormatter
                                                .ofPattern("dd/MM/yyyy")
                                );

                lblMaiorFaturamento.setText(
                        "Caixa #"
                                + caixaMaiorFaturamento.getIdCaixa()
                                + " • "
                                + dataMaior
                                + " • "
                                + formatarMoeda(maiorFaturamento)
                );

            } else {

                lblMaiorFaturamento.setText(
                        "Nenhum caixa encontrado"
                );
            }

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

        carregarHistorico(null, null);

        cardLayoutCaixa.show(
                painelCentral,
                "HISTORICO"
        );
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
                java.time.format.DateTimeFormatter formatoData =
                        java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");

                java.time.format.DateTimeFormatter formatoHora =
                        java.time.format.DateTimeFormatter.ofPattern("HH:mm");

                lblNumeroCaixa.setText(
                        "Caixa #" + caixaAtual.getIdCaixa()
                );

                lblDataCaixa.setText(
                        caixaAtual.getDataHoraAbertura().format(formatoData)
                                + "  •  Aberto às "
                                + caixaAtual.getDataHoraAbertura().format(formatoHora)
                );
                lblValorInicial.setText(
                        formatarMoeda(caixaAtual.getValorInicial())
                );

                BigDecimal totalVendas = caixaDAO.calcularTotalVendas(caixaAtual.getIdCaixa());
                lblTotalVendas.setText(
                        formatarMoeda(totalVendas)
                );

                BigDecimal sangrias = caixaDAO.calcularSangrias(caixaAtual.getIdCaixa());
                lblSangrias.setText(
                        formatarMoeda(sangrias)
                );
                lblStatus.setText("Aberto");

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

                String atendimento;

                if ("MESA".equalsIgnoreCase(c.tipoAtendimento)) {
                    atendimento = "Mesa " + c.numeroMesa;
                } else {
                    atendimento = c.tipoAtendimento;
                }

                modeloTabelaComandas.addRow(new Object[]{
                        atendimento,
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

        JTextField campoValor = new JTextField();
        JTextField campoMotivo = new JTextField();

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

        String valorTexto =
                campoValor.getText().trim();

        String motivo =
                campoMotivo.getText().trim();

        if (valorTexto.isEmpty()) {

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

        BigDecimal valor;

        try {

            valor = new BigDecimal(
                    valorTexto.replace(",", ".")
            );

        } catch (NumberFormatException e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Informe um valor válido.",
                    "Sangria",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

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

        painelDetalhes = new JPanel();
        painelDetalhes.setOpaque(false);
        painelDetalhes.setLayout(
                new BoxLayout(painelDetalhes, BoxLayout.Y_AXIS)
        );

        // ---------- TOPO ----------
        JPanel topo = new JPanel(new BorderLayout());
        topo.setOpaque(false);
        topo.setAlignmentX(Component.LEFT_ALIGNMENT);
        topo.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 45)
        );

        lblDetalheTitulo = new JLabel("Detalhes do caixa");
        lblDetalheTitulo.setFont(
                new Font("SansSerif", Font.BOLD, 20)
        );
        lblDetalheTitulo.setForeground(
                new Color(34, 102, 51)
        );

        btnVoltarHistorico =
                new JButton("← Voltar ao histórico");

        btnVoltarHistorico.setFocusPainted(false);

        btnVoltarHistorico.addActionListener(e ->
                cardLayoutCaixa.show(
                        painelCentral,
                        "HISTORICO"
                )
        );

        topo.add(lblDetalheTitulo, BorderLayout.WEST);
        topo.add(btnVoltarHistorico, BorderLayout.EAST);

        // ---------- INFORMAÇÕES ----------
        lblDetalhePeriodo = new JLabel("-");
        lblDetalhePeriodo.setFont(
                new Font("SansSerif", Font.PLAIN, 13)
        );

        lblDetalheOperador = new JLabel("-");
        lblDetalheOperador.setFont(
                new Font("SansSerif", Font.PLAIN, 13)
        );

        // ---------- CARDS ----------
        JPanel cards = new JPanel(
                new GridLayout(1, 4, 15, 0)
        );

        cards.setOpaque(false);
        cards.setAlignmentX(Component.LEFT_ALIGNMENT);
        cards.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 90)
        );

        lblDetalheInicial = new JLabel("-");
        lblDetalheVendas = new JLabel("-");
        lblDetalheSangrias = new JLabel("-");
        lblDetalheFinal = new JLabel("-");
        lblTituloDetalheFinal = new JLabel("Valor final");

        cards.add(
                criarCard(
                        "Valor inicial",
                        lblDetalheInicial
                )
        );

        cards.add(
                criarCard(
                        "Total de vendas",
                        lblDetalheVendas
                )
        );

        cards.add(
                criarCard(
                        "Sangrias",
                        lblDetalheSangrias
                )
        );

        JPanel cardFinal = new JPanel();
        cardFinal.setLayout(
                new BoxLayout(cardFinal, BoxLayout.Y_AXIS)
        );

        cardFinal.setBackground(
                new Color(224, 224, 224)
        );

        cardFinal.setBorder(
                BorderFactory.createEmptyBorder(
                        15, 15, 15, 15
                )
        );

        lblTituloDetalheFinal.setFont(
                new Font("SansSerif", Font.PLAIN, 12)
        );

        lblDetalheFinal.setFont(
                new Font("SansSerif", Font.BOLD, 18)
        );

        lblTituloDetalheFinal.setAlignmentX(
                Component.LEFT_ALIGNMENT
        );

        lblDetalheFinal.setAlignmentX(
                Component.LEFT_ALIGNMENT
        );

        cardFinal.add(lblTituloDetalheFinal);
        cardFinal.add(
                Box.createRigidArea(
                        new Dimension(0, 8)
                )
        );
        cardFinal.add(lblDetalheFinal);

        cards.add(cardFinal);

        // ---------- PAGAMENTOS ----------
        JLabel tituloPagamentos =
                new JLabel("Formas de pagamento");

        tituloPagamentos.setFont(
                new Font("SansSerif", Font.BOLD, 14)
        );

        tituloPagamentos.setForeground(
                new Color(34, 102, 51)
        );

        tituloPagamentos.setAlignmentX(
                Component.LEFT_ALIGNMENT
        );

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

        tabelaPagamentos.setRowHeight(27);

        JScrollPane scrollPagamentos =
                new JScrollPane(tabelaPagamentos);

        scrollPagamentos.setAlignmentX(
                Component.LEFT_ALIGNMENT
        );

        scrollPagamentos.setMaximumSize(
                new Dimension(
                        Integer.MAX_VALUE,
                        130
                )
        );

        // ---------- MOVIMENTAÇÕES ----------
        JLabel tituloMovimentacoes =
                new JLabel("Movimentações do caixa");

        tituloMovimentacoes.setFont(
                new Font("SansSerif", Font.BOLD, 14)
        );

        tituloMovimentacoes.setForeground(
                new Color(34, 102, 51)
        );

        tituloMovimentacoes.setAlignmentX(
                Component.LEFT_ALIGNMENT
        );

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

        tabelaMovimentacoes.setRowHeight(27);

        JScrollPane scrollMovimentacoes =
                new JScrollPane(tabelaMovimentacoes);

        scrollMovimentacoes.setAlignmentX(
                Component.LEFT_ALIGNMENT
        );

        scrollMovimentacoes.setMaximumSize(
                new Dimension(
                        Integer.MAX_VALUE,
                        150
                )
        );

        // ---------- COMANDAS ----------
        JLabel tituloComandas =
                new JLabel("Comandas deste caixa");

        tituloComandas.setFont(
                new Font("SansSerif", Font.BOLD, 14)
        );

        tituloComandas.setForeground(
                new Color(34, 102, 51)
        );

        tituloComandas.setAlignmentX(
                Component.LEFT_ALIGNMENT
        );

        modeloTabelaComandasDetalhes =
                new DefaultTableModel(
                        new Object[]{
                                "Mesa",
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

        tabelaComandasDetalhes.setRowHeight(27);

        JScrollPane scrollComandas =
                new JScrollPane(tabelaComandasDetalhes);

        scrollComandas.setAlignmentX(
                Component.LEFT_ALIGNMENT
        );

        // ---------- MONTAGEM ----------
        painelDetalhes.add(topo);

        painelDetalhes.add(
                Box.createRigidArea(
                        new Dimension(0, 8)
                )
        );

        painelDetalhes.add(lblDetalhePeriodo);
        painelDetalhes.add(lblDetalheOperador);

        painelDetalhes.add(
                Box.createRigidArea(
                        new Dimension(0, 15)
                )
        );

        painelDetalhes.add(cards);

        painelDetalhes.add(
                Box.createRigidArea(
                        new Dimension(0, 15)
                )
        );

        painelDetalhes.add(tituloPagamentos);

        painelDetalhes.add(
                Box.createRigidArea(
                        new Dimension(0, 5)
                )
        );

        painelDetalhes.add(scrollPagamentos);

        painelDetalhes.add(
                Box.createRigidArea(
                        new Dimension(0, 15)
                )
        );

        painelDetalhes.add(tituloMovimentacoes);

        painelDetalhes.add(
                Box.createRigidArea(
                        new Dimension(0, 5)
                )
        );

        painelDetalhes.add(scrollMovimentacoes);

        painelDetalhes.add(
                Box.createRigidArea(
                        new Dimension(0, 15)
                )
        );

        painelDetalhes.add(tituloComandas);

        painelDetalhes.add(
                Box.createRigidArea(
                        new Dimension(0, 5)
                )
        );

        painelDetalhes.add(scrollComandas);
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
                        .getValueAt(linha, 0)
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
                    caixaSelecionado
                            .getDataHoraAbertura()
                            .format(data)
                            + " • "
                            + caixaSelecionado
                            .getDataHoraAbertura()
                            .format(hora);

            if (caixaSelecionado.getDataHoraFechamento() != null) {

                periodo += " às "
                        + caixaSelecionado
                        .getDataHoraFechamento()
                        .format(hora);

            } else {

                periodo += " • Em andamento";
            }

            lblDetalhePeriodo.setText(periodo);

            String operador =
                    caixaSelecionado.getUsuarioAbertura() != null
                            ? caixaSelecionado
                            .getUsuarioAbertura()
                            .getNome()
                            : "-";

            lblDetalheOperador.setText(
                    "Operador: " + operador
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
                                entrada.getKey(),
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

            for (MovimentacaoCaixaDAO.MovimentacaoResumo movimentacao
                    : movimentacoes) {

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

            // Comandas
            modeloTabelaComandasDetalhes.setRowCount(0);

            List<CaixaDAO.ComandaResumo> comandas =
                    caixaDAO.listarComandasDoCaixa(
                            idCaixa
                    );

            for (CaixaDAO.ComandaResumo comanda : comandas) {

                String atendimento;

                if ("MESA".equalsIgnoreCase(comanda.tipoAtendimento)) {
                    atendimento = "Mesa " + comanda.numeroMesa;
                } else {
                    atendimento = comanda.tipoAtendimento;
                }

                modeloTabelaComandasDetalhes.addRow(
                        new Object[]{
                                atendimento,
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