package edu.asu.stratego.gui;

import edu.asu.stratego.game.ai.AIGameManager;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

/**
 * Wrapper class for a JavaFX scene. Contains a scene UI to indicate that the 
 * client has successfully connected to a server and is currently waiting for 
 * another opponent to connect to the server. The intended function for this 
 * scene is analogous to a loading screen.
 */
public class WaitingScene {
    
    private final int WINDOW_WIDTH  = 300;
    private final int WINDOW_HEIGHT = 150;
    
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
                Thread manager = new Thread(new AIGameManager(new AIClientStage()));
                manager.setDaemon(true);
                manager.start();
            }
        });

        scene = new Scene(pane, WINDOW_WIDTH, WINDOW_HEIGHT);
        scene.setFill(Color.LIGHTGRAY);
    }
}