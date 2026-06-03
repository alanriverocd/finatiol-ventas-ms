package com.finatiol.ventas.entity;

import com.finatiol.common.tenant.TenantContext;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "pagos_venta")
public class PagoVentaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venta_id", nullable = false)
    private VentaEntity venta;

    @Column(nullable = false)
    private Double monto;

    @Column(nullable = false)
    private LocalDateTime fecha;

    private String concepto;

    @Column(name = "metodo_pago")
    private String metodoPago;

    @Column(name = "registrado_por")
    private String registradoPor;

    @Column(name = "tenant_id")
    private String tenantId;

    @PrePersist
    protected void onPrePersist() {
        if (this.fecha == null) this.fecha = LocalDateTime.now();
        if (this.tenantId == null) this.tenantId = TenantContext.getCurrentTenant();
    }

    // ----- getters / setters -----

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public VentaEntity getVenta() { return venta; }
    public void setVenta(VentaEntity venta) { this.venta = venta; }

    public Double getMonto() { return monto; }
    public void setMonto(Double monto) { this.monto = monto; }

    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }

    public String getConcepto() { return concepto; }
    public void setConcepto(String concepto) { this.concepto = concepto; }

    public String getMetodoPago() { return metodoPago; }
    public void setMetodoPago(String metodoPago) { this.metodoPago = metodoPago; }

    public String getRegistradoPor() { return registradoPor; }
    public void setRegistradoPor(String registradoPor) { this.registradoPor = registradoPor; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
}
