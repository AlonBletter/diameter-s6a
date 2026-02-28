package diameter.transaction;

import diameter.domain.message.DiameterMessage;
import diameter.domain.MessageType;
import diameter.exception.transaction.DuplicateTransactionException;
import diameter.exception.transaction.UnexpectedTransactionAnswerException;

import java.util.HashMap;
import java.util.Map;

public class TransactionManagerImpl implements TransactionManager {
    private static volatile TransactionManagerImpl        instance;
    private                 int                           numberOfCompleteTransactions   = 0;
    private                 int                           numberOfIncompleteTransactions = 0;
    private final           Map<String, Transaction>      transactionsBySessionId        = new HashMap<>();
    private static final    Map<MessageType, MessageType> answerByRequest                =
            Map.of(MessageType.AIR, MessageType.AIA, MessageType.ULR, MessageType.ULA);

    private TransactionManagerImpl() {}

    public static TransactionManagerImpl getInstance() {
        if (instance == null) {
            synchronized (TransactionManagerImpl.class) {
                if (instance == null) {
                    instance = new TransactionManagerImpl();
                }
            }
        }

        return instance;
    }

    @Override
    public void processDiameterMessage(DiameterMessage diameterMessage) {
        if (diameterMessage == null || diameterMessage.getSessionId() == null) {
            throw new IllegalArgumentException("Message and session ID cannot be null");
        }

        if (diameterMessage.getIsRequest()) {
            handleRequestMessage(diameterMessage);
        } else {
            handleAnswerMessage(diameterMessage);
        }
    }

    @Override
    public int getNumberOfCompleteTransactions() {
        return numberOfCompleteTransactions;
    }

    @Override
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
        return answerByRequest.get(request.getMessageType()) == message.getMessageType();
    }

    private void incrementCompleteTransactions() {
        numberOfCompleteTransactions++;
        numberOfIncompleteTransactions--;
    }

    private void incrementIncompleteTransactions() {
        numberOfIncompleteTransactions++;
    }
}

