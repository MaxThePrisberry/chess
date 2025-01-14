package ui;

public class UIException extends RuntimeException {

    public boolean userCaused;

    public UIException(boolean userCaused, String message) {
        super(message);
        this.userCaused = userCaused;
    }
}
