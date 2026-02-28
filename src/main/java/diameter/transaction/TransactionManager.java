package diameter.transaction;

import diameter.domain.message.DiameterMessage;
import diameter.domain.MessageType;
import diameter.exception.transaction.DuplicateTransactionException;
import diameter.exception.transaction.UnexpectedTransactionAnswerException;

import java.util.HashMap;
import java.util.Map;

public class TransactionManager {
    private static volatile TransactionManager            instance;
    private                 int                           numberOfCompleteTransactions   = 0;
    private                 int                           numberOfIncompleteTransactions = 0;
    private final           Map<String, Transaction>      transactionsBySessionId        = new HashMap<>();
    private static final    Map<MessageType, MessageType> requestAnswerMap               =
            Map.of(MessageType.AIR, MessageType.AIA, MessageType.ULR, MessageType.ULA);

    private TransactionManager() {}

    public static TransactionManager getInstance() {
        if (instance == null) {
            synchronized (TransactionManager.class) {
                if (instance == null) {
                    instance = new TransactionManager();
                }
            }
        }

        return instance;
    }

    public void addMessage(DiameterMessage message) {
        if (message == null || message.getSessionId() == null) {
            throw new IllegalArgumentException("Message and session ID cannot be null");
        }

        if (message.getIsRequest()) {
            handleRequestMessage(message);
        } else {
            handleAnswerMessage(message);
        }
    }

    public int getNumberOfCompleteTransactions() {
        return numberOfCompleteTransactions;
    }

    public int getNumberOfIncompleteTransactions() {
        return numberOfIncompleteTransactions;
    }

    private void handleRequestMessage(DiameterMessage message) {
        if (transactionsBySessionId.containsKey(message.getSessionId())) {
            throw new DuplicateTransactionException(message.getSessionId());
        }

        incrementIncompleteTransactions();
        transactionsBySessionId.put(message.getSessionId(), new Transaction(message));
    }

    private void handleAnswerMessage(DiameterMessage message) {
        Transaction transaction = transactionsBySessionId.get(message.getSessionId());
        if (transaction == null) {
            throw new UnexpectedTransactionAnswerException(message.getSessionId());
        }

        if (isMessageTypeMatch(transaction.getRequest(), message)) {
            transaction.setAnswer(message);
            incrementCompleteTransactions();
        }
    }

    private boolean isMessageTypeMatch(DiameterMessage request, DiameterMessage message) {
        return requestAnswerMap.get(request.getMessageType()) == message.getMessageType();
    }

    private void incrementCompleteTransactions() {
        numberOfCompleteTransactions++;
        numberOfIncompleteTransactions--;
    }

    private void incrementIncompleteTransactions() {
        numberOfIncompleteTransactions++;
    }

    private static class Transaction {
        private final DiameterMessage request;
        private       DiameterMessage answer;

        public Transaction(DiameterMessage request) {
            this.request = request;
        }

        public DiameterMessage getRequest() {
            return request;
        }

        public DiameterMessage getAnswer() {
            return answer;
        }

        public void setAnswer(DiameterMessage answer) {
            this.answer = answer;
        }
    }
}

