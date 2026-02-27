package diameter.domain.request;

import diameter.domain.DiameterMessage;
import diameter.domain.MessageType;

public abstract class DiameterRequest extends DiameterMessage {
    protected DiameterRequest(MessageType messageType,
                              boolean isRequest,
                              String sessionId,
                              String originHost,
                              String originRealm,
                              String userName) {
        super(messageType, isRequest, sessionId, originHost, originRealm, userName);
    }

    @Override
    public boolean isMessageValid() {
        return super.isMessageValid() && getMessageType() != null && this.getIsRequest();
    }
}