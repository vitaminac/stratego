package edu.asu.stratego.gui.board;

import edu.asu.stratego.game.*;
import edu.asu.stratego.game.board.ClientSquare;
import edu.asu.stratego.gui.board.setup.SetupPanel;
import edu.asu.stratego.media.ImageConstants;
import edu.asu.stratego.util.HashTables;
import javafx.event.EventHandler;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;

import java.util.ArrayList;
import java.util.Arrays;

import static edu.asu.stratego.gui.board.SelectSquare.isHoverValid;

/**
 * A single square within the BoardEventPane.
 */
public class BoardSquareEventPane extends GridPane {
    private ImageView hover;

    /**
     * Creates a new instance of BoardSquareEventPane.
     */
    public BoardSquareEventPane() {
        hover = new ImageView(ImageConstants.HIGHLIGHT_NONE);

        // Event handlers for the square.
        hover.addEventHandler(MouseEvent.MOUSE_ENTERED_TARGET, new OnHover());
        hover.addEventHandler(MouseEvent.MOUSE_EXITED_TARGET, new OffHover());
        hover.addEventHandler(MouseEvent.MOUSE_CLICKED, new SelectSquare());

        this.getChildren().add(hover);
    }

    /**
     * This event is triggered when the player's cursor is hovering over the
     * BoardSquareEventPane. It changes the hover image to indicate to the user
     * whether or not a square is valid.
     */
    private class OnHover implements EventHandler<MouseEvent> {
        @Override
        public void handle(MouseEvent e) {
            ImageView hover = (ImageView) e.getSource();
            int row = GridPane.getRowIndex(hover);
            int col = GridPane.getColumnIndex(hover);

            // Setting up
            if (Game.getGame().getStatus() == GameStatus.SETTING_UP) {
                checkMove(row, col, hover, Game.getGame());
            }
            // Waiting for opponent
            else if (Game.getGame().getStatus() == GameStatus.WAITING_OPP) {
                invalidMove(hover);
            }
            // In progress
            else if (Game.getGame().getStatus() == GameStatus.IN_PROGRESS) {
                if (Game.getGame().getMoveStatus() == MoveStatus.OPP_TURN)
                    invalidMove(hover);
                else if (Game.getGame().getMoveStatus() == MoveStatus.NONE_SELECTED)
                    checkMove(row, col, hover, Game.getGame());
                else if (Game.getGame().getMoveStatus() == MoveStatus.START_SELECTED) {
                    // <moved to be handled elsewhere>
                }
            }
        }
    }

    /**
     * This event is fired when the cursor leaves the square. It changes the
     * hover image back to its default image: a blank image with a 1% fill.
     */
    private class OffHover implements EventHandler<MouseEvent> {
        @Override
        public void handle(MouseEvent e) {
            ImageView hover = (ImageView) e.getSource();
            // Change the behavior of the off hover based on the game/move status
            if (Game.getGame().getStatus() == GameStatus.SETTING_UP)
                hover.setImage(ImageConstants.HIGHLIGHT_NONE);
            else if (Game.getGame().getStatus() == GameStatus.WAITING_OPP)
                hover.setImage(ImageConstants.HIGHLIGHT_NONE);
            else if (Game.getGame().getStatus() == GameStatus.IN_PROGRESS) {
                if (Game.getGame().getMoveStatus() == MoveStatus.OPP_TURN)
                    hover.setImage(ImageConstants.HIGHLIGHT_NONE);
                else if (Game.getGame().getMoveStatus() == MoveStatus.NONE_SELECTED)
                    hover.setImage(ImageConstants.HIGHLIGHT_NONE);
                else if (Game.getGame().getMoveStatus() == MoveStatus.START_SELECTED) {
                    // Moved elsewhere: Function to only allow highlighting of squares piece can move to
                }
            }
        }
    }

    // Set the image to a red highlight indicating an invalid move
    private void invalidMove(ImageView inImage) {
        inImage.setImage(ImageConstants.HIGHLIGHT_INVALID);
    }

    // Set the image to a green highlight indicating a valid move
    private void validMove(ImageView inImage) {
        inImage.setImage(ImageConstants.HIGHLIGHT_VALID);
    }

    // Check if the move is valid and set the hover accordingly
    private void checkMove(int row, int col, ImageView inImage, Game game) {
        if (isHoverValid(row, col, game))
            validMove(hover);
        else
            invalidMove(hover);
    }

    /**
     * During the Setup phase of the game, this method randomly places the
     * pieces that have not yet been placed when the Setup Timer hits 0.
     */
    public static void randomSetup(Game game) {
        PieceColor playerColor = game.getPlayer().getColor();

        // Iterate through each square
        for (int col = 0; col < 10; ++col) {
            for (int row = 6; row < 10; ++row) {
                BoardSquarePane squarePane = game.getBoard().getSquare(row, col).getPiecePane();
                ClientSquare square = game.getBoard().getSquare(row, col);
                Piece squarePiece = square.getPiece();

                // Create an arraylist of all the available values
                ArrayList<PieceType> availTypes =
                        new ArrayList<>(Arrays.asList(PieceType.values()));

                // If the square is null (will not overwrite existing pieces)
                if (squarePiece == null) {
                    PieceType pieceType = null;

                    // While the pieceType that is going to be placed is null, loop finding a random one
                    // checking that its count is > 0
                    while (pieceType == null) {
                        int randInt = (int) (Math.random() * availTypes.size());
                        if (game.getSetupPieces().getPieceCount(availTypes.get(randInt)) > 0)
                            pieceType = availTypes.get(randInt);
                            // There are no more available for that piecetype, remove it from the array so it won't be randomly generated again
                        else
                            availTypes.remove(randInt);
                    }

                    // Set the square to the piecetype once a suitable piecetype has been found
                    square.setPiece(new Piece(pieceType, playerColor, false));
                    squarePane.setPiece(HashTables.PIECE_MAP.get(square.getPiece().getPieceSpriteKey()));

                    // And lower the availability count of that piece
                    game.getSetupPieces().decrementPieceCount(pieceType);
                    SetupPanel.getSetupPanel().getChildren().remove(SetupPanel.getSaveimportPane());
                }
            }
        }
    }

    /**
     * @return the ImageView object that displays the hover image.
     */
    public ImageView getHover() {
        return hover;
    }
}