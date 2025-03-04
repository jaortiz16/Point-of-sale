package com.banquito.pos.client.dto;

import java.math.BigDecimal;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TransaccionRequestDTO {
    
    private String codigoPOS;
    private String codigoComercio;
    private String tipo;
    private String marca;
    private String modalidad;
    private BigDecimal monto;
    private String moneda;
    private String pais;
    private Integer plazo;
    private String numeroTarjeta;
    private String nombreTitular;
    private Integer codigoSeguridad;
    private String fechaExpiracion;
    private String codigoUnicoTransaccion;
    private String referencia;
    private Boolean recurrente;
    private Integer frecuenciaDias;
} 