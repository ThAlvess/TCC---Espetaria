package br.com.trevizan.espetinhos.dao;

import br.com.trevizan.espetinhos.connection.ConnectionFactory;
import br.com.trevizan.espetinhos.model.Usuario;
import br.com.trevizan.espetinhos.util.SenhaUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDAO {

    /**
     * Confere login e senha de um usuário ativo.
     *
     * <p>A senha é conferida com o BCrypt ({@link SenhaUtil#conferir}). Se o
     * valor gravado ainda estiver em texto puro (usuários antigos), ele é
     * regravado como hash BCrypt no mesmo login.</p>
     *
     * @return o usuário, ou {@code null} se login/senha não conferem ou se o
     *         usuário estiver inativo
     */
    public Usuario autenticar(String login, String senha) {
        String sql = """
            SELECT id_usuario, nome, login, senha, ativo, cpf, perfil, troca_senha_obrigatoria
            FROM usuario
            WHERE login = ?
              AND ativo = true
            """;

        Usuario usuario = null;
        String senhaGravada = null;

        try (Connection conexao = ConnectionFactory.getConnection();
             PreparedStatement stmt = conexao.prepareStatement(sql)) {

            stmt.setString(1, login);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    senhaGravada = rs.getString("senha");

                    usuario = new Usuario();
                    usuario.setIdUsuario(rs.getInt("id_usuario"));
                    usuario.setNome(rs.getString("nome"));
                    usuario.setLogin(rs.getString("login"));
                    // a senha (ou o hash) não fica no objeto da sessão
                    usuario.setSenha(null);
                    usuario.setAtivo(rs.getBoolean("ativo"));
                    usuario.setCpf(rs.getString("cpf"));
                    usuario.setPerfil(rs.getString("perfil"));
                    usuario.setTrocaSenhaObrigatoria(
                            rs.getBoolean("troca_senha_obrigatoria")
                    );
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao autenticar usuário.", e);
        }

        if (usuario == null || !SenhaUtil.conferir(senha, senhaGravada)) {
            return null;
        }

        // senha gravada em texto puro: regrava como hash BCrypt
        if (!SenhaUtil.ehHashBCrypt(senhaGravada)) {
            gravarHash(usuario.getIdUsuario(), senha);
        }

        return usuario;
    }

    /** Confere a senha atual de um usuário (usado na troca de senha). */
    public boolean conferirSenha(int idUsuario, String senha) {
        return SenhaUtil.conferir(senha, buscarSenhaGravada(idUsuario));
    }

    /**
     * Grava a nova senha pessoal (criptografada) e libera o usuário da troca
     * obrigatória.
     */
    public void alterarSenha(int idUsuario, String novaSenha) {
        String sql = """
            UPDATE usuario
            SET senha = ?,
                troca_senha_obrigatoria = FALSE
            WHERE id_usuario = ?
            """;
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, SenhaUtil.gerarHash(novaSenha));
            stmt.setInt(2, idUsuario);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao alterar a senha.", e);
        }
    }

    /**
     * Volta a senha do usuário para a padrão ({@link SenhaUtil#SENHA_PADRAO})
     * e marca a troca obrigatória: no próximo login ele terá que criar uma
     * senha pessoal.
     */
    public void redefinirSenhaPadrao(int idUsuario) {
        String sql = """
            UPDATE usuario
            SET senha = ?,
                troca_senha_obrigatoria = TRUE
            WHERE id_usuario = ?
            """;
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, SenhaUtil.gerarHash(SenhaUtil.SENHA_PADRAO));
            stmt.setInt(2, idUsuario);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao resetar a senha.", e);
        }
    }

    /** Regrava só o hash da senha, sem mexer na troca obrigatória. */
    private void gravarHash(int idUsuario, String senha) {
        String sql = "UPDATE usuario SET senha = ? WHERE id_usuario = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, SenhaUtil.gerarHash(senha));
            stmt.setInt(2, idUsuario);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao criptografar a senha.", e);
        }
    }

    private String buscarSenhaGravada(int idUsuario) {
        String sql = "SELECT senha FROM usuario WHERE id_usuario = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idUsuario);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getString("senha") : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao consultar a senha do usuário.", e);
        }
    }

    public Usuario buscarPorId(int id) {
        // não carrega a senha
        String sql = "SELECT id_usuario, nome, login, ativo, cpf, perfil FROM usuario WHERE id_usuario = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Usuario u = new Usuario();
                    u.setIdUsuario(rs.getInt("id_usuario"));
                    u.setNome(rs.getString("nome"));
                    u.setLogin(rs.getString("login"));
                    u.setAtivo(rs.getBoolean("ativo"));
                    u.setCpf(rs.getString("cpf"));
                    u.setPerfil(rs.getString("perfil"));
                    return u;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar usuário por ID.", e);
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

    public Boolean verificarStatusLogin(String login) {
        String sql = "SELECT ativo FROM usuario WHERE login = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, login);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getBoolean("ativo");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao verificar status do login.", e);
        }
        return null;
    }

    public List<Usuario> listar() {
        // não carrega a senha
        String sql = "SELECT id_usuario, nome, login, ativo, cpf, perfil FROM usuario";
        List<Usuario> usuarios = new ArrayList<>();
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Usuario u = new Usuario();
                u.setIdUsuario(rs.getInt("id_usuario"));
                u.setNome(rs.getString("nome"));
                u.setLogin(rs.getString("login"));
                u.setAtivo(rs.getBoolean("ativo"));
                u.setCpf(rs.getString("cpf"));
                u.setPerfil(rs.getString("perfil"));
                usuarios.add(u);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar usuários.", e);
        }
        return usuarios;
    }

    /**
     * Cadastra o usuário com a senha padrão ({@link SenhaUtil#SENHA_PADRAO})
     * e a troca obrigatória marcada: no primeiro login ele cria a própria
     * senha.
     */
    public void cadastrar(Usuario u) {
        String senhaInicial = u.getSenha() != null && !u.getSenha().isEmpty()
                ? u.getSenha()
                : SenhaUtil.SENHA_PADRAO;
        String sql = """
            INSERT INTO usuario
            (nome, login, senha, ativo, cpf, perfil,
             troca_senha_obrigatoria)
            VALUES (?, ?, ?, ?, ?, ?, TRUE)
            """;
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, u.getNome());
            stmt.setString(2, u.getLogin());
            stmt.setString(3, SenhaUtil.gerarHash(senhaInicial));
            stmt.setBoolean(4, u.isAtivo());
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

    /**
     * Atualiza os dados cadastrais (nome, login, status, CPF e perfil).
     * A senha não é alterada aqui — use {@link #alterarSenha} ou
     * {@link #redefinirSenhaPadrao}.
     */
    public void atualizar(Usuario u) {
        String sql = "UPDATE usuario SET nome = ?, login = ?, ativo = ?, cpf = ?, perfil = ? WHERE id_usuario = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, u.getNome());
            stmt.setString(2, u.getLogin());
            stmt.setBoolean(3, u.isAtivo());
            stmt.setString(4, u.getCpf());
            stmt.setString(5, u.getPerfil());
            stmt.setInt(6, u.getIdUsuario());
            stmt.executeUpdate();
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) {
                throw new RuntimeException("Este nome de usuário (login) já está em uso.", e);
            }
            throw new RuntimeException("Erro ao atualizar usuário.", e);
        }
    }

    public void alterar(Usuario u) {
        atualizar(u);
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
        String sql = "UPDATE usuario SET ativo = false WHERE id_usuario = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao inativar usuário.", e);
        }
    }
}
