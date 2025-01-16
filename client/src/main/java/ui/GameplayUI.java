package ui;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;
import ui.model.UIData;
import ui.websocket.WSClient;
import websocket.commands.UserGameCommand;

import java.io.IOException;

import static ui.EscapeSequences.*;
import static ui.Variables.inGame;

public class GameplayUI extends ServerFacade {

    public static WSClient wsClient;
    public static ChessBoard currentBoard;

    public static UIData help() {
        String output = """
                redraw - redraws the chess board
                move <START POSITION> <END POSITION> - make a move in the game
                resign - resign from the game (you lose)
                highlight <PIECE> - highlight valid moves for the given piece
                leave - leave the game and return to login options
                help - see available commands""";
        return new UIData(ServerFacade.UIType.LOGIN, output);
    }

    public static void redrawBoard() {

    }

    public static UIData redraw() {
        redrawBoard();
        return new UIData(UIType.GAMEPLAY, "Board redrawn.");
    }

    public static UIData leave() {
        try {
            wsClient.send(UserGameCommand.CommandType.LEAVE);
            inGame = false;
            return new UIData(UIType.LOGIN, "Returned to the login menu.");
        } catch (IOException e) {
            throw new UIException(false, "WebSocket threw an IOException.");
        }
    }

    public static void displayNotification(String notification) {

    }

    public static void displayErrorNotification(String notification) {

    }

    private static String getPieceRepresentation(ChessPiece.PieceType type, ChessGame.TeamColor color) {
        if (color == ChessGame.TeamColor.WHITE) {
            switch (type) {
                case KING -> {
                    return WHITE_KING;
                }
                case QUEEN -> {
                    return WHITE_QUEEN;
                }
                case BISHOP -> {
                    return WHITE_BISHOP;
                }
                case KNIGHT -> {
                    return WHITE_KNIGHT;
                }
                case ROOK -> {
                    return WHITE_ROOK;
                }
                case PAWN -> {
                    return WHITE_PAWN;
                }
            }
        } else {
            switch (type) {
                case KING -> {
                    return BLACK_KING;
                }
                case QUEEN -> {
                    return BLACK_QUEEN;
                }
                case BISHOP -> {
                    return BLACK_BISHOP;
                }
                case KNIGHT -> {
                    return BLACK_KNIGHT;
                }
                case ROOK -> {
                    return BLACK_ROOK;
                }
                case PAWN -> {
                    return BLACK_PAWN;
                }
            }
        }
        return null;
    }

    private static String printChessBoard(String color) {
        ChessBoard board = new ChessBoard();
        board.resetBoard();
        boolean normal = !color.equals("WHITE");

        StringBuilder output = new StringBuilder();
        if (normal) {
            output.append("\n # | A  B  C  D  E  F  G  H\n");
        } else {
            output.append("\n # | H  G  F  E  D  C  B  A\n");
        }
        for (int i = 1; i < 9; i++) {
            for (int j = 1; j < 10; j++) {
                if (j == 1) {
                    output.append(" " + (normal ? 9-i : i) + " |");
                } else if ((i + j) % 2 == 0) {
                    output.append(SET_BG_COLOR_BLACK);
                    ChessPiece piece = board.getPiece(new ChessPosition((normal ? i : 9-i), (normal ? j-1 : 9-(j-1))));
                    if (piece != null) {
                        output.append(getPieceRepresentation(piece.getPieceType(), piece.getTeamColor()));
                    } else {
                        output.append("   ");
                    }
                } else {
                    output.append(SET_BG_COLOR_LIGHT_GREY);
                    ChessPiece piece = board.getPiece(new ChessPosition((normal ? i : 9-i), (normal ? j-1 : 9-(j-1))));
                    if (piece != null) {
                        output.append(getPieceRepresentation(piece.getPieceType(), piece.getTeamColor()));
                    } else {
                        output.append("   ");
                    }
                }
            }
            output.append(RESET_BG_COLOR + "|\n");
        }
        return output.toString();
    }
}
