package com.banquito.pos.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.banquito.pos.exception.NotFoundException;
import com.banquito.pos.exception.ValidationException;
import com.banquito.pos.model.ConfiguracionPos;
import com.banquito.pos.model.ConfiguracionPosPK;
import com.banquito.pos.repository.ConfiguracionPosRepository;

import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ConfiguracionPosService {

    private static final Logger log = LoggerFactory.getLogger(ConfiguracionPosService.class);
    
    private final ConfiguracionPosRepository repository;
    
    public ConfiguracionPos obtenerConfiguracionLocal() {
        log.info("Obteniendo configuración local del terminal POS");
        List<ConfiguracionPos> configuraciones = this.repository.findAll();
        
        if (configuraciones.isEmpty()) {
            log.error("No se encontró ninguna configuración para el terminal POS");
            throw new NotFoundException("N/A", "ConfiguracionPos");
        }
        
        return configuraciones.get(0);
    }
    
    public ConfiguracionPos findByCodigoPos(String codigoPos) {
        log.info("Buscando configuración del POS con código: {}", codigoPos);
        ConfiguracionPos configuracion = this.repository.findByCodigoPos(codigoPos);
        if (configuracion == null) {
            log.error("No se encontró configuración para el POS con código: {}", codigoPos);
            throw new NotFoundException(codigoPos, "ConfiguracionPos.codigoPos");
        }
        return configuracion;
    }
    
    public ConfiguracionPos findByDireccionMac(String direccionMac) {
        log.info("Buscando configuración del POS con dirección MAC: {}", direccionMac);
        ConfiguracionPos configuracion = this.repository.findByDireccionMac(direccionMac);
        if (configuracion == null) {
            log.error("No se encontró configuración para el POS con dirección MAC: {}", direccionMac);
            throw new NotFoundException(direccionMac, "ConfiguracionPos.direccionMac");
        }
        return configuracion;
    }
    
    public ConfiguracionPos findById(String modelo, String codigoPos) {
        log.info("Buscando configuración del POS con modelo: {} y código: {}", modelo, codigoPos);
        ConfiguracionPosPK pk = new ConfiguracionPosPK(modelo, codigoPos);
        return this.repository.findById(pk)
                .orElseThrow(() -> {
                    log.error("No se encontró configuración para el POS con modelo: {} y código: {}", modelo, codigoPos);
                    return new NotFoundException(modelo + ":" + codigoPos, "ConfiguracionPos");
                });
    }
    
    @Transactional
    public ConfiguracionPos createConfiguracion(ConfiguracionPos configuracion) {
        log.info("Creando configuración para POS: {}", configuracion.getCodigoPos());
        if (this.repository.findByCodigoPos(configuracion.getCodigoPos()) != null) {
            log.error("Ya existe una configuración para el POS con código: {}", configuracion.getCodigoPos());
            throw new ValidationException("Ya existe una configuración para el POS con código: " + configuracion.getCodigoPos());
        }
        if (this.repository.findByDireccionMac(configuracion.getDireccionMac()) != null) {
            log.error("Ya existe una configuración para el POS con dirección MAC: {}", configuracion.getDireccionMac());
            throw new ValidationException("Ya existe una configuración para el POS con dirección MAC: " + configuracion.getDireccionMac());
        }
        
        configuracion.setFechaActivacion(LocalDateTime.now());
        
        return this.repository.save(configuracion);
    }
    
    @Transactional
    public ConfiguracionPos updateConfiguracion(ConfiguracionPos configuracion) {
        log.info("Actualizando configuración para POS: {}", configuracion.getCodigoPos());
        this.findById(configuracion.getModelo(), configuracion.getCodigoPos());
        
        return this.repository.save(configuracion);
    }
} 