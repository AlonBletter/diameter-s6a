package diameter.reporter;

import diameter.transaction.TransactionResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SummaryReporterImpl Tests")
class SummaryReporterImplTest {

    private SummaryReporter reporter;
    private ByteArrayOutputStream outputStream;
    private ByteArrayOutputStream errorStream;
    private PrintStream originalOut;
    private PrintStream originalErr;

    @BeforeEach
    void setUp() {
        reporter = new SummaryReporterImpl();
        outputStream = new ByteArrayOutputStream();
        errorStream = new ByteArrayOutputStream();
        originalOut = System.out;
        originalErr = System.err;
        System.setOut(new PrintStream(outputStream));
        System.setErr(new PrintStream(errorStream));
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() {
        System.setOut(originalOut);
        System.setErr(originalErr);
    }

    @Nested
    @DisplayName("Report Output")
    class ReportOutput {

        @Test
        @DisplayName("Should report all zero counts for empty results")
        void shouldReportAllZeroCountsForEmptyResults() {
            List<ProcessingResult> results = Collections.emptyList();
            TransactionResult txResult = new TransactionResult(0, 0);

            reporter.report(results, txResult);

            String output = outputStream.toString();
            assertTrue(output.contains("Total messages: 0"));
            assertTrue(output.contains("Valid messages: 0"));
            assertTrue(output.contains("Invalid messages: 0"));
            assertTrue(output.contains("Completed transactions: 0"));
            assertTrue(output.contains("Incomplete transactions: 0"));
        }

        @Test
        @DisplayName("Should report correct counts for successful processing")
        void shouldReportCorrectCountsForSuccessfulProcessing() {
            List<ProcessingResult> results = List.of(
                ProcessingResult.success(),
                ProcessingResult.success(),
                ProcessingResult.success()
            );
            TransactionResult txResult = new TransactionResult(2, 1);

            reporter.report(results, txResult);

            String output = outputStream.toString();
            assertTrue(output.contains("Total messages: 3"));
            assertTrue(output.contains("Valid messages: 3"));
            assertTrue(output.contains("Invalid messages: 0"));
            assertTrue(output.contains("Completed transactions: 2"));
            assertTrue(output.contains("Incomplete transactions: 1"));
        }

        @Test
        @DisplayName("Should report correct counts for mixed results")
        void shouldReportCorrectCountsForMixedResults() {
            List<ProcessingResult> results = List.of(
                ProcessingResult.success(),
                ProcessingResult.validationFailure(),
                ProcessingResult.success(),
                ProcessingResult.error("Test error")
            );
            TransactionResult txResult = new TransactionResult(1, 1);

            reporter.report(results, txResult);

            String output = outputStream.toString();
            assertTrue(output.contains("Total messages: 4"));
            assertTrue(output.contains("Valid messages: 2"));
            assertTrue(output.contains("Invalid messages: 2"));
        }

        @Test
        @DisplayName("Should report all invalid messages")
        void shouldReportAllInvalidMessages() {
            List<ProcessingResult> results = List.of(
                ProcessingResult.validationFailure(),
                ProcessingResult.validationFailure(),
                ProcessingResult.error("Error 1")
            );
            TransactionResult txResult = new TransactionResult(0, 0);

            reporter.report(results, txResult);

            String output = outputStream.toString();
            assertTrue(output.contains("Total messages: 3"));
            assertTrue(output.contains("Valid messages: 0"));
            assertTrue(output.contains("Invalid messages: 3"));
        }
    }

    @Nested
    @DisplayName("ProcessingResult Tests")
    class ProcessingResultTests {

        @Test
        @DisplayName("Success result should be valid and successful")
        void successResultShouldBeValidAndSuccessful() {
            ProcessingResult result = ProcessingResult.success();

            assertTrue(result.isSuccess());
            assertTrue(result.isValid());
            assertNull(result.getErrorMessage());
        }

        @Test
        @DisplayName("Validation failure should not be valid or successful")
        void validationFailureShouldNotBeValidOrSuccessful() {
            ProcessingResult result = ProcessingResult.validationFailure();

            assertFalse(result.isSuccess());
            assertFalse(result.isValid());
            assertNull(result.getErrorMessage());
        }

        @Test
        @DisplayName("Error result should contain error message")
        void errorResultShouldContainErrorMessage() {
            ProcessingResult result = ProcessingResult.error("Test error");

            assertFalse(result.isSuccess());
            assertFalse(result.isValid());
            assertEquals("Test error", result.getErrorMessage());
        }
    }
}

