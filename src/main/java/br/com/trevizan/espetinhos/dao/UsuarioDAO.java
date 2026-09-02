package br.com.trevizan.espetinhos.dao;

import br.com.trevizan.espetinhos.connection.ConnectionFactory;
import br.com.trevizan.espetinhos.model.Usuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDAO {

    public Usuario autenticar(String login, String senha) {
        String sql = """
                SELECT id_usuario, nome, login, senha, ativo, cpf, perfil
                FROM usuario
                WHERE login = ?
                  AND senha = ?
                  AND ativo = 'ativo'
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
                    usuario.setAtivo(rs.getString("ativo"));
                    usuario.setCpf(rs.getString("cpf"));
                    usuario.setPerfil(rs.getString("perfil"));
                    return usuario;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao autenticar usuário.", e);
        }
        return null;
    }

    public boolean verificarLoginExistente(String login) {
        String sql = "SELECT COUNT(*) FROM usuario WHERE login = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, login);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao verificar se o login existe.", e);
        }
        return false;
    }

    public String verificarStatusLogin(String login) {
        String sql = "SELECT ativo FROM usuario WHERE login = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, login);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("ativo");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao verificar status do login.", e);
        }
        return null;
    }

    public List<Usuario> listar() {
        String sql = "SELECT id_usuario, nome, login, senha, ativo, cpf, perfil FROM usuario WHERE ativo = 'ativo'";
        List<Usuario> usuarios = new ArrayList<>();
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Usuario u = new Usuario();
                u.setIdUsuario(rs.getInt("id_usuario"));
                u.setNome(rs.getString("nome"));
                u.setLogin(rs.getString("login"));
                u.setSenha(rs.getString("senha"));
                u.setAtivo(rs.getString("ativo"));
                u.setCpf(rs.getString("cpf"));
                u.setPerfil(rs.getString("perfil"));
                usuarios.add(u);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar usuários.", e);
        }
        return usuarios;
    }

    public void cadastrar(Usuario u) {
        String sql = "INSERT INTO usuario (nome, login, senha, ativo, cpf, perfil) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, u.getNome());
            stmt.setString(2, u.getLogin());
            stmt.setString(3, u.getSenha());
            stmt.setString(4, u.getStatus());
            stmt.setString(5, u.getCpf());
            stmt.setString(6, u.getPerfil());
            stmt.executeUpdate();
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) {
                throw new RuntimeException("Este nome de usuário (login) já está em uso.", e);
            }
            throw new RuntimeException("Erro ao cadastrar usuário.", e);
        }
    }

    public void atualizar(Usuario u) {
        String sql = "UPDATE usuario SET nome = ?, login = ?, senha = ?, ativo = ?, cpf = ?, perfil = ? WHERE id_usuario = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, u.getNome());
            stmt.setString(2, u.getLogin());
            stmt.setString(3, u.getSenha());
            stmt.setString(4, u.getStatus());
            stmt.setString(5, u.getCpf());
            stmt.setString(6, u.getPerfil());
            stmt.setInt(7, u.getIdUsuario());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar usuário.", e);
        }
    }

    public void excluir(int id) {
        String sql = "DELETE FROM usuario WHERE id_usuario = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao excluir usuário.", e);
        }
    }

    public void inativar(int id) {
        String sql = "UPDATE usuario SET ativo = 'inativo' WHERE id_usuario = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao inativar usuário.", e);
        }
    }
}