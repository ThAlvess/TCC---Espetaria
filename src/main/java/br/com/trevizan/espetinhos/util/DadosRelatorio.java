package br.com.trevizan.espetinhos.util;

import br.com.trevizan.espetinhos.dao.RelatorioDAO;
import br.com.trevizan.espetinhos.model.ProdutoVendido;
import br.com.trevizan.espetinhos.model.ResumoPeriodo;
import br.com.trevizan.espetinhos.model.ResumoRelatorio;
import br.com.trevizan.espetinhos.model.VendaDetalhada;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;

/**
 * Tudo o que vai para os arquivos exportados da tela de Relatórios (PDF e
 * Excel). A tela preenche este objeto uma vez e cada exportador só lê.
 */
public class DadosRelatorio {

    /** Qual tabela de detalhamento acompanha o relatório (depende do período). */
    public enum Detalhamento {
        /** Uma linha por venda (comanda) — período de um dia. */
        POR_VENDA,
        /** Uma linha por semana — período de até ~2 meses. */
        POR_SEMANA,
        /** Uma linha por mês — períodos longos. */
        POR_MES
    }

    private final PeriodoRelatorio periodo;
    private PeriodoRelatorio periodoAnterior;
    private String geradoPor;
    private LocalDateTime geradoEm = LocalDateTime.now();

    private ResumoRelatorio resumo = new ResumoRelatorio();
    private ResumoRelatorio resumoAnterior;

    private LinkedHashMap<String, BigDecimal> totaisPorFormaPagamento = new LinkedHashMap<>();
    private LinkedHashMap<String, BigDecimal> vendasPorCategoria = new LinkedHashMap<>();

    /** Gráfico de evolução: rótulo do ponto (ex.: "05/09") -> faturamento. */
    private LinkedHashMap<String, BigDecimal> evolucao = new LinkedHashMap<>();
    /** Mesmos pontos no período anterior, na mesma ordem (rótulo e valor). */
    private List<String> rotulosEvolucaoAnterior = new ArrayList<>();
    private List<BigDecimal> evolucaoAnterior = new ArrayList<>();

    private List<VendaDetalhada> vendas = new ArrayList<>();
    private List<ProdutoVendido> produtos = new ArrayList<>();
    private List<ResumoPeriodo> resumoPeriodos = new ArrayList<>();

    public DadosRelatorio(PeriodoRelatorio periodo) {
        this.periodo = periodo;
    }

    /** Tabela de detalhamento que combina com o período (mesma lógica do gráfico). */
    public Detalhamento getDetalhamento() {
        RelatorioDAO.Granularidade granularidade = periodo.getGranularidade();
        return switch (granularidade) {
            case HORA -> Detalhamento.POR_VENDA;
            case DIA -> Detalhamento.POR_SEMANA;
            case MES -> Detalhamento.POR_MES;
        };
    }

    public boolean temComparacao() {
        return periodoAnterior != null && resumoAnterior != null;
    }

    // ------------------------------------------------------------------
    // Variação percentual (usada na tela, no PDF e no Excel)
    // ------------------------------------------------------------------

    /**
     * Variação do valor atual em relação ao anterior, em %, com 1 casa.
     * Devolve null quando não há base de comparação (anterior zerado).
     */
    public static BigDecimal variacaoPercentual(BigDecimal atual, BigDecimal anterior) {
        if (atual == null || anterior == null || anterior.signum() == 0) {
            return null;
        }
        return atual.subtract(anterior)
                .multiply(BigDecimal.valueOf(100))
                .divide(anterior, 1, RoundingMode.HALF_UP);
    }

    /** "+12,3%", "-5,1%" ou "0,0%"; null quando não há base de comparação. */
    public static String textoVariacao(BigDecimal atual, BigDecimal anterior) {
        BigDecimal variacao = variacaoPercentual(atual, anterior);
        if (variacao == null) {
            return null;
        }
        DecimalFormat formato = new DecimalFormat("0.0'%'", new DecimalFormatSymbols(new Locale("pt", "BR")));
        return (variacao.signum() > 0 ? "+" : "") + formato.format(variacao);
    }

    // ------------------------------------------------------------------
    // Getters / setters
    // ------------------------------------------------------------------

    public PeriodoRelatorio getPeriodo() {
        return periodo;
    }

    public PeriodoRelatorio getPeriodoAnterior() {
        return periodoAnterior;
    }

    public void setPeriodoAnterior(PeriodoRelatorio periodoAnterior) {
        this.periodoAnterior = periodoAnterior;
    }

    public String getGeradoPor() {
        return geradoPor;
    }

    public void setGeradoPor(String geradoPor) {
        this.geradoPor = geradoPor;
    }

    public LocalDateTime getGeradoEm() {
        return geradoEm;
    }

    public void setGeradoEm(LocalDateTime geradoEm) {
        this.geradoEm = geradoEm;
    }

    public ResumoRelatorio getResumo() {
        return resumo;
    }

    public void setResumo(ResumoRelatorio resumo) {
        this.resumo = resumo != null ? resumo : new ResumoRelatorio();
    }

    public ResumoRelatorio getResumoAnterior() {
        return resumoAnterior;
    }

    public void setResumoAnterior(ResumoRelatorio resumoAnterior) {
        this.resumoAnterior = resumoAnterior;
    }

    public LinkedHashMap<String, BigDecimal> getTotaisPorFormaPagamento() {
        return totaisPorFormaPagamento;
    }

    public void setTotaisPorFormaPagamento(LinkedHashMap<String, BigDecimal> totais) {
        this.totaisPorFormaPagamento = totais != null ? totais : new LinkedHashMap<>();
    }

    public LinkedHashMap<String, BigDecimal> getVendasPorCategoria() {
        return vendasPorCategoria;
    }

    public void setVendasPorCategoria(LinkedHashMap<String, BigDecimal> vendasPorCategoria) {
        this.vendasPorCategoria = vendasPorCategoria != null ? vendasPorCategoria : new LinkedHashMap<>();
    }

    public LinkedHashMap<String, BigDecimal> getEvolucao() {
        return evolucao;
    }

    public void setEvolucao(LinkedHashMap<String, BigDecimal> evolucao) {
        this.evolucao = evolucao != null ? evolucao : new LinkedHashMap<>();
    }

    public List<String> getRotulosEvolucaoAnterior() {
        return rotulosEvolucaoAnterior;
    }

    public List<BigDecimal> getEvolucaoAnterior() {
        return evolucaoAnterior;
    }

    /** Pontos do período anterior alinhados com os de {@link #getEvolucao()} (mesma ordem). */
    public void setEvolucaoAnterior(List<String> rotulos, List<BigDecimal> valores) {
        this.rotulosEvolucaoAnterior = rotulos != null ? rotulos : new ArrayList<>();
        this.evolucaoAnterior = valores != null ? valores : new ArrayList<>();
    }

    public List<VendaDetalhada> getVendas() {
        return vendas;
    }

    public void setVendas(List<VendaDetalhada> vendas) {
        this.vendas = vendas != null ? vendas : new ArrayList<>();
    }

    public List<ProdutoVendido> getProdutos() {
        return produtos;
    }

    public void setProdutos(List<ProdutoVendido> produtos) {
        this.produtos = produtos != null ? produtos : new ArrayList<>();
    }

    public List<ResumoPeriodo> getResumoPeriodos() {
        return resumoPeriodos;
    }

    public void setResumoPeriodos(List<ResumoPeriodo> resumoPeriodos) {
        this.resumoPeriodos = resumoPeriodos != null ? resumoPeriodos : new ArrayList<>();
    }
}
