package br.com.trevizan.espetinhos.view;

import br.com.trevizan.espetinhos.PadraoJPanel;
import br.com.trevizan.espetinhos.dao.CategoriaDAO;
import br.com.trevizan.espetinhos.dao.ProdutoDAO;
import br.com.trevizan.espetinhos.model.Categoria;
import br.com.trevizan.espetinhos.model.Produto;

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

    private final Color VERDE =
            new Color(20, 115, 10);

    private final Color CINZA_TABELA =
            new Color(224, 224, 224);

    private final Color CINZA_CABECALHO =
            new Color(135, 135, 132);

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

        painelPrincipal.setBorder(
                BorderFactory.createEmptyBorder(
                        40,
                        35,
                        30,
                        35
                )
        );

        /*
         * ==========================
         * TÍTULO
         * ==========================
         */

        JLabel lblTitulo =
                new JLabel(
                        "PRODUTOS"
                );

        lblTitulo.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        28
                )
        );

        lblTitulo.setForeground(
                VERDE
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
                new JTextField();

        txtPesquisar.setFont(
                new Font(
                        "Segoe UI",
                        Font.PLAIN,
                        16
                )
        );

        txtPesquisar.setPreferredSize(
                new Dimension(
                        500,
                        48
                )
        );

        txtPesquisar.putClientProperty(
                "JTextField.placeholderText",
                "Pesquisar"
        );

        txtPesquisar.setHorizontalAlignment(
                JTextField.CENTER
        );

        btnNovoProduto =
                new JButton(
                        "+  Novo produto"
                );

        btnNovoProduto.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        16
                )
        );

        btnNovoProduto.setForeground(
                Color.WHITE
        );

        btnNovoProduto.setBackground(
                VERDE
        );

        btnNovoProduto.setFocusPainted(
                false
        );

        btnNovoProduto.setPreferredSize(
                new Dimension(
                        220,
                        48
                )
        );

        painelTopo.add(
                txtPesquisar,
                BorderLayout.CENTER
        );

        painelTopo.add(
                btnNovoProduto,
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

        JScrollPane scrollPane =
                new JScrollPane(
                        tabelaProdutos
                );

        scrollPane.setBorder(
                BorderFactory.createLineBorder(
                        new Color(
                                150,
                                150,
                                150
                        )
                )
        );

        scrollPane
                .getViewport()
                .setBackground(
                        CINZA_TABELA
                );

        painelConteudo.add(
                scrollPane,
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

        tabelaProdutos.setRowHeight(
                66
        );

        tabelaProdutos.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        15
                )
        );

        tabelaProdutos.setForeground(
                VERDE
        );

        tabelaProdutos.setBackground(
                CINZA_TABELA
        );

        tabelaProdutos.setGridColor(
                new Color(
                        100,
                        100,
                        100
                )
        );

        tabelaProdutos.setShowVerticalLines(
                true
        );

        tabelaProdutos.setShowHorizontalLines(
                true
        );

        tabelaProdutos.setSelectionBackground(
                new Color(
                        210,
                        225,
                        210
                )
        );

        tabelaProdutos.setSelectionForeground(
                VERDE
        );

        /*
         * Cabeçalho
         */

        tabelaProdutos
                .getTableHeader()
                .setFont(
                        new Font(
                                "Segoe UI",
                                Font.BOLD,
                                15
                        )
                );

        tabelaProdutos
                .getTableHeader()
                .setForeground(
                        Color.WHITE
                );

        tabelaProdutos
                .getTableHeader()
                .setBackground(
                        CINZA_CABECALHO
                );

        tabelaProdutos
                .getTableHeader()
                .setPreferredSize(
                        new Dimension(
                                0,
                                55
                        )
                );

        /*
         * Centraliza células
         */

        DefaultTableCellRenderer centralizado =
                new DefaultTableCellRenderer();

        centralizado.setHorizontalAlignment(
                SwingConstants.CENTER
        );

        for (
                int i = 0;
                i < tabelaProdutos.getColumnCount();
                i++
        ) {

            tabelaProdutos
                    .getColumnModel()
                    .getColumn(i)
                    .setCellRenderer(
                            centralizado
                    );
        }

        /*
         * Renderer específico
         * da coluna Ações
         */

        DefaultTableCellRenderer rendererAcao =
                new DefaultTableCellRenderer() {

                    @Override
                    public Component getTableCellRendererComponent(
                            JTable table,
                            Object value,
                            boolean isSelected,
                            boolean hasFocus,
                            int row,
                            int column
                    ) {

                        JLabel label =
                                new JLabel(
                                        "Editar"
                                );

                        label.setHorizontalAlignment(
                                SwingConstants.CENTER
                        );

                        label.setForeground(
                                VERDE
                        );

                        label.setFont(
                                new Font(
                                        "Segoe UI",
                                        Font.BOLD,
                                        14
                                )
                        );

                        if (isSelected) {

                            label.setBackground(
                                    table.getSelectionBackground()
                            );

                        } else {

                            label.setBackground(
                                    table.getBackground()
                            );
                        }

                        label.setOpaque(
                                true
                        );

                        return label;
                    }
                };

        tabelaProdutos
                .getColumnModel()
                .getColumn(6)
                .setCellRenderer(
                        rendererAcao
                );

        /*
         * Largura das colunas
         */

        tabelaProdutos.getColumnModel().getColumn(0).setPreferredWidth(200); // Produto
        tabelaProdutos.getColumnModel().getColumn(1).setPreferredWidth(260); // Descrição
        tabelaProdutos.getColumnModel().getColumn(2).setPreferredWidth(160); // Categoria
        tabelaProdutos.getColumnModel().getColumn(3).setPreferredWidth(100); // Preço
        tabelaProdutos.getColumnModel().getColumn(4).setPreferredWidth(90);  // Estoque
        tabelaProdutos.getColumnModel().getColumn(5).setPreferredWidth(110); // Status
        tabelaProdutos.getColumnModel().getColumn(6).setPreferredWidth(100); // Ações
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
                            moeda.format(produto.getPreco()),
                            produto.getQuantidadeEstoque(),
                            status,
                            "Editar"
                    }
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
                                        coluna == 6
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

        if (linha < 0 || linha >= produtos.size()) {
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