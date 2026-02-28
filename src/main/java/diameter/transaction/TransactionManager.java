package diameter.transaction;

import diameter.domain.message.DiameterMessage;

public interface TransactionManager {
    void processDiameterMessage(DiameterMessage diameterMessage);

    int getNumberOfCompleteTransactions();

    int getNumberOfIncompleteTransactions();
}
