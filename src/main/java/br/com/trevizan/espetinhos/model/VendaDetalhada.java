package br.com.trevizan.espetinhos.model;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Uma linha da tabela "Vendas do dia" do PDF de relatório (filtro Diário):
 * uma comanda fechada com o total efetivamente pago nela.
 */
public class VendaDetalhada {

    private int idComanda;
    private int numeroMesa;
    private String nomeCliente;
    private String atendente;
    private LocalDateTime dataAbertura;
    private LocalDateTime dataFechamento;
    private int quantidadeItens;
    private String formasPagamento;
    private BigDecimal valorTotal = BigDecimal.ZERO;

    /** Tempo entre abertura e fechamento da comanda, ou null se faltar alguma data. */
    public Duration getPermanencia() {
        if (dataAbertura == null || dataFechamento == null) {
            return null;
        }
        return Duration.between(dataAbertura, dataFechamento);
    }

    public int getIdComanda() {
        return idComanda;
    }

    public void setIdComanda(int idComanda) {
        this.idComanda = idComanda;
    }

    public int getNumeroMesa() {
        return numeroMesa;
    }

    public void setNumeroMesa(int numeroMesa) {
        this.numeroMesa = numeroMesa;
    }

    public String getNomeCliente() {
        return nomeCliente;
    }

    public void setNomeCliente(String nomeCliente) {
        this.nomeCliente = nomeCliente;
    }

    public String getAtendente() {
        return atendente;
    }

    public void setAtendente(String atendente) {
        this.atendente = atendente;
    }

    public LocalDateTime getDataAbertura() {
        return dataAbertura;
    }

    public void setDataAbertura(LocalDateTime dataAbertura) {
        this.dataAbertura = dataAbertura;
    }

    public LocalDateTime getDataFechamento() {
        return dataFechamento;
    }

    public void setDataFechamento(LocalDateTime dataFechamento) {
        this.dataFechamento = dataFechamento;
    }

    public int getQuantidadeItens() {
        return quantidadeItens;
    }

    public void setQuantidadeItens(int quantidadeItens) {
        this.quantidadeItens = quantidadeItens;
    }

    public String getFormasPagamento() {
        return formasPagamento;
    }

    public void setFormasPagamento(String formasPagamento) {
        this.formasPagamento = formasPagamento;
    }

    public BigDecimal getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(BigDecimal valorTotal) {
        this.valorTotal = valorTotal;
    }
}
