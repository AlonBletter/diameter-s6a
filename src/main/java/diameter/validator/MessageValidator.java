package diameter.validator;

import diameter.domain.message.DiameterMessage;

public class MessageValidator {
    public ValidationResult validate(DiameterMessage message) {
        ValidationResult result = new ValidationResult();
        message.validate(result);
        return result;
    }
}