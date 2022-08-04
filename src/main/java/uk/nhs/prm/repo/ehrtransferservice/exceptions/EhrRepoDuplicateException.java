package uk.nhs.prm.repo.ehrtransferservice.exceptions;

public class EhrRepoDuplicateException extends Exception {

    public EhrRepoDuplicateException() {
        super();
    }

    public EhrRepoDuplicateException(String message) {
        super(message);
    }
}
