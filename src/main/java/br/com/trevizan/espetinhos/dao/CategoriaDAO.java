package br.com.trevizan.espetinhos.dao;

import br.com.trevizan.espetinhos.connection.ConnectionFactory;
import br.com.trevizan.espetinhos.model.Categoria;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CategoriaDAO {

    public List<Categoria> listarAtivas() {

        String sql = """
                SELECT id_categoria, nome, ativo
                FROM categoria
                WHERE ativo = TRUE
                ORDER BY nome
                """;

        List<Categoria> categorias = new ArrayList<>();

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
                        resultSet.getString("nome")
                );

                categoria.setAtivo(
                        resultSet.getBoolean("ativo")
                );

                categorias.add(categoria);
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erro ao listar categorias.", e
            );
        }

        return categorias;
    }
}