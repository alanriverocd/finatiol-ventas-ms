package com.finatiol.ventas.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.finatiol.common.constants.ventas.SuccessCodes;
import com.finatiol.common.constants.ventas.SuccessMessages;
import com.finatiol.common.response.SuccessResponse;
import com.finatiol.ventas.dto.VentaRequestDTO;
import com.finatiol.ventas.entity.VentaEntity;
import com.finatiol.ventas.service.VentaService;

@RestController
@RequestMapping("/ventas")
public class VentaController {

    private final VentaService ventaService;

    public VentaController(VentaService ventaService) {
        this.ventaService = ventaService;
    }

    @PostMapping
    public ResponseEntity<SuccessResponse<VentaEntity>> crearVenta(
            @RequestBody VentaRequestDTO request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new SuccessResponse<>(
                        SuccessCodes.VENTA_CREADA,
                        SuccessMessages.VENTA_CREADA,
                        201,
                        ventaService.crearVenta(request)));
    }

    @GetMapping
    public ResponseEntity<SuccessResponse<List<VentaEntity>>> obtenerVentas() {
        return ResponseEntity.ok(new SuccessResponse<>(
                SuccessCodes.VENTAS_OBTENIDAS,
                SuccessMessages.VENTAS_OBTENIDAS,
                200,
                ventaService.obtenerVentas()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SuccessResponse<VentaEntity>> obtenerVenta(
            @PathVariable Long id) {
        return ResponseEntity.ok(new SuccessResponse<>(
                SuccessCodes.VENTA_OBTENIDA,
                SuccessMessages.VENTA_OBTENIDA,
                200,
                ventaService.obtenerVenta(id)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<SuccessResponse<Void>> eliminarVenta(
            @PathVariable Long id) {
        ventaService.eliminarVenta(id);
        return ResponseEntity.ok(new SuccessResponse<>(
                SuccessCodes.VENTA_ELIMINADA,
                SuccessMessages.VENTA_ELIMINADA,
                200,
                null));
    }

    @GetMapping("/usuario/{usuario}")
    public ResponseEntity<SuccessResponse<List<VentaEntity>>> obtenerVentasUsuario(
            @PathVariable String usuario) {
        return ResponseEntity.ok(new SuccessResponse<>(
                SuccessCodes.VENTAS_USUARIO_OBTENIDAS,
                SuccessMessages.VENTAS_USUARIO_OBTENIDAS,
                200,
                ventaService.obtenerVentasUsuario(usuario)));
    }

    @GetMapping("/total")
    public ResponseEntity<SuccessResponse<Double>> obtenerTotalVentas() {
        return ResponseEntity.ok(new SuccessResponse<>(
                SuccessCodes.TOTAL_VENTAS_OBTENIDO,
                SuccessMessages.TOTAL_VENTAS_OBTENIDO,
                200,
                ventaService.obtenerTotalVentas()));
    }

    @GetMapping("/ordenadas")
    public ResponseEntity<SuccessResponse<List<VentaEntity>>> obtenerVentasOrdenadas() {
        return ResponseEntity.ok(new SuccessResponse<>(
                SuccessCodes.VENTAS_ORDENADAS_OBTENIDAS,
                SuccessMessages.VENTAS_ORDENADAS_OBTENIDAS,
                200,
                ventaService.obtenerVentasOrdenadas()));
    }
}
