package diameter.domain.message;

import diameter.domain.MessageType;
import diameter.validator.ValidationResult;

public abstract class DiameterAnswer extends DiameterMessage {
    private final String resultCode;

    protected DiameterAnswer(MessageType messageType,
                             String sessionId,
                             String originHost,
                             String originRealm,
                             String userName,
                             String resultCode) {
        super(messageType, false, sessionId, originHost, originRealm, userName);
        this.resultCode = resultCode;
    }

    @Override
    public void validate(ValidationResult result) {
        require(resultCode, "Result-Code is required", result);
    }
}