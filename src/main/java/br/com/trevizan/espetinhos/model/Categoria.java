package br.com.trevizan.espetinhos.model;

public class Categoria {

    private int idCategoria;
    private String nome;
    private boolean ativo;

    public Categoria() {
    }

    public Categoria(int idCategoria, String nome, boolean ativo) {
        this.idCategoria = idCategoria;
        this.nome = nome;
        this.ativo = ativo;
    }

    public int getIdCategoria() {
        return idCategoria;
    }

    public void setIdCategoria(int idCategoria) {
        this.idCategoria = idCategoria;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    @Override
    public String toString() {
        return nome;
    }
}