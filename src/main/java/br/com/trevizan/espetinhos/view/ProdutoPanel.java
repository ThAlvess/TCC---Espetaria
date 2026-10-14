package br.com.trevizan.espetinhos.view;

import br.com.trevizan.espetinhos.PadraoJPanel;
import br.com.trevizan.espetinhos.dao.CategoriaDAO;
import br.com.trevizan.espetinhos.dao.ProdutoDAO;
import br.com.trevizan.espetinhos.model.Categoria;
import br.com.trevizan.espetinhos.model.Produto;
import br.com.trevizan.espetinhos.util.CampoMoeda;
import br.com.trevizan.espetinhos.util.ImagemProduto;
import br.com.trevizan.espetinhos.util.PadraoComponentes;
import br.com.trevizan.espetinhos.util.PadraoTela;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
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
    private static final int COL_STATUS = 5;
    private static final int COL_ACOES = 6;

    /** Tamanho da prévia da foto no formulário. */
    private static final int TAMANHO_PREVIA = 170;

    /** Última pasta usada para escolher imagens (abre nela da próxima vez). */
    private static File ultimaPastaImagens;

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

        /*
         * Status colorido:
         * verde = ativo, vermelho = inativo
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

        tabelaProdutos.getColumnModel().getColumn(0).setPreferredWidth(230); // Produto
        tabelaProdutos.getColumnModel().getColumn(1).setPreferredWidth(300); // Descrição
        tabelaProdutos.getColumnModel().getColumn(2).setPreferredWidth(150); // Categoria
        tabelaProdutos.getColumnModel().getColumn(3).setPreferredWidth(100); // Destino
        tabelaProdutos.getColumnModel().getColumn(COL_PRECO).setPreferredWidth(100);
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

            String status = produto.isAtivo() ? "Ativo" : "Inativo";

            modeloTabela.addRow(
                    new Object[]{
                            produto.getNome(),
                            produto.getDescricao(),
                            produto.getCategoria().getNome(),
                            produto.getLocalPreparo(),
                            moeda.format(produto.getPreco()),
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

        abrirFormularioProduto(produtosExibidos.get(linha));
    }

    /*
     * ==========================
     * NOVO PRODUTO
     * ==========================
     */

    private void abrirCadastroProduto() {
        abrirFormularioProduto(null);
    }

    /*
     * ==========================
     * FORMULÁRIO (NOVO / EDITAR)
     * ==========================
     */

    /**
     * Formulário de cadastro e edição de produto, com a foto ao lado.
     *
     * <p>Se algum campo estiver errado, mostra o aviso e reabre o formulário
     * mantendo o que já foi preenchido.</p>
     *
     * @param produto o produto a editar, ou {@code null} para um produto novo
     */
    private void abrirFormularioProduto(Produto produto) {

        boolean novo = produto == null;
        String titulo = novo ? "Novo produto" : "Editar produto";

        // ---------- campos ----------
        JTextField txtNome = new JTextField(novo ? "" : produto.getNome(), 24);
        JTextField txtDescricao = new JTextField(
                novo || produto.getDescricao() == null ? "" : produto.getDescricao(), 24);
        CampoMoeda txtPreco = new CampoMoeda(novo ? BigDecimal.ZERO : produto.getPreco());

        JComboBox<Categoria> cbCategoria = new JComboBox<>();
        for (Categoria categoria : categoriaDAO.listarAtivas()) {
            cbCategoria.addItem(categoria);
            if (!novo && categoria.getIdCategoria() == produto.getCategoria().getIdCategoria()) {
                cbCategoria.setSelectedItem(categoria);
            }
        }

        JComboBox<String> cbLocalPreparo = new JComboBox<>(new String[]{"COZINHA", "BALCAO"});
        if (!novo) {
            cbLocalPreparo.setSelectedItem(produto.getLocalPreparo());
        }

        JCheckBox chkAtivo = new JCheckBox("Produto ativo");
        chkAtivo.setSelected(novo || produto.isAtivo());

        for (JTextField campo : new JTextField[]{txtNome, txtDescricao}) {
            PadraoComponentes.aplicarEstiloCampo(campo);
        }

        JPanel campos = new JPanel(new GridLayout(0, 1, 5, 5));
        campos.add(new JLabel("Nome:"));
        campos.add(txtNome);
        campos.add(new JLabel("Descrição:"));
        campos.add(txtDescricao);
        campos.add(new JLabel("Categoria:"));
        campos.add(cbCategoria);
        campos.add(new JLabel("Local de preparo:"));
        campos.add(cbLocalPreparo);
        campos.add(new JLabel("Preço:"));
        campos.add(txtPreco);
        campos.add(chkAtivo);

        // ---------- foto ----------
        // imagemAtual[0] = bytes da foto que será gravada (null = sem foto)
        byte[][] imagemAtual = new byte[1][];
        BufferedImage fotoInicial = novo ? null : ImagemProduto.obter(produto.getIdProduto());
        boolean[] fotoAlterada = {false};

        JLabel previa = new JLabel();
        previa.setHorizontalAlignment(SwingConstants.CENTER);
        previa.setPreferredSize(new Dimension(TAMANHO_PREVIA, TAMANHO_PREVIA));
        previa.setBorder(BorderFactory.createLineBorder(PadraoComponentes.COR_BORDA));

        JButton btnEscolher = new JButton("Escolher imagem...");
        JButton btnRemover = new JButton("Remover imagem");

        Runnable atualizarPrevia = () -> {
            BufferedImage foto = fotoAlterada[0]
                    ? ImagemProduto.ler(imagemAtual[0])
                    : fotoInicial;
            if (foto != null) {
                previa.setIcon(ImagemProduto.criarIcone(foto, TAMANHO_PREVIA, TAMANHO_PREVIA, 0));
                previa.setText(null);
            } else {
                previa.setIcon(null);
                previa.setText("<html><center>Sem imagem</center></html>");
                previa.setForeground(PadraoComponentes.COR_TEXTO_SECUNDARIO);
            }
            btnRemover.setEnabled(foto != null);
        };
        atualizarPrevia.run();

        btnEscolher.addActionListener(e -> {
            JFileChooser seletor = new JFileChooser(ultimaPastaImagens);
            seletor.setDialogTitle("Escolher imagem do produto");
            seletor.setAcceptAllFileFilterUsed(false);
            seletor.setFileFilter(new FileNameExtensionFilter(
                    "Imagens (JPG, PNG, GIF, BMP)", "jpg", "jpeg", "png", "gif", "bmp"));

            if (seletor.showOpenDialog(previa) != JFileChooser.APPROVE_OPTION) {
                return;
            }
            ultimaPastaImagens = seletor.getCurrentDirectory();
            try {
                imagemAtual[0] = ImagemProduto.prepararArquivo(seletor.getSelectedFile());
                fotoAlterada[0] = true;
                atualizarPrevia.run();
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(previa, ex.getMessage(),
                        "Imagem inválida", JOptionPane.WARNING_MESSAGE);
            }
        });

        btnRemover.addActionListener(e -> {
            imagemAtual[0] = null;
            fotoAlterada[0] = true;
            atualizarPrevia.run();
        });

        JPanel botoesFoto = new JPanel(new GridLayout(0, 1, 0, 5));
        botoesFoto.add(btnEscolher);
        botoesFoto.add(btnRemover);

        JPanel painelFoto = new JPanel(new BorderLayout(0, 8));
        painelFoto.add(new JLabel("Imagem:"), BorderLayout.NORTH);
        painelFoto.add(previa, BorderLayout.CENTER);
        painelFoto.add(botoesFoto, BorderLayout.SOUTH);

        JPanel colunaFoto = new JPanel(new BorderLayout());
        colunaFoto.add(painelFoto, BorderLayout.NORTH);

        JPanel painel = new JPanel(new BorderLayout(20, 0));
        painel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        painel.add(campos, BorderLayout.CENTER);
        painel.add(colunaFoto, BorderLayout.EAST);

        CampoMoeda.focarAoAbrir(txtNome);

        // ---------- confirmação e validação ----------
        while (true) {

            int resultado = JOptionPane.showConfirmDialog(
                    this,
                    painel,
                    titulo,
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE
            );

            if (resultado != JOptionPane.OK_OPTION) {
                return;
            }

            String nome = txtNome.getText().trim();
            String descricao = txtDescricao.getText().trim();
            Categoria categoria = (Categoria) cbCategoria.getSelectedItem();
            String erro = null;

            if (nome.isEmpty()) {
                erro = "Informe o nome do produto.";
            } else if (categoria == null) {
                erro = "Selecione uma categoria.";
            } else if (!txtPreco.temValor()) {
                erro = "Informe o preço do produto.";
            }

            if (erro != null) {
                JOptionPane.showMessageDialog(this, erro, titulo, JOptionPane.WARNING_MESSAGE);
                continue; // reabre com o que já foi preenchido
            }

            Produto alvo = novo ? new Produto() : produto;
            alvo.setNome(nome);
            alvo.setDescricao(descricao);
            alvo.setPreco(txtPreco.getValor());
            alvo.setCategoria(categoria);
            alvo.setLocalPreparo((String) cbLocalPreparo.getSelectedItem());
            alvo.setAtivo(chkAtivo.isSelected());

            try {
                if (novo) {
                    produtoDAO.cadastrar(alvo);
                } else {
                    produtoDAO.atualizar(alvo);
                }
            } catch (RuntimeException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this,
                        "Não foi possível salvar o produto no banco de dados.",
                        "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String avisoImagem = "";
            if (fotoAlterada[0]) {
                try {
                    produtoDAO.salvarImagem(alvo.getIdProduto(), imagemAtual[0]);
                    ImagemProduto.atualizar(alvo.getIdProduto(), imagemAtual[0]);
                } catch (RuntimeException e) {
                    e.printStackTrace();
                    avisoImagem = "\n\nA imagem não foi salva. Verifique se o script "
                            + "database/06_cardapio_e_imagens.sql foi executado no banco.";
                }
            }

            JOptionPane.showMessageDialog(
                    this,
                    (novo ? "Produto cadastrado com sucesso!" : "Produto atualizado com sucesso!")
                            + avisoImagem
            );

            carregarProdutos();
            return;
        }
    }

}
