package com.finatiol.ventas.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.finatiol.ventas.entity.VentaEntity;

public interface VentaRepository extends JpaRepository<VentaEntity, Long> {

    List<VentaEntity> findByUsuario(String usuario);

    @Query("""
            SELECT COALESCE(SUM(v.total), 0)
            FROM VentaEntity v
            """)
    Double obtenerTotalVentas();
}
