package uk.nhs.prm.repo.ehrtransferservice.validator;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import uk.nhs.prm.repo.ehrtransferservice.database.model.MessageRecord;

public class MessageRecordValidator implements Validator {
    @Override
    public boolean supports(Class<?> clazz) {
        return MessageRecord.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {

    }
}
