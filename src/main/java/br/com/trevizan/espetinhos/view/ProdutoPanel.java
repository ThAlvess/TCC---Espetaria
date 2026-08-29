package br.com.trevizan.espetinhos.view;

import br.com.trevizan.espetinhos.dao.CategoriaDAO;
import br.com.trevizan.espetinhos.dao.ProdutoDAO;
import br.com.trevizan.espetinhos.model.Categoria;
import br.com.trevizan.espetinhos.model.Produto;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;

public class ProdutoPanel extends JPanel {

    private JTextField txtNome;
    private JTextField txtDescricao;
    private JTextField txtPreco;
    private JTextField txtEstoque;

    private JComboBox<Categoria> cbCategoria;

    private JCheckBox chkAtivo;

    private JButton btnSalvar;

    private JTable tabelaProdutos;

    private final ProdutoDAO produtoDAO;
    private final CategoriaDAO categoriaDAO;

    public ProdutoPanel() {

        produtoDAO = new ProdutoDAO();
        categoriaDAO = new CategoriaDAO();


        criarComponentes();
        carregarCategorias();
        carregarProdutos();
    }

    private void configurarPainel() {
        setLayout(new BorderLayout());
    }

    private void criarComponentes() {

        setLayout(new BorderLayout());

        JPanel painelFormulario = new JPanel(
                new GridLayout(6, 2, 10, 10)
        );

        painelFormulario.setBorder(
                BorderFactory.createEmptyBorder(
                        20,
                        20,
                        20,
                        20
                )
        );

        txtNome = new JTextField();

        txtDescricao = new JTextField();

        txtPreco = new JTextField();

        txtEstoque = new JTextField();

        cbCategoria = new JComboBox<>();

        chkAtivo = new JCheckBox();
        chkAtivo.setSelected(true);

        btnSalvar = new JButton("Salvar");

        painelFormulario.add(
                new JLabel("Nome:")
        );

        painelFormulario.add(txtNome);

        painelFormulario.add(
                new JLabel("Descrição:")
        );

        painelFormulario.add(txtDescricao);

        painelFormulario.add(
                new JLabel("Categoria:")
        );

        painelFormulario.add(cbCategoria);

        painelFormulario.add(
                new JLabel("Preço:")
        );

        painelFormulario.add(txtPreco);

        painelFormulario.add(
                new JLabel("Estoque:")
        );

        painelFormulario.add(txtEstoque);

        painelFormulario.add(
                new JLabel("Ativo:")
        );

        painelFormulario.add(chkAtivo);

        add(
                painelFormulario,
                BorderLayout.NORTH
        );

        String[] colunas = {
                "ID",
                "Nome",
                "Categoria",
                "Preço",
                "Estoque",
                "Status"
        };

        tabelaProdutos = new JTable(
                new DefaultTableModel(
                        colunas,
                        0
                )
        );

        add(
                new JScrollPane(tabelaProdutos),
                BorderLayout.CENTER
        );

        JPanel painelBotoes = new JPanel();

        painelBotoes.add(btnSalvar);

        add(
                painelBotoes,
                BorderLayout.SOUTH
        );

        btnSalvar.addActionListener(
                e -> salvarProduto()
        );
    }

    private void carregarCategorias() {

        cbCategoria.removeAllItems();

        for (
                Categoria categoria :
                categoriaDAO.listarAtivas()
        ) {

            cbCategoria.addItem(
                    categoria
            );
        }
    }

    private void carregarProdutos() {

        DefaultTableModel modelo =
                (DefaultTableModel)
                        tabelaProdutos.getModel();

        modelo.setRowCount(0);

        for (
                Produto produto :
                produtoDAO.listar()
        ) {

            String status;

            if (!produto.isAtivo()) {

                status = "Inativo";

            } else if (
                    produto.getQuantidadeEstoque() <= 0
            ) {

                status = "Sem estoque";

            } else {

                status = "Disponível";
            }

            modelo.addRow(
                    new Object[]{
                            produto.getIdProduto(),
                            produto.getNome(),
                            produto.getCategoria().getNome(),
                            produto.getPreco(),
                            produto.getQuantidadeEstoque(),
                            status
                    }
            );
        }
    }

    private void salvarProduto() {

        try {

            String nome =
                    txtNome.getText().trim();

            String descricao =
                    txtDescricao.getText().trim();

            BigDecimal preco =
                    new BigDecimal(
                            txtPreco
                                    .getText()
                                    .replace(",", ".")
                    );

            int estoque =
                    Integer.parseInt(
                            txtEstoque.getText()
                    );

            Categoria categoria =
                    (Categoria)
                            cbCategoria.getSelectedItem();

            if (nome.isEmpty()) {

                JOptionPane.showMessageDialog(
                        this,
                        "Informe o nome do produto."
                );

                return;
            }

            if (categoria == null) {

                JOptionPane.showMessageDialog(
                        this,
                        "Selecione uma categoria."
                );

                return;
            }

            if (preco.compareTo(BigDecimal.ZERO) < 0) {

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

            Produto produto =
                    new Produto();

            produto.setNome(nome);

            produto.setDescricao(descricao);

            produto.setPreco(preco);

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

            limparCampos();

            carregarProdutos();

        } catch (NumberFormatException e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Verifique os campos preço e estoque."
            );
        }
    }

    private void limparCampos() {

        txtNome.setText("");

        txtDescricao.setText("");

        txtPreco.setText("");

        txtEstoque.setText("");

        chkAtivo.setSelected(true);

        if (cbCategoria.getItemCount() > 0) {
            cbCategoria.setSelectedIndex(0);
        }
    }
}