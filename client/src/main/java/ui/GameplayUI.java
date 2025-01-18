package ui;

import chess.*;
import ui.model.UIData;
import ui.websocket.WSClient;
import websocket.commands.UserGameCommand;

import java.io.IOException;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

import static ui.EscapeSequences.*;
import static ui.Variables.inGame;

public class GameplayUI extends ServerFacade {

    public static WSClient wsClient;
    public static ChessGame currentGame;

    public static UIData help() {
        String output;
        if (inGame && wsClient.color == null) {
            output = """
                    redraw - redraws the chess board
                    highlight <PIECE> - highlight valid moves for the given piece
                    leave - leave the game and return to login options
                    help - see available commands""";
        } else {
            output = """
                    redraw - redraws the chess board
                    move <START POSITION> <END POSITION> - make a move in the game
                    resign - resign from the game (you lose)
                    highlight <PIECE> - highlight valid moves for the given piece
                    leave - leave the game and return to login options
                    help - see available commands""";
        }
        return new UIData(UIType.GAMEPLAY, output);
    }

    public static UIData highlight(String position) {
        ChessPosition chessPosition = translatePosition(position);
        ChessPiece piece = currentGame.getBoard().getPiece(chessPosition);
        if (piece == null) {
            return new UIData(UIType.GAMEPLAY, "No piece at given location.");
        }
        Set<ChessPosition> endPositions = new HashSet<ChessPosition>();
        for (ChessMove move : currentGame.validMoves(chessPosition)) {
            endPositions.add(move.getEndPosition());
        }
        return new UIData(UIType.GAMEPLAY, printHighlightedBoard(wsClient.color, currentGame.getBoard(), chessPosition,
                endPositions));
    }

    public static UIData resign() {
        if (wsClient.color == null) {
            throw new UIException(true, "Cannot resign as observer.");
        }
        Scanner scanner = new Scanner(System.in);
        System.out.println("Are you sure you want to resign? Y/n");
        String input = scanner.nextLine().toLowerCase();
        if (!input.equals("n")) {
            try {
                wsClient.send(UserGameCommand.CommandType.RESIGN);
                return new UIData(UIType.GAMEPLAY, "You have resigned.");
            } catch (IOException e) {
                throw new UIException(false, "WebSocket send resign error.");
            }
        } else {
            return new UIData(UIType.GAMEPLAY, "Resign cancelled.");
        }
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
        if (wsClient.color == null) {
            throw new UIException(true, "Cannot move as observer.");
        }
        ChessPosition endPosition = translatePosition(end);
        for (ChessMove move : currentGame.validMoves(translatePosition(start))) {
            ChessPiece.PieceType type = null;
            if (move.getPromotionPiece() != null) {
                Scanner scanner = new Scanner(System.in);
                System.out.println("What piece would you like to promote to? [Q, B, N, R]");
                switch (scanner.nextLine().toLowerCase()) {
                    case "q" -> type = ChessPiece.PieceType.QUEEN;
                    case "b" -> type = ChessPiece.PieceType.BISHOP;
                    case "n" -> type = ChessPiece.PieceType.KNIGHT;
                    case "r" -> type = ChessPiece.PieceType.ROOK;
                }
            }
            if (move.getEndPosition().equals(endPosition) && move.getPromotionPiece().equals(type)) {
                try {
                    wsClient.send(UserGameCommand.CommandType.MAKE_MOVE, move);
                    return new UIData(UIType.GAMEPLAY, "");
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
        if (color == ChessGame.TeamColor.BLACK) {
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

    private static StringBuilder printTopLine(boolean normal) {
        StringBuilder output = new StringBuilder("\r");
        if (normal) {
            output.append("\n # | A  B  C  D  E  F  G  H\n");
        } else {
            output.append("\n # | H  G  F  E  D  C  B  A\n");
        }
        return output;
    }

    private static void printBottomLine(boolean normal, StringBuilder builder) {
        if (normal) {
            builder.append("   | A  B  C  D  E  F  G  H\n");
        } else {
            builder.append("   | H  G  F  E  D  C  B  A\n");
        }
    }

    private static String printChessBoard(String color, ChessBoard board) {
        boolean normal = color == null || color.equals("WHITE");

        StringBuilder output = printTopLine(normal);
        for (int i = 1; i < 9; i++) {
            for (int j = 1; j < 11; j++) {
                if (j == 1) {
                    output.append(" ").append(normal ? 9 - i : i).append(" |");
                } else if (j == 10) {
                    output.append(RESET_BG_COLOR + "| ").append(normal ? 9 - i : i).append('\n');
                } else {
                    ChessPosition position = new ChessPosition((normal ? 9 - i : i), (normal ? j - 1 : 9 - (j - 1)));
                    ChessPiece piece = board.getPiece(position);
                    if ((i + j) % 2 == 0) {
                        output.append(SET_BG_COLOR_BLACK);
                    } else {
                        output.append(SET_BG_COLOR_LIGHT_GREY);
                    }
                    if (piece != null) {
                        output.append(getPieceRepresentation(piece.getPieceType(), piece.getTeamColor()));
                    } else {
                        output.append("   ");
                    }
                }
            }
        }
        printBottomLine(normal, output);
        return output.toString();
    }

    private static String printHighlightedBoard(String color, ChessBoard board, ChessPosition start, Set<ChessPosition> highlighted) {
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
                } else {
                    ChessPosition position = new ChessPosition((normal ? 9 - i : i), (normal ? j - 1 : 9 - (j - 1)));
                    ChessPiece piece = board.getPiece(position);
                    if (position.equals(start)) {
                        output.append(SET_BG_COLOR_DARK_GREEN);
                    } else if (highlighted.contains(position)) {
                        output.append(SET_BG_COLOR_GREEN);
                    } else if ((i + j) % 2 == 0) {
                        output.append(SET_BG_COLOR_BLACK);
                    } else {
                        output.append(SET_BG_COLOR_LIGHT_GREY);
                    }
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
