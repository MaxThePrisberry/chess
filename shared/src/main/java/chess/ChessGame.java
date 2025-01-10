package chess;

import java.util.*;

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

    private List<ChessMove> history;

    public ChessGame() {
        turnColor = TeamColor.WHITE;
        board = new ChessBoard();
        history = new ArrayList<>();
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

    private boolean positionHasMoved(ChessPosition position) {
        for (ChessMove move : history) {
            if (move.getStartPosition().equals(position)) {
                return true;
            }
        }
        return false;
    }

    private Collection<ChessMove> getCastlingMoves(ChessPosition startPosition) {
        Collection<ChessMove> moves = new ArrayList<>();
        ChessPosition whiteKing = new ChessPosition(1, 5);
        ChessPosition blackKing = new ChessPosition(8, 5);
        if (startPosition.equals(whiteKing)) {
            if (!positionHasMoved(whiteKing)) {
                if (!positionHasMoved(new ChessPosition(1, 1))) {
                    boolean validCastle = true;
                    for (int i = 2; i <= 4; i++) {
                        if (board.getPiece(new ChessPosition(1, i)) != null) {
                            validCastle = false;
                        }
                    }
                    if (validCastle) {
                        moves.add(new ChessMove(whiteKing, new ChessPosition(1, 3), null));
                    }
                }
                if (!positionHasMoved(new ChessPosition(1, 8))) {
                    boolean validCastle = true;
                    for (int i = 6; i <= 7; i++) {
                        if (board.getPiece(new ChessPosition(1, i)) != null) {
                            validCastle = false;
                        }
                    }
                    if (validCastle) {
                        moves.add(new ChessMove(whiteKing, new ChessPosition(1, 7), null));
                    }
                }
            }
        } else if (startPosition.equals(blackKing)) {
            if (!positionHasMoved(blackKing)) {
                if (!positionHasMoved(new ChessPosition(8, 1))) {
                    boolean validCastle = true;
                    for (int i = 2; i <= 4; i++) {
                        if (board.getPiece(new ChessPosition(8, i)) != null) {
                            validCastle = false;
                        }
                    }
                    if (validCastle) {
                        moves.add(new ChessMove(whiteKing, new ChessPosition(8, 3), null));
                    }
                }
                if (!positionHasMoved(new ChessPosition(8, 8))) {
                    boolean validCastle = true;
                    for (int i = 6; i <= 7; i++) {
                        if (board.getPiece(new ChessPosition(8, i)) != null) {
                            validCastle = false;
                        }
                    }
                    if (validCastle) {
                        moves.add(new ChessMove(whiteKing, new ChessPosition(8, 7), null));
                    }
                }
            }
        }
        return moves;
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessPiece piece = board.getPiece(startPosition);
        if (piece == null) {
            return null;
        }
        Collection<ChessMove> moves = piece.pieceMoves(board, startPosition);
        List<ChessMove> vMoves = new ArrayList<>();
        for (ChessMove move : moves) {
            boolean validMove = true;
            ChessPiece tmp = board.getPiece(move.getEndPosition());
            board.addPiece(move.getEndPosition(), piece);
            board.clearPiece(move.getStartPosition());
            if (isInCheck(piece.getTeamColor())) {
                validMove = false;
            }
            board.addPiece(move.getStartPosition(), piece);
            board.addPiece(move.getEndPosition(), tmp);
            if (validMove) {
                vMoves.add(move);
            }
        }
        return vMoves;
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to preform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessPiece piece = board.getPiece(move.getStartPosition());
        if (piece == null) {
            throw new InvalidMoveException("Invalid move: No piece exists at location.");
        } else if (piece.getTeamColor() != turnColor) {
            throw new InvalidMoveException("Invalid move: Not your turn.");
        } else {
            Collection<ChessMove> moves = piece.pieceMoves(board, move.getStartPosition());
            boolean validMove = false;
            for (ChessMove possibleMove : moves) {
                if (possibleMove.equals(move)) {
                    validMove = true;
                }
            }
            if (!validMove) {
                throw new InvalidMoveException("Invalid move: Not in available moves.");
            }
        }
        ChessPiece tmp = board.getPiece(move.getEndPosition());
        if (move.getPromotionPiece() != null) {
            board.addPiece(move.getEndPosition(), new ChessPiece(piece.getTeamColor(), move.getPromotionPiece()));
        } else {
            board.addPiece(move.getEndPosition(), piece);
        }
        board.clearPiece(move.getStartPosition());
        if (isInCheck(piece.getTeamColor())) {
            board.addPiece(move.getStartPosition(), piece);
            board.addPiece(move.getEndPosition(), tmp);
            throw new InvalidMoveException("Invalid move: This move results in check.");
        }
        history.add(move);
        turnColor = turnColor == TeamColor.WHITE ? TeamColor.BLACK : TeamColor.WHITE;
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

    private Collection<ChessMove> getAllMoves(TeamColor teamColor) {
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
        return moves;
    }

    private Collection<ChessPosition> getTargetedSquares(TeamColor teamColor) {
        Set<ChessPosition> targetedSquares = new HashSet<>();
        for (int i = 1; i <= 8; i++) {
            for (int j = 1; j <= 8; j++) {
                ChessPosition position = new ChessPosition(i, j);
                ChessPiece piece = board.getPiece(position);
                if (piece != null && piece.getTeamColor() == teamColor) {
                    targetedSquares.addAll(piece.pieceTargets(board, position));
                }
            }
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

    private boolean possibleSacrifice(ChessPosition targetPosition, TeamColor teamColor) {
        for (int i = 1; i <= 8; i++) {
            for (int j = 1; j <= 8; j++) {
                ChessPosition position = new ChessPosition(i, j);
                ChessPiece piece = board.getPiece(position);
                if (piece != null && piece.getTeamColor() == teamColor) {
                    if (piece.pieceTargets(board, position).contains(targetPosition)) {
                        ChessPiece tmp = board.getPiece(targetPosition);
                        board.addPiece(targetPosition, piece);
                        board.clearPiece(position);
                        if (!isInCheck(teamColor)) {
                            return true;
                        }
                        board.addPiece(position, piece);
                        board.addPiece(targetPosition, tmp);
                    }
                }
            }
        }
        return false;
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
                ChessPiece target = board.getPiece(position);
                if (!targetedSquares.contains(position)) {
                    target = board.getPiece(position);
                    if (target == null) {
                        return false;
                    }
                } else {
                    if (target != null &&
                            target.getTeamColor() == (teamColor == TeamColor.WHITE ? TeamColor.BLACK : TeamColor.WHITE)
                            && possibleSacrifice(position, teamColor)) {
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
        Collection<ChessMove> availableMoves = getAllMoves(teamColor);
        availableMoves.removeIf(chessMove -> chessMove.getStartPosition().equals(getKingLocation(teamColor)));
        return availableMoves.isEmpty() && !isInCheck(teamColor) && validMoves(getKingLocation(teamColor)).isEmpty();
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
