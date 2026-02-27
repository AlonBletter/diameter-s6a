package diameter.transaction;

import diameter.domain.DiameterMessage;

import java.util.HashMap;
import java.util.Map;

public class TransactionManager {
    private final           Map<String, DiameterMessage> transactionsBySessionId = new HashMap<>();
    private static volatile TransactionManager           instance;

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

    public void addTransaction(DiameterMessage message) {
        if (message != null && message.getSessionId() != null) {
            transactionsBySessionId.put(message.getSessionId(), message);
        }
    }

    public DiameterMessage getTransaction(String sessionId) {
        return transactionsBySessionId.get(sessionId);
    }

    public void removeTransaction(String sessionId) {
        transactionsBySessionId.remove(sessionId);
    }
}

