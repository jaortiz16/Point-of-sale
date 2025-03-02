package com.banquito.pos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.banquito.pos.model.ConfiguracionPos;
import com.banquito.pos.model.ConfiguracionPosPK;

@Repository
public interface ConfiguracionPosRepository extends JpaRepository<ConfiguracionPos, ConfiguracionPosPK> {
    
    ConfiguracionPos findByCodigoPos(String codigoPos);
    
    ConfiguracionPos findByDireccionMac(String direccionMac);
    
    ConfiguracionPos findByCodigoComercio(String codigoComercio);
} 