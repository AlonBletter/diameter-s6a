package diameter.domain;

public abstract class DiameterMessage {
    private final MessageType messageType;
    private final boolean     isRequest;
    private final String      sessionId;
    private final String      originHost;
    private final String      originRealm;
    private final String      userName;

    public DiameterMessage(MessageType messageType,
                           boolean isRequest,
                           String sessionId,
                           String originHost,
                           String originRealm,
                           String userName) {
        this.messageType = messageType;
        this.isRequest = isRequest;
        this.sessionId = sessionId;
        this.originHost = originHost;
        this.originRealm = originRealm;
        this.userName = userName;
    }

    public boolean isMessageValid() {
        return sessionId != null && !sessionId.isEmpty() &&
               originHost != null && !originHost.isEmpty() &&
               originRealm != null && !originRealm.isEmpty() &&
               userName != null && !userName.isEmpty();
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public boolean getIsRequest() {
        return isRequest;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getOriginHost() {
        return originHost;
    }

    public String getOriginRealm() {
        return originRealm;
    }

    public String getUserName() {
        return userName;
    }
}