package com.finatiol.ventas.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.finatiol.ventas.client.FinanzasClient;
import com.finatiol.ventas.client.ProductoClient;
import com.finatiol.ventas.config.RabbitMQConfig;
import com.finatiol.ventas.dto.PagoVentaRequestDTO;
import com.finatiol.ventas.dto.PagoVentaResponseDTO;
import com.finatiol.ventas.dto.SaldoVentaDTO;
import com.finatiol.ventas.entity.PagoVentaEntity;
import com.finatiol.ventas.repository.PagoVentaRepository;
import com.finatiol.ventas.dto.ActualizarStockDTO;
import com.finatiol.ventas.dto.RegistrarMovimientoDTO;
import com.finatiol.ventas.dto.DetalleVentaRequestDTO;
import com.finatiol.ventas.dto.ProductoResponseDTO;
import com.finatiol.ventas.dto.VentaRealizadaEvent;
import com.finatiol.ventas.dto.VentaRequestDTO;
import com.finatiol.ventas.exception.ServicioNoDisponibleException;
import com.finatiol.ventas.entity.DetalleVentaEntity;
import com.finatiol.ventas.entity.VentaEntity;
import com.finatiol.ventas.repository.VentaRepository;

@Service
public class VentaServiceImpl implements VentaService {

    private static final Logger log = LoggerFactory.getLogger(VentaServiceImpl.class);

    private final VentaRepository ventaRepository;
    private final ProductoClient productoClient;
    private final FinanzasClient finanzasClient;
    private final RabbitTemplate rabbitTemplate;
    private final PagoVentaRepository pagoVentaRepository;

    private final Counter ventasCreadasCounter;
    private final Counter ventasMontoCounter;
    private final Timer ventaCreacionTimer;

    public VentaServiceImpl(VentaRepository ventaRepository,
                            ProductoClient productoClient,
                            FinanzasClient finanzasClient,
                            RabbitTemplate rabbitTemplate,
                            PagoVentaRepository pagoVentaRepository,
                            MeterRegistry meterRegistry) {
        this.ventaRepository = ventaRepository;
        this.productoClient = productoClient;
        this.finanzasClient = finanzasClient;
        this.rabbitTemplate = rabbitTemplate;
        this.pagoVentaRepository = pagoVentaRepository;
        this.ventasCreadasCounter = Counter.builder("ventas_creadas_total")
                .description("Total de ventas creadas")
                .register(meterRegistry);
        this.ventasMontoCounter = Counter.builder("ventas_monto_total")
                .description("Monto acumulado de todas las ventas")
                .baseUnit("pesos")
                .register(meterRegistry);
        this.ventaCreacionTimer = Timer.builder("ventas_creacion_segundos")
                .description("Tiempo de procesamiento para crear una venta")
                .register(meterRegistry);
    }

    @Override
    public VentaEntity crearVenta(VentaRequestDTO request) {
        return ventaCreacionTimer.record(() -> {
            VentaEntity venta = new VentaEntity();
            venta.setUsuario(request.getUsuario());
            venta.setFecha(LocalDateTime.now());

            List<DetalleVentaEntity> detalles = new ArrayList<>();
            double total = 0.0;

            for (DetalleVentaRequestDTO d : request.getDetalles()) {
                String nombreProducto = d.getProductoNombre();
                double precioUnitario = d.getPrecio();
                boolean stockActualizado = false;

                try {
                    ProductoResponseDTO producto = productoClient.obtenerProducto(d.getProductoId()).getData();
                    if (producto != null) {
                        nombreProducto = producto.getNombre();
                        precioUnitario = producto.getPrecio();

                        if (producto.getStock() < d.getCantidad()) {
                            throw new RuntimeException("Stock insuficiente para producto: " + producto.getNombre());
                        }

                        ActualizarStockDTO stock = new ActualizarStockDTO();
                        stock.setCantidad(d.getCantidad());
                        productoClient.descontarStock(d.getProductoId(), stock);
                        stockActualizado = true;
                    }
                } catch (ServicioNoDisponibleException ex) {
                    log.warn("Productos-ms no disponible para el producto {}. Se continuara con los datos del formulario. Motivo: {}",
                            d.getProductoId(), ex.getMessage());
                } catch (RuntimeException ex) {
                    throw ex;
                } catch (Exception ex) {
                    log.warn("No se pudo consultar o actualizar stock del producto {}. Se guardara la venta con datos del formulario. Motivo: {}",
                            d.getProductoId(), ex.getMessage());
                }

                DetalleVentaEntity detalle = new DetalleVentaEntity();
                detalle.setProductoId(d.getProductoId());
                detalle.setProductoNombre(nombreProducto);
                detalle.setCantidad(d.getCantidad());
                detalle.setPrecio(precioUnitario);
                detalle.setSubtotal(d.getCantidad() * precioUnitario);
                detalle.setVenta(venta);
                total += detalle.getSubtotal();
                detalles.add(detalle);

                if (!stockActualizado) {
                    log.warn("Venta {} creada sin confirmar stock para producto {}.", request.getUsuario(), d.getProductoId());
                }
            }

            venta.setDetalles(detalles);
            venta.setTotal(total);

            VentaEntity ventaGuardada = ventaRepository.save(venta);

            // Publicar evento a RabbitMQ → notificaciones-ms consume de forma asíncrona
            try {
                VentaRealizadaEvent event = new VentaRealizadaEvent(
                        ventaGuardada.getId(),
                        ventaGuardada.getUsuario(),
                        ventaGuardada.getTotal(),
                        ventaGuardada.getFecha());
                rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.ROUTING_KEY, event);
            } catch (Exception e) {
                // No bloquear la venta si RabbitMQ no está disponible
            }

            ventasCreadasCounter.increment();
            ventasMontoCounter.increment(ventaGuardada.getTotal());

            return ventaGuardada;
        });
    }

    @Override
    public List<VentaEntity> obtenerVentas() {
        return ventaRepository.findAll();
    }

    @Override
    public VentaEntity obtenerVenta(Long id) {
        return ventaRepository.findById(id).orElseThrow();
    }

    @Override
    public void eliminarVenta(Long id) {
        VentaEntity venta = ventaRepository.findById(id).orElseThrow();

        for (DetalleVentaEntity d : venta.getDetalles()) {
            ActualizarStockDTO stock = new ActualizarStockDTO();
            stock.setCantidad(-d.getCantidad());
            productoClient.descontarStock(d.getProductoId(), stock);
        }

        ventaRepository.delete(venta);
    }

    @Override
    public List<VentaEntity> obtenerVentasUsuario(String usuario) {
        return ventaRepository.findByUsuario(usuario);
    }

    @Override
    public Double obtenerTotalVentas() {
        return ventaRepository.obtenerTotalVentas();
    }

    @Override
    public List<VentaEntity> obtenerVentasOrdenadas() {
        return ventaRepository.findAll(Sort.by(Sort.Direction.DESC, "fecha"));
    }

    // ------------------------------------------------------------------ pagos

    @Override
    public PagoVentaResponseDTO registrarPago(Long ventaId, PagoVentaRequestDTO request) {
        VentaEntity venta = ventaRepository.findById(ventaId).orElseThrow();
        PagoVentaEntity pago = new PagoVentaEntity();
        pago.setVenta(venta);
        pago.setMonto(request.getMonto());
        pago.setConcepto(request.getConcepto());
        pago.setMetodoPago(request.getMetodoPago());
        pago.setRegistradoPor(request.getRegistradoPor());
        PagoVentaEntity saved = pagoVentaRepository.save(pago);

        // Registrar automáticamente el ingreso en finanzas-ms
        try {
            String conceptoIngreso = (request.getConcepto() != null && !request.getConcepto().isBlank())
                    ? "Cobro - " + request.getConcepto() + " | Venta #" + ventaId + " | " + venta.getUsuario()
                    : "Cobro de venta #" + ventaId + " | " + venta.getUsuario();
            finanzasClient.registrarMovimiento(new RegistrarMovimientoDTO(
                    "INGRESO",
                    conceptoIngreso,
                    BigDecimal.valueOf(request.getMonto()),
                    "PAGO-VENTA-" + saved.getId()
            ));
        } catch (Exception e) {
        }

        return toDTO(saved);
    }

    @Override
    public List<PagoVentaResponseDTO> obtenerPagos(Long ventaId) {
        return pagoVentaRepository.findByVentaIdOrderByFechaDesc(ventaId)
                .stream().map(this::toDTO).toList();
    }

    @Override
    public SaldoVentaDTO obtenerSaldo(Long ventaId) {
        VentaEntity venta = ventaRepository.findById(ventaId).orElseThrow();
        return buildSaldo(venta);
    }

    @Override
    public List<SaldoVentaDTO> obtenerVentasConPendiente() {
        return ventaRepository.findAll(Sort.by(Sort.Direction.DESC, "fecha"))
                .stream()
                .map(this::buildSaldo)
                .filter(s -> s.getSaldoPendiente() > 0.001)
                .toList();
    }

    // ------------------------------------------------------------------ helpers

    private PagoVentaResponseDTO toDTO(PagoVentaEntity p) {
        PagoVentaResponseDTO dto = new PagoVentaResponseDTO();
        dto.setId(p.getId());
        dto.setVentaId(p.getVenta().getId());
        dto.setMonto(p.getMonto());
        dto.setFecha(p.getFecha());
        dto.setConcepto(p.getConcepto());
        dto.setMetodoPago(p.getMetodoPago());
        dto.setRegistradoPor(p.getRegistradoPor());
        return dto;
    }

    private SaldoVentaDTO buildSaldo(VentaEntity venta) {
        double pagado = pagoVentaRepository.sumMontoByVentaId(venta.getId());
        double pendiente = Math.max(0, venta.getTotal() - pagado);
        String estado = pendiente < 0.001 ? "COMPLETO"
                : pagado < 0.001 ? "PENDIENTE" : "PARCIAL";
        SaldoVentaDTO dto = new SaldoVentaDTO();
        dto.setVentaId(venta.getId());
        dto.setUsuario(venta.getUsuario());
        dto.setTotalVenta(venta.getTotal());
        dto.setTotalPagado(pagado);
        dto.setSaldoPendiente(pendiente);
        dto.setEstadoPago(estado);
        return dto;
    }
}
