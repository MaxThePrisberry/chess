package chess;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {

    private TeamColor turnColor;
    private ChessBoard board;
    private static final int[] xdirs = {-1, 1, 0, 0, -1, -1, 1, 1};
    private static final int[] ydirs = {0, 0, -1, 1, -1, 1, -1, 1};

    public ChessGame() {
        turnColor = TeamColor.WHITE;
        board = new ChessBoard();
        board.resetBoard();
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return turnColor;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        turnColor = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to preform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessPiece piece = board.getPiece(move.getStartPosition());
        if (piece.getTeamColor() != turnColor) {
            throw new InvalidMoveException("Invalid move: Not your turn.");
        }
        if (!Arrays.asList(piece.pieceMoves(board, move.getStartPosition())).contains(move.getEndPosition())) {
            throw new InvalidMoveException("Invalid move: Not in available moves.");
        }
        board.addPiece(move.getEndPosition(), piece);
    }

    private ChessPosition getKingLocation(TeamColor teamColor) {
        ChessPosition kingLocation = null;
        for (int i = 1; i <= 8; i++) {
            for (int j = 1; j <= 8; j++) {
                ChessPiece piece = board.getPiece(new ChessPosition(i, j));
                if (piece != null && piece.getTeamColor() == teamColor &&
                        piece.getPieceType() == ChessPiece.PieceType.KING) {
                    kingLocation = new ChessPosition(i, j);
                }
            }
        }
        return kingLocation;
    }

    private Collection<ChessPosition> getTargetedSquares(TeamColor teamColor) {
        List<ChessMove> moves = new ArrayList<>();
        for (int i = 1; i <= 8; i++) {
            for (int j = 1; j <= 8; j++) {
                ChessPosition position = new ChessPosition(i, j);
                ChessPiece piece = board.getPiece(position);
                if (piece != null && piece.getTeamColor() == teamColor) {
                    moves.addAll(piece.pieceMoves(board, position));
                }
            }
        }
        List<ChessPosition> targetedSquares = new ArrayList<>();
        for (ChessMove move : moves) {
            targetedSquares.add(move.getEndPosition());
        }
        return targetedSquares;
    }
    
    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        ChessPosition kingLocation = getKingLocation(teamColor);
        Collection<ChessPosition> targetedSquares = getTargetedSquares(
                teamColor == TeamColor.WHITE ? TeamColor.BLACK : TeamColor.WHITE);
        return targetedSquares.contains(kingLocation);
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        ChessPosition kingLocation = getKingLocation(teamColor);
        Collection<ChessPosition> targetedSquares = getTargetedSquares(
                teamColor == TeamColor.WHITE ? TeamColor.BLACK : TeamColor.WHITE);
        if (!targetedSquares.contains(kingLocation)) {
            return false;
        }
        for (int i = 0; i < 8; i++) {
            if (1 <= kingLocation.getRow() + ydirs[i] && kingLocation.getRow() + ydirs[i] <= 8 &&
                    1 <= kingLocation.getColumn() + xdirs[i] && kingLocation.getColumn() + xdirs[i] <= 8) {
                ChessPosition position = new ChessPosition(kingLocation.getRow() + ydirs[i],
                        kingLocation.getColumn() + xdirs[i]);
                if (targetedSquares.contains(position)) {
                    continue;
                } else {
                    ChessPiece target = board.getPiece(position);
                    if (target == null || target.getTeamColor() == (teamColor == TeamColor.WHITE ? TeamColor.BLACK : TeamColor.WHITE)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.board = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return board;
    }
}
