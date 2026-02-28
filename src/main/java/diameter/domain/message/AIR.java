package diameter.domain.message;

import diameter.domain.MessageType;

public class AIR extends DiameterRequest {
    public AIR(String sessionId, String originHost, String originRealm, String userName) {
        super(MessageType.AIR, sessionId, originHost, originRealm, userName);
    }
}