/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package br.com.trevizan.espetinhos;

/**
 *
 * @author marlo
 */
public class PanelArredondado extends javax.swing.JPanel {

    private int raio = 20;

    public PanelArredondado() {
        setOpaque(false);
    }

    public PanelArredondado(int raio) {
        this();
        this.raio = raio;
    }

    public void setRaio(int raio) {
        this.raio = raio;
        repaint();
    }

    @Override
    public void paint(java.awt.Graphics g) {
        java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
        g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                             java.awt.RenderingHints.VALUE_ANTIALIAS_ON);

        java.awt.geom.RoundRectangle2D forma = new java.awt.geom.RoundRectangle2D.Float(
            0, 0, getWidth(), getHeight(), raio, raio);

        // 1. Pinta o fundo já arredondado
        g2.setColor(getBackground());
        g2.fill(forma);

        // 2. A partir daqui, tudo que for desenhado (inclusive os filhos)
        //    fica restrito a essa forma arredondada
        g2.setClip(forma);

        // 3. Deixa o Swing desenhar o componente normalmente (título + gráfico),
        //    já respeitando o recorte
        super.paint(g2);

        g2.dispose();
    }
}
