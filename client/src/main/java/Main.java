import ui.GameplayUI;
import ui.PreLoginUI;
import ui.UIException;
import ui.ServerFacade;
import ui.model.UIData;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Scanner;

import static ui.Variables.inGame;

public class Main {
    public static void main(String[] args) {
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "WARN");
        run();
    }

    public static void run() {
        Class<? extends ServerFacade> userUI = PreLoginUI.class;
        Scanner scanner = new Scanner(System.in);

        System.out.print("Welcome to the 240 Chess Client!\n\nEnter 'help' to get started.\n\n>>> ");

        while (true) {
            String[] line = scanner.nextLine().strip().split(" ");
            try {
                Class<?>[] parameterClassTypes = new Class<?>[line.length - 1];
                Arrays.fill(parameterClassTypes, String.class);

                Method targetMethod = userUI.getMethod(line[0].toLowerCase(), parameterClassTypes);
                UIData result = (UIData) targetMethod.invoke(null, (Object[]) Arrays.copyOfRange(line, 1, line.length));
                userUI = result.uiType().getUIClass();

                System.out.println(result.output());
                System.out.print("\n>>> ");
            } catch (NoSuchMethodException | IllegalAccessException e) {
                System.out.print("There was an error executing your command. Is it a valid option in ['help']? Did you enter the right number of parameters? Please try again.\n>>> ");
            } catch (InvocationTargetException e) {
                if (e.getCause() instanceof UIException) {
                    if (((UIException) e.getCause()).userCaused) {
                        System.out.print(e.getCause().getMessage() + "\n\n>>> ");
                    } else {
                        System.out.print("A server communication error has occurred. Try again in a moment. " + e.getCause().getMessage() + "\n>>> ");
                    }
                } else {
                    System.out.print("A wild exception has appeared! Give up hope.\n" + e.getCause().getMessage() + "\n>>> ");
                }
            }
        }
    }
}