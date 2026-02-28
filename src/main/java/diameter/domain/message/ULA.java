package diameter.domain.message;

import diameter.domain.MessageType;

public class ULA extends DiameterAnswer {
    public ULA(String SessionId,
               String originHost,
               String originRealm,
               String userName,
               String resultCode) {
        super(MessageType.ULA, SessionId, originHost, originRealm, userName, resultCode);
    }
}

