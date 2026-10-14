package br.com.trevizan.espetinhos.util;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Ícone da logo da loja desenhado a partir da imagem em alta resolução
 * ({@code Trevizan.png}, 888 x 904 px).
 *
 * <p>Na hora de desenhar, calcula quantos pixels reais a logo ocupa no
 * monitor (tamanho na tela x escala do Windows), gera uma cópia exatamente
 * desse tamanho, reduzindo aos poucos (metade por vez) com interpolação de
 * alta qualidade, e desenha essa cópia. As cópias ficam guardadas em
 * memória e são reaproveitadas.</p>
 *
 * <p>Uso: {@code label.setIcon(LogoNitida.criar(120));} (largura em pixels
 * "lógicos", os mesmos usados no resto do layout).</p>
 */
public final class LogoNitida implements Icon {

    private static final String CAMINHO_LOGO = "/Trevizan.png";
    private static final String CAMINHO_LOGO_RESERVA = "/TrevizanPequeno.png";

    private static BufferedImage original;
    private static final Map<Long, BufferedImage> CACHE = new HashMap<>();

    private final int largura;
    private final int altura;

    private LogoNitida(int largura, int altura) {
        this.largura = largura;
        this.altura = altura;
    }

    /**
     * Cria o ícone da logo com a largura informada (a altura segue a
     * proporção da imagem).
     *
     * @return o ícone, ou {@code null} se a imagem não for encontrada
     */
    public static Icon criar(int largura) {
        BufferedImage imagem = carregarOriginal();
        if (imagem == null) {
            return null;
        }
        int altura = Math.round(largura * (float) imagem.getHeight() / imagem.getWidth());
        return new LogoNitida(largura, altura);
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        BufferedImage imagem = carregarOriginal();
        if (imagem == null) {
            return;
        }
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            AffineTransform transformacao = g2.getTransform();
            double escalaX = Math.abs(transformacao.getScaleX());
            double escalaY = Math.abs(transformacao.getScaleY());

            // tamanho em pixels reais do monitor
            int larguraReal = Math.max(1, (int) Math.round(largura * escalaX));
            int alturaReal = Math.max(1, (int) Math.round(altura * escalaY));
            BufferedImage reduzida = obterReduzida(imagem, larguraReal, alturaReal);

            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            // desenha a cópia já no tamanho exato (1 pixel da imagem por pixel do monitor)
            g2.drawImage(reduzida, x, y, largura, altura, null);
        } finally {
            g2.dispose();
        }
    }

    @Override
    public int getIconWidth() {
        return largura;
    }

    @Override
    public int getIconHeight() {
        return altura;
    }

    private static synchronized BufferedImage carregarOriginal() {
        if (original == null) {
            original = ler(CAMINHO_LOGO);
            if (original == null) {
                original = ler(CAMINHO_LOGO_RESERVA);
            }
        }
        return original;
    }

    private static BufferedImage ler(String caminho) {
        try (InputStream entrada = LogoNitida.class.getResourceAsStream(caminho)) {
            return entrada != null ? ImageIO.read(entrada) : null;
        } catch (IOException e) {
            return null;
        }
    }

    private static synchronized BufferedImage obterReduzida(BufferedImage imagem, int larguraAlvo, int alturaAlvo) {
        long chave = ((long) larguraAlvo << 32) | alturaAlvo;
        return CACHE.computeIfAbsent(chave, k -> reduzirComQualidade(imagem, larguraAlvo, alturaAlvo));
    }

    /**
     * Reduz a imagem pela metade repetidas vezes até chegar perto do tamanho
     * final e faz o último ajuste com interpolação bicúbica.
     */
    private static BufferedImage reduzirComQualidade(BufferedImage imagem, int larguraAlvo, int alturaAlvo) {
        BufferedImage atual = imagem;
        int larguraAtual = imagem.getWidth();
        int alturaAtual = imagem.getHeight();

        while (larguraAtual / 2 >= larguraAlvo && alturaAtual / 2 >= alturaAlvo) {
            larguraAtual /= 2;
            alturaAtual /= 2;
            atual = redimensionar(atual, larguraAtual, alturaAtual);
        }
        return redimensionar(atual, larguraAlvo, alturaAlvo);
    }

    private static BufferedImage redimensionar(BufferedImage imagem, int largura, int altura) {
        BufferedImage destino = new BufferedImage(largura, altura, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = destino.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.drawImage(imagem, 0, 0, largura, altura, null);
        g2.dispose();
        return destino;
    }
}
