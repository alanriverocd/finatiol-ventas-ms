package com.finatiol.ventas.service;

import java.util.List;

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
}
