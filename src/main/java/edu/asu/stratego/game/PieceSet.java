package edu.asu.stratego.game;

import java.util.ArrayList;

/**
 * 
 */
public class PieceSet {
    private ArrayList<Piece> pieces = new ArrayList<>();
    
    public PieceSet(PieceColor color) {
        PieceColor player = Game.getGame().getPlayer().getColor();
        boolean isOpponentPiece = (player != color);
        
        for (PieceType type : PieceType.values()) {
            for (int i = 0; i < type.getCount(); ++i) {
                pieces.add(new Piece(type, color, isOpponentPiece));
            }
        }
    }
}