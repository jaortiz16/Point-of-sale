package com.banquito.pos.controller.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

import com.banquito.pos.controller.dto.TransaccionDTO;
import com.banquito.pos.model.Transaccion;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface TransaccionMapper {
    
    TransaccionDTO toDTO(Transaccion model);
    
    Transaccion toModel(TransaccionDTO dto);
} 