package diameter.domain.answer;

import diameter.domain.MessageType;

public class AIA extends DiameterAnswer {
    public AIA(boolean isRequest,
               String sessionId,
               String originHost,
               String originRealm,
               String userName,
               String resultCode) {
        super(MessageType.AIA, isRequest, sessionId, originHost, originRealm, userName, resultCode);
    }

    @Override
    public boolean isMessageValid() {
        return super.isMessageValid() && getMessageType() == MessageType.AIA && !getIsRequest();
    }
}

