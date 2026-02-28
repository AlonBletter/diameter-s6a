package diameter.reporter;

import diameter.transaction.TransactionResult;

import java.util.List;

public interface SummaryReporter {
    void report(List<ProcessingResult> results, TransactionResult transactionResult);
}
