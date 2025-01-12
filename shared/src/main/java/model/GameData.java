package model;

import chess.ChessGame;

record GameData (int gameID, String whiteUsername, String blackUsername, String gameName, ChessGame game) {}
