package diameter.integration;

import diameter.app.AppManager;
import diameter.csv.parser.CsvParser;
import diameter.csv.parser.CsvParserImpl;
import diameter.domain.factory.MessageFactory;
import diameter.domain.factory.MessageFactoryImpl;
import diameter.io.FileReader;
import diameter.reporter.ProcessingResult;
import diameter.reporter.SummaryReporter;
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
    private StubFileReader   fileReader;

    private CapturingSummaryReporter summaryReporter;
    private AppManager               appManager;

    @BeforeEach
    void setUp() throws Exception {
        resetTransactionManagerSingleton();

        fileReader = new StubFileReader();
        CsvParser csvParser = new CsvParserImpl();
        MessageFactory messageFactory = new MessageFactoryImpl();
        MessageValidator validator = new MessageValidatorImpl();

        summaryReporter = new CapturingSummaryReporter();
        appManager = new AppManager(fileReader, csvParser, messageFactory, TransactionManagerImpl.getInstance(),
                                    validator,
                                    summaryReporter);
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

            ProcessingStats stats = runAppWithCsv(csvLines);

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

            ProcessingStats stats = runAppWithCsv(csvLines);

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

            ProcessingStats stats = runAppWithCsv(csvLines);

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

            ProcessingStats stats = runAppWithCsv(csvLines);

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

            ProcessingStats stats = runAppWithCsv(csvLines);

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

            ProcessingStats stats = runAppWithCsv(csvLines);

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
                    "AIR,true,,mme1.example.com,example.com,user1,,",
                    "AIR,true,sess-2,mme1.example.com,example.com,user2,,"
            );

            ProcessingStats stats = runAppWithCsv(csvLines);

            assertEquals(2, stats.totalMessages);
            assertEquals(1, stats.validMessages);
            assertEquals(1, stats.invalidMessages);
        }

        @Test
        @DisplayName("Should count invalid ULR when Visited-PLMN-Id missing")
        void shouldCountInvalidUlrWhenVisitedPlmnIdMissing() {
            List<String> csvLines = List.of(
                    "message_type,is_request,session_id,origin_host,origin_realm,user_name,visited_plmn_id,result_code",
                    "ULR,true,sess-1,mme1.example.com,example.com,user1,,"
            );

            ProcessingStats stats = runAppWithCsv(csvLines);

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
                    "AIA,false,sess-1,hss1.example.com,example.com,,,"
            );

            ProcessingStats stats = runAppWithCsv(csvLines);

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
                    "AIR,true,,,,,,,",
                    "ULR,true,,,,,,,"
            );

            ProcessingStats stats = runAppWithCsv(csvLines);

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

            ProcessingStats stats = runAppWithCsv(csvLines);

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

            ProcessingStats stats = runAppWithCsv(csvLines);

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

            ProcessingStats stats = runAppWithCsv(csvLines);

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

            ProcessingStats stats = runAppWithCsv(csvLines);

            assertEquals(1, stats.totalMessages);
            assertEquals(1, stats.validMessages);
        }

        @Test
        @DisplayName("Should treat whitespace-only fields as empty")
        void shouldTreatWhitespaceOnlyFieldsAsEmpty() {
            List<String> csvLines = List.of(
                    "message_type,is_request,session_id,origin_host,origin_realm,user_name,visited_plmn_id,result_code",
                    "AIR,true,   ,mme1.example.com,example.com,user1,,"
            );

            ProcessingStats stats = runAppWithCsv(csvLines);

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
            List<String> csvLines = List.of(
                    "message_type,is_request,session_id,origin_host,origin_realm,user_name,visited_plmn_id,result_code",
                    "AIR,true,sess-1,mme1.example.com,example.com,001010123456789,,",
                    "ULR,true,sess-2,mme1.example.com,example.com,001010123456789,00101,",
                    "ULA,false,sess-2,hss1.example.com,example.com,,,2001"
            );

            ProcessingStats stats = runAppWithCsv(csvLines);

            assertEquals(3, stats.totalMessages);
            assertEquals(3, stats.validMessages);
            assertEquals(0, stats.invalidMessages);
            assertEquals(1, stats.completedTransactions);
            assertEquals(1, stats.incompleteTransactions);
        }
    }

    private ProcessingStats runAppWithCsv(List<String> csvLines) {
        String fakePath = "/tmp/fake.csv";

        fileReader.stubLines = csvLines;
        fileReader.calls = 0;

        appManager.run(new String[]{fakePath});

        assertEquals(1, fileReader.calls, "Expected FileReader.getLinesFromFile to be called exactly once");

        return summaryReporter.toStats();
    }

    private static final class StubFileReader implements FileReader {
        private List<String> stubLines = List.of();
        private int          calls     = 0;

        @Override
        public List<String> getLinesFromFile(String[] args) {
            calls++;
            return stubLines;
        }
    }

    private static final class CapturingSummaryReporter implements SummaryReporter {
        private List<ProcessingResult> lastResults;
        private TransactionResult      lastTransactionResult;

        @Override
        public void report(List<ProcessingResult> results, TransactionResult transactionResult) {
            this.lastResults = results;
            this.lastTransactionResult = transactionResult;
        }

        ProcessingStats toStats() {
            List<ProcessingResult> results = lastResults == null ? List.of() : lastResults;
            TransactionResult tx = lastTransactionResult == null ? new TransactionResult(0, 0) : lastTransactionResult;

            int total = results.size();
            int valid = (int) results.stream().filter(ProcessingResult::isValid).count();
            int invalid = total - valid;

            return new ProcessingStats(
                    total,
                    valid,
                    invalid,
                    tx.getNumberOfCompleteTransactions(),
                    tx.getNumberOfIncompleteTransactions()
            );
        }
    }

    private record ProcessingStats(
            int totalMessages,
            int validMessages,
            int invalidMessages,
            int completedTransactions,
            int incompleteTransactions
    ) {
    }
}
