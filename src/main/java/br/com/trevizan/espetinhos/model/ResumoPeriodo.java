package br.com.trevizan.espetinhos.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

/**
 * Uma linha das tabelas-resumo do PDF de relatório: uma semana (filtro
 * Mensal) ou um mês (filtros Anual/Fiscal). Os totais são acumulados dia a
 * dia pelo {@link #acumular(LocalDate, int, BigDecimal)}, o que também
 * permite descobrir o melhor dia do intervalo.
 */
public class ResumoPeriodo {

    private final String rotulo;
    private final LocalDate dataInicio;
    private final LocalDate dataFim;

    private int diasComVenda = 0;
    private int quantidadeVendas = 0;
    private BigDecimal faturamento = BigDecimal.ZERO;
    private LocalDate melhorDia;
    private BigDecimal faturamentoMelhorDia = BigDecimal.ZERO;

    public ResumoPeriodo(String rotulo, LocalDate dataInicio, LocalDate dataFim) {
        this.rotulo = rotulo;
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
    }

    /** Soma o movimento de um dia neste período. */
    public void acumular(LocalDate dia, int vendasDoDia, BigDecimal faturamentoDoDia) {
        if (faturamentoDoDia == null || faturamentoDoDia.signum() == 0) {
            return;
        }
        diasComVenda++;
        quantidadeVendas += vendasDoDia;
        faturamento = faturamento.add(faturamentoDoDia);

        if (melhorDia == null || faturamentoDoDia.compareTo(faturamentoMelhorDia) > 0) {
            melhorDia = dia;
            faturamentoMelhorDia = faturamentoDoDia;
        }
    }

    public BigDecimal getTicketMedio() {
        if (quantidadeVendas == 0) {
            return BigDecimal.ZERO;
        }
        return faturamento.divide(BigDecimal.valueOf(quantidadeVendas), 2, RoundingMode.HALF_UP);
    }

    /** Faturamento médio por dia em que houve venda. */
    public BigDecimal getMediaDiaria() {
        if (diasComVenda == 0) {
            return BigDecimal.ZERO;
        }
        return faturamento.divide(BigDecimal.valueOf(diasComVenda), 2, RoundingMode.HALF_UP);
    }

    public String getRotulo() {
        return rotulo;
    }

    public LocalDate getDataInicio() {
        return dataInicio;
    }

    public LocalDate getDataFim() {
        return dataFim;
    }

    public int getDiasComVenda() {
        return diasComVenda;
    }

    public int getQuantidadeVendas() {
        return quantidadeVendas;
    }

    public BigDecimal getFaturamento() {
        return faturamento;
    }

    public LocalDate getMelhorDia() {
        return melhorDia;
    }

    public BigDecimal getFaturamentoMelhorDia() {
        return faturamentoMelhorDia;
    }
}
