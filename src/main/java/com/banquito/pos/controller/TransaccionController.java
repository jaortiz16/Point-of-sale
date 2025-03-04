package com.banquito.pos.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.banquito.pos.controller.dto.TransaccionDTO;
import com.banquito.pos.controller.mapper.TransaccionMapper;
import com.banquito.pos.exception.CommunicationException;
import com.banquito.pos.exception.NotFoundException;
import com.banquito.pos.exception.ValidationException;
import com.banquito.pos.model.Transaccion;
import com.banquito.pos.service.TransaccionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/transacciones")
@RequiredArgsConstructor
@Tag(name = "Transacciones", description = "API para procesar transacciones en el POS")
public class TransaccionController {

    private static final Logger log = LoggerFactory.getLogger(TransaccionController.class);
    
    private final TransaccionService service;
    private final TransaccionMapper mapper;
    
    @PostMapping
    @Operation(summary = "Procesar transacción", 
               description = "Procesa una transacción en el terminal POS (simple o recurrente según los parámetros)")
    @ApiResponse(responseCode = "200", description = "Transacción autorizada correctamente")
    @ApiResponse(responseCode = "400", description = "Transacción rechazada o datos inválidos")
    @ApiResponse(responseCode = "404", description = "Terminal POS no encontrado")
    @ApiResponse(responseCode = "500", description = "Error de comunicación con el Payment Gateway")
    public ResponseEntity<?> procesarTransaccion(@Valid @RequestBody TransaccionDTO transaccionDTO) {
        try {
            log.info("Procesando transacción en terminal POS");
            
            transaccionDTO.setTipo("PAG");
            
            if (transaccionDTO.getModalidad() != null) {
                if (!"SIM".equals(transaccionDTO.getModalidad()) && 
                    !"REC".equals(transaccionDTO.getModalidad()) && 
                    !"DIF".equals(transaccionDTO.getModalidad())) {
                    transaccionDTO.setModalidad("SIM");
                }
            } else {
                transaccionDTO.setModalidad("SIM");
            }
            
            if ("DIF".equals(transaccionDTO.getModalidad()) && 
                (transaccionDTO.getPlazo() == null || transaccionDTO.getPlazo() <= 0)) {
                log.error("Transacción diferida requiere un plazo válido");
                return ResponseEntity.badRequest().body("La transacción diferida requiere un plazo válido");
            }
            
            if ("REC".equals(transaccionDTO.getModalidad()) && 
                (transaccionDTO.getFrecuenciaDias() == null || transaccionDTO.getFrecuenciaDias() <= 0)) {
                log.error("Transacción recurrente requiere una frecuencia válida");
                return ResponseEntity.badRequest().body("La transacción recurrente requiere una frecuencia válida");
            }
            String cvv = transaccionDTO.getCvv();
            String fechaExpiracion = transaccionDTO.getFechaExpiracion();
            
            Transaccion transaccion = this.mapper.toModel(transaccionDTO);
            Transaccion transaccionProcesada = this.service.procesarTransaccion(transaccion, cvv, fechaExpiracion);
            
            if ("REC".equals(transaccionProcesada.getEstado())) {
                log.info("Transacción rechazada: {}", transaccionProcesada.getDetalle());
                return ResponseEntity.status(400).body("Pago rechazado");
            }
            
            log.info("Transacción autorizada: {}", transaccionProcesada.getCodTransaccion());
            return ResponseEntity.ok(this.mapper.toDTO(transaccionProcesada));
            
        } catch (ValidationException e) {
            log.error("Error de validación: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Pago rechazado");
        } catch (NotFoundException e) {
            log.error("Terminal POS no encontrado: {}", e.getMessage());
            return ResponseEntity.status(404).body("Pago rechazado");
        } catch (CommunicationException e) {
            log.error("Error de comunicación con el Payment Gateway: {}", e.getMessage());
            return ResponseEntity.status(400).body("Pago rechazado");
        } catch (Exception e) {
            log.error("Error inesperado: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Pago rechazado");
        }
    }
} 