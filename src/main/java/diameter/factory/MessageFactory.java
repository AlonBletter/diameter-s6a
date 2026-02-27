package diameter.factory;

import diameter.domain.DiameterMessage;
import diameter.domain.MessageType;
import diameter.domain.answer.AIA;
import diameter.domain.answer.ULA;
import diameter.domain.request.AIR;
import diameter.domain.request.ULR;
import diameter.model.CsvRow;
import diameter.validation.exception.InvalidMessageTypeException;
import diameter.validation.exception.ValidationException;

public class MessageFactory {
    public static DiameterMessage createMessage(CsvRow row) {
        DiameterMessage retVal;

        if (row == null || row.getMessageType() == null) {
            throw new ValidationException("CSV row or message type cannot be null");
        }

        MessageType messageType = row.getMessageType();
        boolean isRequest = row.getIsRequest();
        String sessionId = row.getSessionId();
        String originHost = row.getOriginHost();
        String originRealm = row.getOriginRealm();
        String userName = row.getUserName();
        String visitedPlmnId = row.getVisitedPlmnId();
        String resultCode = row.getResultCode();

        switch (messageType) {
            case MessageType.AIR:
                retVal = new AIR(isRequest, sessionId, originHost, originRealm, userName);
                break;
            case MessageType.AIA:
                retVal = new AIA(isRequest, sessionId, originHost, originRealm, userName, resultCode);
                break;
            case MessageType.ULR:
                retVal = new ULR(isRequest, sessionId, originHost, originRealm, userName, visitedPlmnId);
                break;
            case MessageType.ULA:
                retVal = new ULA(isRequest, sessionId, originHost, originRealm, userName, resultCode);
                break;
            default:
                throw new InvalidMessageTypeException("Unknown message type: " + messageType);
        }

        return retVal;
    }
}

