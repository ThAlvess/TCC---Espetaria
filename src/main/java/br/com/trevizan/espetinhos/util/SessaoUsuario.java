package br.com.trevizan.espetinhos.util;

import br.com.trevizan.espetinhos.model.Usuario;

public class SessaoUsuario {

    private static Usuario usuarioLogado;

    public static void login(Usuario usuario) {
        usuarioLogado = usuario;
    }

    public static Usuario getUsuarioLogado() {
        return usuarioLogado;
    }

    public static void logout() {
        usuarioLogado = null;
    }
}