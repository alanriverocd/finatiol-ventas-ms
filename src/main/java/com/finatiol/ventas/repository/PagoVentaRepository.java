package com.finatiol.ventas.repository;

import com.finatiol.ventas.entity.PagoVentaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PagoVentaRepository extends JpaRepository<PagoVentaEntity, Long> {

    List<PagoVentaEntity> findByVentaIdOrderByFechaDesc(Long ventaId);

    @Query("SELECT COALESCE(SUM(p.monto), 0) FROM PagoVentaEntity p WHERE p.venta.id = :ventaId")
    Double sumMontoByVentaId(@Param("ventaId") Long ventaId);
}
