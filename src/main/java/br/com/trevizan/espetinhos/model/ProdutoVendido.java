package br.com.trevizan.espetinhos.model;

import java.math.BigDecimal;

/**
 * Uma linha da tabela "Produtos vendidos" do PDF de relatório: quanto de
 * cada produto saiu no período (quantidade e valor).
 */
public class ProdutoVendido {

    private String nome;
    private String categoria;
    private int quantidade;
    private BigDecimal total = BigDecimal.ZERO;

    public ProdutoVendido() {
    }

    public ProdutoVendido(String nome, String categoria, int quantidade, BigDecimal total) {
        this.nome = nome;
        this.categoria = categoria;
        this.quantidade = quantidade;
        this.total = total;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(int quantidade) {
        this.quantidade = quantidade;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }
}
