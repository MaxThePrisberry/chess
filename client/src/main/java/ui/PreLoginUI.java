package ui;

import ui.model.UIData;

public class PreLoginUI extends UserUI {

    public static UIData help() {
        String output = """
                register <USERNAME> <PASSWORD> <EMAIL> - create a new user to play chess
                login <USERNAME> <PASSWORD> - login to play chess with an existing user
                quit - exit this chess program
                help - see available commands""";
        return new UIData(UIType.PRELOGIN, output);
    }
}
