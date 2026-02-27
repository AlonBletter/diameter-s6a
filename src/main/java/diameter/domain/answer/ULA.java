package diameter.domain.answer;

import diameter.domain.MessageType;

public class ULA extends DiameterAnswer {
    public ULA(boolean isRequest,
               String SessionId,
               String originHost,
               String originRealm,
               String userName,
               String resultCode) {
        super(MessageType.ULA, isRequest, SessionId, originHost, originRealm, userName, resultCode);
    }

    @Override
    public boolean isMessageValid() {
        return super.isMessageValid();
    }
}

