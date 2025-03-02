package com.banquito.pos.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.banquito.pos.model.Transaccion;

@Repository
public interface TransaccionRepository extends JpaRepository<Transaccion, String> {
    
    List<Transaccion> findByFechaBetween(LocalDateTime fechaInicio, LocalDateTime fechaFin);
    
    List<Transaccion> findByEstado(String estado);
    
    List<Transaccion> findByTipo(String tipo);
    
    List<Transaccion> findByModalidad(String modalidad);
    
    List<Transaccion> findByCodigoUnicoTransaccion(String codigoUnicoTransaccion);
} 