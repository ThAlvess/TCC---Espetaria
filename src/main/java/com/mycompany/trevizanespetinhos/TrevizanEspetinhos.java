
package com.mycompany.trevizanespetinhos;

import com.mycompany.trevizanespetinhos.telas.Login; //importando tela de login da pasta


public class TrevizanEspetinhos {

    public static void main(String[] args) {
        // Torna a tela de Login visível ao iniciar a aplicação
        java.awt.EventQueue.invokeLater(() -> {
            new Login().setVisible(true);// torna a janela de Login Visível.
        });
    }
}