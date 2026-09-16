package br.com.trevizan.espetinhos.view;

import javax.swing.JFrame;

public class TesteCaixa {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Teste - Tela de Caixa");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(new Caixa());
        frame.setSize(900, 500);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}