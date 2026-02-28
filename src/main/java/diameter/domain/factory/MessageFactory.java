package diameter.domain.factory;

import diameter.csv.model.CsvRow;
import diameter.domain.message.DiameterMessage;

public interface MessageFactory {
    DiameterMessage createDiameterMessage(CsvRow csvRow);
}
