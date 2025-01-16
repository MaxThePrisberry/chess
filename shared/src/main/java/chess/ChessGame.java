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
    private static final int[] X_DIRS = {-1, 1, 0, 0, -1, -1, 1, 1};
    private static final int[] Y_DIRS = {0, 0, -1, 1, -1, 1, -1, 1};

    private boolean isOver;

    public boolean isIsOver() {
        return isOver;
    }

    public void setIsOver(boolean isOver) {
        this.isOver = isOver;
    }

    public ChessGame() {
        turnColor = TeamColor.WHITE;
        board = new ChessBoard();
        board.resetBoard();
        isOver = false;
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

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessGame chessGame = (ChessGame) o;
        return turnColor == chessGame.turnColor && Objects.equals(board, chessGame.board);
    }

    @Override
    public int hashCode() {
        return Objects.hash(turnColor, board);
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
        ChessPiece piece = board.getPiece(startPosition);
        if (piece == null) {
            return null;
        }
        Collection<ChessMove> moves = piece.pieceMoves(board, startPosition);
        List<ChessMove> vMoves = new ArrayList<>();
        for (ChessMove move : moves) {
            if (piece.getPieceType() == ChessPiece.PieceType.KING &&
                    Math.abs(move.getStartPosition().getColumn()-move.getEndPosition().getColumn()) == 2) {
                if (isInCheck(piece.getTeamColor())) {
                    continue;
                }
                ChessPosition moveThroughPosition = new ChessPosition(move.getStartPosition().getRow(),
                        move.getStartPosition().getColumn() + (move.getEndPosition().getColumn() -
                                move.getStartPosition().getColumn())/2);
                board.addPiece(moveThroughPosition, new ChessPiece(piece.getTeamColor(), ChessPiece.PieceType.KING));
                board.clearPiece(move.getStartPosition());
                if (isInCheck(piece.getTeamColor())) {
                    board.addPiece(move.getStartPosition(), new ChessPiece(piece.getTeamColor(), ChessPiece.PieceType.KING));
                    board.clearPiece(moveThroughPosition);
                    continue;
                }
                board.addPiece(move.getEndPosition(), new ChessPiece(piece.getTeamColor(), ChessPiece.PieceType.KING));
                board.clearPiece(moveThroughPosition);
                if (isInCheck(piece.getTeamColor())) {
                    board.addPiece(move.getStartPosition(), new ChessPiece(piece.getTeamColor(), ChessPiece.PieceType.KING));
                    board.clearPiece(move.getEndPosition());
                    continue;
                }
                board.addPiece(move.getStartPosition(), new ChessPiece(piece.getTeamColor(), ChessPiece.PieceType.KING));
                board.clearPiece(move.getEndPosition());
                vMoves.add(move);
            } else {
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
        }
        return vMoves;
    }

    private boolean doCastleMove(ChessMove move) {
        ChessPiece piece = board.getPiece(move.getStartPosition());
        if (move.equals(new ChessMove(new ChessPosition(1, 5), new ChessPosition(1, 3), null))) {
            board.addPiece(new ChessPosition(1, 3), new ChessPiece(TeamColor.WHITE, ChessPiece.PieceType.KING));
            board.addPiece(new ChessPosition(1, 4), new ChessPiece(TeamColor.WHITE, ChessPiece.PieceType.ROOK));
            board.clearPiece(new ChessPosition(1, 1));
            board.clearPiece(new ChessPosition(1, 5));
        } else if (move.equals(new ChessMove(new ChessPosition(1, 5), new ChessPosition(1, 7), null))) {
            board.addPiece(new ChessPosition(1, 7), new ChessPiece(TeamColor.WHITE, ChessPiece.PieceType.KING));
            board.addPiece(new ChessPosition(1, 6), new ChessPiece(TeamColor.WHITE, ChessPiece.PieceType.ROOK));
            board.clearPiece(new ChessPosition(1, 8));
            board.clearPiece(new ChessPosition(1, 5));
        } else if (move.equals(new ChessMove(new ChessPosition(8, 5), new ChessPosition(8, 3), null))) {
            board.addPiece(new ChessPosition(8, 3), new ChessPiece(TeamColor.BLACK, ChessPiece.PieceType.KING));
            board.addPiece(new ChessPosition(8, 4), new ChessPiece(TeamColor.BLACK, ChessPiece.PieceType.ROOK));
            board.clearPiece(new ChessPosition(8, 1));
            board.clearPiece(new ChessPosition(8, 5));
        } else if (move.equals(new ChessMove(new ChessPosition(8, 5), new ChessPosition(8, 7), null))) {
            board.addPiece(new ChessPosition(8, 7), new ChessPiece(TeamColor.BLACK, ChessPiece.PieceType.KING));
            board.addPiece(new ChessPosition(8, 6), new ChessPiece(TeamColor.BLACK, ChessPiece.PieceType.ROOK));
            board.clearPiece(new ChessPosition(8, 8));
            board.clearPiece(new ChessPosition(8, 5));
        }
        if (isInCheck(piece.getTeamColor())) {
            if (move.equals(new ChessMove(new ChessPosition(1, 5), new ChessPosition(1, 3), null))) {
                board.addPiece(new ChessPosition(1, 5), new ChessPiece(TeamColor.WHITE, ChessPiece.PieceType.KING));
                board.addPiece(new ChessPosition(1, 1), new ChessPiece(TeamColor.WHITE, ChessPiece.PieceType.ROOK));
                board.clearPiece(new ChessPosition(1, 3));
                board.clearPiece(new ChessPosition(1, 4));
            } else if (move.equals(new ChessMove(new ChessPosition(1, 5), new ChessPosition(1, 7), null))) {
                board.addPiece(new ChessPosition(1, 5), new ChessPiece(TeamColor.WHITE, ChessPiece.PieceType.KING));
                board.addPiece(new ChessPosition(1, 8), new ChessPiece(TeamColor.WHITE, ChessPiece.PieceType.ROOK));
                board.clearPiece(new ChessPosition(1, 7));
                board.clearPiece(new ChessPosition(1, 6));
            } else if (move.equals(new ChessMove(new ChessPosition(8, 5), new ChessPosition(8, 3), null))) {
                board.addPiece(new ChessPosition(8, 5), new ChessPiece(TeamColor.BLACK, ChessPiece.PieceType.KING));
                board.addPiece(new ChessPosition(8, 1), new ChessPiece(TeamColor.BLACK, ChessPiece.PieceType.ROOK));
                board.clearPiece(new ChessPosition(8, 3));
                board.clearPiece(new ChessPosition(8, 4));
            } else if (move.equals(new ChessMove(new ChessPosition(8, 5), new ChessPosition(8, 7), null))) {
                board.addPiece(new ChessPosition(8, 5), new ChessPiece(TeamColor.BLACK, ChessPiece.PieceType.KING));
                board.addPiece(new ChessPosition(8, 8), new ChessPiece(TeamColor.BLACK, ChessPiece.PieceType.ROOK));
                board.clearPiece(new ChessPosition(8, 7));
                board.clearPiece(new ChessPosition(8, 6));
            }
            return false;
        }
        return true;
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
                    break;
                }
            }
            if (!validMove) {
                throw new InvalidMoveException("Invalid move: Not in available moves.");
            }
        }
        if (piece.getPieceType() == ChessPiece.PieceType.KING &&
                Math.abs(move.getStartPosition().getColumn()-move.getEndPosition().getColumn()) == 2) {
            boolean success = doCastleMove(move);
            if (!success) {
                throw new InvalidMoveException("Invalid move: this move results in check.");
            }
        } else {
            ChessMove lastMove = board.getLastMove();
            if (piece.getPieceType() == ChessPiece.PieceType.PAWN && piece.getTeamColor() == TeamColor.WHITE &&
                    move.getStartPosition().getRow() == 5 && lastMove.getEndPosition().getRow() == 5 &&
                    board.getPiece(lastMove.getEndPosition()).getPieceType() == ChessPiece.PieceType.PAWN &&
                    Math.abs(lastMove.getEndPosition().getRow()-lastMove.getStartPosition().getRow()) == 2 &&
                    board.getPiece(lastMove.getEndPosition()).getTeamColor() != piece.getTeamColor() &&
                    Math.abs(lastMove.getEndPosition().getColumn()-move.getStartPosition().getColumn()) == 1) {
                board.addPiece(move.getEndPosition(), piece);
                board.clearPiece(move.getStartPosition());
                board.clearPiece(lastMove.getEndPosition());
            } else if (piece.getPieceType() == ChessPiece.PieceType.PAWN && piece.getTeamColor() == TeamColor.BLACK &&
                    move.getStartPosition().getRow() == 4 && lastMove.getEndPosition().getRow() == 4 &&
                    board.getPiece(lastMove.getEndPosition()).getPieceType() == ChessPiece.PieceType.PAWN &&
                    Math.abs(lastMove.getEndPosition().getRow()-lastMove.getStartPosition().getRow()) == 2 &&
                    board.getPiece(lastMove.getEndPosition()).getTeamColor() != piece.getTeamColor() &&
                    Math.abs(lastMove.getEndPosition().getColumn()-move.getStartPosition().getColumn()) == 1) {
                board.addPiece(move.getEndPosition(), piece);
                board.clearPiece(move.getStartPosition());
                board.clearPiece(lastMove.getEndPosition());
            } else {
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
            }
        }
        board.logMove(move);
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
                if (piece != null && piece.getTeamColor() == teamColor &&
                        piece.pieceTargets(board, position).contains(targetPosition)) {
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
            if (1 <= kingLocation.getRow() + Y_DIRS[i] && kingLocation.getRow() + Y_DIRS[i] <= 8 &&
                    1 <= kingLocation.getColumn() + X_DIRS[i] && kingLocation.getColumn() + X_DIRS[i] <= 8) {
                ChessPosition position = new ChessPosition(kingLocation.getRow() + Y_DIRS[i],
                        kingLocation.getColumn() + X_DIRS[i]);
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
