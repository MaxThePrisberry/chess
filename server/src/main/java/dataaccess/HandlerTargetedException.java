package dataaccess;

/**
 * Indicates there was an error connecting to the database
 */
public class HandlerTargetedException extends Exception{
    private final int errorNumber;

    public HandlerTargetedException(int errorNumber, String message) {
        super(message);
        this.errorNumber = errorNumber;
    }

    public int getErrorNumber() {
        return errorNumber;
    }
}
