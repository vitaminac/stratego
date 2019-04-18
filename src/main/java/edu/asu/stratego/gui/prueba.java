package edu.asu.stratego.gui;

import edu.asu.stratego.game.Game;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;

public class prueba extends Application {
    protected static String serverIP;
    protected static String nickname;
    protected static final Object playerLogin = new Object();

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
        Parent root = FXMLLoader.load(getClass().getResource("ConnectionScreen.fxml"));


        //Fin movimiento de ventana.


        scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
    }

    public static void main(String[] args) {
        launch(args);
    }


    public static class ConnectToServer implements Runnable {
        @Override
        public void run() {

            while (Game.getGame().getSocket() == null) {
                synchronized (playerLogin) {
                    try {
                        // Wait for submitFields button event.
                        playerLogin.wait();

                        // Attempt connection to server.
                        Game.getGame().connect();
                    } catch (IOException | InterruptedException e) {
                        e.printStackTrace();
                    } finally {
                        // Wake up button event thread.
                        playerLogin.notify();
                    }
                }
            }
        }
    }
}
