package com.banquito.pos.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.banquito.pos.controller.dto.ConfiguracionPosDTO;
import com.banquito.pos.controller.mapper.ConfiguracionPosMapper;
import com.banquito.pos.exception.NotFoundException;
import com.banquito.pos.model.ConfiguracionPos;
import com.banquito.pos.service.ConfiguracionPosService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/configuracion")
@RequiredArgsConstructor
@Tag(name = "Configuración POS", description = "API para configuración del terminal POS")
public class ConfiguracionPosController {

    private static final Logger log = LoggerFactory.getLogger(ConfiguracionPosController.class);
    
    private final ConfiguracionPosService service;
    private final ConfiguracionPosMapper mapper;
    
    @GetMapping
    @Operation(summary = "Obtener configuración actual", 
               description = "Obtiene la configuración actual del terminal POS")
    @ApiResponse(responseCode = "200", description = "Configuración obtenida correctamente")
    @ApiResponse(responseCode = "404", description = "Terminal POS no configurado")
    public ResponseEntity<ConfiguracionPosDTO> getConfiguracion() {
        try {
            log.info("Obteniendo configuración actual del terminal POS");
            ConfiguracionPos configuracion = this.service.obtenerConfiguracionLocal();
            return ResponseEntity.ok(this.mapper.toDTO(configuracion));
        } catch (NotFoundException e) {
            log.error("Terminal POS no configurado: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
    
    @PostMapping
    @Operation(summary = "Configurar terminal POS", 
               description = "Configura el terminal POS con los datos proporcionados")
    @ApiResponse(responseCode = "200", description = "Terminal POS configurado correctamente")
    @ApiResponse(responseCode = "400", description = "Datos inválidos para la configuración")
    public ResponseEntity<ConfiguracionPosDTO> configurar(@Valid @RequestBody ConfiguracionPosDTO configuracionDTO) {
        log.info("Configurando terminal POS");
        ConfiguracionPos configuracion = this.mapper.toModel(configuracionDTO);
        ConfiguracionPos nuevaConfiguracion = this.service.createConfiguracion(configuracion);
        return ResponseEntity.ok(this.mapper.toDTO(nuevaConfiguracion));
    }
} 