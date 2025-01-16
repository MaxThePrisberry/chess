package ui;

import chess.*;
import ui.model.UIData;
import ui.websocket.WSClient;
import websocket.commands.UserGameCommand;

import java.io.IOException;

import static ui.EscapeSequences.*;
import static ui.Variables.inGame;

public class GameplayUI extends ServerFacade {

    public static WSClient wsClient;
    public static ChessGame currentGame;

    public static UIData help() {
        String output = """
                redraw - redraws the chess board
                move <START POSITION> <END POSITION> - make a move in the game
                resign - resign from the game (you lose)
                highlight <PIECE> - highlight valid moves for the given piece
                leave - leave the game and return to login options
                help - see available commands""";
        return new UIData(UIType.GAMEPLAY, output);
    }

    public static void highlight() {

    }

    public static void resign() {

    }

    private static ChessPosition translatePosition(String given) {
        given = given.toLowerCase();
        int col;
        switch (given.charAt(0)) {
            case 'a' -> col = 1;
            case 'b' -> col = 2;
            case 'c' -> col = 3;
            case 'd' -> col = 4;
            case 'e' -> col = 5;
            case 'f' -> col = 6;
            case 'g' -> col = 7;
            case 'h' -> col = 8;
            default -> {
                throw new UIException(true, "Invalid position given.");
            }
        }
        int row = Integer.parseInt(String.valueOf(given.charAt(1)));
        if (row < 1 || row > 8) {
            throw new UIException(true, "Invalid position given.");
        }
        return new ChessPosition(row, col);
    }

    public static UIData move(String start, String end) {
        ChessPosition endPosition = translatePosition(end);
        for (ChessMove move : currentGame.validMoves(translatePosition(start))) {
            if (move.getEndPosition().equals(endPosition)) {
                try {
                    wsClient.send(UserGameCommand.CommandType.MAKE_MOVE, move);
                    return new UIData(UIType.GAMEPLAY, "Piece moved successfully.");
                } catch (IOException e) {
                    throw new UIException(false, "Error sending move over websocket.");
                }
            }
        }
        throw new UIException(true, "Move given is not a valid move.");
    }

    public static void redrawBoard() {
        System.out.print(ERASE_SCREEN);
        System.out.print('\n' + printChessBoard(wsClient.color, currentGame.getBoard()) + '\n');
    }

    public static void redrawBoardComplete() {
        redrawBoard();
        System.out.print(">>> ");
    }

    public static UIData redraw() {
        redrawBoard();
        return new UIData(UIType.GAMEPLAY, "\nBoard redrawn.");
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
        redrawBoard();
        System.out.print(notification + "\n\n>>> ");
    }

    public static void displayErrorNotification(String notification) {
        redrawBoard();
        System.out.print(SET_TEXT_COLOR_RED + notification + RESET_TEXT_COLOR + "\n\n>>> ");
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

    private static String printChessBoard(String color, ChessBoard board) {
        boolean normal = color == null || color.equals("WHITE");

        StringBuilder output = new StringBuilder("\r");
        if (normal) {
            output.append("\n # | A  B  C  D  E  F  G  H\n");
        } else {
            output.append("\n # | H  G  F  E  D  C  B  A\n");
        }
        for (int i = 1; i < 9; i++) {
            for (int j = 1; j < 11; j++) {
                if (j == 1) {
                    output.append(" ").append(normal ? 9 - i : i).append(" |");
                } else if (j == 10) {
                    output.append(RESET_BG_COLOR + "| ").append(normal ? 9 - i : i).append('\n');
                } else if ((i + j) % 2 == 0) {
                    output.append(SET_BG_COLOR_BLACK);
                    ChessPiece piece = board.getPiece(new ChessPosition((normal ? 9-i : i), (normal ? j-1 : 9-(j-1))));
                    if (piece != null) {
                        output.append(getPieceRepresentation(piece.getPieceType(), piece.getTeamColor()));
                    } else {
                        output.append("   ");
                    }
                } else {
                    output.append(SET_BG_COLOR_LIGHT_GREY);
                    ChessPiece piece = board.getPiece(new ChessPosition((normal ? 9-i : i), (normal ? j-1 : 9-(j-1))));
                    if (piece != null) {
                        output.append(getPieceRepresentation(piece.getPieceType(), piece.getTeamColor()));
                    } else {
                        output.append("   ");
                    }
                }
            }
        }
        if (normal) {
            output.append("   | A  B  C  D  E  F  G  H\n");
        } else {
            output.append("   | H  G  F  E  D  C  B  A\n");
        }
        return output.toString();
    }
}
