package br.com.trevizan.espetinhos.dao;

import br.com.trevizan.espetinhos.connection.ConnectionFactory;
import br.com.trevizan.espetinhos.model.Usuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import br.com.trevizan.espetinhos.util.SenhaUtil;

public class UsuarioDAO {


    public Usuario autenticar(String login, String senha) {
        String sql = """
            SELECT id_usuario, nome, login, senha, ativo, cpf, perfil, troca_senha_obrigatoria
            FROM usuario
            WHERE login = ?
              AND ativo = true
            """;

        try (Connection conexao = ConnectionFactory.getConnection();
             PreparedStatement stmt = conexao.prepareStatement(sql)) {

            stmt.setString(1, login);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {

                    String senhaArmazenada = rs.getString("senha");

                    boolean senhaCorreta;

                    if (senhaArmazenada.startsWith("$2a$")
                            || senhaArmazenada.startsWith("$2b$")
                            || senhaArmazenada.startsWith("$2y$")) {

                        senhaCorreta = SenhaUtil.verificarSenha(
                                senha,
                                senhaArmazenada
                        );

                    } else {
                        // Compatibilidade temporária para migração.
                        senhaCorreta = senhaArmazenada.equals(senha);
                    }

                    if (!senhaCorreta) {
                        return null;
                    }

                    Usuario usuario = new Usuario();
                    usuario.setIdUsuario(rs.getInt("id_usuario"));
                    usuario.setNome(rs.getString("nome"));
                    usuario.setLogin(rs.getString("login"));

                    // Não colocamos a senha ou o hash na sessão.
                    usuario.setSenha(null);

                    usuario.setAtivo(rs.getBoolean("ativo"));
                    usuario.setCpf(rs.getString("cpf"));
                    usuario.setPerfil(rs.getString("perfil"));
                    usuario.setTrocaSenhaObrigatoria(
                            rs.getBoolean("troca_senha_obrigatoria")
                    );

                    return usuario;
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao autenticar usuário.", e);
        }

        return null;
    }


    public Usuario buscarPorId(int id) {
        String sql = "SELECT id_usuario, nome, login, senha, ativo, cpf, perfil FROM usuario WHERE id_usuario = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Usuario u = new Usuario();
                    u.setIdUsuario(rs.getInt("id_usuario"));
                    u.setNome(rs.getString("nome"));
                    u.setLogin(rs.getString("login"));
                    u.setSenha(rs.getString("senha"));
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
        String sql = "SELECT id_usuario, nome, login, senha, ativo, cpf, perfil FROM usuario";
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

    public void cadastrar(Usuario u) {
        String sql = """
        INSERT INTO usuario
        (nome, login, senha, ativo, cpf, perfil,
         troca_senha_obrigatoria)
        VALUES (?, ?, ?, ?, ?, ?, ?)
        """;
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, u.getNome());
            stmt.setString(2, u.getLogin());
            stmt.setString(3, SenhaUtil.gerarHash(u.getSenha()));
            stmt.setBoolean(4, u.isAtivo());
            stmt.setString(5, u.getCpf());
            stmt.setString(6, u.getPerfil());
            stmt.executeUpdate();
            stmt.setBoolean(7, !"Administrador".equalsIgnoreCase(u.getPerfil()));
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) {
                throw new RuntimeException("Este nome de usuário (login) já está em uso.", e);
            }
            throw new RuntimeException("Erro ao cadastrar usuário.", e);
        }
    }


    public void atualizar(Usuario u) {

        String novaSenha = u.getSenha();
        boolean alterarSenha = novaSenha != null && !novaSenha.isEmpty();

        if (alterarSenha && novaSenha.length() < 6) {
            throw new IllegalArgumentException(
                    "A senha deve ter no mínimo 6 caracteres."
            );
        }

        String sql;

        if (alterarSenha) {
            sql = """
                UPDATE usuario
                SET nome = ?, login = ?, senha = ?,
                    ativo = ?, cpf = ?, perfil = ?
                WHERE id_usuario = ?
                """;
        } else {
            sql = """
                UPDATE usuario
                SET nome = ?, login = ?,
                    ativo = ?, cpf = ?, perfil = ?
                WHERE id_usuario = ?
                """;
        }

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            int parametro = 1;

            stmt.setString(parametro++, u.getNome());
            stmt.setString(parametro++, u.getLogin());

            if (alterarSenha) {
                stmt.setString(
                        parametro++,
                        SenhaUtil.gerarHash(novaSenha)
                );
            }

            stmt.setBoolean(parametro++, u.isAtivo());
            stmt.setString(parametro++, u.getCpf());
            stmt.setString(parametro++, u.getPerfil());
            stmt.setInt(parametro, u.getIdUsuario());

            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erro ao atualizar usuário.", e
            );
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


    public boolean trocarSenhaObrigatoria(
            int idUsuario,
            String senhaAtual,
            String novaSenha
    ) {
        if (senhaAtual == null || novaSenha == null
                || novaSenha.length() < 6) {
            throw new IllegalArgumentException(
                    "A nova senha deve ter no mínimo 6 caracteres."
            );
        }

        String sqlConsulta = """
            SELECT senha
            FROM usuario
            WHERE id_usuario = ?
              AND ativo = TRUE
              AND troca_senha_obrigatoria = TRUE
            FOR UPDATE
            """;

        String sqlAtualizacao = """
            UPDATE usuario
            SET senha = ?,
                troca_senha_obrigatoria = FALSE
            WHERE id_usuario = ?
              AND troca_senha_obrigatoria = TRUE
            """;

        try (Connection conn = ConnectionFactory.getConnection()) {
            conn.setAutoCommit(false);

            try {
                String senhaArmazenada;

                try (PreparedStatement stmt =
                             conn.prepareStatement(sqlConsulta)) {
                    stmt.setInt(1, idUsuario);

                    try (ResultSet rs = stmt.executeQuery()) {
                        if (!rs.next()) {
                            conn.rollback();
                            return false;
                        }
                        senhaArmazenada = rs.getString("senha");
                    }
                }

                boolean senhaAtualCorreta;

                if (senhaArmazenada.startsWith("$2a$")
                        || senhaArmazenada.startsWith("$2b$")
                        || senhaArmazenada.startsWith("$2y$")) {
                    senhaAtualCorreta = SenhaUtil.verificarSenha(
                            senhaAtual, senhaArmazenada
                    );
                } else {
                    // Compatibilidade temporária com senhas antigas.
                    senhaAtualCorreta = senhaArmazenada.equals(senhaAtual);
                }

                if (!senhaAtualCorreta) {
                    conn.rollback();
                    return false;
                }

                if (novaSenha.equals(senhaAtual)) {
                    throw new IllegalArgumentException(
                            "A nova senha deve ser diferente da provisória."
                    );
                }

                String novoHash = SenhaUtil.gerarHash(novaSenha);

                try (PreparedStatement stmt =
                             conn.prepareStatement(sqlAtualizacao)) {
                    stmt.setString(1, novoHash);
                    stmt.setInt(2, idUsuario);

                    if (stmt.executeUpdate() != 1) {
                        conn.rollback();
                        return false;
                    }
                }

                conn.commit();
                return true;

            } catch (SQLException | RuntimeException e) {
                conn.rollback();
                throw e;
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erro ao realizar a troca obrigatória de senha.", e
            );
        }
    }

}