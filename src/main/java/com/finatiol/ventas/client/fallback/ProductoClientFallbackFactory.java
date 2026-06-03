package com.finatiol.ventas.client.fallback;

import com.finatiol.ventas.client.ProductoClient;
import com.finatiol.ventas.dto.ActualizarStockDTO;
import com.finatiol.ventas.dto.ApiResponse;
import com.finatiol.ventas.dto.ProductoResponseDTO;
import com.finatiol.ventas.exception.ServicioNoDisponibleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Component
public class ProductoClientFallbackFactory implements FallbackFactory<ProductoClient> {

    private static final Logger log = LoggerFactory.getLogger(ProductoClientFallbackFactory.class);

    @Override
    public ProductoClient create(Throwable cause) {
        log.error("[Circuit Breaker] productos-ms no disponible: {}", cause.getMessage());
        return new ProductoClient() {

            @Override
            public ApiResponse<ProductoResponseDTO> obtenerProducto(Long id) {
                throw new ServicioNoDisponibleException(
                        "Servicio de productos no disponible. Intente más tarde.");
            }

            @Override
            public void descontarStock(Long id, ActualizarStockDTO request) {
                throw new ServicioNoDisponibleException(
                        "Servicio de productos no disponible. No se pudo descontar stock.");
            }
        };
    }
}
