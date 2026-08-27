package br.com.trevizan.espetinhos.dao;

import br.com.trevizan.espetinhos.connection.ConnectionFactory;
import br.com.trevizan.espetinhos.model.Usuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UsuarioDAO {

    public Usuario autenticar(String login, String senha) {

        String sql = """
                SELECT id_usuario, nome, login, senha, ativo
                FROM usuario
                WHERE login = ?
                  AND senha = ?
                  AND ativo = TRUE
                """;

        try (
                Connection conexao = ConnectionFactory.getConnection();
                PreparedStatement stmt = conexao.prepareStatement(sql)
        ) {

            stmt.setString(1, login);
            stmt.setString(2, senha);

            try (ResultSet rs = stmt.executeQuery()) {

                if (rs.next()) {

                    Usuario usuario = new Usuario();

                    usuario.setIdUsuario(rs.getInt("id_usuario"));
                    usuario.setNome(rs.getString("nome"));
                    usuario.setLogin(rs.getString("login"));
                    usuario.setSenha(rs.getString("senha"));
                    usuario.setAtivo(rs.getBoolean("ativo"));

                    return usuario;
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erro ao autenticar usuário.",
                    e
            );
        }

        return null;
    }
}