package br.com.trevizan.espetinhos.model;

import java.math.BigDecimal;

public class ItemComanda {

    private int idItemComanda;
    private int idComanda;
    private int idProduto;
    private int quantidade;
    private BigDecimal precoUnitario;
    private String observacao;
    private BigDecimal subtotal;
    private String statusItem;

    public int getIdItemComanda() {
        return idItemComanda;
    }

    public void setIdItemComanda(int idItemComanda) {
        this.idItemComanda = idItemComanda;
    }

    public int getIdComanda() {
        return idComanda;
    }

    public void setIdComanda(int idComanda) {
        this.idComanda = idComanda;
    }

    public int getIdProduto() {
        return idProduto;
    }

    public void setIdProduto(int idProduto) {
        this.idProduto = idProduto;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(int quantidade) {
        this.quantidade = quantidade;
    }

    public BigDecimal getPrecoUnitario() {
        return precoUnitario;
    }

    public void setPrecoUnitario(BigDecimal precoUnitario) {
        this.precoUnitario = precoUnitario;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public String getStatusItem() {
        return statusItem;
    }

    public void setStatusItem(String statusItem) {
        this.statusItem = statusItem;
    }
}