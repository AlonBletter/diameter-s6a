package diameter.validator;

import diameter.domain.message.DiameterMessage;

public interface MessageValidator {
    ValidationResult validate(DiameterMessage message);
}
