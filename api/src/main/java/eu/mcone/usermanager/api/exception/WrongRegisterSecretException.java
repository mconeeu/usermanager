package eu.mcone.usermanager.api.exception;

public class WrongRegisterSecretException extends UserRegisterException {

    public WrongRegisterSecretException() {
        super();
    }

    public WrongRegisterSecretException(String message) {
        super(message);
    }

    public WrongRegisterSecretException(String message, Throwable cause) {
        super(message, cause);
    }

    public WrongRegisterSecretException(Throwable cause) {
        super(cause);
    }

}
