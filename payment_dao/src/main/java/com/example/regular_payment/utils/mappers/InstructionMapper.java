package com.example.regular_payment.utils.mappers;

import com.example.regular_payment.dtos.InstructionCreateDTO;
import com.example.regular_payment.dtos.InstructionDTO;
import com.example.regular_payment.models.Instruction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface InstructionMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "transactions", ignore = true)
    void updateEntityFromDto(InstructionDTO dto, @MappingTarget Instruction entity);

    InstructionDTO toDTO(Instruction instruction);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "transactions", ignore = true)
    Instruction toEntity(InstructionCreateDTO instructionCreateDTO);
}
