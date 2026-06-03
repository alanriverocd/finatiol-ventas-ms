package com.finatiol.ventas.client;

import com.finatiol.ventas.client.fallback.FinanzasClientFallback;
import com.finatiol.ventas.dto.RegistrarMovimientoDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "finatiol-finanzas-ms", fallback = FinanzasClientFallback.class)
public interface FinanzasClient {

    @PostMapping("/finanzas")
    void registrarMovimiento(@RequestBody RegistrarMovimientoDTO request);
}
