package edu.asu.stratego.gui;

import edu.asu.stratego.game.ClientSocket;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;

public class prueba extends Application {
    private double xOffset;
    private double yOffset;
    public static String serverIP, nickname;
    public static final Object playerLogin = new Object();

    public Scene scene;
    {
        try {
            start(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));


        //Fin movimiento de ventana.


        scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
       // primaryStage.setScene(scene);
       // primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }



    public static class ConnectToServer implements Runnable {
        @Override
        public void run() {

            while (ClientSocket.getInstance() == null) {
                synchronized (playerLogin) {
                    try {
                        // Wait for submitFields button event.
                        playerLogin.wait();

                        // Attempt connection to server.
                        ClientSocket.connect(serverIP, 4212);
                    }
                    catch (IOException | InterruptedException e) {
                     //   Platform.runLater(() -> {
                     //       statusLabel.setText("Cannot connect to the Server");
                     //   });
                    }
                    finally {
                        // Wake up button event thread.
                        playerLogin.notify();
                    }
                }
            }
        }
    }
}
