package br.com.trevizan.espetinhos.dao;

import br.com.trevizan.espetinhos.connection.ConnectionFactory;
import br.com.trevizan.espetinhos.model.Mesa;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MesaDAO {

    public List<Mesa> listarTodas() {

        String sql = """
                SELECT id_mesa, numero, status
                FROM mesa
                ORDER BY numero
                """;

        List<Mesa> mesas = new ArrayList<>();

        try (
                Connection conn = ConnectionFactory.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()
        ) {

            while (rs.next()) {

                Mesa mesa = new Mesa();

                mesa.setIdMesa(rs.getInt("id_mesa"));
                mesa.setNumero(rs.getInt("numero"));
                mesa.setStatus(rs.getString("status"));

                mesas.add(mesa);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar mesas.", e);
        }

        return mesas;
    }

    public Mesa buscarPorId(int idMesa) {

        String sql = """
                SELECT id_mesa, numero, status
                FROM mesa
                WHERE id_mesa = ?
                """;

        try (
                Connection conn = ConnectionFactory.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)
        ) {

            stmt.setInt(1, idMesa);

            try (ResultSet rs = stmt.executeQuery()) {

                if (rs.next()) {

                    Mesa mesa = new Mesa();

                    mesa.setIdMesa(rs.getInt("id_mesa"));
                    mesa.setNumero(rs.getInt("numero"));
                    mesa.setStatus(rs.getString("status"));

                    return mesa;
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar mesa por ID.", e);
        }

        return null;
    }

    public Mesa buscarPorNumero(int numero) {

        String sql = """
                SELECT id_mesa, numero, status
                FROM mesa
                WHERE numero = ?
                """;

        try (
                Connection conn = ConnectionFactory.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)
        ) {

            stmt.setInt(1, numero);

            try (ResultSet rs = stmt.executeQuery()) {

                if (rs.next()) {

                    Mesa mesa = new Mesa();

                    mesa.setIdMesa(rs.getInt("id_mesa"));
                    mesa.setNumero(rs.getInt("numero"));
                    mesa.setStatus(rs.getString("status"));

                    return mesa;
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar mesa por número.", e);
        }

        return null;
    }

    public void atualizarStatus(int idMesa, String status) {

        String sql = """
                UPDATE mesa
                SET status = ?
                WHERE id_mesa = ?
                """;

        try (
                Connection conn = ConnectionFactory.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)
        ) {

            stmt.setString(1, status);
            stmt.setInt(2, idMesa);

            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar status da mesa.", e);
        }
    }
}