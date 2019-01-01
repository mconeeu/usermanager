package eu.mcone.usermanager.api.exception;

public class MojangApiException extends Exception {

    public MojangApiException() {
        super();
    }

    public MojangApiException(String message) {
        super(message);
    }

    public MojangApiException(String message, Throwable cause) {
        super(message, cause);
    }

    public MojangApiException(Throwable cause) {
        super(cause);
    }

}
