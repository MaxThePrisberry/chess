import chess.*;
import ui.PreLoginUI;
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
                Method targetMethod = userUI.getMethod(line[0]);
                UIData result = (UIData) targetMethod.invoke(null, (Object[]) Arrays.copyOfRange(line, 1, line.length));
                System.out.println(result.output());
                userUI = result.uiType().getUIClass();

                System.out.print("\n>>> ");
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
    }
}