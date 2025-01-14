import chess.*;
import ui.PreLoginUI;
import ui.UIException;
import ui.UserUI;
import ui.model.UIData;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Class<? extends UserUI> userUI = PreLoginUI.class;
        Scanner scanner = new Scanner(System.in);
        System.out.print("Welcome to the 240 Chess Client!\n\nEnter 'help' to get started.\n\n>>> ");
        while (true) {
            String[] line = scanner.nextLine().strip().split(" ");
            try {
                Class<?>[] parameterClassTypes = new Class<?>[line.length - 1];
                Arrays.fill(parameterClassTypes, String.class);

                Method targetMethod = userUI.getMethod(line[0].toLowerCase(), parameterClassTypes);
                UIData result = (UIData) targetMethod.invoke(null, (Object[]) Arrays.copyOfRange(line, 1, line.length));
                System.out.println(result.output());
                userUI = result.uiType().getUIClass();

                System.out.print("\n>>> ");
            } catch (NoSuchMethodException | IllegalAccessException e) {
                System.out.print("There was an error executing your command. Is it a valid option in ['help']? Did you enter the right number of parameters? Please try again.\n>>> ");
            } catch (InvocationTargetException e) {
                if (e.getCause() instanceof UIException) {
                    if (((UIException) e.getCause()).userCaused) {
                        System.out.print("An error occurred due to the parameters you entered: " + e.getCause().getMessage() + "\n>>> ");
                    } else {
                        System.out.print("There was an error communicating with the server: " + e.getCause().getMessage() + "\n>>> ");
                    }
                } else {
                    System.out.print("A wild exception has appeared! Give up hope.\n" + e.getCause().getMessage() + "\n>>> ");
                }
            }
        }
    }
}