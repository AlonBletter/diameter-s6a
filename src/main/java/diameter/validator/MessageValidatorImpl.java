package diameter.validator;

import diameter.domain.message.DiameterMessage;

public class MessageValidatorImpl implements MessageValidator {
    @Override
    public ValidationResult validate(DiameterMessage message) {
        ValidationResult result = new ValidationResult();
        message.validate(result);
        return result;
    }
}