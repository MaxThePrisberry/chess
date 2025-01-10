package chess;

import java.util.*;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {

    private ChessGame.TeamColor color;
    private PieceType type;
    private static final int[] xdirs = {-1, 1, 0, 0, -1, -1, 1, 1};
    private static final int[] ydirs = {0, 0, -1, 1, -1, 1, -1, 1};
    private static final int[][] knightMoves = {{2, -1}, {2, 1}, {-2, -1}, {-2, 1}, {-1, -2}, {-1, 2}, {1, -2}, {1, 2}};


    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType pieceType) {
        color = pieceColor;
        type = pieceType;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessPiece that = (ChessPiece) o;
        return color == that.color && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(color, type);
    }

    @Override
    public String toString() {
        return "ChessPiece{" +
                "color=" + color +
                ", type=" + type +
                '}';
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return color;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return type;
    }

    /**
     * This function is used to calculate all directions and is used currently only for the King piece, one square only.
     *
     * @param board Current board state moves are evaluated on
     * @param myPosition Current position moves are evaluated from
     * @return Collection of valid moves
     */
    private Collection<ChessMove> calcDirections(ChessBoard board, ChessPosition myPosition) {
        List<ChessMove> moves = new ArrayList<>();
        for(int i = 0; i < xdirs.length; i++) {
            if (1 <= myPosition.getRow() + ydirs[i] && myPosition.getRow() + ydirs[i] <= 8 &&
                    1 <= myPosition.getColumn() + xdirs[i] && myPosition.getColumn() + xdirs[i] <= 8) {
                ChessPiece target = board.getPiece(new ChessPosition(myPosition.getRow() + ydirs[i],
                        myPosition.getColumn() + xdirs[i]));
                if (target == null || target.getTeamColor() != color) {
                    moves.add(new ChessMove(myPosition, new ChessPosition(myPosition.getRow() + ydirs[i],
                            myPosition.getColumn() + xdirs[i]), null));
                }
            }
        }
        return moves;
    }

    /**
     * This function calculates line-of-sight valid moves as they apply to the Queen, Bishop, and Rook piece types.
     *
     * @param board Current board state moves are evaluated on
     * @param myPosition Current position moves are evaluated from
     * @param directions Which directions as declared at the class level the function should evaluate
     * @return Collection of valid moves
     */
    private Collection<ChessMove> calcDirections(ChessBoard board, ChessPosition myPosition, int[] directions) {
        List<ChessMove> moves = new ArrayList<>();
        for(int i : directions) {
            int distanceFactor = 1;
            while (1 <= myPosition.getRow() + (ydirs[i]*distanceFactor) &&
                    myPosition.getRow() + (ydirs[i]*distanceFactor) <= 8 &&
                    1 <= myPosition.getColumn() + (xdirs[i]*distanceFactor) &&
                    myPosition.getColumn() + (xdirs[i]*distanceFactor) <= 8){
                ChessPiece target = board.getPiece(new ChessPosition(myPosition.getRow() +
                        (ydirs[i]*distanceFactor), myPosition.getColumn() +
                        (xdirs[i]*distanceFactor)));
                if (target == null) {
                    moves.add(new ChessMove(myPosition, new ChessPosition(myPosition.getRow() +
                            (ydirs[i]*distanceFactor), myPosition.getColumn() +
                            (xdirs[i]*distanceFactor)), null));
                } else if (target.getTeamColor() == color){
                    break;
                } else {
                    moves.add(new ChessMove(myPosition, new ChessPosition(myPosition.getRow() +
                            (ydirs[i]*distanceFactor), myPosition.getColumn() +
                            (xdirs[i]*distanceFactor)), null));
                    break;
                }
                distanceFactor++;
            }
        }
        return moves;
    }

    /**
     * This function calculates valid moves for the Knight piece by iterating through relative a relative coordinate
     * set declared on the class level.
     *
     * @param board Current board state moves are evaluated on
     * @param myPosition Current position moves are evaluated from
     * @return Collection of valid knight moves
     */
    private Collection<ChessMove> calcKnight(ChessBoard board, ChessPosition myPosition) {
        List<ChessMove> moves = new ArrayList<>();
        for (int[] knightMove : knightMoves) {
            if (1 <= myPosition.getRow() + knightMove[0] && myPosition.getRow() + knightMove[0] <= 8 &&
                    1 <= myPosition.getColumn() + knightMove[1] && myPosition.getColumn() + knightMove[1] <= 8) {
                ChessPiece target = board.getPiece(new ChessPosition(myPosition.getRow() + knightMove[0],
                        myPosition.getColumn() + knightMove[1]));
                if (target == null || target.getTeamColor() != color) {
                    moves.add(new ChessMove(myPosition, new ChessPosition(myPosition.getRow() + knightMove[0],
                            myPosition.getColumn() + knightMove[1]), null));
                }
            }
        }
        return moves;
    }

    /**
     * This function calculates valid pawn moves that are only in the forward direction, depending on the color of the
     * piece. This function accounts for valid beginning moves that are two squares forward.
     *
     * @param board Current board state moves are evaluated on
     * @param myPosition Current position moves are evaluated from
     * @return Collection of valid default moves
     */
    private Collection<ChessMove> calcPawnDefaultMovement(ChessBoard board, ChessPosition myPosition) {
        List<ChessMove> moves = new ArrayList<>();
        int forward = color == ChessGame.TeamColor.WHITE ? 1 : -1;
        ChessPiece target = board.getPiece(new ChessPosition(myPosition.getRow() + forward,
                myPosition.getColumn()));
        if (target == null) {
            if ((color == ChessGame.TeamColor.WHITE && myPosition.getRow() == 7) ||
                    (color == ChessGame.TeamColor.BLACK && myPosition.getRow() == 2)) {
                for (PieceType pt : PieceType.values()) {
                    if (pt == PieceType.PAWN || pt == PieceType.KING) {continue;}
                    moves.add(new ChessMove(myPosition, new ChessPosition(myPosition.getRow() + forward,
                            myPosition.getColumn()), pt));
                }
            } else {
                moves.add(new ChessMove(myPosition, new ChessPosition(myPosition.getRow() + forward,
                        myPosition.getColumn()), null));
            }
            if ((color == ChessGame.TeamColor.WHITE && myPosition.getRow() == 2) ||
                    (color == ChessGame.TeamColor.BLACK && myPosition.getRow() == 7)) {
                target = board.getPiece(new ChessPosition(myPosition.getRow() + forward*2,
                        myPosition.getColumn()));
                if (target == null) {
                    moves.add(new ChessMove(myPosition, new ChessPosition(myPosition.getRow() + forward*2,
                            myPosition.getColumn()), null));
                }
            }
        }
        return moves;
    }

    /**
     * This function calculates valid pawn moves that are diagonal, or capture moves.
     *
     * @param board Current board state moves are evaluated on
     * @param myPosition Current position moves are evaluated from
     * @return Collection of valid capture moves
     */
    private Collection<ChessMove> calcPawnCapture(ChessBoard board, ChessPosition myPosition) {
        List<ChessMove> moves = new ArrayList<>();
        ChessPiece target;
        int forward = color == ChessGame.TeamColor.WHITE ? 1 : -1;
        if (myPosition.getColumn() > 1) {
            target = board.getPiece(new ChessPosition(myPosition.getRow() + forward,
                    myPosition.getColumn() - 1));
            if (target != null && target.getTeamColor() != color) {
                if ((color == ChessGame.TeamColor.WHITE && myPosition.getRow() == 7) ||
                        (color == ChessGame.TeamColor.BLACK && myPosition.getRow() == 2)) {
                    PieceType[] ptt = PieceType.values();
                    ptt = Arrays.stream(ptt).filter(piece -> piece != PieceType.PAWN &&
                            piece != PieceType.KING).toArray(PieceType[]::new);
                    for (PieceType pt : ptt) {
                        moves.add(new ChessMove(myPosition, new ChessPosition(myPosition.getRow() + forward,
                                myPosition.getColumn() - 1), pt));
                    }
                } else {
                    moves.add(new ChessMove(myPosition, new ChessPosition(myPosition.getRow() + forward,
                            myPosition.getColumn() - 1), null));
                }
            }
        }
        if (myPosition.getColumn() < 8) {
            target = board.getPiece(new ChessPosition(myPosition.getRow() + forward,
                    myPosition.getColumn() + 1));
            if (target != null && target.getTeamColor() != color) {
                if ((color == ChessGame.TeamColor.WHITE && myPosition.getRow() == 7) ||
                        (color == ChessGame.TeamColor.BLACK && myPosition.getRow() == 2)) {
                    PieceType[] ptt = PieceType.values();
                    ptt = Arrays.stream(ptt).filter(piece -> piece != PieceType.PAWN &&
                            piece != PieceType.KING).toArray(PieceType[]::new);
                    for (PieceType pt : ptt) {
                        moves.add(new ChessMove(myPosition, new ChessPosition(myPosition.getRow() + forward,
                                myPosition.getColumn() + 1), pt));
                    }
                } else {
                    moves.add(new ChessMove(myPosition, new ChessPosition(myPosition.getRow() + forward,
                            myPosition.getColumn() + 1), null));
                }
            }
        }
        return moves;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        List<ChessMove> moves = new ArrayList<>();
        switch (type) {
            case KING -> {
                moves.addAll(calcDirections(board, myPosition));
            }
            case QUEEN -> {
                moves.addAll(calcDirections(board, myPosition, new int[]{0, 1, 2, 3, 4, 5, 6, 7}));
            }
            case BISHOP -> {
                moves.addAll(calcDirections(board, myPosition, new int[]{4, 5, 6, 7}));
            }
            case ROOK -> {
                moves.addAll(calcDirections(board, myPosition, new int[]{0, 1, 2, 3}));
            }
            case KNIGHT -> {
                moves.addAll(calcKnight(board, myPosition));
            }
            case PAWN -> {
                moves.addAll(calcPawnDefaultMovement(board, myPosition));
                moves.addAll(calcPawnCapture(board, myPosition));
            }
        }
        return moves;
    }
}
