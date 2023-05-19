package exceptions;

public class TerminalException extends Exception {
    private String message;

    public TerminalException(String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return this.message;
    }
}
