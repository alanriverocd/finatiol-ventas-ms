package com.finatiol.ventas.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.finatiol.ventas.client.NotificacionClient;
import com.finatiol.ventas.client.ProductoClient;
import com.finatiol.ventas.dto.ActualizarStockDTO;
import com.finatiol.ventas.dto.DetalleVentaRequestDTO;
import com.finatiol.ventas.dto.EmailRequestDTO;
import com.finatiol.ventas.dto.ProductoResponseDTO;
import com.finatiol.ventas.dto.VentaRequestDTO;
import com.finatiol.ventas.entity.DetalleVentaEntity;
import com.finatiol.ventas.entity.VentaEntity;
import com.finatiol.ventas.repository.VentaRepository;

@Service
public class VentaServiceImpl implements VentaService {

    private final VentaRepository ventaRepository;
    private final ProductoClient productoClient;
    private final NotificacionClient notificacionClient;

    public VentaServiceImpl(VentaRepository ventaRepository,
                            ProductoClient productoClient,
                            NotificacionClient notificacionClient) {
        this.ventaRepository = ventaRepository;
        this.productoClient = productoClient;
        this.notificacionClient = notificacionClient;
    }

    @Override
    public VentaEntity crearVenta(VentaRequestDTO request) {
        VentaEntity venta = new VentaEntity();
        venta.setUsuario(request.getUsuario());
        venta.setFecha(LocalDateTime.now());

        List<DetalleVentaEntity> detalles = new ArrayList<>();
        double total = 0.0;

        for (DetalleVentaRequestDTO d : request.getDetalles()) {
            ProductoResponseDTO producto = productoClient.obtenerProducto(d.getProductoId()).getData();

            if (producto.getStock() < d.getCantidad()) {
                throw new RuntimeException("Stock insuficiente para producto: " + producto.getNombre());
            }

            ActualizarStockDTO stock = new ActualizarStockDTO();
            stock.setCantidad(d.getCantidad());
            productoClient.descontarStock(d.getProductoId(), stock);

            DetalleVentaEntity detalle = new DetalleVentaEntity();
            detalle.setProductoId(d.getProductoId());
            detalle.setProductoNombre(producto.getNombre());
            detalle.setCantidad(d.getCantidad());
            detalle.setPrecio(producto.getPrecio());
            detalle.setSubtotal(d.getCantidad() * producto.getPrecio());
            detalle.setVenta(venta);
            total += detalle.getSubtotal();
            detalles.add(detalle);
        }

        venta.setDetalles(detalles);
        venta.setTotal(total);

        VentaEntity ventaGuardada = ventaRepository.save(venta);

        EmailRequestDTO email = new EmailRequestDTO();
        email.setDestinatario("TU_CORREO@gmail.com");
        email.setAsunto("Venta registrada");
        email.setMensaje("La venta " + ventaGuardada.getId()
                + " fue creada correctamente. Total: $" + ventaGuardada.getTotal());
        notificacionClient.enviarEmail(email);

        return ventaGuardada;
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
}
