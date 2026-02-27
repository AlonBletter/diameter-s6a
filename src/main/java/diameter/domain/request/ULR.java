package diameter.domain.request;

import diameter.domain.MessageType;

public class ULR extends DiameterRequest {
    private final String visitedPlmnId;

    public ULR(boolean isRequest,
               String sessionId,
               String originHost,
               String originRealm,
               String userName,
               String visitedPlmnId) {
        super(MessageType.ULR, isRequest, sessionId, originHost, originRealm, userName);
        this.visitedPlmnId = visitedPlmnId;
    }

    @Override
    public boolean isMessageValid() {
        return super.isMessageValid() && visitedPlmnId != null && !visitedPlmnId.isEmpty();
    }
}