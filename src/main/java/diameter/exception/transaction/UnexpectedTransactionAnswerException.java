package diameter.exception.transaction;

public class UnexpectedTransactionAnswerException extends TransactionException {
    public UnexpectedTransactionAnswerException(String sessionId) {
        super("No existing transaction for session ID " + sessionId);
    }
}
