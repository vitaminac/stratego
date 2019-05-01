package edu.asu.stratego.gui;

import edu.asu.stratego.game.Game;

import static edu.asu.stratego.gui.board.BoardSquareEventPane.randomSetup;

public class AIClientStage implements IClientStage {

    @Override
    public void setBoardScene() {
        new Thread(() -> {
            try {
                synchronized (Game.getGame().getSetupPieces()) {
                    Game.getGame().getSetupPieces().wait();
                    Game.getComputer().getSetupPieces().setup(Game.getComputer().getPlayer());
                    randomSetup(Game.getComputer());
                }
                synchronized (Game.getComputer().getSetupPieces()) {
                    Game.getComputer().getSetupPieces().notifyAll();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    @Override
    public void setWaitingScene() {
    }
}
