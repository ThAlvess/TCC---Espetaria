package br.com.trevizan.espetinhos;

import br.com.trevizan.espetinhos.connection.ConnectionFactory;

import java.sql.Connection;

public class TesteConexao {

    public static void main(String[] args) {
 
        try (Connection connection = ConnectionFactory.getConnection()) {

            if (connection != null) {
                System.out.println("Conexão realizada com sucesso!");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}