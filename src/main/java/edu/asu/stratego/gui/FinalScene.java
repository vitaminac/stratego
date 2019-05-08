package edu.asu.stratego.gui;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;

import java.io.IOException;

/**
 * Wrapper class for a JavaFX scene. Contains a scene UI to indicate that the
 * client has successfully connected to a server and is currently waiting for
 * another opponent to connect to the server. The intended function for this
 * scene is analogous to a loading screen.
 */
public class FinalScene {

    public Scene scene;

    public FinalScene() {
        Parent root = null;
        try {
            root = FXMLLoader.load(getClass().getResource("FinalScreen.fxml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
    }
}