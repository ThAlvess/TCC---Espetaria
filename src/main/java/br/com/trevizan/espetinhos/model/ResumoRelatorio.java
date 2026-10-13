package br.com.trevizan.espetinhos.model;

import java.math.BigDecimal;

/**
 * Dados agregados (KPIs) exibidos nos 4 cartões no topo da tela de Relatórios:
 * faturamento total, quantidade de vendas, ticket médio e forma de pagamento
 * mais usada, todos referentes a um período (data início/fim) já filtrado.
 */
public class ResumoRelatorio {

    private BigDecimal faturamentoTotal = BigDecimal.ZERO;
    private int quantidadeVendas = 0;
    private BigDecimal ticketMedio = BigDecimal.ZERO;
    private String formaPagamentoPrincipal;
    private BigDecimal valorFormaPagamentoPrincipal = BigDecimal.ZERO;

    public BigDecimal getFaturamentoTotal() {
        return faturamentoTotal;
    }

    public void setFaturamentoTotal(BigDecimal faturamentoTotal) {
        this.faturamentoTotal = faturamentoTotal;
    }

    public int getQuantidadeVendas() {
        return quantidadeVendas;
    }

    public void setQuantidadeVendas(int quantidadeVendas) {
        this.quantidadeVendas = quantidadeVendas;
    }

    public BigDecimal getTicketMedio() {
        return ticketMedio;
    }

    public void setTicketMedio(BigDecimal ticketMedio) {
        this.ticketMedio = ticketMedio;
    }

    public String getFormaPagamentoPrincipal() {
        return formaPagamentoPrincipal;
    }

    public void setFormaPagamentoPrincipal(String formaPagamentoPrincipal) {
        this.formaPagamentoPrincipal = formaPagamentoPrincipal;
    }

    public BigDecimal getValorFormaPagamentoPrincipal() {
        return valorFormaPagamentoPrincipal;
    }

    public void setValorFormaPagamentoPrincipal(BigDecimal valorFormaPagamentoPrincipal) {
        this.valorFormaPagamentoPrincipal = valorFormaPagamentoPrincipal;
    }
}
