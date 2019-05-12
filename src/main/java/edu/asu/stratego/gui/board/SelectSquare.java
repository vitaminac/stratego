package edu.asu.stratego.gui.board;

import edu.asu.stratego.game.*;
import edu.asu.stratego.game.board.ClientSquare;
import edu.asu.stratego.media.ImageConstants;
import edu.asu.stratego.util.HashTables;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.effect.Glow;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * This event is fired when the player clicks on the event square.
 */
public class SelectSquare implements EventHandler<MouseEvent> {
    @Override
    public void handle(MouseEvent e) {
        // Square position.
        ImageView source = (ImageView) e.getSource();

        int row = GridPane.getRowIndex((Node) source);
        int col = GridPane.getColumnIndex((Node) source);

        // The square and the piece at this position.
        BoardSquarePane squarePane = Game.getGame().getBoard()
                .getSquare(row, col)
                .getPiecePane();

        ClientSquare square = Game.getGame().getBoard().getSquare(row, col);
        Piece squarePiece = square.getPiece();

        // Player color.
        PieceColor playerColor = Game.getGame().getPlayer().getColor();

        /* Game Setup Handler */
        if (Game.getGame().getStatus() == GameStatus.SETTING_UP && isHoverValid(row, col, Game.getGame())) {

            // Get the selected piece (piece type and count) from the SetupPanel.
            PieceType selectedPiece = Game.getGame().getSetupPieces().getSelectedPieceType();
            int selectedPieceCount = 0;
            if (selectedPiece != null)
                selectedPieceCount = Game.getGame().getSetupPieces().getPieceCount(selectedPiece);

            // If the square contains a piece...
            if (squarePiece != null) {

                // Remove the existing piece if it is the same piece on board as the
                // selected piece (in SetupPanel) or if no piece is selected (in SetupPanel).
                if (squarePiece.getPieceType() == selectedPiece || selectedPiece == null) {
                    if (squarePiece.getPieceType() != null)
                        Game.getGame().getSetupPieces().incrementPieceCount(squarePiece.getPieceType());
                    squarePane.setPiece(null);
                    square.setPiece(null);
                }

                // Replace the existing piece with the selected piece (in SetupPanel).
                else if (squarePiece.getPieceType() != selectedPiece && selectedPieceCount > 0) {
                    Game.getGame().getSetupPieces().decrementPieceCount(selectedPiece);
                    Game.getGame().getSetupPieces().incrementPieceCount(squarePiece.getPieceType());
                    square.setPiece(new Piece(selectedPiece, playerColor, false));
                    squarePane.setPiece(HashTables.PIECE_MAP.get(square.getPiece().getPieceSpriteKey()));
                }
            }

            // Otherwise, if the square does not contain a piece...
            else {
                // Place a new piece on the square.
                if (selectedPiece != null && selectedPieceCount > 0) {
                    square.setPiece(new Piece(selectedPiece, playerColor, false));
                    squarePane.setPiece(HashTables.PIECE_MAP.get(square.getPiece().getPieceSpriteKey()));
                    Game.getGame().getSetupPieces().decrementPieceCount(selectedPiece);
                }
            }
        } else if (Game.getGame().getStatus() == GameStatus.IN_PROGRESS && Game.getGame().getTurn() == playerColor) {
            // If it is the first piece being selected to move (start)
            if (Game.getGame().getMoveStatus() == MoveStatus.NONE_SELECTED && isHoverValid(row, col, Game.getGame())) {
                Game.getGame().getMove().setStart(row, col);

                // Backup opacity check to fix rare race condition
                Game.getGame().getBoard().getSquare(row, col).getPiecePane().getPiece().setOpacity(1.0);

                // Update the movestatus to reflect a start has been selected
                Game.getGame().setMoveStatus(MoveStatus.START_SELECTED);

                // Calculate and display the valid moves upon selecting the piece
                displayValidMoves(row, col, computeValidMoves(row, col, Game.getGame()));
            }
            // If a start piece has already been selected, but user is changing start piece
            else if (Game.getGame().getMoveStatus() == MoveStatus.START_SELECTED && !isNullPiece(row, col, Game.getGame())) {
                Piece highlightPiece = Game.getGame().getBoard().getSquare(row, col).getPiece();
                if (highlightPiece.getPieceColor() == playerColor) {
                    Game.getGame().getMove().setStart(row, col);

                    // Backup opacity check to fix rare race condition
                    Game.getGame().getBoard().getSquare(row, col).getPiecePane().getPiece().setOpacity(1.0);

                    // Calculate and display the valid moves upon selecting the piece
                    displayValidMoves(row, col, computeValidMoves(row, col, Game.getGame()));
                }
            }
            if (Game.getGame().getMoveStatus() == MoveStatus.START_SELECTED && isValidMove(row, col, computeValidMoves((int) Game.getGame().getMove().getStart().getX(), (int) Game.getGame().getMove().getStart().getY(), Game.getGame()))) {
                // Remove the hover off all pieces
                for (int rowClear = 0; rowClear < 10; ++rowClear) {
                    for (int colClear = 0; colClear < 10; ++colClear) {
                        Game.getGame().getBoard().getSquare(rowClear, colClear).getEventPane().getHover().setImage(ImageConstants.HIGHLIGHT_NONE);
                        Game.getGame().getBoard().getSquare(rowClear, colClear).getEventPane().getHover().setOpacity(1.0);
                    }
                }

                // Set the end location and color in the move
                Game.getGame().getMove().setEnd(row, col);
                Game.getGame().getMove().setMoveColor(Game.getGame().getPlayer().getColor());

                // Change the movestatus to reflect that the end point has been selected
                Game.getGame().setMoveStatus(MoveStatus.END_SELECTED);

                synchronized (Game.getGame().getSendMove()) {
                    Game.getGame().getSendMove().notify();
                }
            }
        }
    }

    private void displayValidMoves(int pieceRow, int pieceCol, List<Point> validMoves) {
        // Iterate through and unhighlight/unglow all squares/pieces
        for (int row = 0; row < 10; ++row) {
            for (int col = 0; col < 10; ++col) {
                Game.getGame().getBoard().getSquare(row, col).getEventPane().getHover().setImage(ImageConstants.HIGHLIGHT_NONE);
                Game.getGame().getBoard().getSquare(row, col).getEventPane().getHover().setOpacity(1.0);
                Game.getGame().getBoard().getSquare(row, col).getPiecePane().getPiece().setEffect(new Glow(0.0));
            }
        }

        // Glow and set a white highlight around the selected piece
        Game.getGame().getBoard().getSquare(pieceRow, pieceCol).getPiecePane().getPiece().setEffect(new Glow(0.75));
        Game.getGame().getBoard().getSquare(pieceRow, pieceCol).getEventPane().getHover().setImage(ImageConstants.HIGHLIGHT_WHITE);

        // Iterate through all valid moves and highlight respective squares
        for (Point validMove : validMoves) {
            Game.getGame().getBoard().getSquare((int) validMove.getX(), (int) validMove.getY()).getEventPane().getHover().setImage(ImageConstants.HIGHLIGHT_VALID);
            Game.getGame().getBoard().getSquare((int) validMove.getX(), (int) validMove.getY()).getEventPane().getHover().setOpacity(0.5);
        }
    }

    // Returns false if the given square is outside of the board
    private static boolean isInBounds(int row, int col) {
        if (row < 0 || row > 9)
            return false;
        if (col < 0 || col > 9)
            return false;

        return true;
    }

    // Returns true if the piece is the opponent (from the client's perspective)
    private static boolean isOpponentPiece(int row, int col, Game game) {
        return game.getBoard().getSquare(row, col).getPiece().getPieceColor() != game.getPlayer().getColor();
    }

    // Returns true if the piece is null
    public static boolean isNullPiece(int row, int col, Game game) {
        return game.getBoard().getSquare(row, col).getPiece() == null;
    }


    public static ArrayList<Point> computeValidMoves(int row, int col, Game game) {
        // Set the max distance of a valid move to 1
        int max = 1;

        // Set the max distance of a valid move to the board width if the piece is a scout
        PieceType pieceType = game.getBoard().getSquare(row, col).getPiece().getPieceType();
        if (pieceType == PieceType.SCOUT)
            max = 8;

        ArrayList<Point> validMoves = new ArrayList<>();

        // Iterate through each direction and add valid moves based on if:
        // 1) The square is in bounds (inside the board)
        // 2) If the square is not a lake
        // 3) If the square has no piece on it OR there is a piece, but it is an opponent piece

        if (pieceType != PieceType.BOMB && pieceType != PieceType.FLAG) {
            // Negative Row (UP)
            for (int i = -1; i >= -max; --i) {
                if (isInBounds(row + i, col) && (!isLake(row + i, col) || (!isNullPiece(row + i, col, game) && !isOpponentPiece(row + i, col, game)))) {
                    if (isNullPiece(row + i, col, game) || isOpponentPiece(row + i, col, game)) {
                        validMoves.add(new Point(row + i, col));

                        if (!isNullPiece(row + i, col, game) && isOpponentPiece(row + i, col, game))
                            break;
                    } else
                        break;
                } else
                    break;
            }
            // Positive Col (RIGHT)
            for (int i = 1; i <= max; ++i) {
                if (isInBounds(row, col + i) && (!isLake(row, col + i) || (!isNullPiece(row, col + i, game) && !isOpponentPiece(row, col + i, game)))) {
                    if (isNullPiece(row, col + i, game) || isOpponentPiece(row, col + i, game)) {
                        validMoves.add(new Point(row, col + i));

                        if (!isNullPiece(row, col + i, game) && isOpponentPiece(row, col + i, game))
                            break;
                    } else
                        break;
                } else
                    break;
            }
            // Positive Row (DOWN)
            for (int i = 1; i <= max; ++i) {
                if (isInBounds(row + i, col) && (!isLake(row + i, col) || (!isNullPiece(row + i, col, game) && !isOpponentPiece(row + i, col, game)))) {
                    if (isNullPiece(row + i, col, game) || isOpponentPiece(row + i, col, game)) {
                        validMoves.add(new Point(row + i, col));

                        if (!isNullPiece(row + i, col, game) && isOpponentPiece(row + i, col, game))
                            break;
                    } else
                        break;
                } else
                    break;
            }
            // Negative Col (LEFT)
            for (int i = -1; i >= -max; --i) {
                if (isInBounds(row, col + i) && (!isLake(row, col + i) || (!isNullPiece(row, col + i, game) && !isOpponentPiece(row, col + i, game)))) {
                    if (isNullPiece(row, col + i, game) || isOpponentPiece(row, col + i, game)) {
                        validMoves.add(new Point(row, col + i));

                        if (!isNullPiece(row, col + i, game) && isOpponentPiece(row, col + i, game))
                            break;
                    } else
                        break;
                } else
                    break;
            }
        }

        return validMoves;
    }

    public static boolean isValidMove(int row, int col, List<Point> validMoves) {
        // Iterate through validMoves arraylist and check if a square is a valid move (after computing valid moves)
        if (validMoves != null && !validMoves.isEmpty()) {
            for (Point validMove : validMoves) {
                if (row == validMove.getX() && col == validMove.getY())
                    return true;
            }
        }
        return false;
    }

    // Returns true if the given square is a lake
    private static boolean isLake(int row, int col) {
        if ((col == 2 || col == 3 || col == 6 || col == 7) && (row == 4 || row == 5)) {
            return true;
        }
        return false;
    }

    /**
     * Indicates whether or not a square is a valid square to click.
     *
     * @param row row index of the square
     * @param col column index of the square
     * @return true if the square is valid, false otherwise
     */
    public static boolean isHoverValid(int row, int col, Game game) {
        PieceColor playerColor = game.getPlayer().getColor();

        /* Initially assumes that the square is valid. */

        // Lakes are always invalid.
        if (isLake(row, col))
            return false;

        // The game is setting up and the square is outside of the setup area.
        if (game.getStatus() == GameStatus.SETTING_UP && row <= 5)
            return false;

            // The player has finished setting up and is waiting for the opponent.
        else if (game.getStatus() == GameStatus.WAITING_OPP)
            return false;

        else if (game.getStatus() == GameStatus.IN_PROGRESS) {
            if (game.getMoveStatus() == MoveStatus.OPP_TURN)
                return false;

            if (game.getMoveStatus() == MoveStatus.NONE_SELECTED) {
                if (game.getBoard().getSquare(row, col).getPiece() != null) {
                    Piece highlightPiece = game.getBoard().getSquare(row, col).getPiece();

                    if (highlightPiece.getPieceColor() != playerColor)
                        return false;
                } else
                    return false;
            }
        }

        return true;
    }
}