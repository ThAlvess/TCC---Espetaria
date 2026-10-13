package br.com.trevizan.espetinhos.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Gerador simples de planilhas Excel (.xlsx), sem dependências externas.
 *
 * Um arquivo .xlsx é um ZIP com alguns XMLs (formato Office Open XML). Esta
 * classe escreve só o necessário para o relatório: várias abas, textos,
 * números, datas/horas, fórmulas (ex.: SUM nos totais), estilos fixos
 * (moeda, percentual, cabeçalho verde, linha de total), largura das colunas e
 * linhas de cabeçalho congeladas. Abre normalmente no Excel, LibreOffice e
 * Google Planilhas.
 *
 * Exemplo:
 * <pre>
 * PlanilhaXlsx planilha = new PlanilhaXlsx();
 * PlanilhaXlsx.Aba aba = planilha.novaAba("Produtos");
 * aba.linha(texto("Produto", Estilo.CABECALHO), texto("Total", Estilo.CABECALHO));
 * aba.linha(texto("Espeto", Estilo.PADRAO), numero(new BigDecimal("12.50"), Estilo.MOEDA));
 * planilha.salvar(new File("produtos.xlsx"));
 * </pre>
 */
public class PlanilhaXlsx {

    /**
     * Estilos disponíveis. A ORDEM precisa bater com a lista &lt;cellXfs&gt;
     * gerada em {@link #xmlEstilos()} (o índice do enum é o índice do estilo).
     */
    public enum Estilo {
        PADRAO,
        TITULO,
        SUBTITULO,
        CABECALHO,
        TEXTO_NEGRITO,
        INTEIRO,
        MOEDA,
        PERCENTUAL,
        VARIACAO,
        HORA,
        DECIMAL,
        DATA,
        TOTAL_TEXTO,
        TOTAL_INTEIRO,
        TOTAL_MOEDA,
        TOTAL_PERCENTUAL,
        TOTAL_DECIMAL
    }

    /** Uma célula: texto, número ou fórmula (com o valor já calculado). */
    public static final class Celula {
        private final String texto;
        private final String numero;
        private final String formula;
        private final Estilo estilo;

        private Celula(String texto, String numero, String formula, Estilo estilo) {
            this.texto = texto;
            this.numero = numero;
            this.formula = formula;
            this.estilo = estilo;
        }
    }

    public static Celula texto(String texto, Estilo estilo) {
        return new Celula(texto == null ? "" : texto, null, null, estilo);
    }

    public static Celula numero(Number valor, Estilo estilo) {
        if (valor == null) {
            return vazio(estilo);
        }
        String numero = valor instanceof BigDecimal decimal
                ? decimal.stripTrailingZeros().toPlainString()
                : valor.toString();
        return new Celula(null, numero, null, estilo);
    }

    /**
     * Fórmula do Excel (sem o "="), ex.: "SUM(B2:B9)". O valor calculado vai
     * junto para aparecer mesmo em leitores que não recalculam.
     */
    public static Celula formula(String formula, Number valorCalculado, Estilo estilo) {
        Celula base = numero(valorCalculado, estilo);
        return new Celula(null, base.numero, formula, estilo);
    }

    /** Data/hora como número de série do Excel (formatado pelo estilo, ex.: HORA ou DATA). */
    public static Celula dataHora(LocalDateTime dataHora, Estilo estilo) {
        if (dataHora == null) {
            return vazio(estilo);
        }
        LocalDateTime base = LocalDate.of(1899, 12, 30).atStartOfDay();
        double dias = Duration.between(base, dataHora).getSeconds() / 86400.0;
        return new Celula(null, BigDecimal.valueOf(dias).setScale(8, java.math.RoundingMode.HALF_UP)
                .stripTrailingZeros().toPlainString(), null, estilo);
    }

    public static Celula data(LocalDate data, Estilo estilo) {
        return data == null ? vazio(estilo) : dataHora(data.atStartOfDay(), estilo);
    }

    public static Celula vazio(Estilo estilo) {
        return new Celula(null, null, null, estilo);
    }

    /** Uma aba (worksheet) da planilha. */
    public final class Aba {
        private final String nome;
        private final List<Celula[]> linhas = new ArrayList<>();
        private double[] larguras = new double[0];
        private int linhasCongeladas = 0;

        private Aba(String nome) {
            this.nome = nome;
        }

        public Aba linha(Celula... celulas) {
            linhas.add(celulas);
            return this;
        }

        public Aba linhaVazia() {
            linhas.add(new Celula[0]);
            return this;
        }

        /** Número (1, 2, 3...) que a PRÓXIMA linha adicionada terá — útil para montar fórmulas. */
        public int proximaLinha() {
            return linhas.size() + 1;
        }

        /** Largura das colunas A, B, C..., em "caracteres" (unidade do Excel). */
        public Aba larguras(double... larguras) {
            this.larguras = larguras;
            return this;
        }

        /** Mantém as primeiras N linhas fixas ao rolar (cabeçalho da tabela). */
        public Aba congelarLinhas(int quantidade) {
            this.linhasCongeladas = quantidade;
            return this;
        }
    }

    private final List<Aba> abas = new ArrayList<>();
    private String titulo = "";
    private String autor = "";

    public Aba novaAba(String nome) {
        // o Excel limita o nome da aba a 31 caracteres e proíbe alguns símbolos
        String limpo = nome.replaceAll("[\\\\/?*\\[\\]:]", "-");
        Aba aba = new Aba(limpo.length() > 31 ? limpo.substring(0, 31) : limpo);
        abas.add(aba);
        return aba;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo == null ? "" : titulo;
    }

    public void setAutor(String autor) {
        this.autor = autor == null ? "" : autor;
    }

    /** Letra(s) da coluna: 1 -> A, 2 -> B, ..., 27 -> AA. */
    public static String coluna(int numero) {
        StringBuilder letras = new StringBuilder();
        while (numero > 0) {
            int resto = (numero - 1) % 26;
            letras.insert(0, (char) ('A' + resto));
            numero = (numero - 1) / 26;
        }
        return letras.toString();
    }

    // ------------------------------------------------------------------
    // Gravação do arquivo
    // ------------------------------------------------------------------

    public void salvar(File arquivo) throws IOException {
        try (OutputStream saida = new FileOutputStream(arquivo)) {
            salvar(saida);
        }
    }

    public void salvar(OutputStream saida) throws IOException {
        if (abas.isEmpty()) {
            novaAba("Planilha1");
        }
        ZipOutputStream zip = new ZipOutputStream(saida, StandardCharsets.UTF_8);

        adicionar(zip, "[Content_Types].xml", xmlTiposConteudo());
        adicionar(zip, "_rels/.rels", xmlRelacoesRaiz());
        adicionar(zip, "docProps/core.xml", xmlPropriedades());
        adicionar(zip, "xl/workbook.xml", xmlPastaDeTrabalho());
        adicionar(zip, "xl/_rels/workbook.xml.rels", xmlRelacoesPasta());
        adicionar(zip, "xl/styles.xml", xmlEstilos());
        for (int i = 0; i < abas.size(); i++) {
            adicionar(zip, "xl/worksheets/sheet" + (i + 1) + ".xml", xmlAba(abas.get(i)));
        }

        zip.finish();
    }

    private static void adicionar(ZipOutputStream zip, String caminho, String conteudo) throws IOException {
        zip.putNextEntry(new ZipEntry(caminho));
        zip.write(conteudo.getBytes(StandardCharsets.UTF_8));
        zip.closeEntry();
    }

    private static final String CABECALHO_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n";
    private static final String NS_MAIN = "http://schemas.openxmlformats.org/spreadsheetml/2006/main";
    private static final String NS_REL = "http://schemas.openxmlformats.org/officeDocument/2006/relationships";

    private String xmlTiposConteudo() {
        StringBuilder xml = new StringBuilder(CABECALHO_XML);
        xml.append("<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\">");
        xml.append("<Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/>");
        xml.append("<Default Extension=\"xml\" ContentType=\"application/xml\"/>");
        xml.append("<Override PartName=\"/xl/workbook.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml\"/>");
        xml.append("<Override PartName=\"/xl/styles.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml\"/>");
        xml.append("<Override PartName=\"/docProps/core.xml\" ContentType=\"application/vnd.openxmlformats-package.core-properties+xml\"/>");
        for (int i = 1; i <= abas.size(); i++) {
            xml.append("<Override PartName=\"/xl/worksheets/sheet").append(i)
                    .append(".xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml\"/>");
        }
        xml.append("</Types>");
        return xml.toString();
    }

    private String xmlRelacoesRaiz() {
        return CABECALHO_XML
                + "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">"
                + "<Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument\" Target=\"xl/workbook.xml\"/>"
                + "<Relationship Id=\"rId2\" Type=\"http://schemas.openxmlformats.org/package/2006/relationships/metadata/core-properties\" Target=\"docProps/core.xml\"/>"
                + "</Relationships>";
    }

    private String xmlPropriedades() {
        return CABECALHO_XML
                + "<cp:coreProperties xmlns:cp=\"http://schemas.openxmlformats.org/package/2006/metadata/core-properties\""
                + " xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:dcterms=\"http://purl.org/dc/terms/\""
                + " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">"
                + "<dc:title>" + escapar(titulo) + "</dc:title>"
                + "<dc:creator>" + escapar(autor) + "</dc:creator>"
                + "</cp:coreProperties>";
    }

    private String xmlPastaDeTrabalho() {
        StringBuilder xml = new StringBuilder(CABECALHO_XML);
        xml.append("<workbook xmlns=\"").append(NS_MAIN).append("\" xmlns:r=\"").append(NS_REL).append("\"><sheets>");
        for (int i = 0; i < abas.size(); i++) {
            xml.append("<sheet name=\"").append(escapar(abas.get(i).nome)).append("\" sheetId=\"").append(i + 1)
                    .append("\" r:id=\"rId").append(i + 1).append("\"/>");
        }
        // fullCalcOnLoad: o Excel recalcula as fórmulas (totais) ao abrir
        xml.append("</sheets><calcPr calcId=\"191029\" fullCalcOnLoad=\"1\"/></workbook>");
        return xml.toString();
    }

    private String xmlRelacoesPasta() {
        StringBuilder xml = new StringBuilder(CABECALHO_XML);
        xml.append("<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">");
        for (int i = 1; i <= abas.size(); i++) {
            xml.append("<Relationship Id=\"rId").append(i)
                    .append("\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet\" Target=\"worksheets/sheet")
                    .append(i).append(".xml\"/>");
        }
        xml.append("<Relationship Id=\"rId").append(abas.size() + 1)
                .append("\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles\" Target=\"styles.xml\"/>");
        xml.append("</Relationships>");
        return xml.toString();
    }

    /**
     * Estilos fixos. Fontes: 0 normal, 1 negrito, 2 título (verde 14),
     * 3 subtítulo (cinza itálico), 4 negrito branco. Preenchimentos: 2 verde
     * (cabeçalho), 3 verde-claro (total). Formatos: 164 moeda, 165 %,
     * 166 variação com sinal, 167 hora, 168 decimal, 169 data.
     */
    private String xmlEstilos() {
        String verde = "FF196419";
        String cinza = "FF707070";
        return CABECALHO_XML
                + "<styleSheet xmlns=\"" + NS_MAIN + "\">"
                + "<numFmts count=\"6\">"
                + "<numFmt numFmtId=\"164\" formatCode=\"&quot;R$&quot;\\ #,##0.00\"/>"
                + "<numFmt numFmtId=\"165\" formatCode=\"0.0%\"/>"
                + "<numFmt numFmtId=\"166\" formatCode=\"+0.0%;\\-0.0%;0.0%\"/>"
                + "<numFmt numFmtId=\"167\" formatCode=\"hh:mm\"/>"
                + "<numFmt numFmtId=\"168\" formatCode=\"0.0\"/>"
                + "<numFmt numFmtId=\"169\" formatCode=\"dd/mm/yyyy\"/>"
                + "</numFmts>"
                + "<fonts count=\"5\">"
                + "<font><sz val=\"11\"/><name val=\"Calibri\"/></font>"
                + "<font><b/><sz val=\"11\"/><name val=\"Calibri\"/></font>"
                + "<font><b/><sz val=\"14\"/><color rgb=\"" + verde + "\"/><name val=\"Calibri\"/></font>"
                + "<font><i/><sz val=\"10\"/><color rgb=\"" + cinza + "\"/><name val=\"Calibri\"/></font>"
                + "<font><b/><sz val=\"11\"/><color rgb=\"FFFFFFFF\"/><name val=\"Calibri\"/></font>"
                + "</fonts>"
                + "<fills count=\"4\">"
                + "<fill><patternFill patternType=\"none\"/></fill>"
                + "<fill><patternFill patternType=\"gray125\"/></fill>"
                + "<fill><patternFill patternType=\"solid\"><fgColor rgb=\"" + verde + "\"/><bgColor indexed=\"64\"/></patternFill></fill>"
                + "<fill><patternFill patternType=\"solid\"><fgColor rgb=\"FFE8F1E8\"/><bgColor indexed=\"64\"/></patternFill></fill>"
                + "</fills>"
                + "<borders count=\"2\">"
                + "<border><left/><right/><top/><bottom/><diagonal/></border>"
                + "<border><left/><right/><top/><bottom style=\"thin\"><color rgb=\"FFE1DCD5\"/></bottom><diagonal/></border>"
                + "</borders>"
                + "<cellStyleXfs count=\"1\"><xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\"/></cellStyleXfs>"
                + "<cellXfs count=\"" + Estilo.values().length + "\">"
                + xf(0, 0, 0, 0, "")                                         // PADRAO
                + xf(0, 2, 0, 0, "")                                         // TITULO
                + xf(0, 3, 0, 0, "")                                         // SUBTITULO
                + xf(0, 4, 2, 0, "<alignment vertical=\"center\" wrapText=\"1\"/>") // CABECALHO
                + xf(0, 1, 0, 0, "")                                         // TEXTO_NEGRITO
                + xf(1, 0, 0, 1, "")                                         // INTEIRO
                + xf(164, 0, 0, 1, "")                                       // MOEDA
                + xf(165, 0, 0, 1, "")                                       // PERCENTUAL
                + xf(166, 0, 0, 1, "")                                       // VARIACAO
                + xf(167, 0, 0, 1, "<alignment horizontal=\"center\"/>")     // HORA
                + xf(168, 0, 0, 1, "")                                       // DECIMAL
                + xf(169, 0, 0, 1, "<alignment horizontal=\"center\"/>")     // DATA
                + xf(0, 1, 3, 0, "")                                         // TOTAL_TEXTO
                + xf(1, 1, 3, 0, "")                                         // TOTAL_INTEIRO
                + xf(164, 1, 3, 0, "")                                       // TOTAL_MOEDA
                + xf(165, 1, 3, 0, "")                                       // TOTAL_PERCENTUAL
                + xf(168, 1, 3, 0, "")                                       // TOTAL_DECIMAL
                + "</cellXfs>"
                + "<cellStyles count=\"1\"><cellStyle name=\"Normal\" xfId=\"0\" builtinId=\"0\"/></cellStyles>"
                + "</styleSheet>";
    }

    private static String xf(int formato, int fonte, int preenchimento, int borda, String alinhamento) {
        String abre = "<xf numFmtId=\"" + formato + "\" fontId=\"" + fonte + "\" fillId=\"" + preenchimento
                + "\" borderId=\"" + borda + "\" xfId=\"0\""
                + (formato != 0 ? " applyNumberFormat=\"1\"" : "")
                + (fonte != 0 ? " applyFont=\"1\"" : "")
                + (preenchimento != 0 ? " applyFill=\"1\"" : "")
                + (borda != 0 ? " applyBorder=\"1\"" : "")
                + (alinhamento.isEmpty() ? "" : " applyAlignment=\"1\"");
        return alinhamento.isEmpty() ? abre + "/>" : abre + ">" + alinhamento + "</xf>";
    }

    private String xmlAba(Aba aba) {
        StringBuilder xml = new StringBuilder(CABECALHO_XML);
        xml.append("<worksheet xmlns=\"").append(NS_MAIN).append("\" xmlns:r=\"").append(NS_REL).append("\">");

        xml.append("<sheetViews><sheetView workbookViewId=\"0\"");
        if (abas.indexOf(aba) == 0) {
            xml.append(" tabSelected=\"1\"");
        }
        if (aba.linhasCongeladas > 0) {
            xml.append("><pane ySplit=\"").append(aba.linhasCongeladas).append("\" topLeftCell=\"A")
                    .append(aba.linhasCongeladas + 1).append("\" activePane=\"bottomLeft\" state=\"frozen\"/>")
                    .append("<selection pane=\"bottomLeft\"/></sheetView>");
        } else {
            xml.append("/>");
        }
        xml.append("</sheetViews>");
        xml.append("<sheetFormatPr defaultRowHeight=\"15\"/>");

        if (aba.larguras.length > 0) {
            xml.append("<cols>");
            for (int i = 0; i < aba.larguras.length; i++) {
                xml.append("<col min=\"").append(i + 1).append("\" max=\"").append(i + 1)
                        .append("\" width=\"").append(aba.larguras[i]).append("\" customWidth=\"1\"/>");
            }
            xml.append("</cols>");
        }

        xml.append("<sheetData>");
        for (int l = 0; l < aba.linhas.size(); l++) {
            Celula[] celulas = aba.linhas.get(l);
            int numeroLinha = l + 1;
            xml.append("<row r=\"").append(numeroLinha).append("\">");
            for (int c = 0; c < celulas.length; c++) {
                Celula celula = celulas[c];
                if (celula == null) {
                    continue;
                }
                String referencia = coluna(c + 1) + numeroLinha;
                int estilo = celula.estilo == null ? 0 : celula.estilo.ordinal();
                xml.append("<c r=\"").append(referencia).append("\" s=\"").append(estilo).append("\"");

                if (celula.texto != null) {
                    xml.append(" t=\"inlineStr\"><is><t xml:space=\"preserve\">")
                            .append(escapar(celula.texto)).append("</t></is></c>");
                } else if (celula.formula != null) {
                    xml.append("><f>").append(escapar(celula.formula)).append("</f>");
                    if (celula.numero != null) {
                        xml.append("<v>").append(celula.numero).append("</v>");
                    }
                    xml.append("</c>");
                } else if (celula.numero != null) {
                    xml.append("><v>").append(celula.numero).append("</v></c>");
                } else {
                    xml.append("/>");
                }
            }
            xml.append("</row>");
        }
        xml.append("</sheetData>");
        xml.append("<pageMargins left=\"0.5\" right=\"0.5\" top=\"0.75\" bottom=\"0.75\" header=\"0.3\" footer=\"0.3\"/>");
        xml.append("</worksheet>");
        return xml.toString();
    }

    private static String escapar(String texto) {
        StringBuilder saida = new StringBuilder(texto.length());
        for (char ch : texto.toCharArray()) {
            switch (ch) {
                case '&' -> saida.append("&amp;");
                case '<' -> saida.append("&lt;");
                case '>' -> saida.append("&gt;");
                case '"' -> saida.append("&quot;");
                default -> {
                    // caracteres de controle não são permitidos em XML
                    if (ch >= 0x20 || ch == '\t' || ch == '\n' || ch == '\r') {
                        saida.append(ch);
                    }
                }
            }
        }
        return saida.toString();
    }
}
