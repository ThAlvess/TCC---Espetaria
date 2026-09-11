package br.com.trevizan.espetinhos.view;

import br.com.trevizan.espetinhos.PadraoJPanel;
import br.com.trevizan.espetinhos.dao.CategoriaDAO;
import br.com.trevizan.espetinhos.dao.ComandaDAO;
import br.com.trevizan.espetinhos.dao.ItemComandaDAO;
import br.com.trevizan.espetinhos.dao.MesaDAO;
import br.com.trevizan.espetinhos.dao.PagamentoDAO;
import br.com.trevizan.espetinhos.dao.ProdutoDAO;
import br.com.trevizan.espetinhos.model.Categoria;
import br.com.trevizan.espetinhos.model.Comanda;
import br.com.trevizan.espetinhos.model.ItemComanda;
import br.com.trevizan.espetinhos.model.Mesa;
import br.com.trevizan.espetinhos.model.Pagamento;
import br.com.trevizan.espetinhos.model.Produto;
import br.com.trevizan.espetinhos.model.Usuario;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ComandaPanel extends PadraoJPanel {

    private static final Color COR_FUNDO = new Color(238, 232, 227);
    private static final Color COR_CARD = Color.WHITE;
    private static final Color COR_VERDE = new Color(45, 94, 72);
    private static final Color COR_VERDE_ESCURO = new Color(35, 75, 57);
    private static final Color COR_VERDE_CLARO = new Color(231, 240, 235);
    private static final Color COR_TEXTO = new Color(46, 46, 46);
    private static final Color COR_TEXTO_SECUNDARIO = new Color(112, 112, 112);
    private static final Color COR_BORDA = new Color(222, 216, 211);
    private static final Color COR_DESTAQUE = new Color(179, 82, 67);
    private static final Color COR_DESTAQUE_ESCURO = new Color(150, 66, 54);
    private static final Color COR_ITEM = new Color(250, 248, 246);

    private final Mesa mesa;
    private final Comanda comanda;
    private final Usuario usuarioLogado;
    private final Runnable aoVoltar;

    private final ProdutoDAO produtoDAO;
    private final CategoriaDAO categoriaDAO;
    private final ItemComandaDAO itemComandaDAO;
    private final ComandaDAO comandaDAO;
    private final PagamentoDAO pagamentoDAO;
    private final MesaDAO mesaDAO;

    private final List<Integer> itensRemovidos = new ArrayList<>();
    private final List<ItemTemporario> itensTemporarios = new ArrayList<>();

    private final JPanel painelItens;
    private final JPanel painelProdutos;
    private final JPanel painelCategorias;
    private final JPanel painelPagamentos;

    private final JLabel lblSubtotal;
    private final JLabel lblTotal;
    private final JLabel lblTotalPago;
    private final JLabel lblRestante;

    private JButton botaoCategoriaSelecionada;

    private final NumberFormat moeda =
            NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

    public ComandaPanel(
            Mesa mesa,
            Comanda comanda,
            Usuario usuarioLogado,
            Runnable aoVoltar
    ) {
        this.mesa = mesa;
        this.comanda = comanda;
        this.usuarioLogado = usuarioLogado;
        this.aoVoltar = aoVoltar;

        produtoDAO = new ProdutoDAO();
        categoriaDAO = new CategoriaDAO();
        itemComandaDAO = new ItemComandaDAO();
        comandaDAO = new ComandaDAO();
        pagamentoDAO = new PagamentoDAO();
        mesaDAO = new MesaDAO();

        setBackground(COR_FUNDO);
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(18, 18, 18, 18));

        JPanel conteudo = new JPanel(new BorderLayout(18, 0));
        conteudo.setOpaque(false);
        add(conteudo, BorderLayout.CENTER);

        // ================================================================
        // COLUNA ESQUERDA - COMANDA
        // ================================================================
        RoundedPanel painelComanda = new RoundedPanel(22, COR_CARD);
        painelComanda.setLayout(new BorderLayout(0, 14));
        painelComanda.setBorder(new EmptyBorder(18, 18, 18, 18));
        painelComanda.setPreferredSize(new Dimension(410, 0));

        // Cabeçalho
        JPanel cabecalho = new JPanel();
        cabecalho.setOpaque(false);
        cabecalho.setLayout(new BoxLayout(cabecalho, BoxLayout.Y_AXIS));

        JPanel linhaVoltar = new JPanel(new BorderLayout());
        linhaVoltar.setOpaque(false);

        RoundedButton btnVoltar = new RoundedButton("←  VOLTAR", 12);
        btnVoltar.setPreferredSize(new Dimension(108, 34));
        btnVoltar.setBackground(new Color(243, 240, 237));
        btnVoltar.setForeground(COR_TEXTO);
        btnVoltar.setBorderColor(COR_BORDA);
        btnVoltar.setFont(new Font("Arial", Font.BOLD, 12));
        btnVoltar.addActionListener(e -> {
            if (aoVoltar != null) {
                aoVoltar.run();
            }
        });

        JPanel btnVoltarWrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        btnVoltarWrapper.setOpaque(false);
        btnVoltarWrapper.add(btnVoltar);
        linhaVoltar.add(btnVoltarWrapper, BorderLayout.WEST);

        JLabel lblIdComanda = new JLabel("COMANDA #" + comanda.getIdComanda());
        lblIdComanda.setForeground(COR_TEXTO_SECUNDARIO);
        lblIdComanda.setFont(new Font("Arial", Font.BOLD, 12));
        linhaVoltar.add(lblIdComanda, BorderLayout.EAST);

        JLabel lblMesa = new JLabel("MESA " + mesa.getNumero());
        lblMesa.setFont(new Font("Arial", Font.BOLD, 28));
        lblMesa.setForeground(COR_TEXTO);
        lblMesa.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblAtendente = new JLabel("Atendente: " + usuarioLogado.getNome());
        lblAtendente.setFont(new Font("Arial", Font.PLAIN, 13));
        lblAtendente.setForeground(COR_TEXTO_SECUNDARIO);
        lblAtendente.setAlignmentX(Component.LEFT_ALIGNMENT);

        JSeparator separadorCabecalho = new JSeparator();
        separadorCabecalho.setForeground(COR_BORDA);
        separadorCabecalho.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblPedido = new JLabel("PEDIDO DA MESA");
        lblPedido.setFont(new Font("Arial", Font.BOLD, 13));
        lblPedido.setForeground(COR_VERDE);
        lblPedido.setAlignmentX(Component.LEFT_ALIGNMENT);

        cabecalho.add(linhaVoltar);
        cabecalho.add(Box.createVerticalStrut(16));
        cabecalho.add(lblMesa);
        cabecalho.add(Box.createVerticalStrut(3));
        cabecalho.add(lblAtendente);
        cabecalho.add(Box.createVerticalStrut(14));
        cabecalho.add(separadorCabecalho);
        cabecalho.add(Box.createVerticalStrut(12));
        cabecalho.add(lblPedido);

        painelComanda.add(cabecalho, BorderLayout.NORTH);

        // Lista de itens
        painelItens = new JPanel();
        painelItens.setOpaque(false);
        painelItens.setLayout(new BoxLayout(painelItens, BoxLayout.Y_AXIS));

        JScrollPane scrollItens = new JScrollPane(painelItens);
        scrollItens.setBorder(null);
        scrollItens.setOpaque(false);
        scrollItens.getViewport().setOpaque(false);
        scrollItens.getVerticalScrollBar().setUnitIncrement(16);
        scrollItens.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        painelComanda.add(scrollItens, BorderLayout.CENTER);

        // Rodapé / totais / pagamentos
        JPanel rodape = new JPanel();
        rodape.setOpaque(false);
        rodape.setLayout(new BoxLayout(rodape, BoxLayout.Y_AXIS));

        JSeparator separadorTotais = new JSeparator();
        separadorTotais.setForeground(COR_BORDA);
        rodape.add(separadorTotais);
        rodape.add(Box.createVerticalStrut(12));

        JPanel linhaSubtotal = criarLinhaResumo("Subtotal", false);
        lblSubtotal = (JLabel) linhaSubtotal.getComponent(1);
        rodape.add(linhaSubtotal);
        rodape.add(Box.createVerticalStrut(8));

        JPanel linhaTotal = criarLinhaResumo("TOTAL", true);
        lblTotal = (JLabel) linhaTotal.getComponent(1);
        rodape.add(linhaTotal);
        rodape.add(Box.createVerticalStrut(14));

        JSeparator separadorPagamento = new JSeparator();
        separadorPagamento.setForeground(COR_BORDA);
        rodape.add(separadorPagamento);
        rodape.add(Box.createVerticalStrut(12));

        JPanel cabPagamento = new JPanel(new BorderLayout());
        cabPagamento.setOpaque(false);
        JLabel lblTituloPagamento = new JLabel("PAGAMENTOS");
        lblTituloPagamento.setFont(new Font("Arial", Font.BOLD, 13));
        lblTituloPagamento.setForeground(COR_VERDE);
        cabPagamento.add(lblTituloPagamento, BorderLayout.WEST);
        rodape.add(cabPagamento);
        rodape.add(Box.createVerticalStrut(7));

        painelPagamentos = new JPanel();
        painelPagamentos.setOpaque(false);
        painelPagamentos.setLayout(new BoxLayout(painelPagamentos, BoxLayout.Y_AXIS));
        rodape.add(painelPagamentos);
        rodape.add(Box.createVerticalStrut(8));

        JPanel linhaPago = criarLinhaResumo("Total pago", false);
        lblTotalPago = (JLabel) linhaPago.getComponent(1);
        rodape.add(linhaPago);
        rodape.add(Box.createVerticalStrut(6));

        JPanel linhaRestante = criarLinhaResumo("Restante", true);
        lblRestante = (JLabel) linhaRestante.getComponent(1);
        rodape.add(linhaRestante);
        rodape.add(Box.createVerticalStrut(12));

        RoundedButton btnPagamento = new RoundedButton("+ ADICIONAR PAGAMENTO", 14);
        btnPagamento.setBackground(COR_VERDE_CLARO);
        btnPagamento.setForeground(COR_VERDE_ESCURO);
        btnPagamento.setBorderColor(new Color(194, 217, 204));
        btnPagamento.setFont(new Font("Arial", Font.BOLD, 12));
        btnPagamento.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        btnPagamento.setPreferredSize(new Dimension(0, 38));
        btnPagamento.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnPagamento.addActionListener(e -> adicionarPagamento());
        rodape.add(btnPagamento);
        rodape.add(Box.createVerticalStrut(8));

        RoundedButton btnFecharMesa = new RoundedButton("FECHAR MESA", 14);
        btnFecharMesa.setBackground(COR_DESTAQUE);
        btnFecharMesa.setForeground(Color.WHITE);
        btnFecharMesa.setBorderColor(COR_DESTAQUE);
        btnFecharMesa.setHoverColor(COR_DESTAQUE_ESCURO);
        btnFecharMesa.setFont(new Font("Arial", Font.BOLD, 13));
        btnFecharMesa.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        btnFecharMesa.setPreferredSize(new Dimension(0, 42));
        btnFecharMesa.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnFecharMesa.addActionListener(e -> fecharMesa());
        rodape.add(btnFecharMesa);

        painelComanda.add(rodape, BorderLayout.SOUTH);
        conteudo.add(painelComanda, BorderLayout.WEST);

        // ================================================================
        // COLUNA DIREITA - PRODUTOS
        // ================================================================
        JPanel ladoDireito = new JPanel(new BorderLayout(0, 14));
        ladoDireito.setOpaque(false);

        RoundedPanel topoProdutos = new RoundedPanel(22, COR_CARD);
        topoProdutos.setLayout(new BorderLayout(0, 12));
        topoProdutos.setBorder(new EmptyBorder(18, 20, 14, 20));

        JPanel linhaTitulo = new JPanel(new BorderLayout());
        linhaTitulo.setOpaque(false);

        JPanel blocoTitulo = new JPanel();
        blocoTitulo.setOpaque(false);
        blocoTitulo.setLayout(new BoxLayout(blocoTitulo, BoxLayout.Y_AXIS));

        JLabel tituloProdutos = new JLabel("ADICIONAR PRODUTOS");
        tituloProdutos.setFont(new Font("Arial", Font.BOLD, 24));
        tituloProdutos.setForeground(COR_TEXTO);
        tituloProdutos.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtituloProdutos = new JLabel("Selecione uma categoria e adicione itens à comanda");
        subtituloProdutos.setFont(new Font("Arial", Font.PLAIN, 12));
        subtituloProdutos.setForeground(COR_TEXTO_SECUNDARIO);
        subtituloProdutos.setAlignmentX(Component.LEFT_ALIGNMENT);

        blocoTitulo.add(tituloProdutos);
        blocoTitulo.add(Box.createVerticalStrut(3));
        blocoTitulo.add(subtituloProdutos);
        linhaTitulo.add(blocoTitulo, BorderLayout.WEST);
        topoProdutos.add(linhaTitulo, BorderLayout.NORTH);

        painelCategorias = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        painelCategorias.setOpaque(false);
        topoProdutos.add(painelCategorias, BorderLayout.CENTER);
        ladoDireito.add(topoProdutos, BorderLayout.NORTH);

        RoundedPanel areaProdutos = new RoundedPanel(22, new Color(246, 242, 239));
        areaProdutos.setLayout(new BorderLayout());
        areaProdutos.setBorder(new EmptyBorder(16, 16, 16, 16));

        painelProdutos = new JPanel(new GridLayout(0, 3, 14, 14));
        painelProdutos.setOpaque(false);

        JScrollPane scrollProdutos = new JScrollPane(painelProdutos);
        scrollProdutos.setBorder(null);
        scrollProdutos.setOpaque(false);
        scrollProdutos.getViewport().setOpaque(false);
        scrollProdutos.getVerticalScrollBar().setUnitIncrement(16);
        scrollProdutos.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        areaProdutos.add(scrollProdutos, BorderLayout.CENTER);
        ladoDireito.add(areaProdutos, BorderLayout.CENTER);

        RoundedPanel barraSalvar = new RoundedPanel(18, COR_CARD);
        barraSalvar.setLayout(new BorderLayout());
        barraSalvar.setBorder(new EmptyBorder(10, 14, 10, 14));

        JLabel dicaSalvar = new JLabel("As alterações só são gravadas ao salvar a comanda.");
        dicaSalvar.setFont(new Font("Arial", Font.PLAIN, 12));
        dicaSalvar.setForeground(COR_TEXTO_SECUNDARIO);
        barraSalvar.add(dicaSalvar, BorderLayout.WEST);

        RoundedButton btnSalvar = new RoundedButton("SALVAR COMANDA", 14);
        btnSalvar.setBackground(COR_VERDE);
        btnSalvar.setForeground(Color.WHITE);
        btnSalvar.setBorderColor(COR_VERDE);
        btnSalvar.setHoverColor(COR_VERDE_ESCURO);
        btnSalvar.setFont(new Font("Arial", Font.BOLD, 13));
        btnSalvar.setPreferredSize(new Dimension(180, 42));
        btnSalvar.addActionListener(e -> salvarComanda());
        barraSalvar.add(btnSalvar, BorderLayout.EAST);

        ladoDireito.add(barraSalvar, BorderLayout.SOUTH);
        conteudo.add(ladoDireito, BorderLayout.CENTER);

        carregarCategorias();
        carregarProdutos(null);
        carregarItensSalvos();
        carregarPagamentos();
    }

    private JPanel criarLinhaResumo(String texto, boolean destaque) {
        JPanel linha = new JPanel(new BorderLayout());
        linha.setOpaque(false);

        JLabel lblTexto = new JLabel(texto);
        JLabel lblValor = new JLabel(moeda.format(BigDecimal.ZERO));

        if (destaque) {
            lblTexto.setFont(new Font("Arial", Font.BOLD, 17));
            lblTexto.setForeground(COR_TEXTO);
            lblValor.setFont(new Font("Arial", Font.BOLD, 18));
            lblValor.setForeground(COR_VERDE_ESCURO);
        } else {
            lblTexto.setFont(new Font("Arial", Font.PLAIN, 13));
            lblTexto.setForeground(COR_TEXTO_SECUNDARIO);
            lblValor.setFont(new Font("Arial", Font.BOLD, 13));
            lblValor.setForeground(COR_TEXTO);
        }

        linha.add(lblTexto, BorderLayout.WEST);
        linha.add(lblValor, BorderLayout.EAST);
        return linha;
    }

    // ================================================================
    // ITENS SALVOS
    // ================================================================
    private void carregarItensSalvos() {
        itensTemporarios.clear();

        List<ItemComanda> itensSalvos =
                itemComandaDAO.listarPorComanda(comanda.getIdComanda());

        List<Produto> produtos = produtoDAO.listar();

        for (ItemComanda itemSalvo : itensSalvos) {
            Produto produtoEncontrado = null;

            for (Produto produto : produtos) {
                if (produto.getIdProduto() == itemSalvo.getIdProduto()) {
                    produtoEncontrado = produto;
                    break;
                }
            }

            if (produtoEncontrado != null) {
                itensTemporarios.add(
                        new ItemTemporario(
                                itemSalvo.getIdItemComanda(),
                                produtoEncontrado,
                                itemSalvo.getQuantidade(),
                                itemSalvo.getObservacao(),
                                itemSalvo.getPrecoUnitario()
                        )
                );
            }
        }

        atualizarListaItens();
    }

    // ================================================================
    // CATEGORIAS
    // ================================================================
    private void carregarCategorias() {
        painelCategorias.removeAll();

        JButton btnTodos = criarBotaoCategoria("TODOS");
        btnTodos.addActionListener(e -> {
            selecionarCategoria(btnTodos);
            carregarProdutos(null);
        });
        painelCategorias.add(btnTodos);
        selecionarCategoria(btnTodos);

        List<Categoria> categorias = categoriaDAO.listarAtivas();

        for (Categoria categoria : categorias) {
            JButton botao = criarBotaoCategoria(categoria.getNome().toUpperCase());
            botao.addActionListener(e -> {
                selecionarCategoria(botao);
                carregarProdutos(categoria);
            });
            painelCategorias.add(botao);
        }

        painelCategorias.revalidate();
        painelCategorias.repaint();
    }

    private JButton criarBotaoCategoria(String texto) {
        RoundedButton botao = new RoundedButton(texto, 14);
        botao.setBackground(Color.WHITE);
        botao.setForeground(COR_TEXTO);
        botao.setBorderColor(COR_BORDA);
        botao.setFont(new Font("Arial", Font.BOLD, 11));
        botao.setPreferredSize(new Dimension(Math.max(88, texto.length() * 9 + 28), 36));
        return botao;
    }

    private void selecionarCategoria(JButton botaoSelecionado) {
        if (botaoCategoriaSelecionada instanceof RoundedButton anterior) {
            anterior.setBackground(Color.WHITE);
            anterior.setForeground(COR_TEXTO);
            anterior.setBorderColor(COR_BORDA);
        }

        botaoCategoriaSelecionada = botaoSelecionado;

        if (botaoSelecionado instanceof RoundedButton atual) {
            atual.setBackground(COR_VERDE);
            atual.setForeground(Color.WHITE);
            atual.setBorderColor(COR_VERDE);
        }
    }

    // ================================================================
    // PRODUTOS
    // ================================================================
    private void carregarProdutos(Categoria categoria) {
        painelProdutos.removeAll();

        List<Produto> produtos = produtoDAO.listar();

        for (Produto produto : produtos) {
            if (categoria != null
                    && produto.getCategoria().getIdCategoria() != categoria.getIdCategoria()) {
                continue;
            }

            if (!produto.isAtivo()) {
                continue;
            }

            painelProdutos.add(criarCardProduto(produto));
        }

        painelProdutos.revalidate();
        painelProdutos.repaint();
    }

    private JPanel criarCardProduto(Produto produto) {
        RoundedPanel card = new RoundedPanel(18, COR_CARD);
        card.setLayout(new BorderLayout(0, 10));
        card.setBorder(new EmptyBorder(12, 12, 12, 12));
        card.setPreferredSize(new Dimension(190, 235));
        card.setMinimumSize(new Dimension(170, 235));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        RoundedPanel areaImagem = new RoundedPanel(14, new Color(241, 236, 232));
        areaImagem.setLayout(new GridBagLayout());
        areaImagem.setPreferredSize(new Dimension(0, 98));

        JLabel inicial = new JLabel(obterIniciais(produto.getNome()));
        inicial.setFont(new Font("Arial", Font.BOLD, 34));
        inicial.setForeground(new Color(159, 146, 136));
        areaImagem.add(inicial);

        JPanel dados = new JPanel();
        dados.setOpaque(false);
        dados.setLayout(new BoxLayout(dados, BoxLayout.Y_AXIS));
        dados.setPreferredSize(new Dimension(0, 78));

        JLabel nome = new JLabel(
                "<html><div style='width:155px'>" + escaparHtml(produto.getNome()) + "</div></html>"
        );
        nome.setFont(new Font("Arial", Font.BOLD, 15));
        nome.setForeground(COR_TEXTO);
        nome.setAlignmentX(Component.LEFT_ALIGNMENT);
        nome.setToolTipText(produto.getNome());

        JLabel categoria = new JLabel(produto.getCategoria().getNome());
        categoria.setFont(new Font("Arial", Font.PLAIN, 11));
        categoria.setForeground(COR_TEXTO_SECUNDARIO);
        categoria.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel preco = new JLabel(moeda.format(produto.getPreco()));
        preco.setFont(new Font("Arial", Font.BOLD, 16));
        preco.setForeground(COR_VERDE_ESCURO);
        preco.setAlignmentX(Component.LEFT_ALIGNMENT);

        dados.add(nome);
        dados.add(Box.createVerticalStrut(3));
        dados.add(categoria);
        dados.add(Box.createVerticalStrut(5));
        dados.add(preco);

        card.add(areaImagem, BorderLayout.NORTH);
        card.add(dados, BorderLayout.CENTER);

        RoundedButton adicionar = new RoundedButton("+ ADICIONAR", 12);
        adicionar.setBackground(COR_VERDE_CLARO);
        adicionar.setForeground(COR_VERDE_ESCURO);
        adicionar.setBorderColor(new Color(194, 217, 204));
        adicionar.setFont(new Font("Arial", Font.BOLD, 11));
        adicionar.setPreferredSize(new Dimension(0, 36));
        adicionar.addActionListener(e -> adicionarProduto(produto));
        card.add(adicionar, BorderLayout.SOUTH);

        MouseAdapter cliqueCard = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                adicionarProduto(produto);
            }
        };
        areaImagem.addMouseListener(cliqueCard);
        nome.addMouseListener(cliqueCard);

        return card;
    }

    private String escaparHtml(String texto) {
        if (texto == null) {
            return "";
        }
        return texto
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    private String obterIniciais(String nome) {
        if (nome == null || nome.isBlank()) {
            return "?";
        }

        String[] partes = nome.trim().split("\\s+");
        String primeira = partes[0].substring(0, 1).toUpperCase();

        if (partes.length == 1) {
            return primeira;
        }

        return primeira + partes[partes.length - 1].substring(0, 1).toUpperCase();
    }

    // ================================================================
    // ADICIONAR PRODUTO
    // ================================================================
    private void adicionarProduto(Produto produto) {
        for (ItemTemporario item : itensTemporarios) {
            if (item.produto.getIdProduto() == produto.getIdProduto()) {
                item.quantidade++;
                atualizarListaItens();
                return;
            }
        }

        itensTemporarios.add(
                new ItemTemporario(
                        produto,
                        1,
                        "",
                        produto.getPreco()
                )
        );

        atualizarListaItens();
    }

    // ================================================================
    // LISTA DA COMANDA
    // ================================================================
    private void atualizarListaItens() {
        painelItens.removeAll();

        if (itensTemporarios.isEmpty()) {
            JPanel vazio = new JPanel();
            vazio.setOpaque(false);
            vazio.setLayout(new BoxLayout(vazio, BoxLayout.Y_AXIS));
            vazio.setBorder(new EmptyBorder(30, 10, 10, 10));

            JLabel titulo = new JLabel("Nenhum item adicionado");
            titulo.setFont(new Font("Arial", Font.BOLD, 14));
            titulo.setForeground(COR_TEXTO_SECUNDARIO);
            titulo.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel dica = new JLabel("Selecione um produto ao lado para começar.");
            dica.setFont(new Font("Arial", Font.PLAIN, 12));
            dica.setForeground(new Color(155, 155, 155));
            dica.setAlignmentX(Component.CENTER_ALIGNMENT);

            vazio.add(titulo);
            vazio.add(Box.createVerticalStrut(5));
            vazio.add(dica);
            painelItens.add(vazio);
        } else {
            for (ItemTemporario item : itensTemporarios) {
                painelItens.add(criarCardItem(item));
                painelItens.add(Box.createVerticalStrut(9));
            }
        }

        atualizarTotal();
        painelItens.revalidate();
        painelItens.repaint();
    }

    private JPanel criarCardItem(ItemTemporario item) {
        RoundedPanel card = new RoundedPanel(16, COR_ITEM);
        card.setLayout(new BorderLayout(0, 8));
        card.setBorder(new EmptyBorder(11, 12, 11, 12));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));

        JPanel topo = new JPanel(new BorderLayout(8, 0));
        topo.setOpaque(false);

        JLabel descricao = new JLabel(item.produto.getNome());
        descricao.setFont(new Font("Arial", Font.BOLD, 14));
        descricao.setForeground(COR_TEXTO);

        BigDecimal subtotal = item.precoUnitario.multiply(BigDecimal.valueOf(item.quantidade));
        JLabel preco = new JLabel(moeda.format(subtotal));
        preco.setFont(new Font("Arial", Font.BOLD, 14));
        preco.setForeground(COR_VERDE_ESCURO);

        topo.add(descricao, BorderLayout.WEST);
        topo.add(preco, BorderLayout.EAST);
        card.add(topo, BorderLayout.NORTH);

        JPanel centro = new JPanel(new BorderLayout());
        centro.setOpaque(false);

        JLabel observacao = new JLabel(
                item.observacao != null && !item.observacao.isBlank()
                        ? "Obs: " + item.observacao
                        : "Sem observação"
        );
        observacao.setFont(new Font("Arial", Font.ITALIC, 11));
        observacao.setForeground(COR_TEXTO_SECUNDARIO);
        centro.add(observacao, BorderLayout.WEST);
        card.add(centro, BorderLayout.CENTER);

        JPanel rodapeItem = new JPanel(new BorderLayout());
        rodapeItem.setOpaque(false);

        JPanel controleQuantidade = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        controleQuantidade.setOpaque(false);

        RoundedButton btnMenos = criarBotaoPequeno("−");
        RoundedButton btnMais = criarBotaoPequeno("+");

        JLabel lblQtd = new JLabel(String.valueOf(item.quantidade), SwingConstants.CENTER);
        lblQtd.setFont(new Font("Arial", Font.BOLD, 13));
        lblQtd.setForeground(COR_TEXTO);
        lblQtd.setPreferredSize(new Dimension(30, 28));

        controleQuantidade.add(btnMenos);
        controleQuantidade.add(lblQtd);
        controleQuantidade.add(btnMais);

        RoundedButton btnObservacao = new RoundedButton("OBSERVAÇÃO", 10);
        btnObservacao.setBackground(Color.WHITE);
        btnObservacao.setForeground(COR_TEXTO_SECUNDARIO);
        btnObservacao.setBorderColor(COR_BORDA);
        btnObservacao.setFont(new Font("Arial", Font.BOLD, 10));
        btnObservacao.setPreferredSize(new Dimension(96, 28));

        btnMais.addActionListener(e -> {
            item.quantidade++;
            atualizarListaItens();
        });

        btnMenos.addActionListener(e -> {
            item.quantidade--;

            if (item.quantidade <= 0) {
                if (item.idItemComanda > 0) {
                    itensRemovidos.add(item.idItemComanda);
                }
                itensTemporarios.remove(item);
            }

            atualizarListaItens();
        });

        btnObservacao.addActionListener(e -> {
            String novaObservacao = JOptionPane.showInputDialog(
                    this,
                    "Observação do produto:",
                    item.observacao
            );

            if (novaObservacao != null) {
                item.observacao = novaObservacao;
                atualizarListaItens();
            }
        });

        rodapeItem.add(controleQuantidade, BorderLayout.WEST);
        rodapeItem.add(btnObservacao, BorderLayout.EAST);
        card.add(rodapeItem, BorderLayout.SOUTH);

        return card;
    }

    private RoundedButton criarBotaoPequeno(String texto) {
        RoundedButton botao = new RoundedButton(texto, 9);
        botao.setBackground(Color.WHITE);
        botao.setForeground(COR_TEXTO);
        botao.setBorderColor(COR_BORDA);
        botao.setFont(new Font("Arial", Font.BOLD, 14));
        botao.setPreferredSize(new Dimension(30, 28));
        return botao;
    }

    private void atualizarTotal() {
        BigDecimal total = BigDecimal.ZERO;

        for (ItemTemporario item : itensTemporarios) {
            BigDecimal subtotal =
                    item.precoUnitario.multiply(BigDecimal.valueOf(item.quantidade));
            total = total.add(subtotal);
        }

        lblSubtotal.setText(moeda.format(total));
        lblTotal.setText(moeda.format(total));
    }

    // ================================================================
    // SALVAR COMANDA
    // ================================================================
    private void salvarComanda() {
        try {
            for (Integer idItem : itensRemovidos) {
                itemComandaDAO.cancelar(idItem);
            }

            for (ItemTemporario item : itensTemporarios) {
                if (item.idItemComanda == 0) {
                    ItemComanda novoItem = new ItemComanda();
                    novoItem.setIdComanda(comanda.getIdComanda());
                    novoItem.setIdProduto(item.produto.getIdProduto());
                    novoItem.setQuantidade(item.quantidade);
                    novoItem.setPrecoUnitario(item.precoUnitario);
                    novoItem.setObservacao(item.observacao);

                    int idGerado = itemComandaDAO.adicionar(novoItem);
                    item.idItemComanda = idGerado;
                } else {
                    itemComandaDAO.atualizarQuantidade(
                            item.idItemComanda,
                            item.quantidade
                    );

                    itemComandaDAO.atualizarObservacao(
                            item.idItemComanda,
                            item.observacao
                    );
                }
            }

            comandaDAO.recalcularTotal(comanda.getIdComanda());
            itensRemovidos.clear();

            Comanda comandaAtualizada =
                    comandaDAO.buscarPorId(comanda.getIdComanda());

            if (comandaAtualizada != null) {
                comanda.setValorTotal(comandaAtualizada.getValorTotal());
            }

            atualizarTotal();
            carregarPagamentos();

            JOptionPane.showMessageDialog(
                    this,
                    "Comanda salva com sucesso!",
                    "Comanda",
                    JOptionPane.INFORMATION_MESSAGE
            );

        } catch (RuntimeException e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Erro ao salvar comanda:\n" + e.getMessage(),
                    "Erro",
                    JOptionPane.ERROR_MESSAGE
            );
            e.printStackTrace();
        }
    }

    // ================================================================
    // PAGAMENTOS
    // ================================================================
    private void carregarPagamentos() {
        painelPagamentos.removeAll();

        List<Pagamento> pagamentos =
                pagamentoDAO.listarPorComanda(comanda.getIdComanda());

        if (pagamentos.isEmpty()) {
            JLabel nenhum = new JLabel("Nenhum pagamento registrado");
            nenhum.setFont(new Font("Arial", Font.ITALIC, 11));
            nenhum.setForeground(COR_TEXTO_SECUNDARIO);
            painelPagamentos.add(nenhum);
        } else {
            for (Pagamento pagamento : pagamentos) {
                JPanel linha = new JPanel(new BorderLayout(5, 0));
                linha.setOpaque(false);

                JLabel descricao = new JLabel(formatarFormaPagamento(pagamento.getFormaPagamento()));
                descricao.setFont(new Font("Arial", Font.PLAIN, 12));
                descricao.setForeground(COR_TEXTO);

                JLabel valor = new JLabel(moeda.format(pagamento.getValor()));
                valor.setFont(new Font("Arial", Font.BOLD, 12));
                valor.setForeground(COR_TEXTO);

                RoundedButton btnExcluir = new RoundedButton("×", 8);
                btnExcluir.setBackground(new Color(249, 236, 233));
                btnExcluir.setForeground(COR_DESTAQUE_ESCURO);
                btnExcluir.setBorderColor(new Color(234, 204, 198));
                btnExcluir.setPreferredSize(new Dimension(28, 25));
                btnExcluir.setToolTipText("Excluir pagamento");
                btnExcluir.addActionListener(e -> {
                    int resposta = JOptionPane.showConfirmDialog(
                            this,
                            "Deseja remover este pagamento?",
                            "Remover pagamento",
                            JOptionPane.YES_NO_OPTION
                    );

                    if (resposta == JOptionPane.YES_OPTION) {
                        pagamentoDAO.excluir(pagamento.getIdPagamento());
                        carregarPagamentos();
                    }
                });

                JPanel ladoDireito = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
                ladoDireito.setOpaque(false);
                ladoDireito.add(valor);
                ladoDireito.add(btnExcluir);

                linha.add(descricao, BorderLayout.WEST);
                linha.add(ladoDireito, BorderLayout.EAST);
                painelPagamentos.add(linha);
                painelPagamentos.add(Box.createVerticalStrut(4));
            }
        }

        atualizarResumoPagamento();
        painelPagamentos.revalidate();
        painelPagamentos.repaint();
    }

    private String formatarFormaPagamento(String forma) {
        if (forma == null) {
            return "";
        }

        return switch (forma) {
            case "DEBITO" -> "Débito";
            case "CREDITO" -> "Crédito";
            case "DINHEIRO" -> "Dinheiro";
            case "PIX" -> "Pix";
            default -> forma;
        };
    }

    private void atualizarResumoPagamento() {
        BigDecimal totalComanda = comandaDAO.obterTotal(comanda.getIdComanda());
        BigDecimal totalPago = pagamentoDAO.obterTotalPago(comanda.getIdComanda());
        BigDecimal restante = totalComanda.subtract(totalPago);

        if (restante.compareTo(BigDecimal.ZERO) < 0) {
            restante = BigDecimal.ZERO;
        }

        lblTotalPago.setText(moeda.format(totalPago));
        lblRestante.setText(moeda.format(restante));
    }

    private void adicionarPagamento() {
        BigDecimal totalComanda = comandaDAO.obterTotal(comanda.getIdComanda());

        if (totalComanda.compareTo(BigDecimal.ZERO) <= 0) {
            JOptionPane.showMessageDialog(
                    this,
                    "Salve os produtos da comanda antes de adicionar um pagamento.",
                    "Pagamento",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        BigDecimal totalPago = pagamentoDAO.obterTotalPago(comanda.getIdComanda());
        BigDecimal restante = totalComanda.subtract(totalPago);

        if (restante.compareTo(BigDecimal.ZERO) <= 0) {
            JOptionPane.showMessageDialog(this, "A comanda já está totalmente paga.");
            return;
        }

        JComboBox<String> cmbForma = new JComboBox<>(
                new String[]{"DINHEIRO", "PIX", "DEBITO", "CREDITO"}
        );

        JTextField txtValor = new JTextField(restante.setScale(2).toString());

        JPanel painel = new JPanel(new GridLayout(0, 1, 5, 5));
        painel.add(new JLabel("Forma de pagamento:"));
        painel.add(cmbForma);
        painel.add(new JLabel("Valor:"));
        painel.add(txtValor);
        painel.add(new JLabel("Restante: " + moeda.format(restante)));

        int resposta = JOptionPane.showConfirmDialog(
                this,
                painel,
                "Adicionar pagamento",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (resposta != JOptionPane.OK_OPTION) {
            return;
        }

        try {
            String texto = txtValor.getText()
                    .trim()
                    .replace("R$", "")
                    .replace(" ", "");

            if (texto.contains(",")) {
                texto = texto.replace(".", "").replace(",", ".");
            }

            BigDecimal valor = new BigDecimal(texto);

            if (valor.compareTo(BigDecimal.ZERO) <= 0) {
                JOptionPane.showMessageDialog(this, "Informe um valor maior que zero.");
                return;
            }

            if (valor.compareTo(restante) > 0) {
                JOptionPane.showMessageDialog(
                        this,
                        "O valor informado é maior que o restante da comanda.\n"
                                + "Restante: " + moeda.format(restante),
                        "Pagamento",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            Pagamento pagamento = new Pagamento();
            pagamento.setIdComanda(comanda.getIdComanda());
            pagamento.setFormaPagamento((String) cmbForma.getSelectedItem());
            pagamento.setValor(valor);

            pagamentoDAO.adicionar(pagamento);
            carregarPagamentos();

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Valor inválido.",
                    "Pagamento",
                    JOptionPane.ERROR_MESSAGE
            );
        } catch (RuntimeException e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Erro ao registrar pagamento:\n" + e.getMessage(),
                    "Erro",
                    JOptionPane.ERROR_MESSAGE
            );
            e.printStackTrace();
        }
    }

    // ================================================================
    // FECHAMENTO
    // ================================================================
    private void fecharMesa() {
        try {
            BigDecimal totalComanda = comandaDAO.obterTotal(comanda.getIdComanda());
            BigDecimal totalPago = pagamentoDAO.obterTotalPago(comanda.getIdComanda());

            if (totalComanda.compareTo(BigDecimal.ZERO) <= 0) {
                JOptionPane.showMessageDialog(
                        this,
                        "A comanda não possui valor para fechamento.",
                        "Fechar mesa",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            if (totalPago.compareTo(totalComanda) < 0) {
                BigDecimal restante = totalComanda.subtract(totalPago);

                JOptionPane.showMessageDialog(
                        this,
                        "A comanda ainda não está totalmente paga.\n\n"
                                + "Total: " + moeda.format(totalComanda)
                                + "\nPago: " + moeda.format(totalPago)
                                + "\nRestante: " + moeda.format(restante),
                        "Pagamento pendente",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            int resposta = JOptionPane.showConfirmDialog(
                    this,
                    "Confirma o fechamento da Mesa " + mesa.getNumero() + "?\n\n"
                            + "Total: " + moeda.format(totalComanda),
                    "Fechar mesa",
                    JOptionPane.YES_NO_OPTION
            );

            if (resposta != JOptionPane.YES_OPTION) {
                return;
            }

            comandaDAO.fecharComanda(comanda.getIdComanda());
            mesaDAO.atualizarStatus(mesa.getIdMesa(), "LIVRE");
            mesa.setStatus("LIVRE");

            JOptionPane.showMessageDialog(
                    this,
                    "Mesa " + mesa.getNumero() + " fechada com sucesso!"
            );

            if (aoVoltar != null) {
                aoVoltar.run();
            }

        } catch (RuntimeException e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Erro ao fechar mesa:\n" + e.getMessage(),
                    "Erro",
                    JOptionPane.ERROR_MESSAGE
            );
            e.printStackTrace();
        }
    }

    // ================================================================
    // ITEM TEMPORÁRIO
    // ================================================================
    private static class ItemTemporario {
        private int idItemComanda;
        private final Produto produto;
        private int quantidade;
        private String observacao;
        private final BigDecimal precoUnitario;

        public ItemTemporario(
                Produto produto,
                int quantidade,
                String observacao,
                BigDecimal precoUnitario
        ) {
            this(0, produto, quantidade, observacao, precoUnitario);
        }

        public ItemTemporario(
                int idItemComanda,
                Produto produto,
                int quantidade,
                String observacao,
                BigDecimal precoUnitario
        ) {
            this.idItemComanda = idItemComanda;
            this.produto = produto;
            this.quantidade = quantidade;
            this.observacao = observacao;
            this.precoUnitario = precoUnitario;
        }
    }

    // ================================================================
    // COMPONENTES VISUAIS
    // ================================================================
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

    private static class RoundedButton extends JButton {
        private final int radius;
        private Color borderColor = new Color(0, 0, 0, 0);
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

        public void setBorderColor(Color borderColor) {
            this.borderColor = borderColor;
            repaint();
        }

        public void setHoverColor(Color hoverColor) {
            this.hoverColor = hoverColor;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Color fundo = hover && hoverColor != null ? hoverColor : getBackground();
            g2.setColor(fundo);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);

            if (borderColor != null && borderColor.getAlpha() > 0) {
                g2.setColor(borderColor);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
            }

            g2.dispose();
            super.paintComponent(g);
        }
    }
}
