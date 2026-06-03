package com.finatiol.ventas.config;

import com.finatiol.common.tenant.TenantContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class TenantInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(TenantInterceptor.class);

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) {

        String tenantId = TenantContext.getCurrentTenant();
        if (tenantId != null && !tenantId.isBlank()) {
            try {
                Session session = entityManager.unwrap(Session.class);
                session.enableFilter("tenantFilter")
                       .setParameter("tenantId", tenantId);
                log.debug("[TenantInterceptor][ventas] Filtro activado para tenantId={}", tenantId);
            } catch (Exception e) {
                log.warn("[TenantInterceptor][ventas] No se pudo activar filtro Hibernate: {}", e.getMessage());
            }
        }
        return true;
    }
}
