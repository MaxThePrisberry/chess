package service.model;

public record joinGameRequest(String authToken, String playerColor, int gameID) {}
