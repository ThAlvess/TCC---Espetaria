package br.com.trevizan.espetinhos;

import br.com.trevizan.espetinhos.dao.ProdutoDAO;
import br.com.trevizan.espetinhos.model.Categoria;
import br.com.trevizan.espetinhos.model.Produto;

import java.math.BigDecimal;

public class TesteProdutoDAO {

    public static void main(String[] args) {

        Categoria categoria = new Categoria();
        categoria.setIdCategoria(1);
        categoria.setNome("Espeto");
        categoria.setAtivo(true);

        Produto produto = new Produto();

        produto.setNome("Espeto de Carne");
        produto.setDescricao("Espeto bovino");
        produto.setPreco(new BigDecimal("12.00"));
        produto.setQuantidadeEstoque(50);
        produto.setCategoria(categoria);
        produto.setAtivo(true);

        ProdutoDAO produtoDAO = new ProdutoDAO();

        produtoDAO.cadastrar(produto);

        System.out.println(
                "Produto cadastrado com sucesso! ID: "
                        + produto.getIdProduto()
        );

        System.out.println("\nProdutos cadastrados:");

        for (Produto p : produtoDAO.listar()) {

            System.out.println(
                    p.getIdProduto()
                            + " - "
                            + p.getNome()
                            + " - "
                            + p.getCategoria().getNome()
                            + " - R$ "
                            + p.getPreco()
                            + " - Estoque: "
                            + p.getQuantidadeEstoque()
            );
        }
    }
}