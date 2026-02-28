package diameter.reporter;

import diameter.transaction.TransactionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;

public class SummaryReporterImpl implements SummaryReporter {
    private static final Logger LOG = LoggerFactory.getLogger(SummaryReporterImpl.class);

    @Override
    public void report(List<ProcessingResult> results, TransactionResult transactionResult) {
        long total      = results.size();
        long valid      = results.stream().filter(ProcessingResult::isValid).count();
        long invalid    = total - valid;
        int  completed  = transactionResult.getNumberOfCompleteTransactions();
        int  incomplete = transactionResult.getNumberOfIncompleteTransactions();

        logSummary(total, valid, invalid, completed, incomplete);
        logErrors(results);
    }

    private static void logSummary(long total, long valid, long invalid, int completed, int incomplete) {
        String output = String.format(
            "Summary Report:\n" +
            "\t- Total messages: %d\n" +
            "\t- Valid messages: %d\n" +
            "\t- Invalid messages: %d\n" +
            "\t- Completed transactions: %d\n" +
            "\t- Incomplete transactions: %d", total, valid, invalid, completed, incomplete
        );

        LOG.info(output);
    }

    private static void logErrors(List<ProcessingResult> results) {
        List<String> errors = results.stream()
                                     .map(ProcessingResult::getErrorMessage)
                                     .filter(Objects::nonNull)
                                     .toList();

        if (!errors.isEmpty()) {
            StringBuilder errorLog = new StringBuilder("Processing errors:\n");
            errors.forEach(error -> errorLog.append("- ").append(error).append("\n"));
            LOG.warn(errorLog.toString());
        }
    }
}
