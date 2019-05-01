package edu.asu.stratego.game;

import java.io.Serializable;

public class SetupBoard implements Serializable {

    private static final long serialVersionUID = 1854992492401962054L;
    private Piece[][] pieces = new Piece[4][10];

    public SetupBoard() {
    }

    public SetupBoard(Game game) {
        /**
         * Store the player's initial piece positions in positions[][].
         */
        for (int row = 6; row < 10; ++row) {
            for (int col = 0; col < 10; ++col)
                pieces[row - 6][col] = game.getBoard().getSquare(row, col).getPiece();
        }
    }

    /**
     * @param row row index of positions[][]
     * @param col column index of positions[][]
     * @return the PieceType at positions[row][col]
     */
    public Piece getPiece(int row, int col) {
        return pieces[row][col];
    }

    public void setPiece(Piece piece, int row, int col) {
        pieces[row][col] = piece;
    }
}