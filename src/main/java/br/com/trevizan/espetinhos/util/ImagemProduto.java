package br.com.trevizan.espetinhos.util;

import br.com.trevizan.espetinhos.dao.ProdutoDAO;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import javax.swing.Icon;
import javax.swing.JComponent;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Fotos dos produtos do cardápio.
 *
 * <ul>
 *   <li>As fotos ficam no banco (coluna {@code produto.imagem}) em JPEG.
 *       {@link #prepararArquivo} reduz a imagem escolhida para no máximo
 *       {@value #LADO_MAXIMO} px antes de salvar, para o banco não crescer
 *       demais.</li>
 *   <li>Na primeira vez que alguma tela pede uma foto, todas são lidas do
 *       banco de uma vez e ficam guardadas em memória.</li>
 *   <li>As fotos são desenhadas "cobrindo" o espaço (cortando as sobras,
 *       sem distorcer) e com os cantos arredondados, já no tamanho exato de
 *       pixels do monitor, para ficarem nítidas.</li>
 * </ul>
 */
public final class ImagemProduto {

    /** Maior largura/altura da foto gravada no banco. */
    public static final int LADO_MAXIMO = 600;

    /** Maior arquivo aceito na escolha da imagem (10 MB). */
    private static final long TAMANHO_MAXIMO_ARQUIVO = 10L * 1024 * 1024;

    private static Map<Integer, BufferedImage> fotos;
    private static boolean avisoColunaMostrado;

    private ImagemProduto() {
    }

    /** Foto do produto, ou {@code null} se ele não tiver foto. */
    public static synchronized BufferedImage obter(int idProduto) {
        carregarSeNecessario();
        return fotos.get(idProduto);
    }

    /** Indica se o produto tem foto cadastrada. */
    public static boolean temFoto(int idProduto) {
        return obter(idProduto) != null;
    }

    /** Atualiza a foto guardada em memória depois de salvar no banco. */
    public static synchronized void atualizar(int idProduto, byte[] imagem) {
        carregarSeNecessario();
        BufferedImage foto = ler(imagem);
        if (foto == null) {
            fotos.remove(idProduto);
        } else {
            fotos.put(idProduto, foto);
        }
    }

    /** Descarta as fotos em memória; serão lidas do banco de novo. */
    public static synchronized void recarregar() {
        fotos = null;
    }

    /**
     * Lê o arquivo escolhido pelo usuário e devolve a foto pronta para
     * gravar: JPEG com no máximo {@value #LADO_MAXIMO} px de lado.
     *
     * @throws IOException se o arquivo não for uma imagem válida
     */
    public static byte[] prepararArquivo(File arquivo) throws IOException {
        if (arquivo.length() > TAMANHO_MAXIMO_ARQUIVO) {
            throw new IOException("A imagem tem mais de 10 MB. Escolha um arquivo menor.");
        }
        BufferedImage original = ImageIO.read(arquivo);
        if (original == null) {
            throw new IOException("Formato de imagem não reconhecido. Use JPG, PNG, GIF ou BMP.");
        }

        double escala = Math.min(1.0, (double) LADO_MAXIMO
                / Math.max(original.getWidth(), original.getHeight()));
        int largura = Math.max(1, (int) Math.round(original.getWidth() * escala));
        int altura = Math.max(1, (int) Math.round(original.getHeight() * escala));

        // fundo branco no lugar da transparência (o JPEG não tem transparência)
        BufferedImage reduzida = reduzirComQualidade(original, largura, altura);
        BufferedImage semTransparencia = new BufferedImage(largura, altura, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = semTransparencia.createGraphics();
        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, largura, altura);
        g2.drawImage(reduzida, 0, 0, null);
        g2.dispose();

        return gravarJpeg(semTransparencia, 0.85f);
    }

    /** Converte os bytes gravados no banco em imagem ({@code null} se vazio). */
    public static BufferedImage ler(byte[] imagem) {
        if (imagem == null || imagem.length == 0) {
            return null;
        }
        try {
            return ImageIO.read(new ByteArrayInputStream(imagem));
        } catch (IOException e) {
            return null;
        }
    }

    /** Ícone com a foto cobrindo {@code largura x altura} e cantos arredondados. */
    public static Icon criarIcone(BufferedImage foto, int largura, int altura, int arco) {
        return new IconeFoto(foto, largura, altura, arco);
    }

    /**
     * Componente que desenha a foto ocupando todo o seu tamanho (usado nos
     * cartões do cardápio, onde a largura muda com a janela).
     */
    public static JComponent criarPainelFoto(BufferedImage foto, int arco) {
        return new PainelFoto(foto, arco);
    }

    // ------------------------------------------------------------------

    private static void carregarSeNecessario() {
        if (fotos != null) {
            return;
        }
        fotos = new HashMap<>();
        try {
            for (Map.Entry<Integer, byte[]> item : new ProdutoDAO().listarImagens().entrySet()) {
                BufferedImage foto = ler(item.getValue());
                if (foto != null) {
                    fotos.put(item.getKey(), foto);
                }
            }
        } catch (RuntimeException e) {
            // banco sem a coluna "imagem" (script database/06_cardapio_e_imagens.sql
            // ainda não executado): o sistema segue funcionando, sem fotos
            if (!avisoColunaMostrado) {
                avisoColunaMostrado = true;
                System.err.println("Fotos dos produtos indisponíveis: " + e.getMessage());
            }
        }
    }

    private static byte[] gravarJpeg(BufferedImage imagem, float qualidade) throws IOException {
        Iterator<ImageWriter> escritores = ImageIO.getImageWritersByFormatName("jpg");
        if (!escritores.hasNext()) {
            throw new IOException("Não foi possível gerar a imagem JPEG.");
        }
        ImageWriter escritor = escritores.next();
        ByteArrayOutputStream saida = new ByteArrayOutputStream();
        try (ImageOutputStream fluxo = ImageIO.createImageOutputStream(saida)) {
            escritor.setOutput(fluxo);
            ImageWriteParam parametros = escritor.getDefaultWriteParam();
            parametros.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            parametros.setCompressionQuality(qualidade);
            escritor.write(null, new IIOImage(imagem, null, null), parametros);
        } finally {
            escritor.dispose();
        }
        return saida.toByteArray();
    }

    /**
     * Recorta o centro da foto na proporção do destino e reduz para
     * {@code largura x altura} (a foto "cobre" o espaço, sem distorcer).
     */
    private static BufferedImage cobrir(BufferedImage foto, int largura, int altura) {
        double escala = Math.max((double) largura / foto.getWidth(), (double) altura / foto.getHeight());
        int larguraCorte = Math.min(foto.getWidth(), Math.max(1, (int) Math.round(largura / escala)));
        int alturaCorte = Math.min(foto.getHeight(), Math.max(1, (int) Math.round(altura / escala)));
        int x = (foto.getWidth() - larguraCorte) / 2;
        int y = (foto.getHeight() - alturaCorte) / 2;
        BufferedImage corte = foto.getSubimage(x, y, larguraCorte, alturaCorte);
        return reduzirComQualidade(corte, largura, altura);
    }

    /** Reduz pela metade em etapas e faz o ajuste final com interpolação bicúbica. */
    private static BufferedImage reduzirComQualidade(BufferedImage imagem, int largura, int altura) {
        BufferedImage atual = imagem;
        int larguraAtual = imagem.getWidth();
        int alturaAtual = imagem.getHeight();
        while (larguraAtual / 2 >= largura && alturaAtual / 2 >= altura) {
            larguraAtual /= 2;
            alturaAtual /= 2;
            atual = redimensionar(atual, larguraAtual, alturaAtual);
        }
        return redimensionar(atual, largura, altura);
    }

    private static BufferedImage redimensionar(BufferedImage imagem, int largura, int altura) {
        BufferedImage destino = new BufferedImage(largura, altura, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = destino.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.drawImage(imagem, 0, 0, largura, altura, null);
        g2.dispose();
        return destino;
    }

    /**
     * Desenha a foto em (x, y) com {@code largura x altura} pixels lógicos,
     * gerando a cópia reduzida no tamanho real do monitor.
     */
    private static BufferedImage desenhar(Graphics g, BufferedImage foto, BufferedImage reduzida,
                                          int x, int y, int largura, int altura, int arco) {
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            AffineTransform transformacao = g2.getTransform();
            int larguraReal = Math.max(1, (int) Math.round(largura * Math.abs(transformacao.getScaleX())));
            int alturaReal = Math.max(1, (int) Math.round(altura * Math.abs(transformacao.getScaleY())));
            if (reduzida == null || reduzida.getWidth() != larguraReal || reduzida.getHeight() != alturaReal) {
                reduzida = cobrir(foto, larguraReal, alturaReal);
            }
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            if (arco > 0) {
                g2.clip(new RoundRectangle2D.Float(x, y, largura, altura, arco, arco));
            }
            g2.drawImage(reduzida, x, y, largura, altura, null);
        } finally {
            g2.dispose();
        }
        return reduzida;
    }

    private static final class IconeFoto implements Icon {
        private final BufferedImage foto;
        private final int largura;
        private final int altura;
        private final int arco;
        private BufferedImage reduzida;

        IconeFoto(BufferedImage foto, int largura, int altura, int arco) {
            this.foto = foto;
            this.largura = largura;
            this.altura = altura;
            this.arco = arco;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            if (foto != null) {
                reduzida = desenhar(g, foto, reduzida, x, y, largura, altura, arco);
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
    }

    private static final class PainelFoto extends JComponent {
        private final BufferedImage foto;
        private final int arco;
        private BufferedImage reduzida;

        PainelFoto(BufferedImage foto, int arco) {
            this.foto = foto;
            this.arco = arco;
        }

        @Override
        protected void paintComponent(Graphics g) {
            if (foto != null && getWidth() > 0 && getHeight() > 0) {
                reduzida = desenhar(g, foto, reduzida, 0, 0, getWidth(), getHeight(), arco);
            }
        }
    }
}
