package edu.asu.stratego.gui;

import edu.asu.stratego.game.Game;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;

import static edu.asu.stratego.gui.prueba.*;

public class Controller {

    @FXML private ImageView userArrow;
    @FXML private ImageView AIArrow;

    @FXML private AnchorPane AIPanel;
    @FXML private AnchorPane userPanel;

    @FXML private TextField nombreIA;
    @FXML private TextField dirIP;


    public void onExitButtonClicked(MouseEvent event) {
        Platform.exit();
        System.exit(0);
    }

    public void onAIButtonClicked(MouseEvent event) {
        if (!AIArrow.isVisible() || userArrow.isVisible()) {
            AIArrow.setVisible(true);
            userArrow.setVisible(false);
        }
        else
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


    public void onPlayButtonClicked(MouseEvent event) {

        nickname = nombreIA.getText();
        serverIP = dirIP.getText();


        Game.getPlayer().setNickname(nickname);


        synchronized (playerLogin) {
            try {
                playerLogin.notify();  // Signal submitFields button event.
                playerLogin.wait();    // Wait for connection attempt.
            }
            catch (InterruptedException e) {
                // TODO Handle this exception somehow...
                e.printStackTrace();
            }
        }
    }
}
