package br.com.trevizan.espetinhos.util;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import javax.swing.JLabel;

/**
 * Padrão visual do cabeçalho (título + subtítulo) das telas do sistema.
 *
 * A referência é a tela de Relatórios:
 * <ul>
 *   <li>título em Segoe UI, negrito, 28, verde (21, 97, 0), em MAIÚSCULAS;</li>
 *   <li>canto superior esquerdo do título a {@link #MARGEM_LATERAL} px da
 *       esquerda e {@link #MARGEM_TOPO} px do topo da tela — o conteúdo
 *       abaixo também começa na mesma margem lateral, alinhado ao título;</li>
 *   <li>subtítulo na mesma fonte do título (Segoe UI), sem negrito, com o
 *       tamanho do subtítulo da tela de Mesas (13) e cinza (112, 112, 112).</li>
 * </ul>
 *
 * Para mudar o padrão de todas as telas de uma vez, basta alterar as
 * constantes abaixo.
 */
public final class PadraoTela {

    public static final Font FONTE_TITULO = new Font("Segoe UI", Font.BOLD, 28);
    public static final Color COR_TITULO = new Color(21, 97, 0);

    public static final Font FONTE_SUBTITULO = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Color COR_SUBTITULO = new Color(112, 112, 112);

    /** Distância do topo da tela até o título (igual à tela de Relatórios). */
    public static final int MARGEM_TOPO = 51;

    /**
     * Distância da borda esquerda (e direita) da tela até o título e o
     * conteúdo. Na tela de Relatórios isso vem do layout gerado pelo NetBeans:
     * 63 px + 6 px de espaçamento padrão do GroupLayout com o FlatLaf.
     */
    public static final int MARGEM_LATERAL = 69;

    /** Espaço vertical entre o título e o subtítulo (igual à tela de Mesas). */
    public static final int ESPACO_TITULO_SUBTITULO = 5;

    private PadraoTela() {
    }

    public static JLabel criarTitulo(String texto) {
        JLabel titulo = new JLabel(texto);
        aplicarTitulo(titulo);
        return titulo;
    }

    public static JLabel criarSubtitulo(String texto) {
        JLabel subtitulo = new JLabel(texto);
        aplicarSubtitulo(subtitulo);
        return subtitulo;
    }

    public static void aplicarTitulo(JLabel titulo) {
        titulo.setFont(FONTE_TITULO);
        titulo.setForeground(COR_TITULO);
        titulo.setAlignmentX(Component.LEFT_ALIGNMENT);
    }

    public static void aplicarSubtitulo(JLabel subtitulo) {
        subtitulo.setFont(FONTE_SUBTITULO);
        subtitulo.setForeground(COR_SUBTITULO);
        subtitulo.setAlignmentX(Component.LEFT_ALIGNMENT);
    }
}
