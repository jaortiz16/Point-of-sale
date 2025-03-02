package com.banquito.pos.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "POS_CONFIGURACION")
@IdClass(ConfiguracionPosPK.class)
@Getter
@Setter
@NoArgsConstructor
@ToString
public class ConfiguracionPos implements Serializable {

    @Id
    @Column(name = "MODELO", length = 10, nullable = false)
    private String modelo;

    @Id
    @Column(name = "CODIGO_POS", length = 10, nullable = false)
    private String codigoPos;

    @Column(name = "DIRECCION_MAC", length = 32, nullable = false)
    private String direccionMac;

    @Column(name = "CODIGO_COMERCIO", length = 10, nullable = false)
    private String codigoComercio;

    @Column(name = "FECHA_ACTIVACION", nullable = false)
    private LocalDateTime fechaActivacion;

    public ConfiguracionPos(String modelo, String codigoPos) {
        this.modelo = modelo;
        this.codigoPos = codigoPos;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConfiguracionPos that = (ConfiguracionPos) o;
        return Objects.equals(modelo, that.modelo) && 
               Objects.equals(codigoPos, that.codigoPos);
    }

    @Override
    public int hashCode() {
        return Objects.hash(modelo, codigoPos);
    }
} 