package exception;

public class ResponseException extends Exception {

    private String message;
    private int status;

    public ResponseException(String message, int status) {
        super(message);
        this.message = message;
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public int getStatus() {
        return status;
    }
}
