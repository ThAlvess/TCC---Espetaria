
package br.com.trevizan.espetinhos.dao;

import br.com.trevizan.espetinhos.connection.ConnectionFactory;
import br.com.trevizan.espetinhos.model.ItemComanda;
import br.com.trevizan.espetinhos.model.PedidoCozinha;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PedidoCozinhaDAO {

    public List<PedidoCozinha> listarPendentes() {
        Map<Integer, PedidoCozinha> pedidos = new LinkedHashMap<>();

        String sql = """
            SELECT
                c.id_comanda,
                c.tipo_atendimento,
                c.nome_cliente,
                c.data_abertura,
                m.numero AS numero_mesa,
                ic.id_item_comanda,
                ic.id_produto,
                ic.quantidade,
                ic.preco_unitario,
                ic.observacao,
                ic.subtotal,
                ic.status_item,
                p.nome AS nome_produto
            FROM item_comanda ic
            INNER JOIN comanda c
                ON c.id_comanda = ic.id_comanda
            INNER JOIN produto p
                ON p.id_produto = ic.id_produto
            LEFT JOIN mesa m
                ON m.id_mesa = c.id_mesa
            WHERE c.status = 'ABERTA'
              AND ic.status_item = 'PENDENTE'
              AND p.local_preparo = 'COZINHA'
            ORDER BY c.data_abertura ASC,
                     c.id_comanda ASC,
                     ic.id_item_comanda ASC
            """;

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int idComanda = rs.getInt("id_comanda");

                PedidoCozinha pedido = pedidos.get(idComanda);

                if (pedido == null) {
                    pedido = new PedidoCozinha();

                    pedido.setIdComanda(idComanda);
                    pedido.setTipoAtendimento(
                            rs.getString("tipo_atendimento"));
                    pedido.setNomeCliente(
                            rs.getString("nome_cliente"));

                    int numeroMesa = rs.getInt("numero_mesa");
                    if (rs.wasNull()) {
                        pedido.setNumeroMesa(null);
                    } else {
                        pedido.setNumeroMesa(numeroMesa);
                    }

                    Timestamp abertura =
                            rs.getTimestamp("data_abertura");

                    if (abertura != null) {
                        pedido.setDataAbertura(
                                abertura.toLocalDateTime());
                    }

                    pedidos.put(idComanda, pedido);
                }

                ItemComanda item = new ItemComanda();

                item.setIdItemComanda(
                        rs.getInt("id_item_comanda"));
                item.setIdComanda(idComanda);
                item.setIdProduto(
                        rs.getInt("id_produto"));
                item.setNomeProduto(
                        rs.getString("nome_produto"));
                item.setQuantidade(
                        rs.getInt("quantidade"));
                item.setPrecoUnitario(
                        rs.getBigDecimal("preco_unitario"));
                item.setObservacao(
                        rs.getString("observacao"));
                item.setSubtotal(
                        rs.getBigDecimal("subtotal"));
                item.setStatusItem(
                        rs.getString("status_item"));

                pedido.getItens().add(item);
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erro ao listar pedidos pendentes da cozinha.", e);
        }

        return new ArrayList<>(pedidos.values());
    }
}
