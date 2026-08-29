package br.com.trevizan.espetinhos;

import br.com.trevizan.espetinhos.dao.UsuarioDAO;
import br.com.trevizan.espetinhos.model.Usuario;

public class TesteUsuarioDAO {

    public static void main(String[] args) {

        UsuarioDAO usuarioDAO = new UsuarioDAO();

        Usuario usuario = usuarioDAO.autenticar(
                "admin",
                "senhaerrada"
        );

        if (usuario != null) {

            System.out.println("Login realizado com sucesso!");
            System.out.println("Usuário: " + usuario.getNome());

        } else {

            System.out.println("Login ou senha inválidos.");
        }
    }
}