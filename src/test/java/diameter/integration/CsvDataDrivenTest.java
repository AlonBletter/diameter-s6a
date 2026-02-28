package diameter.integration;

import diameter.csv.model.CsvRow;
import diameter.csv.parser.CsvParser;
import diameter.csv.parser.CsvParserImpl;
import diameter.domain.factory.MessageFactory;
import diameter.domain.factory.MessageFactoryImpl;
import diameter.domain.message.DiameterMessage;
import diameter.transaction.TransactionManager;
import diameter.transaction.TransactionManagerImpl;
import diameter.transaction.TransactionResult;
import diameter.validator.MessageValidator;
import diameter.validator.MessageValidatorImpl;
import diameter.validator.ValidationResult;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Data-driven tests using CSV test data files.
 * These tests validate the system behavior against predefined test scenarios.
 */
@DisplayName("CSV Data-Driven Tests")
class CsvDataDrivenTest {

    private static final String TEST_DATA_PATH = "src/test/resources/testdata/";

    private CsvParser csvParser;
    private MessageFactory messageFactory;
    private MessageValidator validator;
    private TransactionManager transactionManager;

    @BeforeEach
    void setUp() throws Exception {
        resetTransactionManagerSingleton();
        csvParser = new CsvParserImpl();
        messageFactory = new MessageFactoryImpl();
        validator = new MessageValidatorImpl();
        transactionManager = TransactionManagerImpl.getInstance();
    }

    @AfterEach
    void tearDown() throws Exception {
        resetTransactionManagerSingleton();
    }

    private void resetTransactionManagerSingleton() throws Exception {
        Field instance = TransactionManagerImpl.class.getDeclaredField("instance");
        instance.setAccessible(true);
        instance.set(null, null);
    }

    @Nested
    @DisplayName("File-Based Test Scenarios")
    class FileBasedScenarios {

        @Test
        @DisplayName("Should process valid_complete_transactions.csv correctly")
        void shouldProcessValidCompleteTransactions() throws IOException {
            ProcessingStats stats = processFile("valid_complete_transactions.csv");

            assertEquals(4, stats.totalMessages);
            assertEquals(4, stats.validMessages);
            assertEquals(0, stats.invalidMessages);
            assertEquals(2, stats.completedTransactions);
            assertEquals(0, stats.incompleteTransactions);
        }

        @Test
        @DisplayName("Should process open_transactions.csv correctly")
        void shouldProcessOpenTransactions() throws IOException {
            ProcessingStats stats = processFile("open_transactions.csv");

            assertEquals(3, stats.totalMessages);
            assertEquals(3, stats.validMessages);
            assertEquals(0, stats.invalidMessages);
            assertEquals(0, stats.completedTransactions);
            assertEquals(3, stats.incompleteTransactions);
        }

        @Test
        @DisplayName("Should process invalid_messages.csv correctly")
        void shouldProcessInvalidMessages() throws IOException {
            ProcessingStats stats = processFile("invalid_messages.csv");

            assertEquals(4, stats.totalMessages);
            assertTrue(stats.invalidMessages > 0);
        }

        @Test
        @DisplayName("Should process spec_example.csv correctly")
        void shouldProcessSpecExample() throws IOException {
            ProcessingStats stats = processFile("spec_example.csv");

            assertEquals(3, stats.totalMessages);
            assertEquals(3, stats.validMessages);
            assertEquals(0, stats.invalidMessages);
            assertEquals(1, stats.completedTransactions);  // ULR-ULA pair
            assertEquals(1, stats.incompleteTransactions); // AIR without AIA
        }

        @Test
        @DisplayName("Should process out_of_order_answers.csv correctly")
        void shouldProcessOutOfOrderAnswers() throws IOException {
            ProcessingStats stats = processFile("out_of_order_answers.csv");

            assertEquals(6, stats.totalMessages);
            assertEquals(6, stats.validMessages);
            assertEquals(3, stats.completedTransactions);
            assertEquals(0, stats.incompleteTransactions);
        }

        @Test
        @DisplayName("Should process type_mismatch.csv correctly")
        void shouldProcessTypeMismatch() throws IOException {
            ProcessingStats stats = processFile("type_mismatch.csv");

            assertEquals(4, stats.totalMessages);
            // All messages have type mismatch issues
            assertEquals(4, stats.invalidMessages);
            assertEquals(0, stats.validMessages);
        }
    }

    @Nested
    @DisplayName("Parameterized Test Scenarios")
    class ParameterizedScenarios {

        @ParameterizedTest
        @DisplayName("Should correctly count transactions for various scenarios")
        @CsvSource({
            "valid_complete_transactions.csv, 2, 0",
            "open_transactions.csv, 0, 3",
            "spec_example.csv, 1, 1",
            "out_of_order_answers.csv, 3, 0"
        })
        void shouldCorrectlyCountTransactions(String filename, int expectedCompleted, int expectedIncomplete)
                throws IOException {
            ProcessingStats stats = processFile(filename);

            assertEquals(expectedCompleted, stats.completedTransactions,
                "Completed transactions mismatch for " + filename);
            assertEquals(expectedIncomplete, stats.incompleteTransactions,
                "Incomplete transactions mismatch for " + filename);
        }

        @ParameterizedTest
        @DisplayName("Should correctly count valid/invalid messages")
        @CsvSource({
            "valid_complete_transactions.csv, 4, 0",
            "open_transactions.csv, 3, 0",
            "type_mismatch.csv, 0, 4"
        })
        void shouldCorrectlyCountValidInvalidMessages(String filename, int expectedValid, int expectedInvalid)
                throws IOException {
            ProcessingStats stats = processFile(filename);

            assertEquals(expectedValid, stats.validMessages,
                "Valid messages mismatch for " + filename);
            assertEquals(expectedInvalid, stats.invalidMessages,
                "Invalid messages mismatch for " + filename);
        }
    }

    @Nested
    @DisplayName("Transaction Invariants")
    class TransactionInvariants {

        @Test
        @DisplayName("Should maintain invariant: valid + invalid = total")
        void shouldMaintainValidInvalidTotalInvariant() throws Exception {
            for (String filename : List.of(
                    "valid_complete_transactions.csv",
                    "open_transactions.csv",
                    "spec_example.csv",
                    "out_of_order_answers.csv",
                    "type_mismatch.csv")) {

                resetTransactionManagerSingleton();
                transactionManager = TransactionManagerImpl.getInstance();

                ProcessingStats stats = processFile(filename);

                assertEquals(stats.totalMessages(), stats.validMessages() + stats.invalidMessages(),
                    "Invariant violated for " + filename);
            }
        }

        @Test
        @DisplayName("Should maintain invariant: completed + incomplete = valid requests processed")
        void shouldMaintainTransactionCountInvariant() throws Exception {
            ProcessingStats stats = processFile("valid_complete_transactions.csv");

            // For fully valid files, number of requests should equal completed + incomplete
            int totalTransactions = stats.completedTransactions() + stats.incompleteTransactions();
            // In this file, we have 2 requests (AIR, ULR) and both are completed
            assertEquals(2, totalTransactions);
        }
    }

    // Helper method to process a CSV file and return statistics
    private ProcessingStats processFile(String filename) throws IOException {
        Path filePath = Path.of(TEST_DATA_PATH + filename);
        List<String> lines = Files.readAllLines(filePath);

        List<CsvRow> rows = csvParser.parse(lines);
        int validCount = 0;
        int invalidCount = 0;

        for (CsvRow row : rows) {
            try {
                DiameterMessage message = messageFactory.createDiameterMessage(row);
                ValidationResult validationResult = validator.validate(message);

                if (validationResult.isValid()) {
                    transactionManager.processDiameterMessage(message);
                    validCount++;
                } else {
                    invalidCount++;
                }
            } catch (Exception e) {
                invalidCount++;
            }
        }

        TransactionResult txResult = transactionManager.getTransactionResult();

        return new ProcessingStats(
            rows.size(),
            validCount,
            invalidCount,
            txResult.getNumberOfCompleteTransactions(),
            txResult.getNumberOfIncompleteTransactions()
        );
    }

    // Statistics holder record
        private static final class ProcessingStats {
        private final int totalMessages;
        private final int validMessages;
        private final int invalidMessages;
        private final int completedTransactions;
        private final int incompleteTransactions;

        private ProcessingStats(int totalMessages,
                                int validMessages,
                                int invalidMessages,
                                int completedTransactions,
                                int incompleteTransactions) {
            this.totalMessages = totalMessages;
            this.validMessages = validMessages;
            this.invalidMessages = invalidMessages;
            this.completedTransactions = completedTransactions;
            this.incompleteTransactions = incompleteTransactions;
        }

        public int totalMessages() {
            return totalMessages;
        }

        public int validMessages() {
            return validMessages;
        }

        public int invalidMessages() {
            return invalidMessages;
        }

        public int completedTransactions() {
            return completedTransactions;
        }

        public int incompleteTransactions() {
            return incompleteTransactions;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }
            var that = (ProcessingStats) obj;
            return this.totalMessages == that.totalMessages && this.validMessages == that.validMessages &&
                   this.invalidMessages == that.invalidMessages &&
                   this.completedTransactions == that.completedTransactions && this.incompleteTransactions == that.incompleteTransactions;
        }

        @Override
        public int hashCode() {
            return Objects.hash(totalMessages, validMessages, invalidMessages, completedTransactions,
                                incompleteTransactions);
        }

        @Override
        public String toString() {
            return "ProcessingStats[" + "totalMessages=" + totalMessages + ", " + "validMessages=" + validMessages +
                   ", " + "invalidMessages=" + invalidMessages + ", " + "completedTransactions=" +
                   completedTransactions + ", " + "incompleteTransactions=" + incompleteTransactions + ']';
        }
    }
}
