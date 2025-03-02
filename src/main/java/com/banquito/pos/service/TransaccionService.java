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
        
        // Configuración inicial de la transacción
        transaccion.setCodTransaccion(codTransaccion);
        transaccion.setCodigoUnicoTransaccion(uuid);
        transaccion.setFecha(LocalDateTime.now());
        
        // Establecer estado inicial como ENV (enviado)
        transaccion.setEstado("ENV");
        transaccion.setEstadoRecibo("PEN");
        
        // Asegurar que el tipo sea PAG
        transaccion.setTipo("PAG");
        
        // Asegurar que la modalidad sea SIM o REC
        if (!"SIM".equals(transaccion.getModalidad()) && !"REC".equals(transaccion.getModalidad())) {
            transaccion.setModalidad("SIM");
        }
        
        // Guardar la transacción inicial con estado ENV
        Transaccion transaccionEnviada = this.repository.save(transaccion);
        log.info("Transacción guardada con estado ENV: {}", transaccionEnviada.getCodTransaccion());
        
        try {
            // Crear una nueva instancia para la transacción de respuesta
            Transaccion transaccionRespuesta = new Transaccion();
            
            // Copiar datos de la transacción original
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
            
            // Estado inicial del recibo para la segunda transacción
            transaccionRespuesta.setEstadoRecibo("PEN");
            
            // Enviar al gateway y procesar respuesta
            TransaccionResponseDTO respuesta = enviarTransaccionPaymentGateway(transaccion, configuracion, cvv, fechaExpiracion);
            
            // Establecer estado según respuesta
            if ("APR".equals(respuesta.getEstado())) {
                // Si fue aprobada en el gateway, es autorizada (AUT) en el sistema
                transaccionRespuesta.setEstado("AUT");
            } else {
                // Si fue rechazada o cualquier otro estado, es rechazada (REC) en el sistema
                transaccionRespuesta.setEstado("REC");
                transaccionRespuesta.setDetalle(respuesta.getMensaje());
            }
            
            // Guardar la segunda transacción con el estado actualizado
            Transaccion transaccionFinal = this.repository.save(transaccionRespuesta);
            log.info("Transacción de respuesta guardada con estado {}: {}", 
                    transaccionFinal.getEstado(), transaccionFinal.getCodTransaccion());
            
            return transaccionFinal;
            
        } catch (FeignException e) {
            log.error("Error al comunicarse con el Payment Gateway: {}", e.getMessage());
            
            // Crear transacción de respuesta con error
            Transaccion transaccionError = new Transaccion();
            
            // Copiar datos de la transacción original
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
            
            // Guardar la transacción de error
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
        
        // Forzar tipo PAG
        transaccion.setTipo("PAG");
        
        if (transaccion.getMarca() == null) {
            log.error("La marca de la tarjeta es requerida");
            throw new ValidationException("La marca de la tarjeta es requerida");
        }
        
        // Para transacciones recurrentes, verificar frecuencia
        if ("REC".equals(transaccion.getModalidad()) && (transaccion.getFrecuenciaDias() == null || transaccion.getFrecuenciaDias() < 1)) {
            log.error("La frecuencia para una transacción recurrente debe ser mayor a 0");
            throw new ValidationException("La frecuencia para una transacción recurrente debe ser mayor a 0");
        }
        
        // Si la modalidad no es SIM o REC, establecer SIM por defecto
        if (transaccion.getModalidad() == null || (!"SIM".equals(transaccion.getModalidad()) && !"REC".equals(transaccion.getModalidad()))) {
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
        
        // Enviar datos sensibles al gateway (no se almacenan en la BD)
        if (cvv != null && !cvv.isEmpty()) {
            request.setCodigoSeguridad(Integer.parseInt(cvv));
        }
        
        if (fechaExpiracion != null && !fechaExpiracion.isEmpty()) {
            request.setFechaExpiracion(fechaExpiracion);
        }
        
        // Si es recurrente, enviar los datos correspondientes
        if ("REC".equals(transaccion.getModalidad())) {
            request.setRecurrente(true);
            request.setFrecuenciaDias(transaccion.getFrecuenciaDias());
        }
        
        // Si es diferido (modalidad SIM con plazo), enviar el plazo
        if ("SIM".equals(transaccion.getModalidad()) && transaccion.getPlazo() != null) {
            request.setPlazo(transaccion.getPlazo());
        }
        
        String requestId = UUID.randomUUID().toString();
        return paymentGatewayClient.procesarTransaccion(requestId, request);
    }
    
    private String generarCodigoTransaccion() {
        return "TRX" + String.format("%07d", (int) (Math.random() * 10000000));
    }
} 