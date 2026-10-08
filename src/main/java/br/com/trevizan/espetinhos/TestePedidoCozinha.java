
package br.com.trevizan.espetinhos;

import br.com.trevizan.espetinhos.dao.PedidoCozinhaDAO;
import br.com.trevizan.espetinhos.model.PedidoCozinha;
import br.com.trevizan.espetinhos.model.ItemComanda;

import java.util.List;

public class TestePedidoCozinha {

    public static void main(String[] args) {

        PedidoCozinhaDAO dao = new PedidoCozinhaDAO();
        List<PedidoCozinha> pedidos = dao.listarPendentes();

        System.out.println("=== PEDIDOS DA COZINHA ===");

        for (PedidoCozinha pedido : pedidos) {

            System.out.println("-------------------------");
            System.out.println("Comanda: " + pedido.getIdComanda());
            System.out.println("Tipo: " + pedido.getTipoAtendimento());
            System.out.println("Mesa: " + pedido.getNumeroMesa());
            System.out.println("Cliente: " + pedido.getNomeCliente());
            System.out.println("Abertura: " + pedido.getDataAbertura());

            for (ItemComanda item : pedido.getItens()) {
                System.out.println(
                        item.getQuantidade() + "x " +
                                item.getNomeProduto()
                );

                if (item.getObservacao() != null &&
                        !item.getObservacao().isBlank()) {
                    System.out.println(
                            "Observação: " + item.getObservacao()
                    );
                }
            }
        }

        System.out.println("-------------------------");
        System.out.println("Total de comandas: " + pedidos.size());
    }
}
