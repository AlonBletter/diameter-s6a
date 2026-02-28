package diameter.reporter;

public interface SummaryReporter {

    void incrementTotalMessages();

    void incrementNumberOfValidMessages();

    void incrementNumberOfInvalidMessages();

    void setNumberOfCompletedTransactions(int numberOfCompletedTransactions);

    void setNumberOfIncompleteTransactions(int numberOfIncompleteTransactions);

    String getReport();
}
