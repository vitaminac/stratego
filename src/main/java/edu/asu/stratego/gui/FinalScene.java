package edu.asu.stratego.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

/**
 * Wrapper class for a JavaFX scene. Contains a scene UI to indicate that the
 * client has successfully connected to a server and is currently waiting for
 * another opponent to connect to the server. The intended function for this
 * scene is analogous to a loading screen.
 */
public class FinalScene extends Application {

    public Scene scene;

    {
        try {
            start(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("FinalScreen.fxml"));

        scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

}