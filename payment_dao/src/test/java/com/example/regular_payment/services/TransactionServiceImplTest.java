package com.example.regular_payment.services;

import com.example.regular_payment.models.Instruction;
import com.example.regular_payment.models.Transaction;
import com.example.regular_payment.repositories.InstructionRepository;
import com.example.regular_payment.repositories.TransactionRepository;
import com.example.regular_payment.utils.enums.InstructionStatus;
import com.example.regular_payment.utils.enums.TransactionStatus;
import com.example.regular_payment.utils.exceptions.TransactionNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
@Testcontainers
public class TransactionServiceImplTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private InstructionRepository instructionRepository;

    @BeforeEach
    void setUp() {
        instructionRepository.deleteAll();
        transactionRepository.deleteAll();
    }

    @Test
    void shouldCreateTransactionSuccessfully() {

        Instruction savedInstruction = createAndSaveInstructionWithFirstName("Taras", "12345");

        Transaction transaction = new Transaction();
        transaction.setAmount(new BigDecimal("150.00"));
        transaction.setTransactionStatus(TransactionStatus.ACTIVE.getStatusCode()); // Припустимо, є такий статус
        transaction.setIdempotencyId(UUID.randomUUID().toString());
        transaction.setTransactionTime(OffsetDateTime.now().truncatedTo(ChronoUnit.MILLIS));

        transaction.setInstruction(savedInstruction);

        Transaction createdTransaction = transactionService.createTransaction(transaction);

        assertThat(createdTransaction.getId()).isNotNull();
        assertThat(createdTransaction.getAmount()).isEqualByComparingTo(new BigDecimal("150.00"));
        assertThat(createdTransaction.getIdempotencyId()).isEqualTo(transaction.getIdempotencyId());

        Optional<Transaction> fetchedFromDb = transactionRepository.findById(createdTransaction.getId());

        assertThat(fetchedFromDb).isPresent();
        assertThat(fetchedFromDb.get().getInstruction().getId()).isEqualTo(savedInstruction.getId());
        assertThat(fetchedFromDb.get().getTransactionTime()).isEqualTo(transaction.getTransactionTime());
    }

    @Test
    void shouldThrowExceptionWhenSavingTransactionWithoutInstruction() {

        Transaction transaction = new Transaction();
        transaction.setAmount(new BigDecimal("150.00"));
        transaction.setTransactionStatus(TransactionStatus.ACTIVE.getStatusCode()); // Припустимо, є такий статус
        transaction.setIdempotencyId(UUID.randomUUID().toString());
        transaction.setTransactionTime(OffsetDateTime.now().truncatedTo(ChronoUnit.MILLIS));

        assertThatThrownBy(() -> transactionService.createTransaction(transaction))
                .isInstanceOf(Exception.class);
    }

    @Test
    void shouldUpdateTransactionStatusAndAmountSuccessfully() {

        Instruction savedInstruction = createAndSaveInstructionWithFirstName("Taras", "12345");

        Transaction originalTransaction = new Transaction();
        originalTransaction.setAmount(new BigDecimal("100.00"));
        originalTransaction.setTransactionStatus(TransactionStatus.ACTIVE.getStatusCode());
        originalTransaction.setIdempotencyId("idem-id-123");
        originalTransaction.setTransactionTime(OffsetDateTime.now().truncatedTo(ChronoUnit.MILLIS));
        originalTransaction.setInstruction(savedInstruction);

        Transaction savedTransaction = transactionRepository.save(originalTransaction);
        Long transactionId = savedTransaction.getId();

        Transaction updateData = new Transaction();
        updateData.setAmount(new BigDecimal("250.00"));
        updateData.setTransactionStatus(TransactionStatus.REVERSED.getStatusCode());
        originalTransaction.setIdempotencyId("idem-id-123");
        originalTransaction.setTransactionTime(OffsetDateTime.now().truncatedTo(ChronoUnit.MILLIS));
        originalTransaction.setInstruction(savedInstruction);

        Transaction result = transactionService.updateTransaction(transactionId, updateData);

        assertThat(result.getAmount()).isEqualByComparingTo(new BigDecimal("250.00"));
        assertThat(result.getTransactionStatus()).isEqualTo(TransactionStatus.REVERSED.getStatusCode());

        Optional<Transaction> fetchedFromDb = transactionRepository.findById(transactionId);
        assertThat(fetchedFromDb).isPresent();
        Transaction inDb = fetchedFromDb.get();

        assertThat(inDb.getAmount()).isEqualByComparingTo(new BigDecimal("250.00"));
        assertThat(inDb.getTransactionStatus()).isEqualTo(TransactionStatus.REVERSED.getStatusCode());

        assertThat(inDb.getIdempotencyId()).isEqualTo("idem-id-123");
        assertThat(inDb.getTransactionTime()).isEqualTo(originalTransaction.getTransactionTime());
        assertThat(inDb.getInstruction().getId()).isEqualTo(savedInstruction.getId());
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonExistentTransaction() {

        Long nonExistentId = 9999L;
        Transaction updateData = new Transaction();
        updateData.setAmount(BigDecimal.TEN);

        assertThatThrownBy(() -> transactionService.updateTransaction(nonExistentId, updateData))
                .isInstanceOf(TransactionNotFoundException.class)
                .hasMessageContaining("Transaction with ID " + nonExistentId + " not found in PDS.");
    }

    @Test
    void shouldSoftDeleteTransactionBySettingStatusToReversed() {

        Instruction instruction = createAndSaveInstructionWithFirstName("Taras", "12345");

        Transaction transaction = new Transaction();
        transaction.setAmount(new BigDecimal("100.00"));
        transaction.setIdempotencyId(UUID.randomUUID().toString());
        transaction.setTransactionStatus(TransactionStatus.ACTIVE.getStatusCode());
        transaction.setTransactionTime(OffsetDateTime.now().truncatedTo(ChronoUnit.MILLIS));
        transaction.setInstruction(instruction);

        Transaction savedTransaction = transactionRepository.save(transaction);
        Long transactionId = savedTransaction.getId();

        transactionService.deleteTransaction(transactionId);

        Optional<Transaction> fetchedFromDb = transactionRepository.findById(transactionId);

        assertThat(fetchedFromDb).isPresent();

        assertThat(fetchedFromDb.get().getTransactionStatus())
                .isEqualTo(TransactionStatus.REVERSED.getStatusCode());
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistentTransaction() {

        Long nonExistentId = 99999L;

        assertThatThrownBy(() -> transactionService.deleteTransaction(nonExistentId))
                .isInstanceOf(TransactionNotFoundException.class)
                .hasMessageContaining("Transaction with ID " + nonExistentId + " not found in PDS.");
    }

    @Test
    void shouldReturnTransactionWhenItExists() {

        Instruction instruction = createAndSaveInstructionWithFirstName("Taras", "12345");

        Transaction transaction = new Transaction();
        transaction.setAmount(new BigDecimal("100.00"));
        transaction.setIdempotencyId(UUID.randomUUID().toString());
        transaction.setTransactionStatus(TransactionStatus.ACTIVE.getStatusCode());
        transaction.setTransactionTime(OffsetDateTime.now().truncatedTo(ChronoUnit.MILLIS));
        transaction.setInstruction(instruction);

        Transaction savedTransaction = transactionRepository.save(transaction);
        Long existingId = savedTransaction.getId();

        Transaction result = transactionService.getTransaction(existingId);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(existingId);
        assertThat(result.getAmount()).isEqualByComparingTo(new BigDecimal("100.00"));

        assertThat(result.getInstruction()).isNotNull();
        assertThat(result.getInstruction().getId()).isEqualTo(instruction.getId());
    }

    @Test
    void shouldThrowTransactionNotFoundExceptionWhenIdDoesNotExist() {
        Long nonExistentId = 55555L;

        assertThatThrownBy(() -> transactionService.getTransaction(nonExistentId))
                .isInstanceOf(TransactionNotFoundException.class)
                .hasMessage("Transaction with ID " + nonExistentId + " not found in PDS.");
    }
    @Test
    void shouldReturnTransactionsOnlyForSpecificInstruction() {

        Instruction instructionTarget = createAndSaveInstructionWithFirstName("User_1", "12345");
        Instruction instructionNoise = createAndSaveInstructionWithFirstName("User_2", "54321");

        createAndSaveTransaction(instructionTarget, new BigDecimal("100.00"));
        createAndSaveTransaction(instructionTarget, new BigDecimal("200.00"));

        createAndSaveTransaction(instructionNoise, new BigDecimal("999.00"));

        List<Transaction> result = transactionService.getTransactionsByInstruction(instructionTarget.getId());

        assertThat(result).hasSize(2);

        assertThat(result)
                .allMatch(t -> t.getInstruction().getId().equals(instructionTarget.getId()));

        assertThat(result)
                .extracting(Transaction::getAmount)
                .containsExactlyInAnyOrder(new BigDecimal("100.00"), new BigDecimal("200.00"));

        assertThat(result)
                .extracting(Transaction::getAmount)
                .doesNotContain(new BigDecimal("999.00"));
    }

    @Test
    void shouldReturnEmptyListWhenInstructionHasNoTransactions() {

        Instruction emptyInstruction = createAndSaveInstructionWithFirstName("User", "12345");

        List<Transaction> result = transactionService.getTransactionsByInstruction(emptyInstruction.getId());

        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnEmptyListWhenInstructionIdDoesNotExist() {

        Long nonExistentId = 999999L;

        List<Transaction> result = transactionService.getTransactionsByInstruction(nonExistentId);

        assertThat(result).isEmpty();
    }

    private Instruction createAndSaveInstructionWithFirstName(String firstName, String iin) {
        Instruction instruction = new Instruction();
        instruction.setPayerFirstName(firstName);
        instruction.setPayerSecondName("Ivanko");
        instruction.setPayerPatronymic("Tarasovich");
        instruction.setAmount(new BigDecimal("100.50"));
        instruction.setPayerIin(iin);
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

        return instructionRepository.save(instruction);
    }

    private void createAndSaveTransaction(Instruction instruction, BigDecimal amount) {
        Transaction transaction = new Transaction();
        transaction.setAmount(amount);
        transaction.setIdempotencyId(UUID.randomUUID().toString());
        transaction.setTransactionStatus(TransactionStatus.ACTIVE.getStatusCode());
        transaction.setTransactionTime(OffsetDateTime.now().truncatedTo(ChronoUnit.MILLIS));
        transaction.setInstruction(instruction);

        transactionRepository.save(transaction);
    }
}
