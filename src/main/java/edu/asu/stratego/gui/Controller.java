package edu.asu.stratego.gui;

import edu.asu.stratego.game.Game;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;

import static edu.asu.stratego.gui.prueba.*;

public class Controller {

    // Connection Screen
    @FXML
    private ImageView userArrow;
    @FXML
    private ImageView AIArrow;

    @FXML
    private AnchorPane AIPanel;
    @FXML
    private AnchorPane userPanel;

    //@FXML private TextField nombreIA; WHEN IMPLEMENT IA WE PUT THIS WITH IF.
    @FXML
    private TextField nombreUsuario;
    @FXML
    private TextField dirIP;


    // END CONNECTION SCREEN

    // FINAL SCREEN

    // END FINAL SCREEN


    public void onExitButtonClicked(MouseEvent event) {
        Platform.exit();
        System.exit(0);
    }

    public void onAIButtonClicked(MouseEvent event) {
        if (!AIArrow.isVisible() || userArrow.isVisible()) {
            AIArrow.setVisible(true);
            userArrow.setVisible(false);
        } else
            AIArrow.setVisible(false);
        if (!AIPanel.isVisible()) {
            AIPanel.setVisible(true);
            return;
        } else
            AIPanel.setVisible(false);

        userPanel.setVisible(false);
        userArrow.setVisible(false);
    }

    public void onPlayerButtonClicked(MouseEvent event) {
        if (AIArrow.isVisible() || AIPanel.isVisible() || !userPanel.isVisible()) {
            AIArrow.setVisible(false);
            AIPanel.setVisible(false);
            userPanel.setVisible(true);
            userArrow.setVisible(true);
        } else {
            userPanel.setVisible(false);
            userArrow.setVisible(false);
        }

    }

    public void onPlayAgainButtonClicked(MouseEvent event) {
        try {

            FXMLLoader loader = new FXMLLoader(this.getClass().getResource("ConnectionScreen.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene;
            scene = new Scene(loader.load());

            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void onPlayButtonClicked(MouseEvent event) {

        nickname = nombreUsuario.getText();
        serverIP = dirIP.getText();

        Game.getGame().getPlayer().setNickname(nickname);

        synchronized (playerLogin) {
            try {
                playerLogin.notify();  // Signal submitFields button event.
                playerLogin.wait();    // Wait for connection attempt.
            } catch (InterruptedException e) { e.printStackTrace(); }
        }
    }

    public void onPlayAiButtonClicked(MouseEvent event ) {

    }
}
