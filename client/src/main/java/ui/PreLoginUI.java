package ui;

public class PreLoginUI extends UserUI {

    @Override
    public String help() {
        return """
                register <USERNAME> <PASSWORD> <EMAIL> - create a new user to play chess
                login <USERNAME> <PASSWORD> - login to play chess with an existing user
                quit - exit this chess program
                help - see available commands""";
    }
}
