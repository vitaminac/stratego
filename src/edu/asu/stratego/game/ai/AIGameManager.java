package edu.asu.stratego.game.ai;

import edu.asu.stratego.game.ClientGameManager;
import edu.asu.stratego.game.Game;
import edu.asu.stratego.game.PieceColor;
import edu.asu.stratego.game.Player;
import edu.asu.stratego.gui.IClientStage;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.logging.Level;

public class AIGameManager extends ClientGameManager {
    public AIGameManager(IClientStage stage) {
        super(stage);
    }

    protected void waitForOpponent() {
        try {
            AISocket.connect(Game.getServerIP(), Game.PORT);
            // I/O Streams.
            toServer = new ObjectOutputStream(AISocket.getInstance().getOutputStream());
            fromServer = new ObjectInputStream(AISocket.getInstance().getInputStream());

            // Exchange player information.
            toServer.writeObject(Game.getPlayer());
            Game.setOpponent((Player) fromServer.readObject());

            // Infer player color from opponent color.
            if (Game.getOpponent().getColor() == PieceColor.RED)
                Game.getPlayer().setColor(PieceColor.BLUE);
            else
                Game.getPlayer().setColor(PieceColor.RED);
        } catch (IOException | ClassNotFoundException e) {
            // TO DO Handle this exception somehow...
            logger.log(Level.SEVERE, e.toString(), e);
        }
    }
}
