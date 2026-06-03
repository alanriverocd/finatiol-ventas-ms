package com.finatiol.ventas.dto;

import java.math.BigDecimal;

public class RegistrarMovimientoDTO {

    private String tipo;
    private String concepto;
    private BigDecimal monto;
    private String referencia;

    public RegistrarMovimientoDTO() {}

    public RegistrarMovimientoDTO(String tipo, String concepto, BigDecimal monto, String referencia) {
        this.tipo = tipo;
        this.concepto = concepto;
        this.monto = monto;
        this.referencia = referencia;
    }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getConcepto() { return concepto; }
    public void setConcepto(String concepto) { this.concepto = concepto; }

    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }

    public String getReferencia() { return referencia; }
    public void setReferencia(String referencia) { this.referencia = referencia; }
}
