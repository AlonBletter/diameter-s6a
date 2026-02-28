package diameter.domain.message;

import diameter.domain.MessageType;
import diameter.validator.ValidationResult;

public abstract class DiameterRequest extends DiameterMessage {
    protected DiameterRequest(MessageType messageType,
                              String sessionId,
                              String originHost,
                              String originRealm,
                              String userName) {
        super(messageType, true, sessionId, originHost, originRealm, userName);
    }

    @Override
    public void validate(ValidationResult result) {
        require(sessionId, "Session-Id is mandatory", result);
        require(originHost, "Origin-Host is mandatory", result);
        require(originRealm, "Origin-Realm is mandatory", result);
        require(userName, "User-Name is mandatory", result);
    }
}