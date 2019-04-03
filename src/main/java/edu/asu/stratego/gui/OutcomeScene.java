package main.java.edu.asu.stratego.gui;

import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

/**
 * Wrapper class for a JavaFX scene. Contains a scene UI to indicate the
 * outcome of the game.
 * TODO: Add a button with the option to play again
 */

public class OutcomeScene {

    private final int WINDOW_WIDTH  = 400;
    private final int WINDOW_HEIGHT = 200;

    Scene scene;

    /**
     * Creates a new instance of WaitingScene.
     */
    public OutcomeScene() {
        // Create UI.
        StackPane pane = new StackPane();
        pane.getChildren().add(new Label("The game has ended.\nPlay again?"));

        scene = new Scene(pane, WINDOW_WIDTH, WINDOW_HEIGHT);
        scene.setFill(Color.LIGHTGRAY);
    }
}