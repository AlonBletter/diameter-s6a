package diameter.reporter;

public class SummaryReporterImpl implements SummaryReporter {
    private int totalMessages;
    private int numberOfValidMessages;
    private int numberOfInvalidMessages;
    private int numberOfCompletedTransactions;
    private int numberOfIncompleteTransactions;

    public SummaryReporterImpl() {
        this.totalMessages = 0;
        this.numberOfValidMessages = 0;
        this.numberOfInvalidMessages = 0;
        this.numberOfCompletedTransactions = 0;
        this.numberOfIncompleteTransactions = 0;
    }

    @Override
    public void incrementTotalMessages() {
        this.totalMessages++;
    }

    @Override
    public void incrementNumberOfValidMessages() {
        this.numberOfValidMessages++;
    }

    @Override
    public void incrementNumberOfInvalidMessages() {
        this.numberOfInvalidMessages++;
    }

    @Override
    public void setNumberOfCompletedTransactions(int numberOfCompletedTransactions) {
        this.numberOfCompletedTransactions = numberOfCompletedTransactions;
    }

    @Override
    public void setNumberOfIncompleteTransactions(int numberOfIncompleteTransactions) {
        this.numberOfIncompleteTransactions = numberOfIncompleteTransactions;
    }

    @Override
    public String getReport() {
        return String.format(
                "Total Messages: %d%n" +
                "Valid Messages: %d%n" +
                "Invalid Messages: %d%n" +
                "Completed Transactions: %d%n" +
                "Incomplete Transactions: %d",
                totalMessages, numberOfValidMessages, numberOfInvalidMessages, numberOfCompletedTransactions,
                numberOfIncompleteTransactions);
    }
}
