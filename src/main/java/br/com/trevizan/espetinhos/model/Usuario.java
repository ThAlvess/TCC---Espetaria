package br.com.trevizan.espetinhos.model;

public class Usuario {
    private int id;
    private String nome;
    private String usuario;
    private String cpf;
    private String perfil;
    private boolean ativo;
    private String senha;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdUsuario() {
        return id;
    }

    public void setIdUsuario(int id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getLogin() {
        return usuario;
    }

    public void setLogin(String usuario) {
        this.usuario = usuario;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getPerfil() {
        return perfil;
    }

    public void setPerfil(String perfil) {
        this.perfil = perfil;
    }

    public String getStatus() {
        return ativo ? "ativo" : "inativo";
    }

    public void setStatus(String status) {
        this.ativo = "ativo".equalsIgnoreCase(status) || "true".equalsIgnoreCase(status);
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    public void setAtivo(String status) {
        this.ativo = "ativo".equalsIgnoreCase(status) || "true".equalsIgnoreCase(status);
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }
}