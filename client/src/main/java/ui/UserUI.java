package ui;

public abstract class UserUI {

    public static final String SERVER_LOCATION = "http://localhost:8080/user";

    public enum UIType {
        PRELOGIN(PreLoginUI.class),
        LOGIN(LoginUI.class);

        private final Class<? extends UserUI> uiClass;

        UIType(Class<? extends UserUI> uiClass) {  // Constructor
            this.uiClass = uiClass;
        }

        public Class<? extends UserUI> getUIClass() {  // Method
            return uiClass;
        }
    }

    public static void quit() {
        System.exit(0);
    }
}
