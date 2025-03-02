package com.banquito.pos.controller.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Schema(description = "Información de una transacción en el POS")
public class TransaccionDTO {
    
    @Schema(description = "Código de la transacción (generado automáticamente)", example = "TRX001")
    private String codTransaccion;
    
    @NotBlank(message = "El tipo de transacción es requerido")
    @Size(max = 3, message = "El tipo no puede exceder los 3 caracteres")
    @Pattern(regexp = "PAG", message = "El tipo debe ser PAG (Pago)")
    @Schema(description = "Tipo de transacción: PAG (Pago)", example = "PAG")
    private String tipo;
    
    @NotBlank(message = "La marca de la tarjeta es requerida")
    @Size(max = 4, message = "La marca no puede exceder los 4 caracteres")
    @Pattern(regexp = "VISA|MAST|AMEX|DISC", message = "La marca debe ser VISA, MAST (Mastercard), AMEX (American Express) o DISC (Discover)")
    @Schema(description = "Marca de la tarjeta: VISA, MAST (Mastercard), AMEX (American Express), DISC (Discover)", example = "VISA")
    private String marca;
    
    @NotBlank(message = "La modalidad es requerida")
    @Size(max = 3, message = "La modalidad no puede exceder los 3 caracteres")
    @Pattern(regexp = "SIM|REC", message = "La modalidad debe ser SIM (Simple) o REC (Recurrente)")
    @Schema(description = "Modalidad de pago: SIM (Simple), REC (Recurrente)", example = "SIM")
    private String modalidad;
    
    @Size(max = 50, message = "El detalle no puede exceder los 50 caracteres")
    @Schema(description = "Detalle adicional de la transacción", example = "Compra de productos electrónicos")
    private String detalle;
    
    @NotNull(message = "El monto es requerido")
    @DecimalMin(value = "0.01", message = "El monto debe ser mayor a cero")
    @Digits(integer = 18, fraction = 2, message = "El monto no puede tener más de 18 dígitos enteros y 2 decimales")
    @Schema(description = "Monto de la transacción", example = "125.50")
    private BigDecimal monto;
    
    @Schema(description = "Código único de la transacción (generado automáticamente)", example = "8a7b6c5d-4e3f-2g1h-0i9j-8k7l6m5n4o3p")
    private String codigoUnicoTransaccion;
    
    @Schema(description = "Fecha de la transacción", example = "2023-01-01T10:30:00")
    private LocalDateTime fecha;
    
    @Schema(description = "Estado de la transacción: AUT (Autorizada), REC (Rechazada), ENV (Enviada)", example = "AUT")
    private String estado;
    
    @Schema(description = "Estado del recibo: IMP (Imprimido), PEN (Pendiente)", example = "PEN")
    private String estadoRecibo;
    
    @NotBlank(message = "La moneda es requerida")
    @Size(max = 3, message = "La moneda no puede exceder los 3 caracteres")
    @Pattern(regexp = "USD|EUR|MXN", message = "La moneda debe ser USD, EUR o MXN")
    @Schema(description = "Moneda de la transacción: USD, EUR, MXN", example = "USD")
    private String moneda;
    
    @Pattern(regexp = "^[0-9]{16}$", message = "El número de tarjeta debe tener 16 dígitos")
    @Schema(description = "Número de tarjeta (enmascarado)", example = "************1234")
    private String numeroTarjeta;
    
    @Schema(description = "Nombre del titular de la tarjeta", example = "Juan Pérez")
    private String nombreTitular;
    
    @Min(value = 1, message = "La frecuencia debe ser mayor a cero")
    @Schema(description = "Frecuencia en días para transacciones recurrentes", example = "30")
    private Integer frecuenciaDias;
    
    @Min(value = 1, message = "El plazo debe ser mayor a cero")
    @Schema(description = "Número de meses para pagos diferidos (solo para modalidad SIM)", example = "12")
    private Integer plazo;
    
    @Pattern(regexp = "^[0-9]{3,4}$", message = "El CVV debe tener 3 o 4 dígitos")
    @Schema(description = "Código de seguridad de la tarjeta (CVV/CVC)", example = "123")
    private String cvv;
    
    @Pattern(regexp = "^(0[1-9]|1[0-2])/[0-9]{2}$", message = "La fecha de expiración debe tener formato MM/YY")
    @Schema(description = "Fecha de expiración de la tarjeta (MM/YY)", example = "12/25")
    private String fechaExpiracion;
} 