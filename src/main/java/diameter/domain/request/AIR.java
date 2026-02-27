package diameter.domain.request;

import diameter.domain.MessageType;

public class AIR extends DiameterRequest {
    public AIR(boolean isRequest, String sessionId, String originHost, String originRealm, String userName) {
        super(MessageType.AIR, isRequest, sessionId, originHost, originRealm, userName);
    }

    @Override
    public boolean isMessageValid() {
        return super.isMessageValid();
    }
}