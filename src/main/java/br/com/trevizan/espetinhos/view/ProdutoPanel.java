package br.com.trevizan.espetinhos.view;

import br.com.trevizan.espetinhos.PadraoJPanel;
import br.com.trevizan.espetinhos.dao.CategoriaDAO;
import br.com.trevizan.espetinhos.dao.ProdutoDAO;
import br.com.trevizan.espetinhos.model.Categoria;
import br.com.trevizan.espetinhos.model.Produto;
import br.com.trevizan.espetinhos.util.PadraoComponentes;
import br.com.trevizan.espetinhos.util.PadraoTela;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import java.awt.*;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class ProdutoPanel extends PadraoJPanel {

    private JTextField txtPesquisar;
    private JButton btnNovoProduto;

    private JTable tabelaProdutos;
    private DefaultTableModel modeloTabela;

    private final ProdutoDAO produtoDAO;
    private final CategoriaDAO categoriaDAO;

    private List<Produto> produtos;
    private List<Produto> produtosExibidos;

    private JLabel lblResumo;

    private static final int COL_PRECO = 4;
    private static final int COL_ESTOQUE = 5;
    private static final int COL_STATUS = 6;
    private static final int COL_ACOES = 7;

    public ProdutoPanel() {

        produtoDAO =
                new ProdutoDAO();

        categoriaDAO =
                new CategoriaDAO();

        criarComponentes();

        carregarProdutos();
    }

    private void criarComponentes() {

        setLayout(
                new BorderLayout()
        );

        /*
         * A cor principal vem do PadraoJPanel:
         *
         * new Color(238,232,227)
         */

        JPanel painelPrincipal =
                new JPanel(
                        new BorderLayout(
                                0,
                                20
                        )
                );

        painelPrincipal.setBackground(
                getBackground()
        );

        // margens do padrão de telas (título na mesma posição da tela de Relatórios)
        painelPrincipal.setBorder(
                BorderFactory.createEmptyBorder(
                        PadraoTela.MARGEM_TOPO,
                        PadraoTela.MARGEM_LATERAL,
                        30,
                        PadraoTela.MARGEM_LATERAL
                )
        );

        /*
         * ==========================
         * TÍTULO
         * ==========================
         */

        JLabel lblTitulo =
                PadraoTela.criarTitulo(
                        "PRODUTOS"
                );

        painelPrincipal.add(
                lblTitulo,
                BorderLayout.NORTH
        );

        /*
         * ==========================
         * CONTEÚDO
         * ==========================
         */

        JPanel painelConteudo =
                new JPanel(
                        new BorderLayout(
                                0,
                                16
                        )
                );

        painelConteudo.setBackground(
                getBackground()
        );

        /*
         * ==========================
         * PESQUISA + NOVO PRODUTO
         * ==========================
         */

        JPanel painelTopo =
                new JPanel(
                        new BorderLayout(
                                10,
                                0
                        )
                );

        painelTopo.setBackground(
                getBackground()
        );

        txtPesquisar =
                PadraoComponentes.criarCampoPesquisa(
                        "Pesquisar produto ou categoria..."
                );

        btnNovoProduto =
                PadraoComponentes.criarBotaoPrimario(
                        "Novo produto",
                        PadraoComponentes.iconeMais(Color.WHITE)
                );

        painelTopo.add(
                txtPesquisar,
                BorderLayout.CENTER
        );

        // GridBagLayout mantém o botão na altura padrão, centralizado na linha
        JPanel painelBotao =
                new JPanel(
                        new GridBagLayout()
                );

        painelBotao.setOpaque(
                false
        );

        painelBotao.add(
                btnNovoProduto
        );

        painelTopo.add(
                painelBotao,
                BorderLayout.EAST
        );

        painelConteudo.add(
                painelTopo,
                BorderLayout.NORTH
        );

        /*
         * ==========================
         * TABELA
         * ==========================
         */

        String[] colunas = {
                "Produto",
                "Descrição",
                "Categoria",
                "Destino",
                "Preço",
                "Estoque",
                "Status",
                "Ações"
        };

        modeloTabela =
                new DefaultTableModel(
                        colunas,
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

        tabelaProdutos =
                new JTable(
                        modeloTabela
                );

        configurarTabela();

        lblResumo =
                new JLabel(" ");

        lblResumo.setFont(
                new Font(
                        "Segoe UI",
                        Font.PLAIN,
                        13
                )
        );

        lblResumo.setForeground(
                PadraoComponentes.COR_TEXTO_SECUNDARIO
        );

        painelConteudo.add(
                PadraoComponentes.criarCartaoTabela(
                        "Produtos cadastrados",
                        tabelaProdutos,
                        lblResumo
                ),
                BorderLayout.CENTER
        );

        painelPrincipal.add(
                painelConteudo,
                BorderLayout.CENTER
        );

        add(
                painelPrincipal,
                BorderLayout.CENTER
        );

        /*
         * ==========================
         * EVENTOS
         * ==========================
         */

        btnNovoProduto.addActionListener(
                e -> abrirCadastroProduto()
        );

        configurarPesquisa();

        configurarCliqueTabela();
    }

    private void configurarTabela() {

        /*
         * Estilo da tabela (zebrado, sem grade, cabeçalho com linha inferior)
         */

        PadraoComponentes.estilizarTabela(
                tabelaProdutos
        );

        tabelaProdutos.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );

        PadraoComponentes.alinharColuna(tabelaProdutos, COL_PRECO, SwingConstants.RIGHT);
        PadraoComponentes.alinharColuna(tabelaProdutos, COL_ESTOQUE, SwingConstants.CENTER);

        /*
         * Status colorido:
         * verde = ativo, laranja = sem estoque, vermelho = inativo
         */

        tabelaProdutos
                .getColumnModel()
                .getColumn(COL_STATUS)
                .setCellRenderer(
                        new PadraoComponentes.RendererCelula(SwingConstants.LEFT) {

                            @Override
                            public Component getTableCellRendererComponent(
                                    JTable table,
                                    Object value,
                                    boolean isSelected,
                                    boolean hasFocus,
                                    int row,
                                    int column
                            ) {

                                super.getTableCellRendererComponent(
                                        table, value, isSelected, hasFocus, row, column
                                );

                                String status = value != null ? value.toString() : "";

                                setFont(getFont().deriveFont(Font.BOLD));

                                if ("Inativo".equals(status)) {
                                    setForeground(PadraoComponentes.VERMELHO);
                                } else if ("Sem estoque".equals(status)) {
                                    setForeground(PadraoComponentes.LARANJA);
                                } else {
                                    setForeground(PadraoComponentes.VERDE);
                                }

                                return this;
                            }
                        }
                );

        /*
         * Coluna Ações: "Editar" com ícone de lápis
         */

        tabelaProdutos
                .getColumnModel()
                .getColumn(COL_ACOES)
                .setCellRenderer(
                        new PadraoComponentes.RendererCelula(SwingConstants.CENTER) {

                            @Override
                            public Component getTableCellRendererComponent(
                                    JTable table,
                                    Object value,
                                    boolean isSelected,
                                    boolean hasFocus,
                                    int row,
                                    int column
                            ) {

                                super.getTableCellRendererComponent(
                                        table, "Editar", isSelected, hasFocus, row, column
                                );

                                setIcon(
                                        PadraoComponentes.iconeEditar(
                                                PadraoComponentes.VERDE
                                        )
                                );

                                setIconTextGap(6);
                                setForeground(PadraoComponentes.VERDE);
                                setFont(getFont().deriveFont(Font.BOLD));

                                return this;
                            }
                        }
                );

        /*
         * Cursor de "mãozinha" sobre o Editar
         */

        tabelaProdutos.addMouseMotionListener(
                new java.awt.event.MouseMotionAdapter() {

                    @Override
                    public void mouseMoved(
                            java.awt.event.MouseEvent evt
                    ) {

                        boolean sobreEditar =
                                tabelaProdutos.columnAtPoint(evt.getPoint()) == COL_ACOES
                                        && tabelaProdutos.rowAtPoint(evt.getPoint()) >= 0;

                        tabelaProdutos.setCursor(
                                sobreEditar
                                        ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
                                        : Cursor.getDefaultCursor()
                        );
                    }
                }
        );

        /*
         * Largura das colunas
         */

        tabelaProdutos.getColumnModel().getColumn(0).setPreferredWidth(190); // Produto
        tabelaProdutos.getColumnModel().getColumn(1).setPreferredWidth(260); // Descrição
        tabelaProdutos.getColumnModel().getColumn(2).setPreferredWidth(120); // Categoria
        tabelaProdutos.getColumnModel().getColumn(3).setPreferredWidth(100); // Destino
        tabelaProdutos.getColumnModel().getColumn(COL_PRECO).setPreferredWidth(100);
        tabelaProdutos.getColumnModel().getColumn(COL_ESTOQUE).setPreferredWidth(90);
        tabelaProdutos.getColumnModel().getColumn(COL_STATUS).setPreferredWidth(110);
        tabelaProdutos.getColumnModel().getColumn(COL_ACOES).setPreferredWidth(110);
    }

    /*
     * ==========================
     * CARREGAR PRODUTOS
     * ==========================
     */

    private void carregarProdutos() {

        produtos =
                produtoDAO.listar();

        preencherTabela(
                produtos
        );
    }

    private void preencherTabela(
            List<Produto> lista

    ) {
        produtosExibidos = lista;
        modeloTabela.setRowCount(
                0
        );

        NumberFormat moeda =
                NumberFormat.getCurrencyInstance(
                        new Locale(
                                "pt",
                                "BR"
                        )
                );

        for (
                Produto produto :
                lista
        ) {

            String status;

            if (!produto.isAtivo()) {

                status =
                        "Inativo";

            } else if (
                    produto
                            .getQuantidadeEstoque()
                            <= 0
            ) {

                status =
                        "Sem estoque";

            } else {

                status =
                        "Ativo";
            }

            modeloTabela.addRow(
                    new Object[]{
                            produto.getNome(),
                            produto.getDescricao(),
                            produto.getCategoria().getNome(),
                            produto.getLocalPreparo(),
                            moeda.format(produto.getPreco()),
                            produto.getQuantidadeEstoque(),
                            status,
                            "Editar"
                    }
            );
        }

        if (lblResumo != null) {
            lblResumo.setText(
                    lista.size() + (lista.size() == 1 ? " produto" : " produtos")
            );
        }
    }

    /*
     * ==========================
     * PESQUISA
     * ==========================
     */

    private void configurarPesquisa() {

        txtPesquisar
                .getDocument()
                .addDocumentListener(
                        new DocumentListener() {

                            @Override
                            public void insertUpdate(
                                    DocumentEvent e
                            ) {

                                pesquisar();
                            }

                            @Override
                            public void removeUpdate(
                                    DocumentEvent e
                            ) {

                                pesquisar();
                            }

                            @Override
                            public void changedUpdate(
                                    DocumentEvent e
                            ) {

                                pesquisar();
                            }
                        }
                );
    }

    private void pesquisar() {

        String texto =
                txtPesquisar
                        .getText()
                        .trim()
                        .toLowerCase();

        if (
                texto.isEmpty()
        ) {

            preencherTabela(
                    produtos
            );

            return;
        }

        List<Produto> filtrados =
                produtos
                        .stream()
                        .filter(
                                produto ->

                                        produto
                                                .getNome()
                                                .toLowerCase()
                                                .contains(
                                                        texto
                                                )

                                                ||

                                                produto
                                                        .getCategoria()
                                                        .getNome()
                                                        .toLowerCase()
                                                        .contains(
                                                                texto
                                                        )
                        )
                        .toList();

        preencherTabela(
                filtrados
        );
    }

    /*
     * ==========================
     * CLIQUE EM EDITAR
     * ==========================
     */

    private void configurarCliqueTabela() {

        tabelaProdutos.addMouseListener(
                new java.awt.event.MouseAdapter() {

                    @Override
                    public void mouseClicked(
                            java.awt.event.MouseEvent evt
                    ) {

                        int linha =
                                tabelaProdutos.rowAtPoint(
                                        evt.getPoint()
                                );

                        int coluna =
                                tabelaProdutos.columnAtPoint(
                                        evt.getPoint()
                                );

                        if (
                                linha >= 0
                                        &&
                                        coluna == COL_ACOES
                        ) {

                            editarProduto(
                                    linha
                            );
                        }
                    }
                }
        );
    }

    private void editarProduto(int linha) {

        if (linha < 0 || produtosExibidos == null || linha >= produtosExibidos.size()) {
            return;
        }

        Produto produto =
                produtosExibidos.get(linha);

        JTextField txtNome =
                new JTextField(produto.getNome());

        JTextField txtDescricao =
                new JTextField(produto.getDescricao());

        JTextField txtPreco =
                new JTextField(
                        produto.getPreco()
                                .toString()
                                .replace(".", ",")
                );

        JTextField txtEstoque =
                new JTextField(
                        String.valueOf(
                                produto.getQuantidadeEstoque()
                        )
                );

        JComboBox<Categoria> cbCategoria =
                new JComboBox<>();

        JComboBox<String> cbLocalPreparo =
                new JComboBox<>(
                        new String[]{
                                "COZINHA",
                                "BALCAO"
                        }
                );

        cbLocalPreparo.setSelectedItem(
                produto.getLocalPreparo()
        );

        for (Categoria categoria : categoriaDAO.listarAtivas()) {

            cbCategoria.addItem(categoria);

            if (
                    categoria.getIdCategoria()
                            == produto.getCategoria().getIdCategoria()
            ) {
                cbCategoria.setSelectedItem(categoria);
            }
        }

        JCheckBox chkAtivo =
                new JCheckBox("Produto ativo");

        chkAtivo.setSelected(
                produto.isAtivo()
        );

        JPanel painel =
                new JPanel(
                        new GridLayout(
                                0,
                                1,
                                5,
                                5
                        )
                );

        painel.setBorder(
                BorderFactory.createEmptyBorder(
                        10,
                        10,
                        10,
                        10
                )
        );

        painel.add(new JLabel("Nome:"));
        painel.add(txtNome);

        painel.add(new JLabel("Descrição:"));
        painel.add(txtDescricao);

        painel.add(new JLabel("Categoria:"));
        painel.add(cbCategoria);

        painel.add(new JLabel("Local de preparo:"));
        painel.add(cbLocalPreparo);

        painel.add(new JLabel("Preço:"));
        painel.add(txtPreco);

        painel.add(new JLabel("Estoque:"));
        painel.add(txtEstoque);

        painel.add(chkAtivo);

        int resultado =
                JOptionPane.showConfirmDialog(
                        this,
                        painel,
                        "Editar produto",
                        JOptionPane.OK_CANCEL_OPTION,
                        JOptionPane.PLAIN_MESSAGE
                );

        if (resultado != JOptionPane.OK_OPTION) {
            return;
        }

        try {

            String nome =
                    txtNome.getText().trim();

            String descricao =
                    txtDescricao.getText().trim();

            if (nome.isEmpty()) {

                JOptionPane.showMessageDialog(
                        this,
                        "Informe o nome do produto."
                );

                return;
            }

            BigDecimal preco =
                    new BigDecimal(
                            txtPreco
                                    .getText()
                                    .trim()
                                    .replace(",", ".")
                    );

            int estoque =
                    Integer.parseInt(
                            txtEstoque
                                    .getText()
                                    .trim()
                    );

            Categoria categoria =
                    (Categoria)
                            cbCategoria.getSelectedItem();

            if (categoria == null) {

                JOptionPane.showMessageDialog(
                        this,
                        "Selecione uma categoria."
                );

                return;
            }

            if (
                    preco.compareTo(
                            BigDecimal.ZERO
                    ) < 0
            ) {

                JOptionPane.showMessageDialog(
                        this,
                        "O preço não pode ser negativo."
                );

                return;
            }

            if (estoque < 0) {

                JOptionPane.showMessageDialog(
                        this,
                        "O estoque não pode ser negativo."
                );

                return;
            }

            produto.setNome(nome);
            produto.setDescricao(descricao);
            produto.setPreco(preco);
            produto.setQuantidadeEstoque(estoque);
            produto.setCategoria(categoria);
            produto.setLocalPreparo(
                    (String) cbLocalPreparo.getSelectedItem()
            );
            produto.setAtivo(
                    chkAtivo.isSelected()
            );

            produtoDAO.atualizar(
                    produto
            );

            JOptionPane.showMessageDialog(
                    this,
                    "Produto atualizado com sucesso!"
            );

            carregarProdutos();

        } catch (NumberFormatException e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Verifique os campos preço e estoque.",
                    "Dados inválidos",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    /*
     * ==========================
     * NOVO PRODUTO
     * ==========================
     */

    private void abrirCadastroProduto() {

        JTextField txtNome =
                new JTextField();

        JTextField txtDescricao =
                new JTextField();

        JTextField txtPreco =
                new JTextField();

        JTextField txtEstoque =
                new JTextField();

        JComboBox<Categoria> cbCategoria =
                new JComboBox<>();

        JComboBox<String> cbLocalPreparo =
                new JComboBox<>(
                        new String[]{
                                "COZINHA",
                                "BALCAO"
                        }
                );

        JCheckBox chkAtivo =
                new JCheckBox(
                        "Produto ativo"
                );

        chkAtivo.setSelected(
                true
        );

        /*
         * Carrega categorias
         */

        for (
                Categoria categoria :
                categoriaDAO.listarAtivas()
        ) {

            cbCategoria.addItem(
                    categoria
            );
        }

        /*
         * Formulário
         */

        JPanel painel =
                new JPanel(
                        new GridLayout(
                                0,
                                1,
                                5,
                                5
                        )
                );

        painel.setBorder(
                BorderFactory.createEmptyBorder(
                        10,
                        10,
                        10,
                        10
                )
        );

        painel.add(
                new JLabel(
                        "Nome:"
                )
        );

        painel.add(
                txtNome
        );

        painel.add(
                new JLabel(
                        "Descrição:"
                )
        );

        painel.add(
                txtDescricao
        );

        painel.add(
                new JLabel(
                        "Categoria:"
                )
        );

        painel.add(
                cbCategoria
        );

        painel.add(
                new JLabel(
                        "Local de preparo:"
                )
        );

        painel.add(
                cbLocalPreparo
        );

        painel.add(
                new JLabel(
                        "Preço:"
                )
        );

        painel.add(
                txtPreco
        );

        painel.add(
                new JLabel(
                        "Estoque:"
                )
        );

        painel.add(
                txtEstoque
        );

        painel.add(
                chkAtivo
        );

        int resultado =
                JOptionPane.showConfirmDialog(
                        this,
                        painel,
                        "Novo produto",
                        JOptionPane.OK_CANCEL_OPTION,
                        JOptionPane.PLAIN_MESSAGE
                );

        if (
                resultado
                        != JOptionPane.OK_OPTION
        ) {

            return;
        }

        /*
         * Salvar produto
         */

        try {

            String nome =
                    txtNome
                            .getText()
                            .trim();

            String descricao =
                    txtDescricao
                            .getText()
                            .trim();

            if (
                    nome.isEmpty()
            ) {

                JOptionPane.showMessageDialog(
                        this,
                        "Informe o nome do produto."
                );

                return;
            }

            if (
                    txtPreco
                            .getText()
                            .trim()
                            .isEmpty()
            ) {

                JOptionPane.showMessageDialog(
                        this,
                        "Informe o preço."
                );

                return;
            }

            if (
                    txtEstoque
                            .getText()
                            .trim()
                            .isEmpty()
            ) {

                JOptionPane.showMessageDialog(
                        this,
                        "Informe o estoque."
                );

                return;
            }

            BigDecimal preco =
                    new BigDecimal(
                            txtPreco
                                    .getText()
                                    .trim()
                                    .replace(
                                            ",",
                                            "."
                                    )
                    );

            int estoque =
                    Integer.parseInt(
                            txtEstoque
                                    .getText()
                                    .trim()
                    );

            Categoria categoria =
                    (Categoria)
                            cbCategoria
                                    .getSelectedItem();

            if (
                    categoria == null
            ) {

                JOptionPane.showMessageDialog(
                        this,
                        "Selecione uma categoria."
                );

                return;
            }

            if (
                    preco.compareTo(
                            BigDecimal.ZERO
                    ) < 0
            ) {

                JOptionPane.showMessageDialog(
                        this,
                        "O preço não pode ser negativo."
                );

                return;
            }

            if (
                    estoque < 0
            ) {

                JOptionPane.showMessageDialog(
                        this,
                        "O estoque não pode ser negativo."
                );

                return;
            }

            Produto produto =
                    new Produto();

            produto.setNome(
                    nome
            );

            produto.setDescricao(
                    descricao
            );

            produto.setPreco(
                    preco
            );

            produto.setQuantidadeEstoque(
                    estoque
            );

            produto.setCategoria(
                    categoria
            );

            produto.setLocalPreparo(
                    (String) cbLocalPreparo.getSelectedItem()
            );

            produto.setAtivo(
                    chkAtivo.isSelected()
            );


            produtoDAO.cadastrar(
                    produto
            );

            JOptionPane.showMessageDialog(
                    this,
                    "Produto cadastrado com sucesso!"
            );

            /*
             * Atualiza a tabela
             */

            carregarProdutos();

        } catch (
                NumberFormatException e
        ) {

            JOptionPane.showMessageDialog(
                    this,
                    "Verifique os campos preço e estoque.",
                    "Dados inválidos",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }
}