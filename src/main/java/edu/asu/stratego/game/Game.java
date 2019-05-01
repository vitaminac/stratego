package edu.asu.stratego.game;

import edu.asu.stratego.game.board.ClientBoard;

import java.io.IOException;
import java.net.Socket;

/**
 * Contains information about the Stratego game, which is shared between the
 * JavaFX GUI and the ClientGameManager.
 *
 * @see edu.asu.stratego.gui.ClientStage
 * @see edu.asu.stratego.game.ClientGameManager
 */
public class Game {
    private static Game person = new Game();
    private static Game computer = new Game();

    private Player player;
    private Player opponent;

    private Move move;
    private MoveStatus moveStatus;

    private GameStatus status;
    private PieceColor turn;
    private ClientBoard board;

    private Socket socket;

    private static String serverIP = "localhost";

    public static final int PORT = 4212;

    /**
     * Initializes data fields for a new game.
     */
    public Game() {
        player = new Player();
        player.setNickname("anonimo");
        opponent = new Player();

        move = new Move();
        moveStatus = MoveStatus.OPP_TURN;

        status = GameStatus.SETTING_UP;
        turn = PieceColor.RED;

        board = new ClientBoard();
    }

    /**
     * @return Player object containing information about the player.
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * @param player Player object containing information about the player.
     */
    public void setPlayer(Player player) {
        this.player = player;
    }

    /**
     * @return Player object containing information about the opponent.
     */
    public Player getOpponent() {
        return opponent;
    }

    /**
     * @param opponent Player object containing information about the opponent.
     */
    public void setOpponent(Player opponent) {
        this.opponent = opponent;
    }

    /**
     * @return value the status of the game.
     */
    public GameStatus getStatus() {
        return status;
    }

    /**
     * @param status the status of the game
     */
    public void setStatus(GameStatus status) {
        this.status = status;
    }

    /**
     * @return value the color of the current player's turn
     */
    public PieceColor getTurn() {
        return turn;
    }

    /**
     * @param turn the color of the current player's turn
     */
    public void setTurn(PieceColor turn) {
        this.turn = turn;
    }

    /**
     * @return the game board.
     */
    public ClientBoard getBoard() {
        return board;
    }

    /**
     * @param board the game board
     */
    public void setBoard(ClientBoard board) {
        this.board = board;
    }

    public Move getMove() {
        return this.move;
    }

    public void setMove(Move move) {
        this.move = move;
    }

    public MoveStatus getMoveStatus() {
        return this.moveStatus;
    }

    public void setMoveStatus(MoveStatus moveStatus) {
        this.moveStatus = moveStatus;
    }

    public void connect() throws IOException {
        this.socket = new Socket(serverIP, PORT);
    }

    public Socket getSocket() {
        return socket;
    }

    public static String getServerIP() {
        return serverIP;
    }

    public static void setServerIP(String serverIP) {
        Game.serverIP = serverIP;
    }

    public static Game getGame() {
        return person;
    }

    public static Game getComputer() {
        return computer;
    }
}