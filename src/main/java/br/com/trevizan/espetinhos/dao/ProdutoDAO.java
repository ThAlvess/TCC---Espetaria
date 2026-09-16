package br.com.trevizan.espetinhos.dao;

import br.com.trevizan.espetinhos.connection.ConnectionFactory;
import br.com.trevizan.espetinhos.model.Categoria;
import br.com.trevizan.espetinhos.model.Produto;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ProdutoDAO {

    public void cadastrar(Produto produto) {

        String sql = """
                INSERT INTO produto
                (id_categoria, nome, descricao, preco, quantidade_estoque, ativo)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (
                Connection connection = ConnectionFactory.getConnection();
                PreparedStatement statement = connection.prepareStatement(
                        sql,
                        Statement.RETURN_GENERATED_KEYS
                )
        ) {

            statement.setInt(
                    1,
                    produto.getCategoria().getIdCategoria()
            );

            statement.setString(
                    2,
                    produto.getNome()
            );

            statement.setString(
                    3,
                    produto.getDescricao()
            );

            statement.setBigDecimal(
                    4,
                    produto.getPreco()
            );

            statement.setInt(
                    5,
                    produto.getQuantidadeEstoque()
            );

            statement.setBoolean(
                    6,
                    produto.isAtivo()
            );

            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {

                if (generatedKeys.next()) {
                    produto.setIdProduto(
                            generatedKeys.getInt(1)
                    );
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erro ao cadastrar produto.",
                    e
            );
        }
    }

    public List<Produto> listar() {

        String sql = """
                SELECT
                    p.id_produto,
                    p.nome,
                    p.descricao,
                    p.preco,
                    p.quantidade_estoque,
                    p.ativo,
                    c.id_categoria,
                    c.nome AS categoria_nome,
                    c.ativo AS categoria_ativo
                FROM produto p
                INNER JOIN categoria c
                    ON p.id_categoria = c.id_categoria
                ORDER BY p.nome
                """;

        List<Produto> produtos = new ArrayList<>();

        try (
                Connection connection = ConnectionFactory.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()
        ) {

            while (resultSet.next()) {

                Categoria categoria = new Categoria();

                categoria.setIdCategoria(
                        resultSet.getInt("id_categoria")
                );

                categoria.setNome(
                        resultSet.getString("categoria_nome")
                );

                categoria.setAtivo(
                        resultSet.getBoolean("categoria_ativo")
                );

                Produto produto = new Produto();

                produto.setIdProduto(
                        resultSet.getInt("id_produto")
                );

                produto.setNome(
                        resultSet.getString("nome")
                );

                produto.setDescricao(
                        resultSet.getString("descricao")
                );

                produto.setPreco(
                        resultSet.getBigDecimal("preco")
                );

                produto.setQuantidadeEstoque(
                        resultSet.getInt("quantidade_estoque")
                );

                produto.setAtivo(
                        resultSet.getBoolean("ativo")
                );

                produto.setCategoria(categoria);

                produtos.add(produto);
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erro ao listar produtos.",
                    e
            );
        }

        return produtos;
    }

    public void atualizar(Produto produto) {

        String sql = """
            UPDATE produto
            SET nome = ?,
                descricao = ?,
                preco = ?,
                quantidade_estoque = ?,
                id_categoria = ?,
                ativo = ?
            WHERE id_produto = ?
            """;

        try (
                Connection conexao = ConnectionFactory.getConnection();
                PreparedStatement stmt = conexao.prepareStatement(sql)
        ) {

            stmt.setString(1, produto.getNome());
            stmt.setString(2, produto.getDescricao());
            stmt.setBigDecimal(3, produto.getPreco());
            stmt.setInt(4, produto.getQuantidadeEstoque());
            stmt.setInt(5, produto.getCategoria().getIdCategoria());
            stmt.setBoolean(6, produto.isAtivo());
            stmt.setInt(7, produto.getIdProduto());

            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erro ao atualizar produto.",
                    e
            );
        }
    }
}