package edu.asu.stratego;

import edu.asu.stratego.game.ServerGameManager;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Logger;

/**
 * The Stratego Server creates a socket and listens for connections from every
 * two players to form a game session. Each session is handled by a thread,
 * ServerGameManager, that communicates with the two players and determines the
 * status of the game.
 */
public class Server {
    private static final Logger logger = Logger.getLogger(Server.class.getName());

    public static void main(String[] args) throws IOException {
        String hostAddress = InetAddress.getLocalHost().getHostAddress();

        int sessionNumber = 1;
        try (ServerSocket listener = new ServerSocket(4212)) {
            logger.info("Server started @ " + hostAddress);
            logger.info("Waiting for incoming connections...\n");

            while (true) {
                Socket playerOne = listener.accept();
                logger.info("Session " + sessionNumber +
                        ": Player 1 has joined the session");

                Socket playerTwo = listener.accept();
                logger.info("Session " + sessionNumber +
                        ": Player 2 has joined the session");

                Thread session = new Thread(new ServerGameManager(
                        playerOne, playerTwo, sessionNumber++));
                session.setDaemon(true);
                session.start();


            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}