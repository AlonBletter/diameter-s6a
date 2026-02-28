package diameter.exception.transaction;

public class DuplicateTransactionException extends TransactionException {
    public DuplicateTransactionException(String sessionId) {
        super("Transaction with session ID " + sessionId + " already exists");
    }
}
