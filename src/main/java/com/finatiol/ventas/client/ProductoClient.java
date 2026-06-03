package com.finatiol.ventas.client;

import com.finatiol.ventas.client.fallback.ProductoClientFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.finatiol.ventas.dto.ActualizarStockDTO;
import com.finatiol.ventas.dto.ApiResponse;
import com.finatiol.ventas.dto.ProductoResponseDTO;

@FeignClient(name = "finatiol-productos-ms", fallbackFactory = ProductoClientFallbackFactory.class)
public interface ProductoClient {

    @GetMapping("/productos/{id}")
    ApiResponse<ProductoResponseDTO> obtenerProducto(@PathVariable Long id);

    @PutMapping("/productos/{id}/stock")
    void descontarStock(@PathVariable Long id, @RequestBody ActualizarStockDTO request);
}
