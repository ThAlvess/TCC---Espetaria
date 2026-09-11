package br.com.trevizan.espetinhos.dao;

import br.com.trevizan.espetinhos.connection.ConnectionFactory;
import br.com.trevizan.espetinhos.model.Caixa;
import br.com.trevizan.espetinhos.model.Usuario;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

public class CaixaDAO {

    public void abrirCaixa(Caixa caixa) {

        String sql = """
                INSERT INTO caixa
                (id_usuario_abertura, valor_inicial, status)
                VALUES (?, ?, 'ABERTO')
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
                    caixa.getUsuarioAbertura().getIdUsuario()
            );

            statement.setBigDecimal(
                    2,
                    caixa.getValorInicial()
            );

            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {

                if (generatedKeys.next()) {
                    caixa.setIdCaixa(
                            generatedKeys.getInt(1)
                    );
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erro ao abrir caixa.",
                    e
            );
        }
    }

    public Caixa buscarCaixaAberto() {

        String sql = """
                SELECT
                    c.id_caixa,
                    c.data_hora_abertura,
                    c.data_hora_fechamento,
                    c.valor_inicial,
                    c.valor_final,
                    c.status,
                    u.id_usuario,
                    u.nome,
                    u.login,
                    u.senha,
                    u.ativo
                FROM caixa c
                INNER JOIN usuario u
                    ON c.id_usuario_abertura = u.id_usuario
                WHERE c.status = 'ABERTO'
                ORDER BY c.data_hora_abertura DESC
                LIMIT 1
                """;

        try (
                Connection connection = ConnectionFactory.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()
        ) {

            if (resultSet.next()) {

                Usuario usuario = new Usuario();
                usuario.setIdUsuario(resultSet.getInt("id_usuario"));
                usuario.setNome(resultSet.getString("nome"));
                usuario.setLogin(resultSet.getString("login"));
                usuario.setSenha(resultSet.getString("senha"));
                usuario.setAtivo(resultSet.getBoolean("ativo"));

                Caixa caixa = new Caixa();
                caixa.setIdCaixa(resultSet.getInt("id_caixa"));
                caixa.setUsuarioAbertura(usuario);
                caixa.setValorInicial(resultSet.getBigDecimal("valor_inicial"));
                caixa.setValorFinal(resultSet.getBigDecimal("valor_final"));
                caixa.setStatus(resultSet.getString("status"));

                Timestamp abertura = resultSet.getTimestamp("data_hora_abertura");
                if (abertura != null) {
                    caixa.setDataHoraAbertura(abertura.toLocalDateTime());
                }

                Timestamp fechamento = resultSet.getTimestamp("data_hora_fechamento");
                if (fechamento != null) {
                    caixa.setDataHoraFechamento(fechamento.toLocalDateTime());
                }

                return caixa;
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erro ao buscar caixa aberto.",
                    e
            );
        }

        return null;
    }
}