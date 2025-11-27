package com.example.regular_payment.utils.mappers;

import com.example.regular_payment.models.Instruction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TransactionMapperTest {

    private TransactionMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = Mappers.getMapper(TransactionMapper.class);
    }

    @Test
    void toInstructionId_shouldReturnIdWhenInstructionIsPresent() {

        Long expectedId = 77L;
        Instruction mockInstruction = mock(Instruction.class);

        when(mockInstruction.getId()).thenReturn(expectedId);

        Long result = mapper.toInstructionId(mockInstruction);

        assertEquals(expectedId, result);
    }

    @Test
    void toInstructionId_shouldReturnNullWhenInstructionIsNull() {

        Long result = mapper.toInstructionId(null);

        assertNull(result);
    }

    @Test
    void toInstructionId_shouldReturnNullWhenInstructionIdIsNull() {

        Instruction mockInstruction = mock(Instruction.class);
        when(mockInstruction.getId()).thenReturn(null);

        Long result = mapper.toInstructionId(mockInstruction);

        assertNull(result);
    }
}
