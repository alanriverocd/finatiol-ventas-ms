package com.finatiol.ventas.client.fallback;

import com.finatiol.ventas.client.FinanzasClient;
import com.finatiol.ventas.dto.RegistrarMovimientoDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class FinanzasClientFallback implements FinanzasClient {

    private static final Logger log = LoggerFactory.getLogger(FinanzasClientFallback.class);

    @Override
    public void registrarMovimiento(RegistrarMovimientoDTO request) {
        log.warn("[Circuit Breaker] finanzas-ms no disponible. Movimiento financiero no registrado.");
    }
}
