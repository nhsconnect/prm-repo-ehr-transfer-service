package uk.nhs.prm.repo.ehrtransferservice.validator;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import uk.nhs.prm.repo.ehrtransferservice.database.model.ConversationRecord;

import static uk.nhs.prm.repo.ehrtransferservice.validator.ValidationUtility.isValidNhsNumber;

public class ConversationRecordValidator implements Validator {
    @Override
    public boolean supports(Class<?> clazz) {
        return ConversationRecord.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        final ConversationRecord conversationRecord = (ConversationRecord) target;
        if (conversationRecord.nhsNumber().isPresent()) {
            if(!isValidNhsNumber(conversationRecord.nhsNumber().get())) {
                errors.reject("An invalid NHS number was provided.");
            }
        }

        if(isValidNhsNumber(conversationRecord.nhsNumber().get())) {

        }
    }
}
