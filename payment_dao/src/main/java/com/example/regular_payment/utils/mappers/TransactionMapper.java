package com.example.regular_payment.utils.mappers;

import com.example.regular_payment.dtos.TransactionCreateDTO;
import com.example.regular_payment.dtos.TransactionDTO;
import com.example.regular_payment.models.Instruction;
import com.example.regular_payment.models.Transaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface TransactionMapper {

    @Mapping(target = "instructionId", source = "instruction", qualifiedByName = "toInstructionId")
    @Mapping(target = "transactionStatus", source = "transactionStatus")
    TransactionDTO toDTO(Transaction transaction);

    @Mapping(target = "instruction", ignore = true)
    @Mapping(target = "transactionStatus", source = "transactionStatus")
    Transaction toEntity(TransactionDTO transactionDTO);

    @Mapping(target = "id", ignore = true)
    Transaction toEntity(TransactionCreateDTO dto);

    @Named("toInstructionId")
    default Long toInstructionId(Instruction instruction) {
        if (instruction == null) {
            return null;
        }

        return instruction.getId();
    }
}
