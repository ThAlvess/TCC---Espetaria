package br.com.trevizan.espetinhos.model;

public class Mesa {

    private int idMesa;
    private int numero;
    private String status;

    public Mesa() {
    }

    public Mesa(int idMesa, int numero, String status) {
        this.idMesa = idMesa;
        this.numero = numero;
        this.status = status;
    }

    public int getIdMesa() {
        return idMesa;
    }

    public void setIdMesa(int idMesa) {
        this.idMesa = idMesa;
    }

    public int getNumero() {
        return numero;
    }

    public void setNumero(int numero) {
        this.numero = numero;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isLivre() {
        return "LIVRE".equalsIgnoreCase(status);
    }

    public boolean isOcupada() {
        return "OCUPADA".equalsIgnoreCase(status);
    }

    @Override
    public String toString() {
        return "Mesa " + numero;
    }
}