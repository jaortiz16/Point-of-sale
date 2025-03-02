package com.banquito.pos.controller.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

import com.banquito.pos.controller.dto.ConfiguracionPosDTO;
import com.banquito.pos.model.ConfiguracionPos;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface ConfiguracionPosMapper {
    
    ConfiguracionPosDTO toDTO(ConfiguracionPos model);
    
    ConfiguracionPos toModel(ConfiguracionPosDTO dto);
} 