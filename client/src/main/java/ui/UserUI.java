package ui;

public abstract class UserUI {

    public enum UIType {
        PRELOGIN(PreLoginUI.class);

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
