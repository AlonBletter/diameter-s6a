package diameter.domain.message;

import diameter.domain.MessageType;
import diameter.validator.ValidationResult;

public abstract class DiameterMessage {
    private final MessageType messageType;
    private final boolean     isRequest;
    protected final String      sessionId;
    protected final String      originHost;
    protected final String      originRealm;
    protected final String      userName;

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

    public abstract void validate(ValidationResult result);

    protected void require(String value, String errorMessage, ValidationResult result) {
        if (value == null || value.isEmpty()) {
            result.addError(errorMessage);
        }
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