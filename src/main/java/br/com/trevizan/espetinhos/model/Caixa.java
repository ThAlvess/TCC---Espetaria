package br.com.trevizan.espetinhos.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Caixa {

    private int idCaixa;
    private Usuario usuarioAbertura;
    private LocalDateTime dataHoraAbertura;
    private LocalDateTime dataHoraFechamento;
    private BigDecimal valorInicial;
    private BigDecimal valorFinal;
    private String status;

    public Caixa() {
    }

    public Caixa(int idCaixa, Usuario usuarioAbertura, BigDecimal valorInicial, String status) {
        this.idCaixa = idCaixa;
        this.usuarioAbertura = usuarioAbertura;
        this.valorInicial = valorInicial;
        this.status = status;
    }

    public int getIdCaixa() {
        return idCaixa;
    }

    public void setIdCaixa(int idCaixa) {
        this.idCaixa = idCaixa;
    }

    public Usuario getUsuarioAbertura() {
        return usuarioAbertura;
    }

    public void setUsuarioAbertura(Usuario usuarioAbertura) {
        this.usuarioAbertura = usuarioAbertura;
    }

    public LocalDateTime getDataHoraAbertura() {
        return dataHoraAbertura;
    }

    public void setDataHoraAbertura(LocalDateTime dataHoraAbertura) {
        this.dataHoraAbertura = dataHoraAbertura;
    }

    public LocalDateTime getDataHoraFechamento() {
        return dataHoraFechamento;
    }

    public void setDataHoraFechamento(LocalDateTime dataHoraFechamento) {
        this.dataHoraFechamento = dataHoraFechamento;
    }

    public BigDecimal getValorInicial() {
        return valorInicial;
    }

    public void setValorInicial(BigDecimal valorInicial) {
        this.valorInicial = valorInicial;
    }

    public BigDecimal getValorFinal() {
        return valorFinal;
    }

    public void setValorFinal(BigDecimal valorFinal) {
        this.valorFinal = valorFinal;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Caixa #" + idCaixa + " (" + status + ")";
    }
}