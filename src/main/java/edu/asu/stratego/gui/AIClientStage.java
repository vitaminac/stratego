package edu.asu.stratego.gui;

import edu.asu.stratego.game.*;
import edu.asu.stratego.gui.board.SelectSquare;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

import static edu.asu.stratego.gui.board.BoardSquareEventPane.randomSetup;

public class AIClientStage implements IClientStage {
    protected Move chooseBestStep(List<Move> possibleMoves, Piece[][] boards) {
        Collections.shuffle(possibleMoves);
        return possibleMoves.get(0);
    }

    private void step(Game computer) {
        PieceColor color = computer.getPlayer().getColor();
        if (Game.getGame().getStatus() == GameStatus.IN_PROGRESS && computer.getTurn() == computer.getPlayer().getColor()) {
            ArrayList<Move> moves = new ArrayList<>();
            Piece[][] board = new Piece[10][10];
            for (int col = 0; col < 10; ++col) {
                for (int row = 0; row < 10; ++row) {
                    board[row][col] = computer.getBoard().getSquare(row, col).getPiece();
                    if (!SelectSquare.isNullPiece(row, col, computer)
                            && computer.getBoard().getSquare(row, col).getPiece().getPieceColor() == color
                            && SelectSquare.isHoverValid(row, col, computer)) {
                        for (Point point : SelectSquare.computeValidMoves(row, col, computer)) {
                            moves.add(new Move(new Point(row, col), point));
                        }
                    }
                }
            }

            computer.setMove(this.chooseBestStep(moves, board));
            computer.getMove().setMoveColor(color);
            computer.setMoveStatus(MoveStatus.END_SELECTED);
            synchronized (computer.getSendMove()) {
                computer.getSendMove().notify();
            }
        }
    }

    @Override
    public void setBoardScene() {
        new Thread(() -> {
            Game player = Game.getGame();
            Game computer = Game.getComputer();
            try {
                synchronized (player.getSetupPieces()) {
                    player.getSetupPieces().wait();
                    computer.getSetupPieces().setup(computer.getPlayer());
                    randomSetup(computer);
                }
                synchronized (computer.getSetupPieces()) {
                    computer.getSetupPieces().notifyAll();
                }
                do {
                    synchronized (computer.getTurnIndicatorTrigger()) {
                        computer.getTurnIndicatorTrigger().wait();
                    }
                    this.step(computer);
                }
                while (computer.getStatus() == GameStatus.IN_PROGRESS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();

    }

    @Override
    public void setWaitingScene() {
    }
}
