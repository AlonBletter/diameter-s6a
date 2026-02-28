package diameter.transaction;

import diameter.domain.message.*;
import diameter.exception.transaction.DuplicateTransactionException;
import diameter.exception.transaction.UnexpectedTransactionAnswerException;
import org.junit.jupiter.api.*;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TransactionManagerImpl Tests")
class TransactionManagerImplTest {

    private TransactionManager transactionManager;

    @BeforeEach
    void setUp() throws Exception {
        // Reset singleton state before each test
        resetSingleton();
        transactionManager = TransactionManagerImpl.getInstance();
    }

    @AfterEach
    void tearDown() throws Exception {
        resetSingleton();
    }

    /**
     * Reset singleton state using reflection for test isolation
     */
    private void resetSingleton() throws Exception {
        Field instance = TransactionManagerImpl.class.getDeclaredField("instance");
        instance.setAccessible(true);
        instance.set(null, null);
    }

    @Nested
    @DisplayName("Request Processing")
    class RequestProcessing {

        @Test
        @DisplayName("Should open transaction when AIR request is received")
        void shouldOpenTransactionForAirRequest() {
            AIR air = createAir("sess-1");

            transactionManager.processDiameterMessage(air);
            TransactionResult result = transactionManager.getTransactionResult();

            assertEquals(0, result.getNumberOfCompleteTransactions());
            assertEquals(1, result.getNumberOfIncompleteTransactions());
        }

        @Test
        @DisplayName("Should open transaction when ULR request is received")
        void shouldOpenTransactionForUlrRequest() {
            ULR ulr = createUlr("sess-2");

            transactionManager.processDiameterMessage(ulr);
            TransactionResult result = transactionManager.getTransactionResult();

            assertEquals(0, result.getNumberOfCompleteTransactions());
            assertEquals(1, result.getNumberOfIncompleteTransactions());
        }

        @Test
        @DisplayName("Should open multiple transactions for different session IDs")
        void shouldOpenMultipleTransactions() {
            transactionManager.processDiameterMessage(createAir("sess-1"));
            transactionManager.processDiameterMessage(createUlr("sess-2"));
            transactionManager.processDiameterMessage(createAir("sess-3"));

            TransactionResult result = transactionManager.getTransactionResult();

            assertEquals(0, result.getNumberOfCompleteTransactions());
            assertEquals(3, result.getNumberOfIncompleteTransactions());
        }

        @Test
        @DisplayName("Should throw exception for duplicate session ID")
        void shouldThrowExceptionForDuplicateSessionId() {
            transactionManager.processDiameterMessage(createAir("sess-1"));

            DuplicateTransactionException exception = assertThrows(
                DuplicateTransactionException.class,
                () -> transactionManager.processDiameterMessage(createAir("sess-1"))
            );

            assertTrue(exception.getMessage().contains("sess-1"));
        }

        @Test
        @DisplayName("Should throw exception when message is null")
        void shouldThrowExceptionWhenMessageIsNull() {
            assertThrows(IllegalArgumentException.class,
                () -> transactionManager.processDiameterMessage(null));
        }

        @Test
        @DisplayName("Should throw exception when session ID is null")
        void shouldThrowExceptionWhenSessionIdIsNull() {
            AIR air = new AIR(null, "host", "realm", "user");

            assertThrows(IllegalArgumentException.class,
                () -> transactionManager.processDiameterMessage(air));
        }
    }

    @Nested
    @DisplayName("Answer Processing")
    class AnswerProcessing {

        @Test
        @DisplayName("Should complete transaction when matching AIA is received")
        void shouldCompleteTransactionWithMatchingAia() {
            transactionManager.processDiameterMessage(createAir("sess-1"));
            transactionManager.processDiameterMessage(createAia("sess-1"));

            TransactionResult result = transactionManager.getTransactionResult();

            assertEquals(1, result.getNumberOfCompleteTransactions());
            assertEquals(0, result.getNumberOfIncompleteTransactions());
        }

        @Test
        @DisplayName("Should complete transaction when matching ULA is received")
        void shouldCompleteTransactionWithMatchingUla() {
            transactionManager.processDiameterMessage(createUlr("sess-2"));
            transactionManager.processDiameterMessage(createUla("sess-2"));

            TransactionResult result = transactionManager.getTransactionResult();

            assertEquals(1, result.getNumberOfCompleteTransactions());
            assertEquals(0, result.getNumberOfIncompleteTransactions());
        }

        @Test
        @DisplayName("Should throw exception when answer has no matching request")
        void shouldThrowExceptionForOrphanAnswer() {
            UnexpectedTransactionAnswerException exception = assertThrows(
                UnexpectedTransactionAnswerException.class,
                () -> transactionManager.processDiameterMessage(createAia("sess-unknown"))
            );

            assertTrue(exception.getMessage().contains("sess-unknown"));
        }

        @Test
        @DisplayName("Should not complete transaction when answer type mismatches request type")
        void shouldNotCompleteWhenTypeMismatch() {
            // AIR request should only be completed by AIA, not ULA
            transactionManager.processDiameterMessage(createAir("sess-1"));
            transactionManager.processDiameterMessage(createUla("sess-1"));

            TransactionResult result = transactionManager.getTransactionResult();

            // Transaction should remain incomplete due to type mismatch
            assertEquals(0, result.getNumberOfCompleteTransactions());
            assertEquals(1, result.getNumberOfIncompleteTransactions());
        }

        @Test
        @DisplayName("Should not complete ULR transaction with AIA answer")
        void shouldNotCompleteUlrWithAia() {
            transactionManager.processDiameterMessage(createUlr("sess-2"));
            transactionManager.processDiameterMessage(createAia("sess-2"));

            TransactionResult result = transactionManager.getTransactionResult();

            assertEquals(0, result.getNumberOfCompleteTransactions());
            assertEquals(1, result.getNumberOfIncompleteTransactions());
        }
    }

    @Nested
    @DisplayName("Transaction Lifecycle")
    class TransactionLifecycle {

        @Test
        @DisplayName("Should handle complete AIR-AIA transaction flow")
        void shouldHandleCompleteAirAiaFlow() {
            AIR air = createAir("sess-1");
            AIA aia = createAia("sess-1");

            transactionManager.processDiameterMessage(air);

            TransactionResult midResult = transactionManager.getTransactionResult();
            assertEquals(1, midResult.getNumberOfIncompleteTransactions());

            transactionManager.processDiameterMessage(aia);

            TransactionResult finalResult = transactionManager.getTransactionResult();
            assertEquals(1, finalResult.getNumberOfCompleteTransactions());
            assertEquals(0, finalResult.getNumberOfIncompleteTransactions());
        }

        @Test
        @DisplayName("Should handle complete ULR-ULA transaction flow")
        void shouldHandleCompleteUlrUlaFlow() {
            ULR ulr = createUlr("sess-2");
            ULA ula = createUla("sess-2");

            transactionManager.processDiameterMessage(ulr);
            transactionManager.processDiameterMessage(ula);

            TransactionResult result = transactionManager.getTransactionResult();
            assertEquals(1, result.getNumberOfCompleteTransactions());
            assertEquals(0, result.getNumberOfIncompleteTransactions());
        }

        @Test
        @DisplayName("Should handle multiple complete transactions")
        void shouldHandleMultipleCompleteTransactions() {
            transactionManager.processDiameterMessage(createAir("sess-1"));
            transactionManager.processDiameterMessage(createUlr("sess-2"));
            transactionManager.processDiameterMessage(createAia("sess-1"));
            transactionManager.processDiameterMessage(createUla("sess-2"));

            TransactionResult result = transactionManager.getTransactionResult();
            assertEquals(2, result.getNumberOfCompleteTransactions());
            assertEquals(0, result.getNumberOfIncompleteTransactions());
        }

        @Test
        @DisplayName("Should handle mixed complete and incomplete transactions")
        void shouldHandleMixedTransactions() {
            transactionManager.processDiameterMessage(createAir("sess-1"));
            transactionManager.processDiameterMessage(createUlr("sess-2"));
            transactionManager.processDiameterMessage(createAir("sess-3"));
            transactionManager.processDiameterMessage(createAia("sess-1"));

            TransactionResult result = transactionManager.getTransactionResult();
            assertEquals(1, result.getNumberOfCompleteTransactions());
            assertEquals(2, result.getNumberOfIncompleteTransactions());
        }

        @Test
        @DisplayName("Should handle out-of-order transaction processing")
        void shouldHandleOutOfOrderProcessing() {
            // Process in order: AIR1, ULR2, AIR3, AIA1, ULA2
            transactionManager.processDiameterMessage(createAir("sess-1"));
            transactionManager.processDiameterMessage(createUlr("sess-2"));
            transactionManager.processDiameterMessage(createAir("sess-3"));
            transactionManager.processDiameterMessage(createAia("sess-1"));
            transactionManager.processDiameterMessage(createUla("sess-2"));

            TransactionResult result = transactionManager.getTransactionResult();
            assertEquals(2, result.getNumberOfCompleteTransactions());
            assertEquals(1, result.getNumberOfIncompleteTransactions());
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {

        @Test
        @DisplayName("Should return zero counts when no messages processed")
        void shouldReturnZeroCountsWhenNoMessagesProcessed() {
            TransactionResult result = transactionManager.getTransactionResult();

            assertEquals(0, result.getNumberOfCompleteTransactions());
            assertEquals(0, result.getNumberOfIncompleteTransactions());
        }

        @Test
        @DisplayName("Should handle session ID with special characters")
        void shouldHandleSessionIdWithSpecialCharacters() {
            String specialSessionId = "sess-1@host.com;app=test";
            transactionManager.processDiameterMessage(createAir(specialSessionId));
            transactionManager.processDiameterMessage(createAia(specialSessionId));

            TransactionResult result = transactionManager.getTransactionResult();
            assertEquals(1, result.getNumberOfCompleteTransactions());
        }

        @Test
        @DisplayName("Should handle very long session ID")
        void shouldHandleVeryLongSessionId() {
            String longSessionId = "sess-" + "a".repeat(1000);
            transactionManager.processDiameterMessage(createAir(longSessionId));

            TransactionResult result = transactionManager.getTransactionResult();
            assertEquals(1, result.getNumberOfIncompleteTransactions());
        }
    }

    // Helper methods to create test messages
    private AIR createAir(String sessionId) {
        return new AIR(sessionId, "mme1.example.com", "example.com", "user1");
    }

    private AIA createAia(String sessionId) {
        return new AIA(sessionId, "hss1.example.com", "example.com", null, "2001");
    }

    private ULR createUlr(String sessionId) {
        return new ULR(sessionId, "mme1.example.com", "example.com", "user1", "00101");
    }

    private ULA createUla(String sessionId) {
        return new ULA(sessionId, "hss1.example.com", "example.com", null, "2001");
    }
}
