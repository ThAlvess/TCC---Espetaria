package br.com.trevizan.espetinhos.dao;

import br.com.trevizan.espetinhos.connection.ConnectionFactory;
import br.com.trevizan.espetinhos.model.Comanda;

import java.math.BigDecimal;
import java.sql.*;

public class ComandaDAO {

    /**
     * Abre uma nova comanda e retorna o ID gerado.
     */
    public int abrirComanda(int idMesa, int idUsuario, String nomeCliente) {

        String sql = """
                INSERT INTO comanda
                    (id_mesa, id_usuario, nome_cliente, status, valor_total)
                VALUES
                    (?, ?, ?, 'ABERTA', 0.00)
                """;

        try (
                Connection conn = ConnectionFactory.getConnection();
                PreparedStatement stmt = conn.prepareStatement(
                        sql,
                        Statement.RETURN_GENERATED_KEYS
                )
        ) {

            stmt.setInt(1, idMesa);
            stmt.setInt(2, idUsuario);

            if (nomeCliente == null || nomeCliente.isBlank()) {
                stmt.setNull(3, Types.VARCHAR);
            } else {
                stmt.setString(3, nomeCliente.trim());
            }

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }

            throw new RuntimeException(
                    "A comanda foi criada, mas não foi possível obter seu ID."
            );

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao abrir comanda.", e);
        }
    }

    /**
     * Busca a comanda ABERTA de uma mesa.
     */
    public Comanda buscarComandaAbertaPorMesa(int idMesa) {

        String sql = """
                SELECT
                    id_comanda,
                    id_mesa,
                    id_usuario,
                    nome_cliente,
                    data_abertura,
                    data_fechamento,
                    status,
                    valor_total
                FROM comanda
                WHERE id_mesa = ?
                  AND status = 'ABERTA'
                ORDER BY data_abertura DESC
                LIMIT 1
                """;

        try (
                Connection conn = ConnectionFactory.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)
        ) {

            stmt.setInt(1, idMesa);

            try (ResultSet rs = stmt.executeQuery()) {

                if (rs.next()) {
                    return criarComanda(rs);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erro ao buscar comanda aberta da mesa.", e
            );
        }

        return null;
    }

    public Comanda buscarPorId(int idComanda) {

        String sql = """
                SELECT
                    id_comanda,
                    id_mesa,
                    id_usuario,
                    nome_cliente,
                    data_abertura,
                    data_fechamento,
                    status,
                    valor_total
                FROM comanda
                WHERE id_comanda = ?
                """;

        try (
                Connection conn = ConnectionFactory.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)
        ) {

            stmt.setInt(1, idComanda);

            try (ResultSet rs = stmt.executeQuery()) {

                if (rs.next()) {
                    return criarComanda(rs);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar comanda.", e);
        }

        return null;
    }

    /**
     * Atualiza o valor_total com a soma dos itens não cancelados.
     */
    public void recalcularTotal(int idComanda) {

        String sql = """
                UPDATE comanda
                SET valor_total = (
                    SELECT COALESCE(SUM(subtotal), 0)
                    FROM item_comanda
                    WHERE id_comanda = ?
                      AND status_item <> 'CANCELADO'
                )
                WHERE id_comanda = ?
                """;

        try (
                Connection conn = ConnectionFactory.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)
        ) {

            stmt.setInt(1, idComanda);
            stmt.setInt(2, idComanda);

            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erro ao recalcular total da comanda.", e
            );
        }
    }

    public BigDecimal obterTotal(int idComanda) {

        String sql = """
                SELECT valor_total
                FROM comanda
                WHERE id_comanda = ?
                """;

        try (
                Connection conn = ConnectionFactory.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)
        ) {

            stmt.setInt(1, idComanda);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getBigDecimal("valor_total");
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erro ao consultar total da comanda.", e
            );
        }

        return BigDecimal.ZERO;
    }

    /**
     * Usaremos no fechamento/pagamento posteriormente.
     */
    public void fecharComanda(int idComanda) {

        String sql = """
                UPDATE comanda
                SET status = 'FECHADA',
                    data_fechamento = CURRENT_TIMESTAMP
                WHERE id_comanda = ?
                  AND status = 'ABERTA'
                """;

        try (
                Connection conn = ConnectionFactory.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)
        ) {

            stmt.setInt(1, idComanda);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao fechar comanda.", e);
        }
    }

    public void cancelarComanda(int idComanda) {

        String sql = """
                UPDATE comanda
                SET status = 'CANCELADA',
                    data_fechamento = CURRENT_TIMESTAMP
                WHERE id_comanda = ?
                  AND status = 'ABERTA'
                """;

        try (
                Connection conn = ConnectionFactory.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)
        ) {

            stmt.setInt(1, idComanda);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao cancelar comanda.", e);
        }
    }

    /**
     * Lista todas as comandas ABERTAS de uma mesa.
     */
    public java.util.List<Comanda> listarComandasAbertasPorMesa(int idMesa) {

        String sql = """
            SELECT
                id_comanda,
                id_mesa,
                id_usuario,
                nome_cliente,
                data_abertura,
                data_fechamento,
                status,
                valor_total
            FROM comanda
            WHERE id_mesa = ?
              AND status = 'ABERTA'
            ORDER BY data_abertura ASC
            """;

        java.util.List<Comanda> comandas =
                new java.util.ArrayList<>();

        try (
                Connection conn = ConnectionFactory.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)
        ) {

            stmt.setInt(1, idMesa);

            try (ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    comandas.add(criarComanda(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erro ao listar comandas abertas da mesa.",
                    e
            );
        }

        return comandas;
    }

    public boolean existeComandaAbertaNaMesa(int idMesa) {

        String sql = """
            SELECT 1
            FROM comanda
            WHERE id_mesa = ?
              AND status = 'ABERTA'
            LIMIT 1
            """;

        try (
                Connection conn = ConnectionFactory.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)
        ) {

            stmt.setInt(1, idMesa);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erro ao verificar comandas abertas da mesa.",
                    e
            );
        }
    }

    private Comanda criarComanda(ResultSet rs) throws SQLException {

        Comanda comanda = new Comanda();

        comanda.setIdComanda(rs.getInt("id_comanda"));
        comanda.setIdMesa(rs.getInt("id_mesa"));
        comanda.setIdUsuario(rs.getInt("id_usuario"));
        comanda.setNomeCliente(rs.getString("nome_cliente"));

        Timestamp abertura = rs.getTimestamp("data_abertura");

        if (abertura != null) {
            comanda.setDataAbertura(abertura.toLocalDateTime());
        }

        Timestamp fechamento = rs.getTimestamp("data_fechamento");

        if (fechamento != null) {
            comanda.setDataFechamento(fechamento.toLocalDateTime());
        }

        comanda.setStatus(rs.getString("status"));
        comanda.setValorTotal(rs.getBigDecimal("valor_total"));

        return comanda;
    }

/**
     * Lista todas as comandas que já foram encerradas (FECHADA ou CANCELADA)
     * para alimentar a tabela de Histórico.
     */
    public java.util.List<Comanda> listarFechadas() {
        String sql = """
                SELECT
                    id_comanda,
                    id_mesa,
                    id_usuario,
                    nome_cliente,
                    data_abertura,
                    data_fechamento,
                    status,
                    valor_total
                FROM comanda
                WHERE status IN ('FECHADA', 'CANCELADA')
                ORDER BY data_fechamento DESC
                """;

        java.util.List<Comanda> lista = new java.util.ArrayList<>();

        try (
                Connection conn = ConnectionFactory.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()
        ) {
            while (rs.next()) {
                lista.add(criarComanda(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar histórico de comandas.", e);
        }

        return lista;
    }

    public String obterItensFormatados(int idComanda) {
        String sql = """
                SELECT ic.quantidade, p.nome
                FROM item_comanda ic
                INNER JOIN produto p ON ic.id_produto = p.id_produto
                WHERE ic.id_comanda = ? AND ic.status_item <> 'CANCELADO'
                """;

        java.util.List<String> itens = new java.util.ArrayList<>();

        try (
                Connection conn = ConnectionFactory.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setInt(1, idComanda);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    itens.add(rs.getInt("quantidade") + "x " + rs.getString("nome"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar itens formatados: " + e.getMessage());
            return "Erro ao carregar itens";
        }

        return itens.isEmpty() ? "Nenhum item" : String.join(", ", itens);
    }
}