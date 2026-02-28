package diameter.reporter;

import diameter.transaction.TransactionResult;

import java.util.List;

public class SummaryReporterImpl implements SummaryReporter {
    @Override
    public void report(List<ProcessingResult> results, TransactionResult transactionResult) {
        long total   = results.size();
        long valid   = results.stream().filter(ProcessingResult::isValid).count();
        long invalid = total - valid;

        String output = String.format(
            "Total messages: %d\n" +
            "Valid messages: %d\n" +
            "Invalid messages: %d\n" +
            "Completed transactions: %d\n" +
            "Incomplete transactions: %d",
            total,
            valid,
            invalid,
            transactionResult.getNumberOfCompleteTransactions(),
            transactionResult.getNumberOfIncompleteTransactions()
        );

        System.out.println(output);

        System.out.println("\nErrors:");
        results.stream()
               .filter(r -> r.getErrorMessage() != null)
               .forEach(r -> System.err.println(r.getErrorMessage()));
    }
}
