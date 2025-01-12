package service;

import dataaccess.HandlerTargetedException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import service.model.LoginRequest;
import service.model.LogoutRequest;
import service.model.RegisterRequest;
import service.model.UserDataResult;

import static org.junit.jupiter.api.Assertions.*;

public class ServiceTests {

    private Phase3MasterService service = new Phase3MasterService();

    @Test
    @DisplayName("Register Positive Test")
    void registerValidUser() throws HandlerTargetedException {
        service.register(new RegisterRequest("Maxwell", "Pr1sbrey", "spam@gmail.com"));
    }

    @Test
    @DisplayName("Register Repeat User")
    void registerNullValue() throws HandlerTargetedException {
        //Register first user
        service.register(new RegisterRequest("Maxwell", "Pr1sbrey", "spam@gmail.com"));

        //Ensure that attempting to re-register under identical username produces exception
        assertThrows(HandlerTargetedException.class, () -> service.register(new RegisterRequest("Maxwell", "Potato", "spam@ail.com")));
    }

    @Test
    @DisplayName("Login Positive Test")
    void loginValid() throws HandlerTargetedException {
        service.register(new RegisterRequest("Maxwell", "Pr1sbrey", "spam@gmail.com"));

        assertDoesNotThrow(() -> service.login(new LoginRequest("Maxwell", "Pr1sbrey")));
    }

    @Test
    @DisplayName("Login Incorrect Password")
    void loginBadPassword() throws HandlerTargetedException {
        service.register(new RegisterRequest("Maxwell", "Pr1sbrey", "spam@gmail.com"));

        assertThrows(HandlerTargetedException.class, () -> service.login(new LoginRequest("Maxwell", "Incorrect")));
    }

    @Test
    @DisplayName("Login Nonexistent User")
    void loginUnregisteredUser() throws HandlerTargetedException {
        service.register(new RegisterRequest("Maxwell", "Pr1sbrey", "spam@gmail.com"));

        assertThrows(HandlerTargetedException.class, () -> service.login(new LoginRequest("Nathan", "Incorrect")));
    }

    @Test
    @DisplayName("Logout Positive Test")
    void logoutValid() throws HandlerTargetedException {
        UserDataResult user = service.register(new RegisterRequest("Maxwell", "Pr1sbrey", "spam@gmail.com"));
        assertDoesNotThrow(() -> service.logout(new LogoutRequest(user.authToken())));
    }

    @Test
    @DisplayName("Logout Nonexistent User Filled Database")
    void logoutBadFilled() throws HandlerTargetedException {
        UserDataResult user = service.register(new RegisterRequest("Maxwell", "Pr1sbrey", "spam@gmail.com"));
        assertThrows(HandlerTargetedException.class, () -> service.logout(new LogoutRequest("Faked authToken")));
    }

    @Test
    @DisplayName("Logout Nonexistent User Empty Database")
    void logoutBadEmpty() throws HandlerTargetedException {
        assertThrows(HandlerTargetedException.class, () -> service.logout(new LogoutRequest("This is my madeup authToken")));
    }

    @Test
    @DisplayName("")
}