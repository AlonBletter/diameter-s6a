package diameter.csv.model;

public class MessagesSummary {
    private int totalMessages;
    private int numberOfValidMessages;
    private int numberOfInvalidMessages;
    private int numberOfCompletedTransactions;
    private int numberOfIncompleteTransactions;

    public MessagesSummary() {
        this.totalMessages = 0;
        this.numberOfValidMessages = 0;
        this.numberOfInvalidMessages = 0;
        this.numberOfCompletedTransactions = 0;
        this.numberOfIncompleteTransactions = 0;
    }

    public void incrementTotalMessages() {
        this.totalMessages++;
    }

    public void incrementNumberOfValidMessages() {
        this.numberOfValidMessages++;
    }

    public void incrementNumberOfInvalidMessages() {
        this.numberOfInvalidMessages++;
    }

    public void setNumberOfCompletedTransactions(int numberOfCompletedTransactions) {
        this.numberOfCompletedTransactions = numberOfCompletedTransactions;
    }

    public void setNumberOfIncompleteTransactions(int numberOfIncompleteTransactions) {
        this.numberOfIncompleteTransactions = numberOfIncompleteTransactions;
    }

    public String getReport() {
        return String.format(
                "Total Messages: %d%nValid Messages: %d%nInvalid Messages: %d%nCompleted Transactions: %d%nIncomplete Transactions: %d",
                totalMessages, numberOfValidMessages, numberOfInvalidMessages, numberOfCompletedTransactions,
                numberOfIncompleteTransactions);
    }
}
