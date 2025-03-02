package com.banquito.pos.controller.dto;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Schema(description = "Información de configuración del POS")
public class ConfiguracionPosDTO {
    
    @NotBlank(message = "El modelo es requerido")
    @Size(max = 10, message = "El modelo no puede exceder los 10 caracteres")
    @Schema(description = "Modelo del dispositivo POS", example = "VERIFONE")
    private String modelo;
    
    @NotBlank(message = "El código del POS es requerido")
    @Size(max = 10, message = "El código del POS no puede exceder los 10 caracteres")
    @Schema(description = "Código único del POS", example = "POS001")
    private String codigoPos;
    
    @NotBlank(message = "La dirección MAC es requerida")
    @Size(max = 32, message = "La dirección MAC no puede exceder los 32 caracteres")
    @Pattern(regexp = "^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$", message = "La dirección MAC no tiene un formato válido")
    @Schema(description = "Dirección MAC del dispositivo POS", example = "AA:BB:CC:DD:EE:FF")
    private String direccionMac;
    
    @NotBlank(message = "El código del comercio es requerido")
    @Size(max = 10, message = "El código del comercio no puede exceder los 10 caracteres")
    @Schema(description = "Código del comercio asociado al POS", example = "COM001")
    private String codigoComercio;
    
    @Schema(description = "Fecha de activación del POS", example = "2023-01-01T10:00:00")
    private LocalDateTime fechaActivacion;
} 