package com.finatiol.ventas.service;

import java.util.List;

import com.finatiol.ventas.dto.PagoVentaRequestDTO;
import com.finatiol.ventas.dto.PagoVentaResponseDTO;
import com.finatiol.ventas.dto.SaldoVentaDTO;
import com.finatiol.ventas.dto.VentaRequestDTO;
import com.finatiol.ventas.entity.VentaEntity;

public interface VentaService {

    VentaEntity crearVenta(VentaRequestDTO request);

    List<VentaEntity> obtenerVentas();

    VentaEntity obtenerVenta(Long id);

    void eliminarVenta(Long id);

    List<VentaEntity> obtenerVentasUsuario(String usuario);

    Double obtenerTotalVentas();

    List<VentaEntity> obtenerVentasOrdenadas();

    // ---- pagos ----
    PagoVentaResponseDTO registrarPago(Long ventaId, PagoVentaRequestDTO request);

    List<PagoVentaResponseDTO> obtenerPagos(Long ventaId);

    SaldoVentaDTO obtenerSaldo(Long ventaId);

    List<SaldoVentaDTO> obtenerVentasConPendiente();
}
