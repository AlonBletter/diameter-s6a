package diameter.domain.message;

import diameter.domain.MessageType;
import diameter.validator.ValidationResult;

public class ULR extends DiameterRequest {
    private final String visitedPlmnId;

    public ULR(String sessionId,
               String originHost,
               String originRealm,
               String userName,
               String visitedPlmnId) {
        super(MessageType.ULR, sessionId, originHost, originRealm, userName);
        this.visitedPlmnId = visitedPlmnId;
    }

    @Override
    public void validate(ValidationResult result) {
        super.validate(result);
        require(visitedPlmnId, "Visited-PLMN-Id is mandatory", result);
    }
}