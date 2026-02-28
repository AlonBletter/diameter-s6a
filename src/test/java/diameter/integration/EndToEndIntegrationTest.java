package diameter.integration;

import diameter.csv.parser.CsvParser;
import diameter.csv.parser.CsvParserImpl;
import diameter.domain.factory.MessageFactory;
import diameter.domain.factory.MessageFactoryImpl;
import diameter.transaction.TransactionManager;
import diameter.transaction.TransactionManagerImpl;
import diameter.transaction.TransactionResult;
import diameter.validator.MessageValidator;
import diameter.validator.MessageValidatorImpl;
import org.junit.jupiter.api.*;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("End-to-End Integration Tests")
class EndToEndIntegrationTest {

    private CsvParser          csvParser;
    private MessageFactory     messageFactory;
    private MessageValidator   validator;
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
    @DisplayName("Happy Path Scenarios")
    class HappyPathScenarios {

        @Test
        @DisplayName("Should process single complete AIR-AIA transaction")
        void shouldProcessSingleCompleteAirAiaTransaction() {
            List<String> csvLines = List.of(
                "message_type,is_request,session_id,origin_host,origin_realm,user_name,visited_plmn_id,result_code",
                "AIR,true,sess-1,mme1.example.com,example.com,001010123456789,,",
                "AIA,false,sess-1,hss1.example.com,example.com,,,2001"
            );

            ProcessingStats stats = processMessages(csvLines);

            assertEquals(2, stats.totalMessages);
            assertEquals(2, stats.validMessages);
            assertEquals(0, stats.invalidMessages);
            assertEquals(1, stats.completedTransactions);
            assertEquals(0, stats.incompleteTransactions);
        }

        @Test
        @DisplayName("Should process single complete ULR-ULA transaction")
        void shouldProcessSingleCompleteUlrUlaTransaction() {
            List<String> csvLines = List.of(
                "message_type,is_request,session_id,origin_host,origin_realm,user_name,visited_plmn_id,result_code",
                "ULR,true,sess-2,mme1.example.com,example.com,001010123456789,00101,",
                "ULA,false,sess-2,hss1.example.com,example.com,,,2001"
            );

            ProcessingStats stats = processMessages(csvLines);

            assertEquals(2, stats.totalMessages);
            assertEquals(2, stats.validMessages);
            assertEquals(1, stats.completedTransactions);
            assertEquals(0, stats.incompleteTransactions);
        }

        @Test
        @DisplayName("Should process multiple complete transactions")
        void shouldProcessMultipleCompleteTransactions() {
            List<String> csvLines = List.of(
                "message_type,is_request,session_id,origin_host,origin_realm,user_name,visited_plmn_id,result_code",
                "AIR,true,sess-1,mme1.example.com,example.com,user1,,",
                "ULR,true,sess-2,mme1.example.com,example.com,user2,00101,",
                "AIA,false,sess-1,hss1.example.com,example.com,,,2001",
                "ULA,false,sess-2,hss1.example.com,example.com,,,2001"
            );

            ProcessingStats stats = processMessages(csvLines);

            assertEquals(4, stats.totalMessages);
            assertEquals(4, stats.validMessages);
            assertEquals(2, stats.completedTransactions);
            assertEquals(0, stats.incompleteTransactions);
        }

        @Test
        @DisplayName("Should handle out-of-order answers")
        void shouldHandleOutOfOrderAnswers() {
            List<String> csvLines = List.of(
                "message_type,is_request,session_id,origin_host,origin_realm,user_name,visited_plmn_id,result_code",
                "AIR,true,sess-1,mme1.example.com,example.com,user1,,",
                "AIR,true,sess-2,mme1.example.com,example.com,user2,,",
                "AIA,false,sess-2,hss1.example.com,example.com,,,2001",
                "AIA,false,sess-1,hss1.example.com,example.com,,,2001"
            );

            ProcessingStats stats = processMessages(csvLines);

            assertEquals(4, stats.totalMessages);
            assertEquals(4, stats.validMessages);
            assertEquals(2, stats.completedTransactions);
            assertEquals(0, stats.incompleteTransactions);
        }
    }

    @Nested
    @DisplayName("Open Transaction Scenarios")
    class OpenTransactionScenarios {

        @Test
        @DisplayName("Should report open transactions when no answers received")
        void shouldReportOpenTransactionsWhenNoAnswers() {
            List<String> csvLines = List.of(
                "message_type,is_request,session_id,origin_host,origin_realm,user_name,visited_plmn_id,result_code",
                "AIR,true,sess-1,mme1.example.com,example.com,user1,,",
                "ULR,true,sess-2,mme1.example.com,example.com,user2,00101,"
            );

            ProcessingStats stats = processMessages(csvLines);

            assertEquals(2, stats.totalMessages);
            assertEquals(2, stats.validMessages);
            assertEquals(0, stats.completedTransactions);
            assertEquals(2, stats.incompleteTransactions);
        }

        @Test
        @DisplayName("Should report partial completion correctly")
        void shouldReportPartialCompletion() {
            List<String> csvLines = List.of(
                "message_type,is_request,session_id,origin_host,origin_realm,user_name,visited_plmn_id,result_code",
                "AIR,true,sess-1,mme1.example.com,example.com,user1,,",
                "AIR,true,sess-2,mme1.example.com,example.com,user2,,",
                "AIR,true,sess-3,mme1.example.com,example.com,user3,,",
                "AIA,false,sess-1,hss1.example.com,example.com,,,2001"
            );

            ProcessingStats stats = processMessages(csvLines);

            assertEquals(4, stats.totalMessages);
            assertEquals(4, stats.validMessages);
            assertEquals(1, stats.completedTransactions);
            assertEquals(2, stats.incompleteTransactions);
        }
    }

    @Nested
    @DisplayName("Validation Failure Scenarios")
    class ValidationFailureScenarios {

        @Test
        @DisplayName("Should count invalid messages when mandatory AVPs missing")
        void shouldCountInvalidMessagesWhenMandatoryAvpsMissing() {
            List<String> csvLines = List.of(
                "message_type,is_request,session_id,origin_host,origin_realm,user_name,visited_plmn_id,result_code",
                "AIR,true,,mme1.example.com,example.com,user1,,",  // Missing session_id
                "AIR,true,sess-2,mme1.example.com,example.com,user2,,"
            );

            ProcessingStats stats = processMessages(csvLines);

            assertEquals(2, stats.totalMessages);
            assertEquals(1, stats.validMessages);
            assertEquals(1, stats.invalidMessages);
        }

        @Test
        @DisplayName("Should count invalid ULR when Visited-PLMN-Id missing")
        void shouldCountInvalidUlrWhenVisitedPlmnIdMissing() {
            List<String> csvLines = List.of(
                "message_type,is_request,session_id,origin_host,origin_realm,user_name,visited_plmn_id,result_code",
                "ULR,true,sess-1,mme1.example.com,example.com,user1,,"  // Missing visited_plmn_id
            );

            ProcessingStats stats = processMessages(csvLines);

            assertEquals(1, stats.totalMessages);
            assertEquals(0, stats.validMessages);
            assertEquals(1, stats.invalidMessages);
        }

        @Test
        @DisplayName("Should count invalid answer when Result-Code missing")
        void shouldCountInvalidAnswerWhenResultCodeMissing() {
            List<String> csvLines = List.of(
                "message_type,is_request,session_id,origin_host,origin_realm,user_name,visited_plmn_id,result_code",
                "AIR,true,sess-1,mme1.example.com,example.com,user1,,",
                "AIA,false,sess-1,hss1.example.com,example.com,,,"  // Missing result_code
            );

            ProcessingStats stats = processMessages(csvLines);

            assertEquals(2, stats.totalMessages);
            assertEquals(1, stats.validMessages);
            assertEquals(1, stats.invalidMessages);
            assertEquals(0, stats.completedTransactions);
            assertEquals(1, stats.incompleteTransactions);
        }

        @Test
        @DisplayName("Should handle all messages being invalid")
        void shouldHandleAllMessagesBeingInvalid() {
            List<String> csvLines = List.of(
                "message_type,is_request,session_id,origin_host,origin_realm,user_name,visited_plmn_id,result_code",
                "AIR,true,,,,,,,",  // All mandatory AVPs missing
                "ULR,true,,,,,,"    // All mandatory AVPs missing
            );

            ProcessingStats stats = processMessages(csvLines);

            assertEquals(2, stats.totalMessages);
            assertEquals(0, stats.validMessages);
            assertEquals(2, stats.invalidMessages);
            assertEquals(0, stats.completedTransactions);
            assertEquals(0, stats.incompleteTransactions);
        }
    }

    @Nested
    @DisplayName("Type Mismatch Scenarios")
    class TypeMismatchScenarios {

        @Test
        @DisplayName("Should count invalid when AIR has is_request=false")
        void shouldCountInvalidWhenAirHasIsRequestFalse() {
            List<String> csvLines = List.of(
                "message_type,is_request,session_id,origin_host,origin_realm,user_name,visited_plmn_id,result_code",
                "AIR,false,sess-1,mme1.example.com,example.com,user1,,"
            );

            ProcessingStats stats = processMessages(csvLines);

            assertEquals(1, stats.totalMessages);
            assertEquals(0, stats.validMessages);
            assertEquals(1, stats.invalidMessages);
        }

        @Test
        @DisplayName("Should count invalid when AIA has is_request=true")
        void shouldCountInvalidWhenAiaHasIsRequestTrue() {
            List<String> csvLines = List.of(
                "message_type,is_request,session_id,origin_host,origin_realm,user_name,visited_plmn_id,result_code",
                "AIA,true,sess-1,hss1.example.com,example.com,,,2001"
            );

            ProcessingStats stats = processMessages(csvLines);

            assertEquals(1, stats.totalMessages);
            assertEquals(0, stats.validMessages);
            assertEquals(1, stats.invalidMessages);
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {

        @Test
        @DisplayName("Should handle empty CSV (header only)")
        void shouldHandleEmptyCsv() {
            List<String> csvLines = List.of(
                "message_type,is_request,session_id,origin_host,origin_realm,user_name,visited_plmn_id,result_code"
            );

            ProcessingStats stats = processMessages(csvLines);

            assertEquals(0, stats.totalMessages);
            assertEquals(0, stats.validMessages);
            assertEquals(0, stats.invalidMessages);
            assertEquals(0, stats.completedTransactions);
            assertEquals(0, stats.incompleteTransactions);
        }

        @Test
        @DisplayName("Should handle whitespace in field values")
        void shouldHandleWhitespaceInFieldValues() {
            List<String> csvLines = List.of(
                "message_type,is_request,session_id,origin_host,origin_realm,user_name,visited_plmn_id,result_code",
                "AIR,true,  sess-1  ,  mme1.example.com  ,  example.com  ,  user1  ,,"
            );

            ProcessingStats stats = processMessages(csvLines);

            assertEquals(1, stats.totalMessages);
            assertEquals(1, stats.validMessages);
        }

        @Test
        @DisplayName("Should treat whitespace-only fields as empty")
        void shouldTreatWhitespaceOnlyFieldsAsEmpty() {
            List<String> csvLines = List.of(
                "message_type,is_request,session_id,origin_host,origin_realm,user_name,visited_plmn_id,result_code",
                "AIR,true,   ,mme1.example.com,example.com,user1,,"  // session_id is whitespace only
            );

            ProcessingStats stats = processMessages(csvLines);

            assertEquals(1, stats.totalMessages);
            assertEquals(0, stats.validMessages);
            assertEquals(1, stats.invalidMessages);
        }
    }

    @Nested
    @DisplayName("Spec Example Scenarios")
    class SpecExampleScenarios {

        @Test
        @DisplayName("Should process spec example correctly")
        void shouldProcessSpecExampleCorrectly() {
            // Example from the spec document
            List<String> csvLines = List.of(
                "message_type,is_request,session_id,origin_host,origin_realm,user_name,visited_plmn_id,result_code",
                "AIR,true,sess-1,mme1.example.com,example.com,001010123456789,,",
                "ULR,true,sess-2,mme1.example.com,example.com,001010123456789,00101,",
                "ULA,false,sess-2,hss1.example.com,example.com,,,2001"
            );

            ProcessingStats stats = processMessages(csvLines);

            assertEquals(3, stats.totalMessages);
            assertEquals(3, stats.validMessages);
            assertEquals(0, stats.invalidMessages);
            assertEquals(1, stats.completedTransactions);  // ULR-ULA completed
            assertEquals(1, stats.incompleteTransactions); // AIR still open
        }
    }

    // Helper method to process messages and collect statistics
    private ProcessingStats processMessages(List<String> csvLines) {
        var rows = csvParser.parse(csvLines);
        int validCount = 0;
        int invalidCount = 0;

        for (var row : rows) {
            try {
                var message = messageFactory.createDiameterMessage(row);
                var validationResult = validator.validate(message);

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

    // Statistics holder
    private record ProcessingStats(
        int totalMessages,
        int validMessages,
        int invalidMessages,
        int completedTransactions,
        int incompleteTransactions
    ) {}
}
