package br.com.trevizan.espetinhos.view;

import br.com.trevizan.espetinhos.PadraoJPanel;
import br.com.trevizan.espetinhos.dao.CategoriaDAO;
import br.com.trevizan.espetinhos.dao.ProdutoDAO;
import br.com.trevizan.espetinhos.model.Categoria;
import br.com.trevizan.espetinhos.model.Comanda;
import br.com.trevizan.espetinhos.model.Mesa;
import br.com.trevizan.espetinhos.model.Produto;
import br.com.trevizan.espetinhos.model.Usuario;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ComandaPanel extends PadraoJPanel {

    private final Mesa mesa;
    private final Comanda comanda;
    private final Usuario usuarioLogado;

    private final ProdutoDAO produtoDAO;
    private final CategoriaDAO categoriaDAO;

    private final JPanel painelItens;
    private final JPanel painelProdutos;
    private final JPanel painelCategorias;

    private final JLabel lblSubtotal;
    private final JLabel lblTotal;

    private final List<ItemTemporario> itensTemporarios = new ArrayList<>();

    private final NumberFormat moeda =
            NumberFormat.getCurrencyInstance(
                    new Locale("pt", "BR")
            );

    public ComandaPanel(
            Mesa mesa,
            Comanda comanda,
            Usuario usuarioLogado
    ) {

        this.mesa = mesa;
        this.comanda = comanda;
        this.usuarioLogado = usuarioLogado;

        produtoDAO = new ProdutoDAO();
        categoriaDAO = new CategoriaDAO();

        setLayout(new BorderLayout(20, 0));
        setBorder(new EmptyBorder(25, 25, 25, 25));

        /*
         * =========================
         * LADO ESQUERDO - COMANDA
         * =========================
         */

        JPanel painelComanda = new JPanel(
                new BorderLayout(0, 15)
        );

        painelComanda.setBackground(Color.WHITE);

        painelComanda.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                new Color(210, 210, 210)
                        ),
                        new EmptyBorder(20, 20, 20, 20)
                )
        );

        painelComanda.setPreferredSize(
                new Dimension(390, 0)
        );

        JLabel lblMesa = new JLabel(
                "MESA " + mesa.getNumero()
        );

        lblMesa.setFont(
                new Font("Arial", Font.BOLD, 26)
        );

        JLabel lblComanda = new JLabel(
                "Comanda #" + comanda.getIdComanda()
        );

        lblComanda.setFont(
                new Font("Arial", Font.PLAIN, 13)
        );

        JLabel lblAtendente = new JLabel(
                "Atendente: " + usuarioLogado.getNome()
        );

        lblAtendente.setFont(
                new Font("Arial", Font.PLAIN, 13)
        );

        JPanel cabecalhoComanda = new JPanel();
        cabecalhoComanda.setOpaque(false);
        cabecalhoComanda.setLayout(
                new BoxLayout(
                        cabecalhoComanda,
                        BoxLayout.Y_AXIS
                )
        );

        cabecalhoComanda.add(lblMesa);
        cabecalhoComanda.add(
                Box.createVerticalStrut(4)
        );
        cabecalhoComanda.add(lblComanda);
        cabecalhoComanda.add(lblAtendente);

        painelComanda.add(
                cabecalhoComanda,
                BorderLayout.NORTH
        );

        /*
         * Itens da comanda
         */

        painelItens = new JPanel();

        painelItens.setBackground(Color.WHITE);

        painelItens.setLayout(
                new BoxLayout(
                        painelItens,
                        BoxLayout.Y_AXIS
                )
        );

        JScrollPane scrollItens =
                new JScrollPane(painelItens);

        scrollItens.setBorder(null);

        scrollItens.getVerticalScrollBar()
                .setUnitIncrement(16);

        painelComanda.add(
                scrollItens,
                BorderLayout.CENTER
        );

        /*
         * Rodapé da comanda
         */

        JPanel rodapeComanda = new JPanel();

        rodapeComanda.setOpaque(false);

        rodapeComanda.setLayout(
                new BoxLayout(
                        rodapeComanda,
                        BoxLayout.Y_AXIS
                )
        );

        JPanel linhaSubtotal =
                new JPanel(new BorderLayout());

        linhaSubtotal.setOpaque(false);

        JLabel textoSubtotal =
                new JLabel("Subtotal");

        lblSubtotal =
                new JLabel(moeda.format(0));

        linhaSubtotal.add(
                textoSubtotal,
                BorderLayout.WEST
        );

        linhaSubtotal.add(
                lblSubtotal,
                BorderLayout.EAST
        );

        JPanel linhaTotal =
                new JPanel(new BorderLayout());

        linhaTotal.setOpaque(false);

        JLabel textoTotal =
                new JLabel("TOTAL");

        textoTotal.setFont(
                new Font("Arial", Font.BOLD, 20)
        );

        lblTotal =
                new JLabel(moeda.format(0));

        lblTotal.setFont(
                new Font("Arial", Font.BOLD, 20)
        );

        linhaTotal.add(
                textoTotal,
                BorderLayout.WEST
        );

        linhaTotal.add(
                lblTotal,
                BorderLayout.EAST
        );

        rodapeComanda.add(linhaSubtotal);
        rodapeComanda.add(
                Box.createVerticalStrut(8)
        );
        rodapeComanda.add(linhaTotal);

        painelComanda.add(
                rodapeComanda,
                BorderLayout.SOUTH
        );

        /*
         * =========================
         * LADO DIREITO - PRODUTOS
         * =========================
         */

        JPanel ladoDireito =
                new JPanel(new BorderLayout(0, 20));

        ladoDireito.setOpaque(false);

        JLabel tituloProdutos =
                new JLabel("ADICIONAR PRODUTOS");

        tituloProdutos.setFont(
                new Font("Arial", Font.BOLD, 26)
        );

        /*
         * Categorias
         */

        painelCategorias =
                new JPanel(new FlowLayout(
                        FlowLayout.LEFT,
                        10,
                        5
                ));

        painelCategorias.setOpaque(false);

        JPanel topoProdutos =
                new JPanel(new BorderLayout());

        topoProdutos.setOpaque(false);

        topoProdutos.add(
                tituloProdutos,
                BorderLayout.NORTH
        );

        topoProdutos.add(
                painelCategorias,
                BorderLayout.CENTER
        );

        ladoDireito.add(
                topoProdutos,
                BorderLayout.NORTH
        );

        /*
         * Cards dos produtos
         */

        painelProdutos = new JPanel(
                new GridLayout(
                        0,
                        3,
                        15,
                        15
                )
        );

        painelProdutos.setOpaque(false);

        JScrollPane scrollProdutos =
                new JScrollPane(painelProdutos);

        scrollProdutos.setBorder(null);
        scrollProdutos.setOpaque(false);

        scrollProdutos
                .getViewport()
                .setOpaque(false);

        scrollProdutos
                .getVerticalScrollBar()
                .setUnitIncrement(16);

        ladoDireito.add(
                scrollProdutos,
                BorderLayout.CENTER
        );

        /*
         * Botão salvar
         */

        JButton btnSalvar =
                new JButton("SALVAR");

        btnSalvar.setFont(
                new Font("Arial", Font.BOLD, 16)
        );

        btnSalvar.setPreferredSize(
                new Dimension(150, 45)
        );

        JPanel painelSalvar =
                new JPanel(new FlowLayout(
                        FlowLayout.RIGHT
                ));

        painelSalvar.setOpaque(false);
        painelSalvar.add(btnSalvar);

        ladoDireito.add(
                painelSalvar,
                BorderLayout.SOUTH
        );

        /*
         * Junta os dois lados
         */

        add(
                painelComanda,
                BorderLayout.WEST
        );

        add(
                ladoDireito,
                BorderLayout.CENTER
        );

        carregarCategorias();
        carregarProdutos(null);

        btnSalvar.addActionListener(e ->
                salvarComanda()
        );
    }

    /*
     * =========================
     * CATEGORIAS
     * =========================
     */

    private void carregarCategorias() {

        painelCategorias.removeAll();

        JButton btnTodos =
                new JButton("Todos");

        btnTodos.addActionListener(e ->
                carregarProdutos(null)
        );

        painelCategorias.add(btnTodos);

        List<Categoria> categorias =
                categoriaDAO.listarAtivas();

        for (Categoria categoria : categorias) {

            JButton botao = new JButton(
                    categoria.getNome()
            );

            botao.addActionListener(e ->
                    carregarProdutos(categoria)
            );

            painelCategorias.add(botao);
        }

        painelCategorias.revalidate();
        painelCategorias.repaint();
    }

    /*
     * =========================
     * PRODUTOS
     * =========================
     */

    private void carregarProdutos(
            Categoria categoria
    ) {

        painelProdutos.removeAll();

        List<Produto> produtos =
                produtoDAO.listar();

        for (Produto produto : produtos) {

            if (
                    categoria != null
                            && produto.getCategoria().getIdCategoria()
                            != categoria.getIdCategoria()
            ) {
                continue;
            }

            if (!produto.isAtivo()) {
                continue;
            }

            painelProdutos.add(
                    criarCardProduto(produto)
            );
        }

        painelProdutos.revalidate();
        painelProdutos.repaint();
    }

    private JPanel criarCardProduto(
            Produto produto
    ) {

        JPanel card = new JPanel(
                new BorderLayout(0, 8)
        );

        card.setBackground(Color.WHITE);

        card.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                new Color(210, 210, 210)
                        ),
                        new EmptyBorder(
                                15,
                                15,
                                15,
                                15
                        )
                )
        );

        JLabel nome =
                new JLabel(
                        produto.getNome(),
                        SwingConstants.CENTER
                );

        nome.setFont(
                new Font("Arial", Font.BOLD, 16)
        );

        JLabel preco =
                new JLabel(
                        moeda.format(
                                produto.getPreco()
                        ),
                        SwingConstants.CENTER
                );

        preco.setFont(
                new Font("Arial", Font.PLAIN, 15)
        );

        JButton adicionar =
                new JButton("Adicionar");

        adicionar.addActionListener(e ->
                adicionarProduto(produto)
        );

        card.add(
                nome,
                BorderLayout.NORTH
        );

        card.add(
                preco,
                BorderLayout.CENTER
        );

        card.add(
                adicionar,
                BorderLayout.SOUTH
        );

        return card;
    }

    /*
     * =========================
     * ADICIONAR PRODUTO
     * =========================
     */

    private void adicionarProduto(
            Produto produto
    ) {

        for (ItemTemporario item : itensTemporarios) {

            if (
                    item.produto.getIdProduto()
                            == produto.getIdProduto()
            ) {

                item.quantidade++;

                atualizarListaItens();
                return;
            }
        }

        itensTemporarios.add(
                new ItemTemporario(
                        produto,
                        1,
                        ""
                )
        );

        atualizarListaItens();
    }

    /*
     * =========================
     * LISTA DA COMANDA
     * =========================
     */

    private void atualizarListaItens() {

        painelItens.removeAll();

        for (ItemTemporario item : itensTemporarios) {

            JPanel cardItem =
                    criarCardItem(item);

            painelItens.add(cardItem);

            painelItens.add(
                    Box.createVerticalStrut(10)
            );
        }

        atualizarTotal();

        painelItens.revalidate();
        painelItens.repaint();
    }

    private JPanel criarCardItem(
            ItemTemporario item
    ) {

        JPanel card =
                new JPanel(new BorderLayout(10, 5));

        card.setBackground(
                new Color(248, 248, 248)
        );

        card.setBorder(
                new EmptyBorder(
                        10,
                        10,
                        10,
                        10
                )
        );

        JLabel descricao =
                new JLabel(
                        item.quantidade
                                + "x "
                                + item.produto.getNome()
                );

        descricao.setFont(
                new Font("Arial", Font.BOLD, 15)
        );

        BigDecimal subtotal =
                item.produto
                        .getPreco()
                        .multiply(
                                BigDecimal.valueOf(
                                        item.quantidade
                                )
                        );

        JLabel preco =
                new JLabel(
                        moeda.format(subtotal)
                );

        JButton btnMenos =
                new JButton("-");

        JButton btnMais =
                new JButton("+");

        JButton btnObservacao =
                new JButton("Obs.");

        btnMais.addActionListener(e -> {

            item.quantidade++;

            atualizarListaItens();
        });

        btnMenos.addActionListener(e -> {

            item.quantidade--;

            if (item.quantidade <= 0) {
                itensTemporarios.remove(item);
            }

            atualizarListaItens();
        });

        btnObservacao.addActionListener(e -> {

            String observacao =
                    JOptionPane.showInputDialog(
                            this,
                            "Observação do produto:",
                            item.observacao
                    );

            if (observacao != null) {
                item.observacao = observacao;
                atualizarListaItens();
            }
        });

        JPanel botoes =
                new JPanel(new FlowLayout(
                        FlowLayout.RIGHT,
                        5,
                        0
                ));

        botoes.setOpaque(false);

        botoes.add(btnMenos);
        botoes.add(btnMais);
        botoes.add(btnObservacao);

        JPanel topo =
                new JPanel(new BorderLayout());

        topo.setOpaque(false);

        topo.add(
                descricao,
                BorderLayout.WEST
        );

        topo.add(
                preco,
                BorderLayout.EAST
        );

        card.add(
                topo,
                BorderLayout.NORTH
        );

        if (
                item.observacao != null
                        && !item.observacao.isBlank()
        ) {

            JLabel observacao =
                    new JLabel(
                            "Obs: " + item.observacao
                    );

            card.add(
                    observacao,
                    BorderLayout.CENTER
            );
        }

        card.add(
                botoes,
                BorderLayout.SOUTH
        );

        return card;
    }

    private void atualizarTotal() {

        BigDecimal total =
                BigDecimal.ZERO;

        for (ItemTemporario item : itensTemporarios) {

            BigDecimal subtotal =
                    item.produto
                            .getPreco()
                            .multiply(
                                    BigDecimal.valueOf(
                                            item.quantidade
                                    )
                            );

            total = total.add(subtotal);
        }

        lblSubtotal.setText(
                moeda.format(total)
        );

        lblTotal.setText(
                moeda.format(total)
        );
    }

    /*
     * Por enquanto apenas valida.
     * No próximo passo ligaremos ao ItemComandaDAO.
     */
    private void salvarComanda() {

        if (itensTemporarios.isEmpty()) {

            JOptionPane.showMessageDialog(
                    this,
                    "Adicione pelo menos um produto à comanda."
            );

            return;
        }

        JOptionPane.showMessageDialog(
                this,
                "Comanda pronta para ser salva."
        );
    }

    /*
     * Representa um item que ainda está sendo
     * montado na interface.
     */
    private static class ItemTemporario {

        private final Produto produto;
        private int quantidade;
        private String observacao;

        public ItemTemporario(
                Produto produto,
                int quantidade,
                String observacao
        ) {

            this.produto = produto;
            this.quantidade = quantidade;
            this.observacao = observacao;
        }
    }
}