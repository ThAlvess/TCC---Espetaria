package br.com.trevizan.espetinhos.dao;

import br.com.trevizan.espetinhos.connection.ConnectionFactory;
import br.com.trevizan.espetinhos.model.ItemComanda;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ItemComandaDAO {

    /**
     * Adiciona um produto à comanda.
     *
     * O subtotal é calculado no Java:
     * quantidade x precoUnitario.
     */
    public int adicionar(ItemComanda item) {

        String sql = """
                INSERT INTO item_comanda
                    (
                        id_comanda,
                        id_produto,
                        quantidade,
                        preco_unitario,
                        observacao,
                        subtotal,
                        status_item
                    )
                VALUES
                    (?, ?, ?, ?, ?, ?, 'PENDENTE')
                """;

        item.setSubtotal(
                item.getPrecoUnitario().multiply(
                        java.math.BigDecimal.valueOf(item.getQuantidade())
                )
        );

        try (
                Connection conn = ConnectionFactory.getConnection();
                PreparedStatement stmt = conn.prepareStatement(
                        sql,
                        Statement.RETURN_GENERATED_KEYS
                )
        ) {

            stmt.setInt(1, item.getIdComanda());
            stmt.setInt(2, item.getIdProduto());
            stmt.setInt(3, item.getQuantidade());
            stmt.setBigDecimal(4, item.getPrecoUnitario());

            if (
                    item.getObservacao() == null
                            || item.getObservacao().isBlank()
            ) {
                stmt.setNull(5, Types.VARCHAR);
            } else {
                stmt.setString(5, item.getObservacao().trim());
            }

            stmt.setBigDecimal(6, item.getSubtotal());

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {

                if (rs.next()) {
                    int idGerado = rs.getInt(1);
                    item.setIdItemComanda(idGerado);
                    return idGerado;
                }
            }

            throw new RuntimeException(
                    "Item inserido, mas não foi possível obter seu ID."
            );

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erro ao adicionar item à comanda.", e
            );
        }
    }

    /**
     * Lista os itens e também traz o nome do produto.
     *
     * Como ItemComanda ainda não possui nomeProduto,
     * este método retorna somente os dados do model.
     */
    public List<ItemComanda> listarPorComanda(int idComanda) {

        String sql = """
                SELECT
                    id_item_comanda,
                    id_comanda,
                    id_produto,
                    quantidade,
                    preco_unitario,
                    observacao,
                    subtotal,
                    status_item
                FROM item_comanda
                WHERE id_comanda = ?
                  AND status_item <> 'CANCELADO'
                ORDER BY id_item_comanda
                """;

        List<ItemComanda> itens = new ArrayList<>();

        try (
                Connection conn = ConnectionFactory.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)
        ) {

            stmt.setInt(1, idComanda);

            try (ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {

                    ItemComanda item = criarItem(rs);

                    itens.add(item);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erro ao listar itens da comanda.", e
            );
        }

        return itens;
    }

    public ItemComanda buscarPorId(int idItemComanda) {

        String sql = """
                SELECT
                    id_item_comanda,
                    id_comanda,
                    id_produto,
                    quantidade,
                    preco_unitario,
                    observacao,
                    subtotal,
                    status_item
                FROM item_comanda
                WHERE id_item_comanda = ?
                """;

        try (
                Connection conn = ConnectionFactory.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)
        ) {

            stmt.setInt(1, idItemComanda);

            try (ResultSet rs = stmt.executeQuery()) {

                if (rs.next()) {
                    return criarItem(rs);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erro ao buscar item da comanda.", e
            );
        }

        return null;
    }

    public void atualizarQuantidade(
            int idItemComanda,
            int quantidade
    ) {

        String sql = """
                UPDATE item_comanda
                SET quantidade = ?,
                    subtotal = preco_unitario * ?
                WHERE id_item_comanda = ?
                  AND status_item <> 'CANCELADO'
                """;

        try (
                Connection conn = ConnectionFactory.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)
        ) {

            stmt.setInt(1, quantidade);
            stmt.setInt(2, quantidade);
            stmt.setInt(3, idItemComanda);

            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erro ao atualizar quantidade do item.", e
            );
        }
    }

    public void atualizarObservacao(
            int idItemComanda,
            String observacao
    ) {

        String sql = """
                UPDATE item_comanda
                SET observacao = ?
                WHERE id_item_comanda = ?
                  AND status_item <> 'CANCELADO'
                """;

        try (
                Connection conn = ConnectionFactory.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)
        ) {

            if (observacao == null || observacao.isBlank()) {
                stmt.setNull(1, Types.VARCHAR);
            } else {
                stmt.setString(1, observacao.trim());
            }

            stmt.setInt(2, idItemComanda);

            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erro ao atualizar observação do item.", e
            );
        }
    }

    public void atualizarStatus(
            int idItemComanda,
            String status
    ) {

        String sql = """
                UPDATE item_comanda
                SET status_item = ?
                WHERE id_item_comanda = ?
                """;

        try (
                Connection conn = ConnectionFactory.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)
        ) {

            stmt.setString(1, status);
            stmt.setInt(2, idItemComanda);

            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erro ao atualizar status do item.", e
            );
        }
    }

    /**
     * Em vez de apagar fisicamente o registro,
     * marcamos como CANCELADO.
     */
    public void cancelar(int idItemComanda) {

        atualizarStatus(idItemComanda, "CANCELADO");
    }

    private ItemComanda criarItem(ResultSet rs)
            throws SQLException {

        ItemComanda item = new ItemComanda();

        item.setIdItemComanda(
                rs.getInt("id_item_comanda")
        );

        item.setIdComanda(
                rs.getInt("id_comanda")
        );

        item.setIdProduto(
                rs.getInt("id_produto")
        );

        item.setQuantidade(
                rs.getInt("quantidade")
        );

        item.setPrecoUnitario(
                rs.getBigDecimal("preco_unitario")
        );

        item.setObservacao(
                rs.getString("observacao")
        );

        item.setSubtotal(
                rs.getBigDecimal("subtotal")
        );

        item.setStatusItem(
                rs.getString("status_item")
        );

        return item;
    }
}