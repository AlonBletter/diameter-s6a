package diameter.transaction;

import diameter.domain.message.DiameterMessage;
import diameter.exception.transaction.TransactionException;

public interface TransactionManager {
    void processDiameterMessage(DiameterMessage diameterMessage) throws TransactionException;

    TransactionResult getTransactionResult();
}
