package br.com.trevizan.espetinhos.model;

import java.math.BigDecimal;

public class Produto {

    private int idProduto;
    private String nome;
    private BigDecimal preco;
    private Categoria categoria;
    private boolean ativo;
    private String descricao;
    private int quantidadeEstoque;

    public Produto() {
    }

    public Produto(int idProduto, String nome, BigDecimal preco,
                   Categoria categoria, boolean ativo) {
        this.idProduto = idProduto;
        this.nome = nome;
        this.preco = preco;
        this.categoria = categoria;
        this.ativo = ativo;
    }

    public int getIdProduto() {
        return idProduto;
    }

    public void setIdProduto(int idProduto) {
        this.idProduto = idProduto;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public BigDecimal getPreco() {
        return preco;
    }

    public void setPreco(BigDecimal preco) {
        this.preco = preco;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public int getQuantidadeEstoque() {
        return quantidadeEstoque;
    }

    public void setQuantidadeEstoque(int quantidadeEstoque) {
        this.quantidadeEstoque = quantidadeEstoque;
    }

    @Override
    public String toString() {
        return nome;
    }
}