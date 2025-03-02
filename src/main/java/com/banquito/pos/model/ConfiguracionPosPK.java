package com.banquito.pos.model;

import java.io.Serializable;
import java.util.Objects;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ConfiguracionPosPK implements Serializable {
    
    private String modelo;
    private String codigoPos;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConfiguracionPosPK that = (ConfiguracionPosPK) o;
        return Objects.equals(modelo, that.modelo) && 
               Objects.equals(codigoPos, that.codigoPos);
    }

    @Override
    public int hashCode() {
        return Objects.hash(modelo, codigoPos);
    }
} 