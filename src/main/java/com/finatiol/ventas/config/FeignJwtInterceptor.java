package com.finatiol.ventas.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Interceptor Feign que propaga el token JWT del request entrante
 * hacia las llamadas inter-servicio (finanzas-ms, etc.).
 */
@Component
public class FeignJwtInterceptor implements RequestInterceptor {

    private static final Logger log = LoggerFactory.getLogger(FeignJwtInterceptor.class);

    @Override
    public void apply(RequestTemplate template) {
        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs != null) {
            String authorization = attrs.getRequest().getHeader("Authorization");
            if (authorization != null && !authorization.isBlank()) {
                template.header("Authorization", authorization);
            }
        }
    }
}
