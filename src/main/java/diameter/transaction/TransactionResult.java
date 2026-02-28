package diameter.transaction;

public class TransactionResult {
    private final int numberOfCompleteTransactions;
    private final int numberOfIncompleteTransactions;

    public TransactionResult(int numberOfCompleteTransactions, int numberOfIncompleteTransactions) {
        this.numberOfCompleteTransactions = numberOfCompleteTransactions;
        this.numberOfIncompleteTransactions = numberOfIncompleteTransactions;
    }

    public int getNumberOfCompleteTransactions() {
        return numberOfCompleteTransactions;
    }

    public int getNumberOfIncompleteTransactions() {
        return numberOfIncompleteTransactions;
    }
}
