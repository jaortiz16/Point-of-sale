package com.banquito.pos.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "POS_TRANSACCION")
@Getter
@Setter
@NoArgsConstructor
@ToString
public class Transaccion implements Serializable {

    @Id
    @Column(name = "COD_TRANSACCION", length = 10, nullable = false)
    private String codTransaccion;

    @Column(name = "TIPO", length = 3, nullable = false)
    private String tipo;

    @Column(name = "MARCA", length = 4, nullable = false)
    private String marca;

    @Column(name = "MODALIDAD", length = 3, nullable = false)
    private String modalidad;

    @Column(name = "DETALLE", length = 50)
    private String detalle;

    @Column(name = "MONTO", precision = 20, scale = 2, nullable = false)
    private BigDecimal monto;

    @Column(name = "CODIGO_UNICO_TRANSACCION", length = 64)
    private String codigoUnicoTransaccion;

    @Column(name = "FECHA", nullable = false)
    private LocalDateTime fecha;

    @Column(name = "ESTADO", length = 3, nullable = false)
    private String estado;

    @Column(name = "ESTADO_RECIBO", length = 3)
    private String estadoRecibo;

    @Column(name = "MONEDA", length = 3, nullable = false)
    private String moneda;
    
    @Transient
    private Integer plazo;
    
    @Transient
    private String numeroTarjeta;
    
    @Transient
    private String nombreTitular;
    
    @Transient
    private Boolean recurrente;
    
    @Transient
    private Integer frecuenciaDias;

    public Transaccion(String codTransaccion) {
        this.codTransaccion = codTransaccion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transaccion that = (Transaccion) o;
        return Objects.equals(codTransaccion, that.codTransaccion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(codTransaccion);
    }
} 