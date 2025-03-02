package com.banquito.pos.client.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TransaccionResponseDTO {
    
    private String codigoAutorizacion;
    private String estado;
    private String mensaje;
    private String codigoUnicoTransaccion;
    private String idTransaccion;
} 