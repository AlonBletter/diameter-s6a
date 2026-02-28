package diameter.domain.message;

import diameter.domain.MessageType;

public class AIA extends DiameterAnswer {
    public AIA(String sessionId,
               String originHost,
               String originRealm,
               String userName,
               String resultCode) {
        super(MessageType.AIA, sessionId, originHost, originRealm, userName, resultCode);
    }
}

