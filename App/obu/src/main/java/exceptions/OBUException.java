package exceptions;

public class OBUException extends Exception {
    private String message;

    public OBUException(String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return this.message;
    }
}
