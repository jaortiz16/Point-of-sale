package com.banquito.pos.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.banquito.pos.client.PaymentGatewayClient;
import com.banquito.pos.client.dto.TransaccionRequestDTO;
import com.banquito.pos.client.dto.TransaccionResponseDTO;
import com.banquito.pos.exception.CommunicationException;
import com.banquito.pos.exception.NotFoundException;
import com.banquito.pos.exception.ValidationException;
import com.banquito.pos.model.ConfiguracionPos;
import com.banquito.pos.model.Transaccion;
import com.banquito.pos.repository.TransaccionRepository;

import feign.FeignException;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransaccionService {

    private static final Logger log = LoggerFactory.getLogger(TransaccionService.class);
    
    private final TransaccionRepository repository;
    private final ConfiguracionPosService configuracionPosService;
    private final PaymentGatewayClient paymentGatewayClient;
        
    @Transactional
    public Transaccion procesarTransaccion(Transaccion transaccion, String cvv, String fechaExpiracion) {
        log.info("Procesando transacción en terminal POS");
        
        ConfiguracionPos configuracion = obtenerConfiguracionPOS();
        
        validarTransaccion(transaccion);
        
        String uuid = UUID.randomUUID().toString();
        String codTransaccion = generarCodigoTransaccion();
        
        transaccion.setCodTransaccion(codTransaccion);
        transaccion.setCodigoUnicoTransaccion(uuid);
        transaccion.setFecha(LocalDateTime.now());
        
        transaccion.setEstado("ENV");
        transaccion.setEstadoRecibo("PEN");
        
        transaccion.setTipo("PAG");
        
        if (!"SIM".equals(transaccion.getModalidad()) && 
            !"REC".equals(transaccion.getModalidad()) && 
            !"DIF".equals(transaccion.getModalidad())) {
            transaccion.setModalidad("SIM");
        }
        
        Transaccion transaccionEnviada = this.repository.save(transaccion);
        log.info("Transacción guardada con estado ENV: {}", transaccionEnviada.getCodTransaccion());
        
        try {
            Transaccion transaccionRespuesta = new Transaccion();
            
            transaccionRespuesta.setCodTransaccion(codTransaccion + "-RESP");
            transaccionRespuesta.setCodigoUnicoTransaccion(uuid);
            transaccionRespuesta.setFecha(LocalDateTime.now());
            transaccionRespuesta.setTipo(transaccion.getTipo());
            transaccionRespuesta.setMarca(transaccion.getMarca());
            transaccionRespuesta.setModalidad(transaccion.getModalidad());
            transaccionRespuesta.setDetalle(transaccion.getDetalle());
            transaccionRespuesta.setMonto(transaccion.getMonto());
            transaccionRespuesta.setMoneda(transaccion.getMoneda());
            transaccionRespuesta.setNumeroTarjeta(transaccion.getNumeroTarjeta());
            transaccionRespuesta.setNombreTitular(transaccion.getNombreTitular());
            transaccionRespuesta.setFrecuenciaDias(transaccion.getFrecuenciaDias());
            transaccionRespuesta.setPlazo(transaccion.getPlazo());
            
            transaccionRespuesta.setEstadoRecibo("PEN");
            
            TransaccionResponseDTO respuesta = enviarTransaccionPaymentGateway(transaccion, configuracion, cvv, fechaExpiracion);
            
            transaccionRespuesta.setEstado("AUT");
            if (respuesta.getMensaje() != null) {
                transaccionRespuesta.setDetalle(respuesta.getMensaje());
            } else {
                transaccionRespuesta.setDetalle("Transacción autorizada");
            }
            
            Transaccion transaccionFinal = this.repository.save(transaccionRespuesta);
            log.info("Transacción de respuesta guardada con estado {}: {}", 
                    transaccionFinal.getEstado(), transaccionFinal.getCodTransaccion());
            
            return transaccionFinal;
            
        } catch (FeignException e) {
            log.error("Error al comunicarse con el Payment Gateway: {}", e.getMessage());
            
            Transaccion transaccionError = new Transaccion();
            
            transaccionError.setCodTransaccion(codTransaccion + "-ERROR");
            transaccionError.setCodigoUnicoTransaccion(uuid);
            transaccionError.setFecha(LocalDateTime.now());
            transaccionError.setTipo(transaccion.getTipo());
            transaccionError.setMarca(transaccion.getMarca());
            transaccionError.setModalidad(transaccion.getModalidad());
            transaccionError.setDetalle("Error de comunicación: " + e.getMessage());
            transaccionError.setMonto(transaccion.getMonto());
            transaccionError.setMoneda(transaccion.getMoneda());
            transaccionError.setNumeroTarjeta(transaccion.getNumeroTarjeta());
            transaccionError.setNombreTitular(transaccion.getNombreTitular());
            transaccionError.setFrecuenciaDias(transaccion.getFrecuenciaDias());
            transaccionError.setPlazo(transaccion.getPlazo());
            transaccionError.setEstado("REC");
            transaccionError.setEstadoRecibo("PEN");
            
            Transaccion errorGuardado = this.repository.save(transaccionError);
            log.info("Transacción de error guardada: {}", errorGuardado.getCodTransaccion());
            
            throw new CommunicationException("Payment Gateway", e.getMessage());
        }
    }
    
    private ConfiguracionPos obtenerConfiguracionPOS() {
        try {
            return configuracionPosService.obtenerConfiguracionLocal();
        } catch (NotFoundException e) {
            log.error("No se encontró configuración para el terminal POS");
            throw new ValidationException("Terminal POS no configurado correctamente");
        }
    }
    
    private void validarTransaccion(Transaccion transaccion) {
        if (transaccion.getMonto() == null || transaccion.getMonto().doubleValue() <= 0) {
            log.error("El monto debe ser mayor a 0");
            throw new ValidationException("El monto debe ser mayor a 0");
        }
        
        if (transaccion.getNumeroTarjeta() == null || transaccion.getNumeroTarjeta().length() != 16) {
            log.error("El número de tarjeta debe tener 16 dígitos");
            throw new ValidationException("El número de tarjeta debe tener 16 dígitos");
        }
        
        if (transaccion.getNombreTitular() == null || transaccion.getNombreTitular().isEmpty()) {
            log.error("El nombre del titular es requerido");
            throw new ValidationException("El nombre del titular es requerido");
        }
        
        transaccion.setTipo("PAG");
        
        if (transaccion.getMarca() == null) {
            log.error("La marca de la tarjeta es requerida");
            throw new ValidationException("La marca de la tarjeta es requerida");
        }
        
        if ("DIF".equals(transaccion.getModalidad())) {
            if (transaccion.getPlazo() == null || transaccion.getPlazo() <= 0) {
                log.error("Para transacciones diferidas, el plazo es requerido y debe ser mayor a 0");
                throw new ValidationException("Para transacciones diferidas, el plazo es requerido y debe ser mayor a 0");
            }
        } else if ("REC".equals(transaccion.getModalidad())) {
            if (transaccion.getFrecuenciaDias() == null || transaccion.getFrecuenciaDias() <= 0) {
                log.error("Para transacciones recurrentes, la frecuencia es requerida y debe ser mayor a 0");
                throw new ValidationException("Para transacciones recurrentes, la frecuencia es requerida y debe ser mayor a 0");
            }
        }
        
        if (transaccion.getModalidad() == null || 
            (!"SIM".equals(transaccion.getModalidad()) && 
             !"REC".equals(transaccion.getModalidad()) && 
             !"DIF".equals(transaccion.getModalidad()))) {
            transaccion.setModalidad("SIM");
        }
        
        if (transaccion.getMoneda() == null) {
            transaccion.setMoneda("USD");
        }
    }
    
    private TransaccionResponseDTO enviarTransaccionPaymentGateway(Transaccion transaccion, ConfiguracionPos configuracion, String cvv, String fechaExpiracion) {
        TransaccionRequestDTO request = new TransaccionRequestDTO();
        request.setCodigoPOS(configuracion.getCodigoPos());
        request.setCodigoComercio(configuracion.getCodigoComercio());
        request.setTipo(transaccion.getTipo());
        request.setMarca(transaccion.getMarca());
        request.setModalidad(transaccion.getModalidad());
        request.setMonto(transaccion.getMonto());
        request.setMoneda(transaccion.getMoneda());
        request.setNumeroTarjeta(transaccion.getNumeroTarjeta());
        request.setNombreTitular(transaccion.getNombreTitular());
        request.setCodigoUnicoTransaccion(transaccion.getCodigoUnicoTransaccion());
        
        if (transaccion.getDetalle() != null && !transaccion.getDetalle().isEmpty()) {
            request.setReferencia(transaccion.getDetalle());
        }
        
        request.setPais("EC");
        
        if (cvv != null && !cvv.isEmpty()) {
            request.setCodigoSeguridad(Integer.parseInt(cvv));
        }
        
        if (fechaExpiracion != null && !fechaExpiracion.isEmpty()) {
            request.setFechaExpiracion(fechaExpiracion);
        }
        
        if ("DIF".equals(transaccion.getModalidad())) {
       
            request.setModalidad("DIF");
            request.setPlazo(transaccion.getPlazo());
            request.setRecurrente(false);
            request.setFrecuenciaDias(null);
        } else if ("REC".equals(transaccion.getModalidad())) {
        
            request.setModalidad("REC");
            request.setRecurrente(true);
            request.setFrecuenciaDias(transaccion.getFrecuenciaDias());
            request.setPlazo(null);
        } else {
       
            request.setModalidad("SIM");
            request.setRecurrente(false);
            request.setFrecuenciaDias(null);
            request.setPlazo(null);
        }
        
        String requestId = UUID.randomUUID().toString();
        return paymentGatewayClient.procesarTransaccion(requestId, request);
    }
    
    private String generarCodigoTransaccion() {
        return "TRX" + String.format("%07d", (int) (Math.random() * 10000000));
    }
} 