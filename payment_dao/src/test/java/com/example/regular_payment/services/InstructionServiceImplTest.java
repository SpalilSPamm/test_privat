package com.example.regular_payment.services;

import com.example.regular_payment.dtos.InstructionDTO;
import com.example.regular_payment.models.Instruction;
import com.example.regular_payment.repositories.InstructionRepository;
import com.example.regular_payment.services.impl.InstructionServiceImpl;
import com.example.regular_payment.utils.enums.InstructionStatus;
import com.example.regular_payment.utils.exceptions.InstructionNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@SpringBootTest
@Transactional
@Testcontainers
public class InstructionServiceImplTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

    @MockitoBean
    private Clock clock;

    @Autowired
    private InstructionServiceImpl instructionService;

    @Autowired
    private InstructionRepository instructionRepository;

    @BeforeEach
    void setUp() {
        instructionRepository.deleteAll();
    }

    @Test
    void shouldSaveInstructionSuccessfully() {
        Instruction instruction = new Instruction();
        instruction.setPayerFirstName("Taras");
        instruction.setPayerSecondName("Ivanko");
        instruction.setPayerPatronymic("Tarasovich");
        instruction.setAmount(new BigDecimal("100.50"));
        instruction.setPayerIin("12345");
        instruction.setPayerCardNumber("1234567812345678");
        instruction.setRecipientSettlementAccount("12345678123456781234567812345");
        instruction.setRecipientBankCode("000000");
        instruction.setRecipientEdrpou("12345678");
        instruction.setRecipientName("Taras Ivanko");
        instruction.setPeriodUnit(ChronoUnit.MONTHS);
        instruction.setPeriodValue(1);
        instruction.setNextExecutionAt(OffsetDateTime.now().plusDays(1));
        instruction.setLastExecutionAt(null);
        instruction.setInstructionStatus(InstructionStatus.ACTIVE);

        Instruction savedInstruction = instructionService.saveInstruction(instruction);

        assertThat(savedInstruction).isNotNull();
        assertThat(savedInstruction.getId()).isNotNull();
        assertThat(savedInstruction.getPayerFirstName()).isEqualTo("Taras");
        assertThat(savedInstruction.getLastExecutionAt()).isNull();
        assertThat(savedInstruction.getInstructionStatus()).isEqualTo(InstructionStatus.ACTIVE);

        Optional<Instruction> fetchedInstruction = instructionRepository.findById(savedInstruction.getId());

        assertThat(fetchedInstruction).isPresent();
        assertThat(fetchedInstruction.get().getAmount()).isEqualByComparingTo(new BigDecimal("100.50"));
        assertThat(fetchedInstruction.get().getPeriodUnit()).isEqualTo(ChronoUnit.MONTHS);
    }

    @Test
    void shouldThrowExceptionWhenNextExecutionAtIsNull() {

        Instruction instruction = new Instruction();
        instruction.setPayerFirstName("Taras");
        instruction.setPayerSecondName("Ivanko");
        instruction.setPayerPatronymic("Tarasovich");
        instruction.setAmount(new BigDecimal("100.50"));
        instruction.setPayerIin("12345");
        instruction.setPayerCardNumber("1234567812345678");
        instruction.setRecipientSettlementAccount("12345678123456781234567812345");
        instruction.setRecipientBankCode("000000");
        instruction.setRecipientEdrpou("12345678");
        instruction.setRecipientName("Taras Ivanko");
        instruction.setPeriodUnit(ChronoUnit.MONTHS);
        instruction.setPeriodValue(1);
        instruction.setNextExecutionAt(null);
        instruction.setLastExecutionAt(null);
        instruction.setInstructionStatus(InstructionStatus.ACTIVE);

        assertThatThrownBy(() ->  instructionService.saveInstruction(instruction))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void shouldUpdateExistingInstructionSuccessfully() {

        Instruction originalInstruction = new Instruction();
        originalInstruction.setPayerFirstName("Taras");
        originalInstruction.setPayerSecondName("Ivanko");
        originalInstruction.setPayerPatronymic("Tarasovich");
        originalInstruction.setAmount(new BigDecimal("100.50"));
        originalInstruction.setPayerIin("12345");
        originalInstruction.setPayerCardNumber("1234567812345678");
        originalInstruction.setRecipientSettlementAccount("12345678123456781234567812345");
        originalInstruction.setRecipientBankCode("000000");
        originalInstruction.setRecipientEdrpou("12345678");
        originalInstruction.setRecipientName("Taras Ivanko");
        originalInstruction.setPeriodUnit(ChronoUnit.DAYS);
        originalInstruction.setPeriodValue(1);
        originalInstruction.setNextExecutionAt(OffsetDateTime.now().plusDays(1));
        originalInstruction.setLastExecutionAt(null);
        originalInstruction.setInstructionStatus(InstructionStatus.ACTIVE);

        Instruction savedOriginal = instructionRepository.save(originalInstruction);
        Long id = savedOriginal.getId();

        OffsetDateTime nextExecutionAt = OffsetDateTime.now().plusDays(5).truncatedTo(ChronoUnit.MILLIS);
        OffsetDateTime lastExecutionAt = OffsetDateTime.now().truncatedTo(ChronoUnit.MILLIS);

        InstructionDTO updateData = new InstructionDTO(
                id,
                "NewName",
                "NewSecondName",
                "Tarasovich",
                "12345",
                "1234567812345678",
                "12345678123456781234567812345",
                "000000",
                "12345678",
                "Taras Ivanko",
                new BigDecimal("500.50"),
                1,
                ChronoUnit.DAYS,
                lastExecutionAt,
                nextExecutionAt,
                InstructionStatus.ACTIVE
        );

        Instruction result = instructionService.updateInstruction(id, updateData);

        assertThat(result.getId()).isEqualTo(id);
        assertThat(result.getPayerFirstName()).isEqualTo("NewName");
        assertThat(result.getAmount()).isEqualByComparingTo(new BigDecimal("500.50"));

        Optional<Instruction> fetchedFromDb = instructionRepository.findById(id);
        assertThat(fetchedFromDb).isPresent();
        Instruction inDb = fetchedFromDb.get();

        assertThat(inDb.getPayerFirstName()).isEqualTo("NewName");
        assertThat(inDb.getAmount()).isEqualByComparingTo(new BigDecimal("500.50"));

        assertThat(inDb.getNextExecutionAt()).isEqualTo(nextExecutionAt);
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonExistentId() {

        Long nonExistentId = 99999L;
        InstructionDTO updateData = new InstructionDTO(
                nonExistentId,
                "Ghost",
                "User",
                "Patronymic",
                "1234567890",
                "1111222233334444",
                "UA12345678901234567890123456",
                "300711",
                "12345678",
                "Recipient Name",
                new BigDecimal("150.75"),
                1,
                ChronoUnit.DAYS,
                OffsetDateTime.now().truncatedTo(ChronoUnit.MILLIS),
                OffsetDateTime.now().plusDays(5).truncatedTo(ChronoUnit.MILLIS),
                InstructionStatus.ACTIVE
        );

        assertThatThrownBy(() -> instructionService.updateInstruction(nonExistentId, updateData))
                .isInstanceOf(InstructionNotFoundException.class)
                .hasMessageContaining("Instruction with ID 99999 not found in PDS.");
    }

    @Test
    void shouldSoftDeleteInstructionChangeStatusToCanceled() {

        Instruction instruction = new Instruction();
        instruction.setPayerFirstName("Taras");
        instruction.setPayerSecondName("Ivanko");
        instruction.setPayerPatronymic("Tarasovich");
        instruction.setAmount(new BigDecimal("100.50"));
        instruction.setPayerIin("12345");
        instruction.setPayerCardNumber("1234567812345678");
        instruction.setRecipientSettlementAccount("12345678123456781234567812345");
        instruction.setRecipientBankCode("000000");
        instruction.setRecipientEdrpou("12345678");
        instruction.setRecipientName("Taras Ivanko");
        instruction.setPeriodUnit(ChronoUnit.DAYS);
        instruction.setPeriodValue(1);
        instruction.setNextExecutionAt(OffsetDateTime.now().plusDays(1));
        instruction.setLastExecutionAt(null);
        instruction.setInstructionStatus(InstructionStatus.ACTIVE);

        Instruction savedInstruction = instructionRepository.save(instruction);
        Long id = savedInstruction.getId();

        instructionService.deleteInstruction(id);

        Optional<Instruction> deletedInstruction = instructionRepository.findById(id);

        assertThat(deletedInstruction).isPresent();

        assertThat(deletedInstruction.get().getInstructionStatus())
                .isEqualTo(InstructionStatus.CANCELED);
    }

    @Test
    void shouldUpdateExecutionTimesCorrectlyAndPersist() {

        when(clock.instant()).thenReturn(Instant.parse("2025-11-26T10:00:00Z"));
        when(clock.getZone()).thenReturn(ZoneOffset.ofHours(2));

        OffsetDateTime fixedNow = OffsetDateTime.ofInstant(Instant.parse("2025-11-26T10:00:00Z"), ZoneOffset.ofHours(2));

        Instruction originalInstruction = new Instruction();
        originalInstruction.setPayerFirstName("Taras");
        originalInstruction.setPayerSecondName("Ivanko");
        originalInstruction.setPayerPatronymic("Tarasovich");
        originalInstruction.setAmount(new BigDecimal("100.50"));
        originalInstruction.setPayerIin("12345");
        originalInstruction.setPayerCardNumber("1234567812345678");
        originalInstruction.setRecipientSettlementAccount("12345678123456781234567812345");
        originalInstruction.setRecipientBankCode("000000");
        originalInstruction.setRecipientEdrpou("12345678");
        originalInstruction.setRecipientName("Taras Ivanko");
        originalInstruction.setPeriodUnit(ChronoUnit.DAYS);
        originalInstruction.setPeriodValue(1);
        originalInstruction.setNextExecutionAt(fixedNow);
        originalInstruction.setLastExecutionAt(fixedNow.minusDays(1));
        originalInstruction.setInstructionStatus(InstructionStatus.ACTIVE);

        Instruction savedOriginal = instructionRepository.save(originalInstruction);
        Long savedInstructionId = savedOriginal.getId();

        Instruction instructionToUpdate = instructionRepository.findById(savedInstructionId)
                .orElseThrow();

        OffsetDateTime expectedNextExecutionAt = fixedNow.plus(instructionToUpdate.getPeriodValue(), instructionToUpdate.getPeriodUnit());

        instructionService.updateLastAndNextExecutionTime(instructionToUpdate.getId(),
                fixedNow, expectedNextExecutionAt);

        Optional<Instruction> updatedInstructionOpt = instructionRepository.findById(savedInstructionId);
        assertTrue(updatedInstructionOpt.isPresent());
        Instruction persistedInstruction = updatedInstructionOpt.get();

        assertEquals(fixedNow.toInstant(), persistedInstruction.getLastExecutionAt().toInstant());

        assertEquals(expectedNextExecutionAt.toInstant(), persistedInstruction.getNextExecutionAt().toInstant());
    }

    @Test
    void shouldThrowInstructionNotFoundExceptionWhenInstructionIdIsNull() {

        Instruction instruction = new Instruction();

        assertThrows(InstructionNotFoundException.class,
                () -> instructionService.updateLastAndNextExecutionTime(instruction.getId(), null, null));
    }

    @Test
    void shouldThrowInstructionNotFoundExceptionWhenInstructionDoesNotExistInDB() {

        Long nonExistentId = 999L;
        Instruction instruction = new Instruction();
        instruction.setId(nonExistentId);

        InstructionNotFoundException exception = assertThrows(InstructionNotFoundException.class,
                () -> instructionService.updateLastAndNextExecutionTime(instruction.getId(), null, null));

        assertTrue(exception.getMessage().contains("Instruction with ID 999 not found"));
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistentInstruction() {

        Long nonExistentId = 12345L;

        assertThatThrownBy(() -> instructionService.deleteInstruction(nonExistentId))
                .isInstanceOf(InstructionNotFoundException.class)
                .hasMessageContaining("Instruction with ID 12345 not found in PDS.");
    }

    @Test
    void shouldReturnInstructionWhenExists() {

        Instruction instruction = new Instruction();
        instruction.setPayerFirstName("Taras");
        instruction.setPayerSecondName("Ivanko");
        instruction.setPayerPatronymic("Tarasovich");
        instruction.setAmount(new BigDecimal("100.50"));
        instruction.setPayerIin("12345");
        instruction.setPayerCardNumber("1234567812345678");
        instruction.setRecipientSettlementAccount("12345678123456781234567812345");
        instruction.setRecipientBankCode("000000");
        instruction.setRecipientEdrpou("12345678");
        instruction.setRecipientName("Taras Ivanko");
        instruction.setPeriodUnit(ChronoUnit.DAYS);
        instruction.setPeriodValue(1);
        instruction.setNextExecutionAt(OffsetDateTime.now().plusDays(1));
        instruction.setLastExecutionAt(null);
        instruction.setInstructionStatus(InstructionStatus.ACTIVE);

        Instruction savedInstruction = instructionRepository.save(instruction);
        Long existingId = savedInstruction.getId();

        Instruction foundInstruction = instructionService.getInstruction(existingId);

        assertThat(foundInstruction).isNotNull();
        assertThat(foundInstruction.getId()).isEqualTo(existingId);
        assertThat(foundInstruction.getPayerFirstName()).isEqualTo("Taras");
        assertThat(foundInstruction.getAmount()).isEqualByComparingTo(new BigDecimal("100.50"));
    }

    @Test
    void shouldThrowInstructionNotFoundExceptionWhenIdDoesNotExist() {

        Long nonExistentId = 99999L;

        assertThatThrownBy(() -> instructionService.getInstruction(nonExistentId))
                .isInstanceOf(InstructionNotFoundException.class)
                .hasMessage("Instruction with ID " + nonExistentId + " not found in PDS.");
    }

    @Test
    void shouldReturnOnlyInstructionsWithSpecificIin() {

        String targetIin = "1111111111";
        String otherIin = "2222222222";

        createAndSaveInstructionWithTestEdrpouAndIin("01234567", targetIin,"User A");
        createAndSaveInstructionWithTestEdrpouAndIin("87654321", targetIin, "User A");

        createAndSaveInstructionWithTestEdrpouAndIin("01234567", otherIin,"User B");

        List<Instruction> result = instructionService.getInstructionsByIin(targetIin);

        assertThat(result)
                .hasSize(2)
                .extracting(Instruction::getPayerIin)
                .containsExactly(targetIin, targetIin);
    }

    @Test
    void shouldReturnEmptyListWhenNoInstructionsFound() {

        createAndSaveInstructionWithTestEdrpouAndIin("01234567","5555555555", "User C");

        List<Instruction> result = instructionService.getInstructionsByIin("9999999999");

        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnInstructionsOnlyForSpecificEdrpou() {

        String targetEdrpou = "12345678";
        String noiseEdrpou = "87654321";

        createAndSaveInstructionWithTestEdrpouAndIin(targetEdrpou, "1111111111","Company A");
        createAndSaveInstructionWithTestEdrpouAndIin(targetEdrpou, "2222222222","Company A_2");

        createAndSaveInstructionWithTestEdrpouAndIin(noiseEdrpou, "1111111111","Company B");

        List<Instruction> result = instructionService.getInstructionsByEdrpou(targetEdrpou);

        assertThat(result)
                .hasSize(2)
                .extracting(Instruction::getRecipientEdrpou)
                .containsExactly(targetEdrpou, targetEdrpou);
    }

    @Test
    void shouldReturnEmptyListWhenEdrpouNotFound() {

        createAndSaveInstructionWithTestEdrpouAndIin("12345678", "1111111111","Name");

        List<Instruction> result = instructionService.getInstructionsByEdrpou("00000000");

        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnOnlyActiveInstructions() {

        when(clock.instant()).thenReturn(Instant.parse("2026-11-29T10:00:00Z"));
        when(clock.getZone()).thenReturn(ZoneOffset.ofHours(2));

        createAndSaveInstruction( InstructionStatus.ACTIVE, "1111111118");
        createAndSaveInstruction(InstructionStatus.ACTIVE, "2222222222");

        createAndSaveInstruction(InstructionStatus.CANCELED,"3333333333");

        assertEquals(3, instructionRepository.count());

        List<Instruction> activeInstructions = instructionService.getAllActiveInstructions();

        assertEquals(2, activeInstructions.size());

        activeInstructions.forEach(instruction -> assertEquals(InstructionStatus.ACTIVE, instruction.getInstructionStatus()));
    }

    @Test
    void shouldReturnEmptyListWhenNoActiveInstructionsExist() {

        when(clock.instant()).thenReturn(Instant.parse("2025-11-26T10:00:00Z"));
        when(clock.getZone()).thenReturn(ZoneOffset.ofHours(2));

        createAndSaveInstruction( InstructionStatus.CANCELED, "1111111118");
        createAndSaveInstruction(InstructionStatus.CANCELED, "2222222222");

        assertEquals(2, instructionRepository.count(), "2222222222");

        List<Instruction> activeInstructions = instructionService.getAllActiveInstructions();

        assertTrue(activeInstructions.isEmpty());
    }

    private void createAndSaveInstruction(InstructionStatus status, String iin) {

        Instruction instruction = new Instruction();

        instruction.setPayerFirstName("Taras");
        instruction.setPayerSecondName("Ivanko");
        instruction.setPayerPatronymic("Tarasovich");
        instruction.setAmount(new BigDecimal("100.50"));
        instruction.setPayerIin(iin);
        instruction.setPayerCardNumber("1234567812345678");
        instruction.setRecipientSettlementAccount("12345678123456781234567812345");
        instruction.setRecipientBankCode("000000");
        instruction.setRecipientEdrpou("12345678");
        instruction.setRecipientName("Mykola");
        instruction.setPeriodUnit(ChronoUnit.DAYS);
        instruction.setPeriodValue(1);
        instruction.setNextExecutionAt(OffsetDateTime.ofInstant(Instant.parse("2025-11-26T10:00:00Z"), ZoneOffset.ofHours(2)));
        instruction.setLastExecutionAt(null);
        instruction.setInstructionStatus(status);

        instructionRepository.save(instruction);
    }

    private void createAndSaveInstructionWithTestEdrpouAndIin(String edrpou, String iin, String firstName) {
        Instruction instruction = new Instruction();
        instruction.setPayerFirstName(firstName);
        instruction.setPayerSecondName("Ivanko");
        instruction.setPayerPatronymic("Tarasovich");
        instruction.setAmount(new BigDecimal("100.50"));
        instruction.setPayerIin(iin);
        instruction.setPayerCardNumber("1234567812345678");
        instruction.setRecipientSettlementAccount("12345678123456781234567812345");
        instruction.setRecipientBankCode("000000");
        instruction.setRecipientEdrpou(edrpou);
        instruction.setRecipientName("Mykola");
        instruction.setPeriodUnit(ChronoUnit.DAYS);
        instruction.setPeriodValue(1);
        instruction.setNextExecutionAt(OffsetDateTime.now().plusDays(1));
        instruction.setLastExecutionAt(null);
        instruction.setInstructionStatus(InstructionStatus.ACTIVE);

        instructionRepository.save(instruction);
    }
}
