package service.model;

public record JoinGameRequest(String authToken, String playerColor, int gameID) {}
