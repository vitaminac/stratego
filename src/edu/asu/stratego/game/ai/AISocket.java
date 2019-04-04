package edu.asu.stratego.game.ai;

import java.io.IOException;
import java.net.Socket;

public class AISocket {
    private static Socket socket = null;

    private AISocket() {
    }

    public static void connect(String serverIP, int port) throws IOException {
        socket = new Socket(serverIP, port);
    }
    
    public static Socket getInstance() {
        return socket;
    }
}
