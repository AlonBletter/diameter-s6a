package diameter.transaction;

import diameter.domain.message.DiameterMessage;
import diameter.domain.MessageType;
import diameter.exception.transaction.DuplicateTransactionException;
import diameter.exception.transaction.UnexpectedTransactionAnswerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class TransactionManagerImpl implements TransactionManager {
    private static final Logger LOG = LoggerFactory.getLogger(TransactionManagerImpl.class);

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
            LOG.error("Invalid message: message or sessionId is null");
            throw new IllegalArgumentException("Message and session ID cannot be null");
        }

        if (diameterMessage.getIsRequest()) {
            handleRequestMessage(diameterMessage);
        }
        else {
            handleAnswerMessage(diameterMessage);
        }
    }

    @Override
    public TransactionResult getTransactionResult() {
        return new TransactionResult(numberOfCompleteTransactions, numberOfIncompleteTransactions);
    }

    private void handleRequestMessage(DiameterMessage message) {
        String sessionId = message.getSessionId();
        if (transactionsBySessionId.containsKey(sessionId)) {
            throw new DuplicateTransactionException(sessionId);
        }

        incrementIncompleteTransactions();
        transactionsBySessionId.put(sessionId, new Transaction(message));
    }

    private void handleAnswerMessage(DiameterMessage message) {
        String sessionId = message.getSessionId();
        Transaction transaction = transactionsBySessionId.get(sessionId);
        if (transaction == null) {
            throw new UnexpectedTransactionAnswerException(sessionId);
        }

        if (isMessageTypeMatch(transaction.getRequest(), message)) {
            transaction.setAnswer(message);
            incrementCompleteTransactions();
        }
        else {
            LOG.warn("Transaction type mismatch: sessionId = {}, expectedAnswer = {}, actualAnswer = {}",
                    sessionId, answerByRequest.get(transaction.getRequest().getMessageType()), message.getMessageType());
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

