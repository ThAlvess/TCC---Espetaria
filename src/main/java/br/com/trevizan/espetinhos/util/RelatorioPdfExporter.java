package br.com.trevizan.espetinhos.util;

import br.com.trevizan.espetinhos.model.ProdutoVendido;
import br.com.trevizan.espetinhos.model.ResumoPeriodo;
import br.com.trevizan.espetinhos.model.ResumoRelatorio;
import br.com.trevizan.espetinhos.model.VendaDetalhada;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPCellEvent;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfStamper;
import com.lowagie.text.pdf.PdfWriter;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.ui.RectangleInsets;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

/**
 * Gera o PDF da tela de Relatórios.
 *
 * Estrutura do arquivo:
 * <ul>
 *   <li><b>Página 1 – Visão geral</b> (igual para todos os filtros): cabeçalho
 *       com logo e período, os 4 KPIs da tela, os 4 gráficos e um quadro com
 *       o valor recebido em cada forma de pagamento.</li>
 *   <li><b>Página 2 em diante – Detalhamento</b>, que muda conforme o
 *       tamanho do período ({@link DadosRelatorio#getDetalhamento()}):
 *       <ul>
 *         <li>um dia (Diário ou Personalizado de 1 dia): destaques do dia,
 *             tabela com cada venda (comanda) e todos os produtos vendidos;</li>
 *         <li>até ~2 meses (Mensal ou Personalizado curto): destaques,
 *             resumo por semana e produtos mais vendidos;</li>
 *         <li>períodos longos (Anual ou Personalizado longo): destaques,
 *             resumo mês a mês e produtos mais vendidos.</li>
 *       </ul></li>
 * </ul>
 *
 * Quando há período anterior para comparar, os cartões mostram a variação
 * (ex.: "+12,3% vs. agosto/2026") e o gráfico de evolução traz a linha
 * tracejada do período anterior (desenhada pela própria tela).
 *
 * Uso (a tela de Relatório monta o {@link DadosRelatorio} e chama {@link #exportar(File)}):
 * <pre>
 * RelatorioPdfExporter pdf = new RelatorioPdfExporter(dados);
 * pdf.setGraficoRanking(chart);   // deve ser chamado na thread do Swing (EDT)
 * ...
 * pdf.exportar(arquivo);          // pode rodar em segundo plano
 * </pre>
 *
 * Biblioteca usada: OpenPDF (com.github.librepdf:openpdf), fork livre do iText 4.
 */
public class RelatorioPdfExporter {

    // ------------------------------------------------------------------
    // Identidade visual (mesmas cores da tela)
    // ------------------------------------------------------------------
    private static final Color VERDE = new Color(25, 100, 25);
    private static final Color VERDE_CLARO = new Color(232, 241, 232);
    private static final Color LARANJA = new Color(210, 84, 43);
    private static final Color VERMELHO = new Color(178, 58, 38);
    private static final Color BEGE = new Color(246, 242, 238);
    private static final Color BORDA = new Color(225, 220, 213);
    private static final Color ZEBRA = new Color(250, 248, 245);
    private static final Color TEXTO = new Color(45, 45, 45);
    private static final Color TEXTO_SUAVE = new Color(115, 115, 115);

    private static final Font F_TITULO = new Font(Font.HELVETICA, 17, Font.BOLD, VERDE);
    private static final Font F_SUBTITULO = new Font(Font.HELVETICA, 10, Font.NORMAL, TEXTO);
    private static final Font F_INFO = new Font(Font.HELVETICA, 8, Font.NORMAL, TEXTO_SUAVE);
    private static final Font F_SECAO = new Font(Font.HELVETICA, 13, Font.BOLD, VERDE);
    private static final Font F_SECAO_DESC = new Font(Font.HELVETICA, 8.5f, Font.NORMAL, TEXTO_SUAVE);
    private static final Font F_CARD_TITULO = new Font(Font.HELVETICA, 10, Font.BOLD, TEXTO);
    private static final Font F_KPI_ROTULO = new Font(Font.HELVETICA, 8, Font.BOLD, TEXTO_SUAVE);
    private static final Font F_KPI_VALOR = new Font(Font.HELVETICA, 14, Font.BOLD, TEXTO);
    private static final Font F_KPI_VALOR_LONGO = new Font(Font.HELVETICA, 11.5f, Font.BOLD, TEXTO);
    private static final Font F_KPI_SUB = new Font(Font.HELVETICA, 7.5f, Font.NORMAL, TEXTO_SUAVE);
    private static final Font F_KPI_SUB_POSITIVO = new Font(Font.HELVETICA, 7.5f, Font.BOLD, VERDE);
    private static final Font F_KPI_SUB_NEGATIVO = new Font(Font.HELVETICA, 7.5f, Font.BOLD, VERMELHO);
    private static final Font F_DESTAQUE_VALOR = new Font(Font.HELVETICA, 11, Font.BOLD, TEXTO);
    private static final Font F_TAB_CABECALHO = new Font(Font.HELVETICA, 8, Font.BOLD, Color.WHITE);
    private static final Font F_TAB = new Font(Font.HELVETICA, 8, Font.NORMAL, TEXTO);
    private static final Font F_TAB_NEGRITO = new Font(Font.HELVETICA, 8, Font.BOLD, TEXTO);
    private static final Font F_TAB_SUAVE = new Font(Font.HELVETICA, 8, Font.NORMAL, TEXTO_SUAVE);
    private static final Font F_TAB_POSITIVO = new Font(Font.HELVETICA, 8, Font.BOLD, VERDE);
    private static final Font F_TAB_NEGATIVO = new Font(Font.HELVETICA, 8, Font.BOLD, VERMELHO);
    private static final Font F_RODAPE = new Font(Font.HELVETICA, 7.5f, Font.NORMAL, TEXTO_SUAVE);
    private static final Font F_VAZIO = new Font(Font.HELVETICA, 9, Font.ITALIC, TEXTO_SUAVE);

    /** Quantos pixels por ponto usar ao transformar os gráficos em imagem (nitidez na impressão). */
    private static final int ESCALA_IMAGEM = 3;

    /** Altura (em pontos) da área do gráfico nos cards de meia largura. */
    private static final float ALTURA_GRAFICO_MEIO = 165;

    private static final Locale PT_BR = new Locale("pt", "BR");
    private static final DateTimeFormatter FMT_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FMT_DIA_MES = DateTimeFormatter.ofPattern("dd/MM");
    private static final DateTimeFormatter FMT_HORA = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter FMT_DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm");

    // ------------------------------------------------------------------
    // Dados do relatório
    // ------------------------------------------------------------------
    private final DadosRelatorio dados;

    // Atalhos para os dados, lidos no início de exportar() — assim a tabela de
    // detalhamento pode ser carregada no DadosRelatorio depois de o exportador
    // ter sido criado (os gráficos são desenhados antes, na thread do Swing).
    private PeriodoRelatorio.Tipo tipo;
    private String descricaoPeriodo;
    private String geradoPor;
    private LocalDateTime geradoEm;
    private ResumoRelatorio resumo;
    private Map<String, BigDecimal> totaisPorFormaPagamento;
    private List<VendaDetalhada> vendas;
    private List<ProdutoVendido> produtos;
    private List<ResumoPeriodo> resumoPeriodos;

    private byte[] graficoRanking;
    private byte[] graficoCategoria;
    private byte[] graficoPagamento;
    private byte[] graficoEvolucao;

    // Formatadores (NumberFormat não é thread-safe, então cada exportação tem os seus)
    private final NumberFormat moeda = NumberFormat.getCurrencyInstance(PT_BR);
    private final DecimalFormat percentual = new DecimalFormat("0.0'%'", new DecimalFormatSymbols(PT_BR));
    private final DecimalFormat decimal = new DecimalFormat("0.0", new DecimalFormatSymbols(PT_BR));

    public RelatorioPdfExporter(DadosRelatorio dados) {
        this.dados = dados;
    }

    private void lerDados() {
        tipo = dados.getPeriodo().getTipo();
        descricaoPeriodo = dados.getPeriodo().getDescricao();
        geradoPor = dados.getGeradoPor();
        geradoEm = dados.getGeradoEm();
        resumo = dados.getResumo();
        totaisPorFormaPagamento = dados.getTotaisPorFormaPagamento();
        vendas = dados.getVendas();
        produtos = dados.getProdutos();
        resumoPeriodos = dados.getResumoPeriodos();
    }

    /** "mês" no filtro Mensal; "período" nos demais (usado nos textos do detalhamento). */
    private String palavraPeriodo() {
        return tipo == PeriodoRelatorio.Tipo.MENSAL ? "mês" : "período";
    }

    /*
     * Os gráficos são convertidos em imagem na hora em que são recebidos.
     * Como o JFreeChart é um componente Swing, esses 4 métodos devem ser
     * chamados na thread do Swing (EDT); o exportar(), que é a parte
     * demorada, pode rodar depois em segundo plano.
     */

    public void setGraficoRanking(JFreeChart chart) {
        this.graficoRanking = renderizarGrafico(ajustarRankingParaPdf(chart), 420, 270);
    }

    public void setGraficoCategoria(JFreeChart chart) {
        this.graficoCategoria = renderizarGrafico(chart, 420, 270);
    }

    public void setGraficoPagamento(JFreeChart chart) {
        this.graficoPagamento = renderizarGrafico(chart, 420, 270);
    }

    public void setGraficoEvolucao(JFreeChart chart) {
        this.graficoEvolucao = renderizarGrafico(chart, 820, 240);
    }

    // ------------------------------------------------------------------
    // Geração do arquivo
    // ------------------------------------------------------------------

    /**
     * Gera o PDF no arquivo informado. Feito em duas passadas: primeiro o
     * conteúdo é montado em memória e depois o rodapé ("Página X de Y") é
     * carimbado em cada página, já sabendo o total de páginas.
     */
    public void exportar(File destino) throws IOException {
        lerDados();
        byte[] conteudo = gerarConteudo();

        try (OutputStream saida = new FileOutputStream(destino)) {
            carimbarRodape(conteudo, saida);
        } catch (DocumentException e) {
            throw new IOException("Erro ao finalizar o PDF.", e);
        }
    }

    private byte[] gerarConteudo() throws IOException {
        ByteArrayOutputStream memoria = new ByteArrayOutputStream();
        Document documento = new Document(PageSize.A4, 36, 36, 32, 46);

        try {
            PdfWriter.getInstance(documento, memoria);
            documento.addTitle("Relatório de Vendas - " + tipo.getNome() + " - " + descricaoPeriodo);
            documento.addAuthor(geradoPor != null ? geradoPor : "Trevizan Espetinhos");
            documento.addCreator("Trevizan Espetinhos");
            documento.open();

            // Página 1 – visão geral
            adicionarCabecalho(documento);
            adicionarKpis(documento);
            adicionarGraficos(documento);

            // Página 2+ – detalhamento conforme o filtro
            documento.newPage();
            switch (dados.getDetalhamento()) {
                case POR_VENDA -> adicionarDetalhamentoDiario(documento);
                case POR_SEMANA -> adicionarDetalhamentoMensal(documento);
                case POR_MES -> adicionarDetalhamentoAnual(documento);
            }

        } catch (DocumentException e) {
            throw new IOException("Erro ao montar o PDF.", e);
        } finally {
            if (documento.isOpen()) {
                documento.close();
            }
        }

        return memoria.toByteArray();
    }

    private void carimbarRodape(byte[] conteudo, OutputStream saida) throws IOException, DocumentException {
        PdfReader leitor = new PdfReader(conteudo);
        PdfStamper carimbo = new PdfStamper(leitor, saida);

        int totalPaginas = leitor.getNumberOfPages();
        String textoEsquerda = "Trevizan Espetinhos  ·  Relatório de Vendas " + tipo.getNome()
                + "  ·  " + descricaoPeriodo;

        for (int pagina = 1; pagina <= totalPaginas; pagina++) {
            Rectangle tamanho = leitor.getPageSize(pagina);
            float esquerda = 36;
            float direita = tamanho.getWidth() - 36;
            float base = 24;

            PdfContentByte cb = carimbo.getOverContent(pagina);
            cb.saveState();
            cb.setColorStroke(BORDA);
            cb.setLineWidth(0.6f);
            cb.moveTo(esquerda, base + 11);
            cb.lineTo(direita, base + 11);
            cb.stroke();
            cb.restoreState();

            ColumnText.showTextAligned(cb, Element.ALIGN_LEFT,
                    new Phrase(textoEsquerda, F_RODAPE), esquerda, base, 0);
            ColumnText.showTextAligned(cb, Element.ALIGN_RIGHT,
                    new Phrase("Página " + pagina + " de " + totalPaginas, F_RODAPE), direita, base, 0);
        }

        carimbo.close();
        leitor.close();
    }

    // ------------------------------------------------------------------
    // Página 1 – cabeçalho, KPIs e gráficos
    // ------------------------------------------------------------------

    private void adicionarCabecalho(Document documento) throws DocumentException {
        PdfPTable cabecalho = new PdfPTable(new float[] {1f, 9f});
        cabecalho.setWidthPercentage(100);

        PdfPCell celulaLogo = semBorda(new PdfPCell());
        Image logo = carregarLogo();
        if (logo != null) {
            logo.scaleToFit(46, 46);
            celulaLogo.addElement(logo);
        }
        celulaLogo.setVerticalAlignment(Element.ALIGN_MIDDLE);
        celulaLogo.setPaddingBottom(8);
        cabecalho.addCell(celulaLogo);

        PdfPCell celulaTexto = semBorda(new PdfPCell());
        celulaTexto.setVerticalAlignment(Element.ALIGN_MIDDLE);
        celulaTexto.setPaddingLeft(6);
        celulaTexto.setPaddingBottom(8);

        Paragraph titulo = new Paragraph("Relatório de Vendas — " + tipo.getNome(), F_TITULO);
        celulaTexto.addElement(titulo);

        Paragraph periodo = new Paragraph("Período: " + descricaoPeriodo, F_SUBTITULO);
        periodo.setSpacingBefore(1);
        celulaTexto.addElement(periodo);

        if (dados.temComparacao()) {
            Paragraph comparacao = new Paragraph(
                    "Comparado com: " + dados.getPeriodoAnterior().getDescricaoComparacao(), F_INFO);
            comparacao.setSpacingBefore(1);
            celulaTexto.addElement(comparacao);
        }

        String info = "Gerado em " + geradoEm.format(FMT_DATA_HORA)
                + (geradoPor != null && !geradoPor.isBlank() ? " por " + geradoPor : "");
        Paragraph geracao = new Paragraph(info, F_INFO);
        geracao.setSpacingBefore(2);
        celulaTexto.addElement(geracao);

        cabecalho.addCell(celulaTexto);

        // linha verde separando o cabeçalho do conteúdo
        PdfPCell linha = semBorda(new PdfPCell());
        linha.setColspan(2);
        linha.setFixedHeight(2);
        linha.setBackgroundColor(VERDE);
        cabecalho.addCell(linha);

        cabecalho.setSpacingAfter(12);
        documento.add(cabecalho);
    }

    private void adicionarKpis(Document documento) throws DocumentException {
        PdfPTable kpis = new PdfPTable(4);
        kpis.setWidthPercentage(100);

        ResumoRelatorio anterior = dados.getResumoAnterior();

        kpis.addCell(cartaoIndicador("FATURAMENTO TOTAL",
                moeda.format(resumo.getFaturamentoTotal()),
                comparacao(resumo.getFaturamentoTotal(),
                        anterior != null ? anterior.getFaturamentoTotal() : null, "recebido no período")));
        kpis.addCell(cartaoIndicador("QUANTIDADE DE VENDAS",
                String.valueOf(resumo.getQuantidadeVendas()),
                comparacao(BigDecimal.valueOf(resumo.getQuantidadeVendas()),
                        anterior != null ? BigDecimal.valueOf(anterior.getQuantidadeVendas()) : null, "comandas fechadas")));
        kpis.addCell(cartaoIndicador("TICKET MÉDIO",
                moeda.format(resumo.getTicketMedio()),
                comparacao(resumo.getTicketMedio(),
                        anterior != null ? anterior.getTicketMedio() : null, "por venda")));
        kpis.addCell(cartaoIndicador("PAGAMENTO PRINCIPAL",
                resumo.getFormaPagamentoPrincipal() != null ? resumo.getFormaPagamentoPrincipal() : "—",
                new Phrase(resumo.getFormaPagamentoPrincipal() != null
                        ? "Total: " + moeda.format(resumo.getValorFormaPagamentoPrincipal()) : " ", F_KPI_SUB)));

        kpis.setSpacingAfter(10);
        documento.add(kpis);
    }

    private void adicionarGraficos(Document documento) throws DocumentException {
        PdfPTable grade = new PdfPTable(2);
        grade.setWidthPercentage(100);

        grade.addCell(cartaoGrafico("Ranking de Produtos", graficoRanking, ALTURA_GRAFICO_MEIO));
        grade.addCell(cartaoGrafico("Vendas por Categoria", graficoCategoria, ALTURA_GRAFICO_MEIO));
        grade.addCell(cartaoGrafico("Por Forma de Pagamento", graficoPagamento, ALTURA_GRAFICO_MEIO));
        grade.addCell(cartaoFormasPagamento());

        PdfPCell evolucao = cartaoGrafico(tituloEvolucao(), graficoEvolucao, 150);
        evolucao.setColspan(2);
        grade.addCell(evolucao);

        documento.add(grade);
    }

    private String tituloEvolucao() {
        return switch (dados.getDetalhamento()) {
            case POR_VENDA -> "Evolução do Faturamento (por hora)";
            case POR_SEMANA -> "Evolução do Faturamento (por dia)";
            case POR_MES -> "Evolução do Faturamento (por mês)";
        };
    }

    /** Quadro ao lado do gráfico de pagamento com o valor exato de cada forma. */
    private PdfPCell cartaoFormasPagamento() {
        PdfPCell cartao = cartao(Color.WHITE, BORDA);
        cartao.addElement(new Paragraph("Valores por Forma de Pagamento", F_CARD_TITULO));

        BigDecimal total = somar(totaisPorFormaPagamento.values());

        if (totaisPorFormaPagamento.isEmpty()) {
            // mesma posição da mensagem do card de gráfico ao lado (centralizada)
            Paragraph vazio = new Paragraph("Sem pagamentos no período.", F_VAZIO);
            vazio.setSpacingBefore(ALTURA_GRAFICO_MEIO / 2 - 10);
            vazio.setAlignment(Element.ALIGN_CENTER);
            cartao.addElement(vazio);
            return cartao;
        }

        PdfPTable tabela = new PdfPTable(new float[] {0.35f, 4f, 3f, 1.8f});
        tabela.setWidthPercentage(100);
        tabela.setSpacingBefore(10);

        tabela.addCell(cabecalho("", Element.ALIGN_LEFT));
        tabela.addCell(cabecalho("Forma", Element.ALIGN_LEFT));
        tabela.addCell(cabecalho("Valor", Element.ALIGN_RIGHT));
        tabela.addCell(cabecalho("%", Element.ALIGN_RIGHT));

        int linha = 0;
        for (Map.Entry<String, BigDecimal> forma : totaisPorFormaPagamento.entrySet()) {
            Color fundo = linha % 2 == 1 ? ZEBRA : Color.WHITE;

            PdfPCell cor = celula("", F_TAB, Element.ALIGN_LEFT, fundo);
            cor.setCellEvent(new MarcadorCor(corFormaPagamento(forma.getKey())));
            tabela.addCell(cor);

            tabela.addCell(celula(forma.getKey(), F_TAB, Element.ALIGN_LEFT, fundo));
            tabela.addCell(celula(moeda.format(forma.getValue()), F_TAB, Element.ALIGN_RIGHT, fundo));
            tabela.addCell(celula(formatarPercentual(forma.getValue(), total), F_TAB_SUAVE, Element.ALIGN_RIGHT, fundo));
            linha++;
        }

        PdfPCell rotuloTotal = celula("Total recebido", F_TAB_NEGRITO, Element.ALIGN_LEFT, VERDE_CLARO);
        rotuloTotal.setColspan(2);
        tabela.addCell(rotuloTotal);
        tabela.addCell(celula(moeda.format(total), F_TAB_NEGRITO, Element.ALIGN_RIGHT, VERDE_CLARO));
        tabela.addCell(celula("100,0%", F_TAB_NEGRITO, Element.ALIGN_RIGHT, VERDE_CLARO));

        cartao.addElement(tabela);
        return cartao;
    }

    // ------------------------------------------------------------------
    // Página 2 – detalhamento de um dia (cada venda)
    // ------------------------------------------------------------------

    private void adicionarDetalhamentoDiario(Document documento) throws DocumentException {
        adicionarTituloSecao(documento, "Vendas do Dia",
                "Cada comanda fechada no dia, em ordem de fechamento. O total bate com o Faturamento Total da página 1.");

        if (vendas.isEmpty()) {
            adicionarMensagemVazia(documento, "Nenhuma venda registrada neste dia.");
            return;
        }

        adicionarDestaquesDiarios(documento);

        PdfPTable tabela = new PdfPTable(new float[] {1.4f, 0.9f, 2.4f, 2.2f, 1.3f, 1.3f, 1.1f, 0.9f, 2.4f, 1.9f});
        tabela.setWidthPercentage(100);
        tabela.setHeaderRows(1);

        tabela.addCell(cabecalho("Comanda", Element.ALIGN_CENTER));
        tabela.addCell(cabecalho("Mesa", Element.ALIGN_CENTER));
        tabela.addCell(cabecalho("Cliente", Element.ALIGN_LEFT));
        tabela.addCell(cabecalho("Atendente", Element.ALIGN_LEFT));
        tabela.addCell(cabecalho("Abertura", Element.ALIGN_CENTER));
        tabela.addCell(cabecalho("Fecham.", Element.ALIGN_CENTER));
        tabela.addCell(cabecalho("Tempo", Element.ALIGN_CENTER));
        tabela.addCell(cabecalho("Itens", Element.ALIGN_CENTER));
        tabela.addCell(cabecalho("Pagamento", Element.ALIGN_LEFT));
        tabela.addCell(cabecalho("Total", Element.ALIGN_RIGHT));

        BigDecimal totalGeral = BigDecimal.ZERO;
        int totalItens = 0;
        int linha = 0;

        for (VendaDetalhada venda : vendas) {
            Color fundo = linha % 2 == 1 ? ZEBRA : Color.WHITE;

            tabela.addCell(celula("#" + venda.getIdComanda(), F_TAB_NEGRITO, Element.ALIGN_CENTER, fundo));
            tabela.addCell(celula(String.valueOf(venda.getNumeroMesa()), F_TAB, Element.ALIGN_CENTER, fundo));
            tabela.addCell(celula(textoOuTraco(venda.getNomeCliente()), F_TAB, Element.ALIGN_LEFT, fundo));
            tabela.addCell(celula(textoOuTraco(venda.getAtendente()), F_TAB, Element.ALIGN_LEFT, fundo));
            tabela.addCell(celula(formatarHora(venda.getDataAbertura()), F_TAB, Element.ALIGN_CENTER, fundo));
            tabela.addCell(celula(formatarHora(venda.getDataFechamento()), F_TAB, Element.ALIGN_CENTER, fundo));
            tabela.addCell(celula(formatarDuracao(venda.getPermanencia()), F_TAB_SUAVE, Element.ALIGN_CENTER, fundo));
            tabela.addCell(celula(String.valueOf(venda.getQuantidadeItens()), F_TAB, Element.ALIGN_CENTER, fundo));
            tabela.addCell(celula(textoOuTraco(venda.getFormasPagamento()), F_TAB, Element.ALIGN_LEFT, fundo));
            tabela.addCell(celula(moeda.format(venda.getValorTotal()), F_TAB_NEGRITO, Element.ALIGN_RIGHT, fundo));

            totalGeral = totalGeral.add(venda.getValorTotal());
            totalItens += venda.getQuantidadeItens();
            linha++;
        }

        PdfPCell rotulo = celula("Total do dia — " + vendas.size() + (vendas.size() == 1 ? " venda" : " vendas"),
                F_TAB_NEGRITO, Element.ALIGN_LEFT, VERDE_CLARO);
        rotulo.setColspan(7);
        tabela.addCell(rotulo);
        tabela.addCell(celula(String.valueOf(totalItens), F_TAB_NEGRITO, Element.ALIGN_CENTER, VERDE_CLARO));
        tabela.addCell(celula("", F_TAB_NEGRITO, Element.ALIGN_LEFT, VERDE_CLARO));
        tabela.addCell(celula(moeda.format(totalGeral), F_TAB_NEGRITO, Element.ALIGN_RIGHT, VERDE_CLARO));

        tabela.setSpacingAfter(16);
        documento.add(tabela);

        adicionarTabelaProdutos(documento, "Produtos Vendidos no Dia",
                "Tudo o que saiu da cozinha/bar no dia (itens cancelados não entram), do maior para o menor faturamento.");
    }

    private void adicionarDestaquesDiarios(Document documento) throws DocumentException {
        VendaDetalhada maiorVenda = vendas.get(0);
        long minutosTotais = 0;
        int vendasComPermanencia = 0;
        int itens = 0;
        Map<Integer, BigDecimal> faturamentoPorHora = new TreeMap<>();

        for (VendaDetalhada venda : vendas) {
            if (venda.getValorTotal().compareTo(maiorVenda.getValorTotal()) > 0) {
                maiorVenda = venda;
            }
            Duration permanencia = venda.getPermanencia();
            if (permanencia != null && !permanencia.isNegative()) {
                minutosTotais += permanencia.toMinutes();
                vendasComPermanencia++;
            }
            itens += venda.getQuantidadeItens();
            if (venda.getDataFechamento() != null) {
                faturamentoPorHora.merge(venda.getDataFechamento().getHour(), venda.getValorTotal(), BigDecimal::add);
            }
        }

        int horaPico = -1;
        BigDecimal valorPico = BigDecimal.ZERO;
        for (Map.Entry<Integer, BigDecimal> hora : faturamentoPorHora.entrySet()) {
            if (hora.getValue().compareTo(valorPico) > 0) {
                horaPico = hora.getKey();
                valorPico = hora.getValue();
            }
        }

        PdfPTable destaques = new PdfPTable(4);
        destaques.setWidthPercentage(100);
        destaques.addCell(cartaoDestaque("MAIOR VENDA", moeda.format(maiorVenda.getValorTotal()),
                "Comanda #" + maiorVenda.getIdComanda() + " · Mesa " + maiorVenda.getNumeroMesa()));
        destaques.addCell(cartaoDestaque("HORÁRIO DE PICO",
                horaPico >= 0 ? String.format("%02dh às %02dh", horaPico, (horaPico + 1) % 24) : "—",
                horaPico >= 0 ? moeda.format(valorPico) + " faturados" : " "));
        destaques.addCell(cartaoDestaque("PERMANÊNCIA MÉDIA",
                vendasComPermanencia > 0
                        ? formatarDuracao(Duration.ofMinutes(minutosTotais / vendasComPermanencia)) : "—",
                "da abertura ao fechamento"));
        destaques.addCell(cartaoDestaque("ITENS POR VENDA",
                decimal.format((double) itens / vendas.size()),
                itens + " itens no total"));
        destaques.setSpacingAfter(10);
        documento.add(destaques);
    }

    // ------------------------------------------------------------------
    // Página 2 – detalhamento de períodos médios (resumo por semana)
    // ------------------------------------------------------------------

    private void adicionarDetalhamentoMensal(Document documento) throws DocumentException {
        adicionarTituloSecao(documento, "Resumo por Semana",
                "Semanas de segunda a domingo, recortadas no início e no fim do " + palavraPeriodo() + ".");

        BigDecimal totalMes = BigDecimal.ZERO;
        int vendasMes = 0;
        int diasComVenda = 0;
        ResumoPeriodo melhorSemana = null;
        LocalDate melhorDia = null;
        BigDecimal valorMelhorDia = BigDecimal.ZERO;

        for (ResumoPeriodo semana : resumoPeriodos) {
            totalMes = totalMes.add(semana.getFaturamento());
            vendasMes += semana.getQuantidadeVendas();
            diasComVenda += semana.getDiasComVenda();
            if (semana.getFaturamento().signum() > 0
                    && (melhorSemana == null || semana.getFaturamento().compareTo(melhorSemana.getFaturamento()) > 0)) {
                melhorSemana = semana;
            }
            if (semana.getMelhorDia() != null && semana.getFaturamentoMelhorDia().compareTo(valorMelhorDia) > 0) {
                melhorDia = semana.getMelhorDia();
                valorMelhorDia = semana.getFaturamentoMelhorDia();
            }
        }

        if (vendasMes == 0) {
            adicionarMensagemVazia(documento, "Nenhuma venda registrada neste " + palavraPeriodo() + ".");
            return;
        }

        int diasNoPeriodo = resumoPeriodos.isEmpty() ? 0
                : (int) (resumoPeriodos.get(resumoPeriodos.size() - 1).getDataFim().toEpochDay()
                        - resumoPeriodos.get(0).getDataInicio().toEpochDay() + 1);

        PdfPTable destaques = new PdfPTable(4);
        destaques.setWidthPercentage(100);
        destaques.addCell(cartaoDestaque("MELHOR SEMANA",
                melhorSemana != null ? melhorSemana.getRotulo() : "—",
                melhorSemana != null ? moeda.format(melhorSemana.getFaturamento())
                        + " (" + intervalo(melhorSemana) + ")" : " "));
        destaques.addCell(cartaoDestaque("MELHOR DIA",
                melhorDia != null ? melhorDia.format(FMT_DIA_MES) + " (" + diaSemanaCurto(melhorDia) + ")" : "—",
                melhorDia != null ? moeda.format(valorMelhorDia) : " "));
        destaques.addCell(cartaoDestaque("MÉDIA POR DIA",
                moeda.format(dividir(totalMes, diasComVenda)), "considerando só dias com venda"));
        destaques.addCell(cartaoDestaque("DIAS COM VENDA",
                diasComVenda + " de " + diasNoPeriodo, "dias do " + palavraPeriodo()));
        destaques.setSpacingAfter(10);
        documento.add(destaques);

        PdfPTable tabela = new PdfPTable(new float[] {1.4f, 1.9f, 1.2f, 1.1f, 1.9f, 1.7f, 1.2f, 2.4f});
        tabela.setWidthPercentage(100);
        tabela.setHeaderRows(1);

        tabela.addCell(cabecalho("Semana", Element.ALIGN_LEFT));
        tabela.addCell(cabecalho("Período", Element.ALIGN_CENTER));
        tabela.addCell(cabecalho("Dias c/ venda", Element.ALIGN_CENTER));
        tabela.addCell(cabecalho("Vendas", Element.ALIGN_CENTER));
        tabela.addCell(cabecalho("Faturamento", Element.ALIGN_RIGHT));
        tabela.addCell(cabecalho("Ticket médio", Element.ALIGN_RIGHT));
        tabela.addCell(cabecalho("% do " + palavraPeriodo(), Element.ALIGN_RIGHT));
        tabela.addCell(cabecalho("Melhor dia", Element.ALIGN_CENTER));

        int linha = 0;
        for (ResumoPeriodo semana : resumoPeriodos) {
            Color fundo = linha % 2 == 1 ? ZEBRA : Color.WHITE;
            boolean semVenda = semana.getQuantidadeVendas() == 0;

            tabela.addCell(celula(semana.getRotulo(), F_TAB_NEGRITO, Element.ALIGN_LEFT, fundo));
            tabela.addCell(celula(intervalo(semana), F_TAB, Element.ALIGN_CENTER, fundo));
            tabela.addCell(celula(String.valueOf(semana.getDiasComVenda()), F_TAB, Element.ALIGN_CENTER, fundo));
            tabela.addCell(celula(String.valueOf(semana.getQuantidadeVendas()), F_TAB, Element.ALIGN_CENTER, fundo));
            tabela.addCell(celula(moeda.format(semana.getFaturamento()), F_TAB_NEGRITO, Element.ALIGN_RIGHT, fundo));
            tabela.addCell(celula(semVenda ? "—" : moeda.format(semana.getTicketMedio()), F_TAB, Element.ALIGN_RIGHT, fundo));
            tabela.addCell(celula(formatarPercentual(semana.getFaturamento(), totalMes), F_TAB_SUAVE, Element.ALIGN_RIGHT, fundo));
            tabela.addCell(celula(semana.getMelhorDia() != null
                    ? semana.getMelhorDia().format(FMT_DIA_MES) + " · " + moeda.format(semana.getFaturamentoMelhorDia())
                    : "—", F_TAB, Element.ALIGN_CENTER, fundo));
            linha++;
        }

        tabela.addCell(celula("Total", F_TAB_NEGRITO, Element.ALIGN_LEFT, VERDE_CLARO));
        tabela.addCell(celula(resumoPeriodos.isEmpty() ? "" : resumoPeriodos.get(0).getDataInicio().format(FMT_DIA_MES)
                + " a " + resumoPeriodos.get(resumoPeriodos.size() - 1).getDataFim().format(FMT_DIA_MES),
                F_TAB_NEGRITO, Element.ALIGN_CENTER, VERDE_CLARO));
        tabela.addCell(celula(String.valueOf(diasComVenda), F_TAB_NEGRITO, Element.ALIGN_CENTER, VERDE_CLARO));
        tabela.addCell(celula(String.valueOf(vendasMes), F_TAB_NEGRITO, Element.ALIGN_CENTER, VERDE_CLARO));
        tabela.addCell(celula(moeda.format(totalMes), F_TAB_NEGRITO, Element.ALIGN_RIGHT, VERDE_CLARO));
        tabela.addCell(celula(moeda.format(dividir(totalMes, vendasMes)), F_TAB_NEGRITO, Element.ALIGN_RIGHT, VERDE_CLARO));
        tabela.addCell(celula("100,0%", F_TAB_NEGRITO, Element.ALIGN_RIGHT, VERDE_CLARO));
        tabela.addCell(celula("", F_TAB_NEGRITO, Element.ALIGN_CENTER, VERDE_CLARO));

        tabela.setSpacingAfter(16);
        documento.add(tabela);

        adicionarTabelaProdutos(documento, "Produtos Mais Vendidos no " + capitalizar(palavraPeriodo()),
                "Os " + produtos.size() + " produtos com maior faturamento no " + palavraPeriodo()
                        + " (itens cancelados não entram).");
    }

    // ------------------------------------------------------------------
    // Página 2 – detalhamento de períodos longos (resumo mês a mês)
    // ------------------------------------------------------------------

    private void adicionarDetalhamentoAnual(Document documento) throws DocumentException {
        adicionarTituloSecao(documento,
                tipo == PeriodoRelatorio.Tipo.ANUAL ? "Resumo do Ano" : "Resumo por Mês",
                tipo == PeriodoRelatorio.Tipo.ANUAL
                        ? "Mês a mês, nos últimos 12 meses (o mês atual vai até hoje)."
                        : "Mês a mês, no período selecionado (o primeiro e o último mês podem estar incompletos).");

        BigDecimal totalAno = BigDecimal.ZERO;
        int vendasAno = 0;
        int mesesComVenda = 0;
        ResumoPeriodo melhorMes = null;
        ResumoPeriodo piorMes = null;

        for (ResumoPeriodo mes : resumoPeriodos) {
            totalAno = totalAno.add(mes.getFaturamento());
            vendasAno += mes.getQuantidadeVendas();
            if (mes.getFaturamento().signum() > 0) {
                mesesComVenda++;
                if (melhorMes == null || mes.getFaturamento().compareTo(melhorMes.getFaturamento()) > 0) {
                    melhorMes = mes;
                }
                if (piorMes == null || mes.getFaturamento().compareTo(piorMes.getFaturamento()) < 0) {
                    piorMes = mes;
                }
            }
        }

        if (vendasAno == 0) {
            adicionarMensagemVazia(documento, "Nenhuma venda registrada neste período.");
            return;
        }

        BigDecimal mediaMensal = dividir(totalAno, mesesComVenda);

        PdfPTable destaques = new PdfPTable(4);
        destaques.setWidthPercentage(100);
        destaques.addCell(cartaoDestaque("MELHOR MÊS", melhorMes.getRotulo(), moeda.format(melhorMes.getFaturamento())));
        destaques.addCell(cartaoDestaque("MÊS MAIS FRACO", piorMes.getRotulo(), moeda.format(piorMes.getFaturamento())));
        destaques.addCell(cartaoDestaque("MÉDIA MENSAL", moeda.format(mediaMensal),
                "em " + mesesComVenda + (mesesComVenda == 1 ? " mês" : " meses") + " com venda"));
        destaques.addCell(cartaoDestaque("VENDAS NO PERÍODO", String.valueOf(vendasAno),
                "ticket médio " + moeda.format(dividir(totalAno, vendasAno))));
        destaques.setSpacingAfter(10);
        documento.add(destaques);

        PdfPTable tabela = new PdfPTable(new float[] {2.3f, 1.1f, 1.2f, 2f, 1.7f, 1.7f, 1.6f, 1.3f});
        tabela.setWidthPercentage(100);
        tabela.setHeaderRows(1);

        tabela.addCell(cabecalho("Mês", Element.ALIGN_LEFT));
        tabela.addCell(cabecalho("Dias c/ venda", Element.ALIGN_CENTER));
        tabela.addCell(cabecalho("Vendas", Element.ALIGN_CENTER));
        tabela.addCell(cabecalho("Faturamento", Element.ALIGN_RIGHT));
        tabela.addCell(cabecalho("Ticket médio", Element.ALIGN_RIGHT));
        tabela.addCell(cabecalho("Média por dia", Element.ALIGN_RIGHT));
        tabela.addCell(cabecalho("Var. mês ant.", Element.ALIGN_RIGHT));
        tabela.addCell(cabecalho("% do período", Element.ALIGN_RIGHT));

        int linha = 0;
        BigDecimal faturamentoAnterior = null;
        int diasComVendaAno = 0;

        for (ResumoPeriodo mes : resumoPeriodos) {
            Color fundo = linha % 2 == 1 ? ZEBRA : Color.WHITE;
            boolean semVenda = mes.getQuantidadeVendas() == 0;

            tabela.addCell(celula(mes.getRotulo(), F_TAB_NEGRITO, Element.ALIGN_LEFT, fundo));
            tabela.addCell(celula(String.valueOf(mes.getDiasComVenda()), F_TAB, Element.ALIGN_CENTER, fundo));
            tabela.addCell(celula(String.valueOf(mes.getQuantidadeVendas()), F_TAB, Element.ALIGN_CENTER, fundo));
            tabela.addCell(celula(moeda.format(mes.getFaturamento()), F_TAB_NEGRITO, Element.ALIGN_RIGHT, fundo));
            tabela.addCell(celula(semVenda ? "—" : moeda.format(mes.getTicketMedio()), F_TAB, Element.ALIGN_RIGHT, fundo));
            tabela.addCell(celula(semVenda ? "—" : moeda.format(mes.getMediaDiaria()), F_TAB, Element.ALIGN_RIGHT, fundo));
            tabela.addCell(celulaVariacao(faturamentoAnterior, mes.getFaturamento(), fundo));
            tabela.addCell(celula(formatarPercentual(mes.getFaturamento(), totalAno), F_TAB_SUAVE, Element.ALIGN_RIGHT, fundo));

            faturamentoAnterior = mes.getFaturamento();
            diasComVendaAno += mes.getDiasComVenda();
            linha++;
        }

        tabela.addCell(celula("Total", F_TAB_NEGRITO, Element.ALIGN_LEFT, VERDE_CLARO));
        tabela.addCell(celula(String.valueOf(diasComVendaAno), F_TAB_NEGRITO, Element.ALIGN_CENTER, VERDE_CLARO));
        tabela.addCell(celula(String.valueOf(vendasAno), F_TAB_NEGRITO, Element.ALIGN_CENTER, VERDE_CLARO));
        tabela.addCell(celula(moeda.format(totalAno), F_TAB_NEGRITO, Element.ALIGN_RIGHT, VERDE_CLARO));
        tabela.addCell(celula(moeda.format(dividir(totalAno, vendasAno)), F_TAB_NEGRITO, Element.ALIGN_RIGHT, VERDE_CLARO));
        tabela.addCell(celula(moeda.format(dividir(totalAno, diasComVendaAno)), F_TAB_NEGRITO, Element.ALIGN_RIGHT, VERDE_CLARO));
        tabela.addCell(celula("", F_TAB_NEGRITO, Element.ALIGN_RIGHT, VERDE_CLARO));
        tabela.addCell(celula("100,0%", F_TAB_NEGRITO, Element.ALIGN_RIGHT, VERDE_CLARO));

        tabela.addCell(celula("Média mensal", F_TAB_NEGRITO, Element.ALIGN_LEFT, BEGE));
        tabela.addCell(celula(decimal.format((double) diasComVendaAno / Math.max(mesesComVenda, 1)),
                F_TAB, Element.ALIGN_CENTER, BEGE));
        tabela.addCell(celula(decimal.format((double) vendasAno / Math.max(mesesComVenda, 1)),
                F_TAB, Element.ALIGN_CENTER, BEGE));
        tabela.addCell(celula(moeda.format(mediaMensal), F_TAB_NEGRITO, Element.ALIGN_RIGHT, BEGE));
        PdfPCell observacao = celula("considera só os meses com venda", F_TAB_SUAVE, Element.ALIGN_RIGHT, BEGE);
        observacao.setColspan(4);
        tabela.addCell(observacao);

        tabela.setSpacingAfter(16);
        documento.add(tabela);

        adicionarTabelaProdutos(documento,
                tipo == PeriodoRelatorio.Tipo.ANUAL ? "Produtos Mais Vendidos no Ano" : "Produtos Mais Vendidos no Período",
                "Os " + produtos.size() + " produtos com maior faturamento no período (itens cancelados não entram).");
    }

    // ------------------------------------------------------------------
    // Tabela de produtos (usada pelos três detalhamentos)
    // ------------------------------------------------------------------

    private void adicionarTabelaProdutos(Document documento, String titulo, String descricao) throws DocumentException {
        if (produtos.isEmpty()) {
            return;
        }

        adicionarTituloSecao(documento, titulo, descricao);

        BigDecimal totalProdutos = somar(produtos.stream().map(ProdutoVendido::getTotal).toList());
        int quantidadeTotal = produtos.stream().mapToInt(ProdutoVendido::getQuantidade).sum();

        PdfPTable tabela = new PdfPTable(new float[] {0.7f, 4.2f, 2.4f, 1.2f, 1.9f, 1.9f, 1.3f});
        tabela.setWidthPercentage(100);
        tabela.setHeaderRows(1);

        tabela.addCell(cabecalho("#", Element.ALIGN_CENTER));
        tabela.addCell(cabecalho("Produto", Element.ALIGN_LEFT));
        tabela.addCell(cabecalho("Categoria", Element.ALIGN_LEFT));
        tabela.addCell(cabecalho("Qtd.", Element.ALIGN_CENTER));
        tabela.addCell(cabecalho("Preço médio", Element.ALIGN_RIGHT));
        tabela.addCell(cabecalho("Total", Element.ALIGN_RIGHT));
        tabela.addCell(cabecalho("%", Element.ALIGN_RIGHT));

        int posicao = 1;
        for (ProdutoVendido produto : produtos) {
            Color fundo = posicao % 2 == 0 ? ZEBRA : Color.WHITE;

            tabela.addCell(celula(posicao + "º", F_TAB_SUAVE, Element.ALIGN_CENTER, fundo));
            tabela.addCell(celula(produto.getNome(), F_TAB_NEGRITO, Element.ALIGN_LEFT, fundo));
            tabela.addCell(celula(textoOuTraco(produto.getCategoria()), F_TAB, Element.ALIGN_LEFT, fundo));
            tabela.addCell(celula(String.valueOf(produto.getQuantidade()), F_TAB, Element.ALIGN_CENTER, fundo));
            tabela.addCell(celula(moeda.format(dividir(produto.getTotal(), produto.getQuantidade())),
                    F_TAB, Element.ALIGN_RIGHT, fundo));
            tabela.addCell(celula(moeda.format(produto.getTotal()), F_TAB_NEGRITO, Element.ALIGN_RIGHT, fundo));
            tabela.addCell(celula(formatarPercentual(produto.getTotal(), totalProdutos), F_TAB_SUAVE, Element.ALIGN_RIGHT, fundo));
            posicao++;
        }

        PdfPCell rotulo = celula("Total", F_TAB_NEGRITO, Element.ALIGN_LEFT, VERDE_CLARO);
        rotulo.setColspan(3);
        tabela.addCell(rotulo);
        tabela.addCell(celula(String.valueOf(quantidadeTotal), F_TAB_NEGRITO, Element.ALIGN_CENTER, VERDE_CLARO));
        tabela.addCell(celula("", F_TAB_NEGRITO, Element.ALIGN_RIGHT, VERDE_CLARO));
        tabela.addCell(celula(moeda.format(totalProdutos), F_TAB_NEGRITO, Element.ALIGN_RIGHT, VERDE_CLARO));
        tabela.addCell(celula("100,0%", F_TAB_NEGRITO, Element.ALIGN_RIGHT, VERDE_CLARO));

        documento.add(tabela);
    }

    // ------------------------------------------------------------------
    // Blocos visuais reutilizáveis
    // ------------------------------------------------------------------

    private void adicionarTituloSecao(Document documento, String titulo, String descricao) throws DocumentException {
        Paragraph pTitulo = new Paragraph(titulo, F_SECAO);
        pTitulo.setSpacingBefore(2);
        documento.add(pTitulo);

        Paragraph pDescricao = new Paragraph(descricao, F_SECAO_DESC);
        pDescricao.setSpacingBefore(1);
        pDescricao.setSpacingAfter(8);
        documento.add(pDescricao);
    }

    private void adicionarMensagemVazia(Document documento, String mensagem) throws DocumentException {
        Paragraph vazio = new Paragraph(mensagem, F_VAZIO);
        vazio.setSpacingBefore(20);
        vazio.setAlignment(Element.ALIGN_CENTER);
        documento.add(vazio);
    }

    /**
     * Linha de comparação dos cartões, ex.: "+12,3% vs. agosto/2026" (verde),
     * "-5,1% vs. sex. 18/09" (vermelho). Sem período anterior, mostra o texto padrão.
     */
    private Phrase comparacao(BigDecimal atual, BigDecimal anteriorValor, String textoPadrao) {
        if (!dados.temComparacao()) {
            return new Phrase(textoPadrao, F_KPI_SUB);
        }
        String referencia = " vs. " + dados.getPeriodoAnterior().getDescricaoComparacao();
        BigDecimal variacao = DadosRelatorio.variacaoPercentual(atual, anteriorValor);
        if (variacao == null) {
            return new Phrase("sem vendas em " + dados.getPeriodoAnterior().getDescricaoComparacao(), F_KPI_SUB);
        }

        Font fonteVariacao = variacao.signum() > 0 ? F_KPI_SUB_POSITIVO
                : variacao.signum() < 0 ? F_KPI_SUB_NEGATIVO : F_KPI_SUB;
        Phrase frase = new Phrase();
        frase.add(new com.lowagie.text.Chunk(DadosRelatorio.textoVariacao(atual, anteriorValor), fonteVariacao));
        frase.add(new com.lowagie.text.Chunk(referencia, F_KPI_SUB));
        return frase;
    }

    private PdfPCell cartaoIndicador(String rotulo, String valor, Phrase subtitulo) {
        PdfPCell cartao = cartao(BEGE, null);
        cartao.addElement(new Paragraph(rotulo, F_KPI_ROTULO));
        // textos longos (ex.: "Cartão de Crédito") usam fonte menor pra caber numa linha
        Paragraph pValor = new Paragraph(valor, valor.length() > 12 ? F_KPI_VALOR_LONGO : F_KPI_VALOR);
        pValor.setSpacingBefore(3);
        cartao.addElement(pValor);
        Paragraph pSub = new Paragraph(subtitulo);
        pSub.setSpacingBefore(1);
        cartao.addElement(pSub);
        return cartao;
    }

    private PdfPCell cartaoDestaque(String rotulo, String valor, String subtitulo) {
        PdfPCell cartao = cartao(Color.WHITE, BORDA);
        cartao.addElement(new Paragraph(rotulo, F_KPI_ROTULO));
        Paragraph pValor = new Paragraph(valor, F_DESTAQUE_VALOR);
        pValor.setSpacingBefore(2);
        cartao.addElement(pValor);
        Paragraph pSub = new Paragraph(subtitulo, F_KPI_SUB);
        pSub.setSpacingBefore(1);
        cartao.addElement(pSub);
        return cartao;
    }

    private PdfPCell cartaoGrafico(String titulo, byte[] png, float alturaImagem) {
        PdfPCell cartao = cartao(Color.WHITE, BORDA);
        cartao.addElement(new Paragraph(titulo, F_CARD_TITULO));

        Image imagem = null;
        if (png != null) {
            try {
                imagem = Image.getInstance(png);
            } catch (IOException | DocumentException e) {
                imagem = null;
            }
        }

        if (imagem != null) {
            // largura 100% do cartão; a altura acompanha a proporção da imagem
            imagem.setWidthPercentage(100);
            imagem.setSpacingBefore(4);
            cartao.addElement(imagem);
        } else {
            Paragraph vazio = new Paragraph("Sem dados no período.", F_VAZIO);
            vazio.setSpacingBefore(alturaImagem / 2 - 10);
            vazio.setAlignment(Element.ALIGN_CENTER);
            cartao.addElement(vazio);
        }
        cartao.setMinimumHeight(alturaImagem + 30);
        return cartao;
    }

    /** Célula sem borda com fundo arredondado desenhado por um evento (efeito de "card"). */
    private PdfPCell cartao(Color fundo, Color borda) {
        PdfPCell cartao = semBorda(new PdfPCell());
        // Card branco = só o contorno. Um preenchimento branco seria desenhado
        // DEPOIS das tabelas internas (ex.: valores por forma de pagamento) e
        // apagaria o fundo verde do cabeçalho e as linhas zebradas delas.
        Color preenchimento = Color.WHITE.equals(fundo) ? null : fundo;
        cartao.setCellEvent(new FundoArredondado(preenchimento, borda, 3, 8));
        cartao.setPaddingTop(11);
        cartao.setPaddingBottom(11);
        cartao.setPaddingLeft(12);
        cartao.setPaddingRight(12);
        return cartao;
    }

    private PdfPCell cabecalho(String texto, int alinhamento) {
        PdfPCell celula = new PdfPCell(new Phrase(texto, F_TAB_CABECALHO));
        celula.setBackgroundColor(VERDE);
        celula.setBorder(Rectangle.BOX); // borda da mesma cor evita "frestas" brancas entre as células
        celula.setBorderColor(VERDE);
        celula.setBorderWidth(0.5f);
        celula.setHorizontalAlignment(alinhamento);
        celula.setVerticalAlignment(Element.ALIGN_MIDDLE);
        celula.setPaddingTop(5);
        celula.setPaddingBottom(6);
        celula.setPaddingLeft(4);
        celula.setPaddingRight(4);
        return celula;
    }

    private PdfPCell celula(String texto, Font fonte, int alinhamento, Color fundo) {
        PdfPCell celula = new PdfPCell(new Phrase(texto, fonte));
        celula.setBackgroundColor(fundo);
        celula.setBorder(Rectangle.BOTTOM);
        celula.setBorderColor(BORDA);
        celula.setBorderWidth(0.5f);
        celula.setHorizontalAlignment(alinhamento);
        celula.setVerticalAlignment(Element.ALIGN_MIDDLE);
        celula.setPaddingTop(4);
        celula.setPaddingBottom(5);
        celula.setPaddingLeft(4);
        celula.setPaddingRight(4);
        return celula;
    }

    private PdfPCell celulaVariacao(BigDecimal anterior, BigDecimal atual, Color fundo) {
        if (anterior == null || anterior.signum() == 0) {
            return celula("—", F_TAB_SUAVE, Element.ALIGN_RIGHT, fundo);
        }
        BigDecimal variacao = atual.subtract(anterior)
                .multiply(BigDecimal.valueOf(100))
                .divide(anterior, 1, RoundingMode.HALF_UP);
        String texto = (variacao.signum() > 0 ? "+" : "") + percentual.format(variacao);
        Font fonte = variacao.signum() > 0 ? F_TAB_POSITIVO
                : variacao.signum() < 0 ? F_TAB_NEGATIVO : F_TAB_SUAVE;
        return celula(texto, fonte, Element.ALIGN_RIGHT, fundo);
    }

    private static PdfPCell semBorda(PdfPCell celula) {
        celula.setBorder(Rectangle.NO_BORDER);
        return celula;
    }

    /**
     * Desenha um retângulo arredondado atrás da célula (o PdfPCell só
     * suporta cantos retos). A margem cria o espaçamento entre os cards.
     */
    private static class FundoArredondado implements PdfPCellEvent {

        private final Color fundo;
        private final Color borda;
        private final float margem;
        private final float raio;

        FundoArredondado(Color fundo, Color borda, float margem, float raio) {
            this.fundo = fundo;
            this.borda = borda;
            this.margem = margem;
            this.raio = raio;
        }

        @Override
        public void cellLayout(PdfPCell cell, Rectangle posicao, PdfContentByte[] canvases) {
            PdfContentByte cb = canvases[PdfPTable.BACKGROUNDCANVAS];
            cb.saveState();
            cb.roundRectangle(
                    posicao.getLeft() + margem,
                    posicao.getBottom() + margem,
                    posicao.getWidth() - 2 * margem,
                    posicao.getHeight() - 2 * margem,
                    raio);
            if (fundo != null) {
                cb.setColorFill(fundo);
            }
            if (borda != null) {
                cb.setColorStroke(borda);
                cb.setLineWidth(0.8f);
            }
            if (fundo != null && borda != null) {
                cb.fillStroke();
            } else if (fundo != null) {
                cb.fill();
            } else {
                cb.stroke();
            }
            cb.restoreState();
        }
    }

    /** Bolinha colorida (legenda) centralizada na célula. */
    private static class MarcadorCor implements PdfPCellEvent {

        private final Color cor;

        MarcadorCor(Color cor) {
            this.cor = cor;
        }

        @Override
        public void cellLayout(PdfPCell cell, Rectangle posicao, PdfContentByte[] canvases) {
            PdfContentByte cb = canvases[PdfPTable.TEXTCANVAS];
            cb.saveState();
            cb.setColorFill(cor);
            cb.circle(
                    (posicao.getLeft() + posicao.getRight()) / 2,
                    (posicao.getBottom() + posicao.getTop()) / 2,
                    3.2f);
            cb.fill();
            cb.restoreState();
        }
    }

    // ------------------------------------------------------------------
    // Utilitários
    // ------------------------------------------------------------------

    /**
     * Desenha o gráfico num BufferedImage em alta resolução e devolve o PNG.
     * O tamanho lógico (largura x altura) é parecido com o do card na tela,
     * pra que fontes e proporções fiquem iguais às do sistema.
     */
    private static byte[] renderizarGrafico(JFreeChart chart, int largura, int altura) {
        if (chart == null || semDados(chart)) {
            return null; // o card mostra "Sem dados no período." no lugar do gráfico vazio
        }
        BufferedImage imagem = new BufferedImage(
                largura * ESCALA_IMAGEM, altura * ESCALA_IMAGEM, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = imagem.createGraphics();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
            g2.setColor(Color.WHITE);
            g2.fillRect(0, 0, imagem.getWidth(), imagem.getHeight());
            g2.scale(ESCALA_IMAGEM, ESCALA_IMAGEM);
            chart.draw(g2, new Rectangle2D.Double(0, 0, largura, altura));
        } finally {
            g2.dispose();
        }

        try {
            ByteArrayOutputStream png = new ByteArrayOutputStream();
            javax.imageio.ImageIO.write(imagem, "png", png);
            return png.toByteArray();
        } catch (IOException e) {
            return null;
        }
    }

    private static boolean semDados(JFreeChart chart) {
        if (chart.getPlot() instanceof CategoryPlot plot) {
            // sem pontos, ou só pontos zerados (ex.: evolução de um mês sem vendas)
            org.jfree.data.category.CategoryDataset dados = plot.getDataset();
            if (dados == null) {
                return true;
            }
            for (int linha = 0; linha < dados.getRowCount(); linha++) {
                for (int coluna = 0; coluna < dados.getColumnCount(); coluna++) {
                    Number valor = dados.getValue(linha, coluna);
                    if (valor != null && valor.doubleValue() != 0) {
                        return false;
                    }
                }
            }
            return true;
        }
        if (chart.getPlot() instanceof PiePlot<?> plot) {
            return plot.getDataset() == null || plot.getDataset().getItemCount() == 0;
        }
        return false;
    }

    /**
     * O card do PDF é mais estreito que o da tela; numa CÓPIA do gráfico de
     * ranking (a da tela não é alterada) dá mais folga à direita para o
     * rótulo de valor da maior barra e mais espaço para o nome do produto.
     */
    private static JFreeChart ajustarRankingParaPdf(JFreeChart chart) {
        if (chart == null) {
            return null;
        }
        try {
            JFreeChart copia = (JFreeChart) chart.clone();
            if (copia.getPlot() instanceof CategoryPlot plot) {
                if (plot.getRangeAxis() != null) {
                    plot.getRangeAxis().setUpperMargin(0.30);
                }
                if (plot.getDomainAxis() != null) {
                    plot.getDomainAxis().setMaximumCategoryLabelWidthRatio(0.40f);
                }
                plot.setInsets(new RectangleInsets(4, 4, 4, 16));
            }
            return copia;
        } catch (CloneNotSupportedException | RuntimeException e) {
            return chart; // sem cópia, usa o gráfico como está na tela
        }
    }

    private Image carregarLogo() {
        try {
            URL url = RelatorioPdfExporter.class.getResource("/TrevizanPequeno.png");
            return url != null ? Image.getInstance(url) : null;
        } catch (IOException | DocumentException e) {
            return null;
        }
    }

    private static Color corFormaPagamento(String forma) {
        // mesmas cores do gráfico "Por Forma de Pagamento" da tela
        return switch (forma) {
            case "Cartão de Crédito" -> new Color(230, 140, 60);
            case "Cartão de Débito" -> VERMELHO;
            case "Dinheiro" -> VERDE;
            case "Pix" -> new Color(45, 110, 150);
            default -> new Color(150, 150, 150);
        };
    }

    private String formatarPercentual(BigDecimal parte, BigDecimal total) {
        if (total == null || total.signum() == 0 || parte == null) {
            return "—";
        }
        BigDecimal pct = parte.multiply(BigDecimal.valueOf(100)).divide(total, 1, RoundingMode.HALF_UP);
        return percentual.format(pct);
    }

    private static BigDecimal dividir(BigDecimal valor, int divisor) {
        if (valor == null || divisor == 0) {
            return BigDecimal.ZERO;
        }
        return valor.divide(BigDecimal.valueOf(divisor), 2, RoundingMode.HALF_UP);
    }

    private static BigDecimal somar(Iterable<BigDecimal> valores) {
        BigDecimal soma = BigDecimal.ZERO;
        for (BigDecimal valor : valores) {
            if (valor != null) {
                soma = soma.add(valor);
            }
        }
        return soma;
    }

    private static String formatarHora(LocalDateTime dataHora) {
        return dataHora != null ? dataHora.format(FMT_HORA) : "—";
    }

    private static String formatarDuracao(Duration duracao) {
        if (duracao == null || duracao.isNegative()) {
            return "—";
        }
        long minutos = duracao.toMinutes();
        if (minutos < 60) {
            return minutos + " min";
        }
        return String.format("%dh%02d", minutos / 60, minutos % 60);
    }

    private static String textoOuTraco(String texto) {
        return texto == null || texto.isBlank() ? "—" : texto;
    }

    private static String intervalo(ResumoPeriodo periodo) {
        if (periodo.getDataInicio().equals(periodo.getDataFim())) {
            return periodo.getDataInicio().format(FMT_DIA_MES);
        }
        return periodo.getDataInicio().format(FMT_DIA_MES) + " a " + periodo.getDataFim().format(FMT_DIA_MES);
    }

    private static String capitalizar(String texto) {
        return texto.isEmpty() ? texto : Character.toUpperCase(texto.charAt(0)) + texto.substring(1);
    }

    private static String diaSemanaCurto(LocalDate data) {
        String nome = data.getDayOfWeek().getDisplayName(java.time.format.TextStyle.SHORT, PT_BR);
        return nome.replace(".", "");
    }

    /** Usado no nome sugerido do arquivo e no cabeçalho. */
    public static String formatarData(LocalDate data) {
        return data.format(FMT_DATA);
    }
}
