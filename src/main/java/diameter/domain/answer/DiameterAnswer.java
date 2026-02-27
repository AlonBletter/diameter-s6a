package diameter.domain.answer;

import diameter.domain.DiameterMessage;
import diameter.domain.MessageType;

public abstract class DiameterAnswer extends DiameterMessage {
    private final String resultCode;

    protected DiameterAnswer(MessageType messageType,
                             boolean isRequest,
                             String sessionId,
                             String originHost,
                             String originRealm,
                             String userName,
                             String resultCode) {
        super(messageType, isRequest, sessionId, originHost, originRealm, userName);
        this.resultCode = resultCode;
    }

    @Override
    public boolean isMessageValid() {
        return super.isMessageValid() && getMessageType() != null && !this.getIsRequest() &&
               resultCode != null && !resultCode.isEmpty();
    }
}