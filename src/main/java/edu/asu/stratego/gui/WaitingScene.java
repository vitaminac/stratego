package edu.asu.stratego.gui;

import edu.asu.stratego.game.ClientGameManager;
import edu.asu.stratego.game.Game;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.io.IOException;

/**
 * Wrapper class for a JavaFX scene. Contains a scene UI to indicate that the 
 * client has successfully connected to a server and is currently waiting for 
 * another opponent to connect to the server. The intended function for this 
 * scene is analogous to a loading screen.
 */
public class WaitingScene {
    
    private static final int WINDOW_WIDTH  = 300;
    private static final int WINDOW_HEIGHT = 150;
    
    Scene scene;
    private static Button aiButton = new Button();
    
    /**
     * Creates a new instance of WaitingScene.
     */
    public WaitingScene() {
        aiButton.setPrefSize(WINDOW_WIDTH * 0.5, WINDOW_HEIGHT * 0.1);
        aiButton.setText("Play vs Computer");
        
        // Create UI.
        VBox pane = new VBox();
        pane.getChildren().add(new Label("Waiting for an opponent..."));
        pane.getChildren().add(aiButton);
        pane.setAlignment(Pos.CENTER);
        aiButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                try {
                    Game.getComputer().connect();
                    Thread manager = new Thread(new ClientGameManager(new AIClientStage(), Game.getComputer()));
                    manager.setDaemon(true);
                    manager.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        scene = new Scene(pane, WINDOW_WIDTH, WINDOW_HEIGHT);
        scene.setFill(Color.LIGHTGRAY);
    }
}