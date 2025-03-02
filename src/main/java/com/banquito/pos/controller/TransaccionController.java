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
@RequestMapping("/v1/transacciones")
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
            
            // Forzar el tipo a PAG según especificaciones
            transaccionDTO.setTipo("PAG");
            
            // Asegurar modalidad correcta (SIM o REC)
            if (transaccionDTO.getModalidad() != null) {
                if (!"SIM".equals(transaccionDTO.getModalidad()) && !"REC".equals(transaccionDTO.getModalidad())) {
                    transaccionDTO.setModalidad("SIM");
                }
            } else {
                transaccionDTO.setModalidad("SIM");
            }
            
            // Capturar datos sensibles antes de convertir al modelo
            String cvv = transaccionDTO.getCvv();
            String fechaExpiracion = transaccionDTO.getFechaExpiracion();
            
            Transaccion transaccion = this.mapper.toModel(transaccionDTO);
            Transaccion transaccionProcesada = this.service.procesarTransaccion(transaccion, cvv, fechaExpiracion);
            
            // Verificar el estado de la transacción procesada
            // Ahora tendremos dos transacciones en la BD: una ENV (enviada) y otra AUT/REC (respuesta)
            // El servicio ahora devuelve la transacción de respuesta
            if ("REC".equals(transaccionProcesada.getEstado())) {
                log.info("Transacción rechazada: {}", transaccionProcesada.getDetalle());
                return ResponseEntity.badRequest().body(this.mapper.toDTO(transaccionProcesada));
            }
            
            // Si es "AUT" (autorizada), devolver código 200 (OK)
            return ResponseEntity.ok(this.mapper.toDTO(transaccionProcesada));
        } catch (NotFoundException e) {
            log.error("Terminal POS no encontrado: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (ValidationException e) {
            log.error("Error de validación: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (CommunicationException e) {
            log.error("Error de comunicación: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }
} 