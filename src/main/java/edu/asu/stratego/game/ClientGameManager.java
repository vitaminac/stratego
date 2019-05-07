package edu.asu.stratego.game;

import edu.asu.stratego.game.board.ClientSquare;
import edu.asu.stratego.gui.BoardScene;
import edu.asu.stratego.gui.ClientStage;
import edu.asu.stratego.gui.IClientStage;
import edu.asu.stratego.gui.board.BoardTurnIndicator;
import edu.asu.stratego.gui.prueba;
import edu.asu.stratego.media.ImageConstants;
import edu.asu.stratego.util.HashTables;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.util.Duration;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Task to handle the Stratego game on the client-side.
 */
public class ClientGameManager implements Runnable {
    private static Object receiveMove = new Object();
    private static Object waitFade    = new Object();
    private static Object waitVisible = new Object();
    protected static final Logger logger = Logger.getLogger( ClientGameManager.class.getName() );
    protected ObjectOutputStream toServer;
    protected ObjectInputStream  fromServer;

    private final IClientStage stage;
    private final Game game;

    /**
     * Creates a new instance of ClientGameManager.
     *
     * @param stage the stage that the client is set in
     */
    public ClientGameManager(IClientStage stage, Game game) {
        this.stage = stage;
        this.game = game;
    }

    /**
     * See ServerGameManager's run() method to understand how the client
     * interacts with the server.
     *
     *
     */
    @Override
    public void run() {
        connectToServer();
        waitForOpponent();
        setupBoard();
        try {
            playGame();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @return Object used for communication between the Setup Board GUI and
     * the ClientGameManager to indicate when the player has finished setting
     * up their pieces.
     */
    public Object getSetupPieces() {
        return this.game.getSetupPieces();
    }

    /**
     * Executes the ConnectToServer thread. Blocks the current thread until
     * the ConnectToServer thread terminates.
     *
     * @see edu.asu.stratego.gui.prueba.ConnectToServer
     */
    private void connectToServer() {
        try {
            prueba.ConnectToServer connectToServer =
                    new prueba.ConnectToServer();
            Thread serverConnect = new Thread(connectToServer);
            serverConnect.setDaemon(true);
            serverConnect.start();
            serverConnect.join();
        }
        catch(InterruptedException e) {
            // to do Handle this exception somehow...
            logger.log( Level.SEVERE, e.toString(), e );
            // Restore interrupted state...
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Establish I/O streams between the client and the server. Send player
     * information to the server. Then, wait until an object containing player
     * information about the opponent is received from the server.
     *
     * <p>
     * After the player information has been sent and opponent information has
     * been received, the method terminates indicating that it is time to set up
     * the game.
     * </p>
     */
    protected void waitForOpponent() {

        Platform.runLater(() -> stage.setWaitingScene());

        try {
            // I/O Streams.
            toServer = new ObjectOutputStream(this.game.getSocket().getOutputStream());
            fromServer = new ObjectInputStream(this.game.getSocket().getInputStream());

            // Exchange player information.
            toServer.writeObject(this.game.getPlayer());
            this.game.setOpponent((Player) fromServer.readObject());

            // Infer player color from opponent color.
            if (this.game.getOpponent().getColor() == PieceColor.RED)
                this.game.getPlayer().setColor(PieceColor.BLUE);
            else
                this.game.getPlayer().setColor(PieceColor.RED);
        }
        catch (IOException | ClassNotFoundException e) {
            // TO DO Handle this exception somehow...
            logger.log( Level.SEVERE, e.toString(), e );
        }
    }

    /**
     * Switches to the game setup scene. Players will place their pieces to
     * their initial starting positions. Once the pieces are placed, their
     * positions are sent to the server.
     */
    private void setupBoard() {
        Platform.runLater(stage::setBoardScene);

        synchronized (this.getSetupPieces()) {
            try {
                // Wait for the player to set up their pieces.
                this.getSetupPieces().wait();
                this.game.setStatus(GameStatus.WAITING_OPP);

                // Send initial piece positions to server.
                SetupBoard initial = new SetupBoard(this.game);
                toServer.writeObject(initial);

                // Receive opponent's initial piece positions from server.
                final SetupBoard opponentInitial = (SetupBoard) fromServer.readObject();

                // Place the opponent's pieces on the board.
                Platform.runLater(() -> {
                    for (int row = 0; row < 4; ++row) {
                        for (int col = 0; col < 10; ++col) {
                            ClientSquare square = this.game.getBoard().getSquare(row, col);
                            square.setPiece(opponentInitial.getPiece(row, col));

                            if (this.game.getPlayer().getColor() == PieceColor.RED)
                                square.getPiecePane().setPiece(ImageConstants.BLUE_BACK);
                            else
                                square.getPiecePane().setPiece(ImageConstants.RED_BACK);
                        }
                    }
                });
            }
            catch (InterruptedException | IOException | ClassNotFoundException e) {
                // to do Handle this exception somehow...
                logger.log( Level.SEVERE, e.toString(), e );
                // Restore interrupted state...
                Thread.currentThread().interrupt();
            }
        }
    }

    private void playGame() throws Exception {
        // Remove setup panel
        Platform.runLater(() ->
                BoardScene.getRootPane().getChildren().remove(BoardScene.getSetupPanel()));

        // Get game status from the server
        try {
            this.game.setStatus((GameStatus) fromServer.readObject());
        } catch (ClassNotFoundException | IOException e1) {
            // TO DO Handle this somehow...
            logger.log( Level.SEVERE, e1.toString(), e1 );
        }


        // Main loop (when playing)
        while (this.game.getStatus() == GameStatus.IN_PROGRESS) {
            try {
                // Get turn color from server.
                this.game.setTurn((PieceColor) fromServer.readObject());

                // If the turn is the client's, set move status to none selected
                if(this.game.getPlayer().getColor() == this.game.getTurn())
                    this.game.setMoveStatus(MoveStatus.NONE_SELECTED);
                else
                    this.game.setMoveStatus(MoveStatus.OPP_TURN);

                // Notify turn indicator.
                synchronized (this.game.getTurnIndicatorTrigger()) {
                    this.game.getTurnIndicatorTrigger().notify();
                }

                // Send move to the server.
                if (this.game.getPlayer().getColor() == this.game.getTurn() && this.game.getMoveStatus() != MoveStatus.SERVER_VALIDATION) {
                    synchronized (this.game.getSendMove()) {
                        this.game.getSendMove().wait();
                        toServer.writeObject(this.game.getMove());
                        this.game.setMoveStatus(MoveStatus.SERVER_VALIDATION);
                    }
                }

                // Receive move from the server.
                this.game.setMove((Move) fromServer.readObject());
                Piece startPiece = this.game.getMove().getStartPiece();
                Piece endPiece = this.game.getMove().getEndPiece();

                // If the move is an attack, not just a move to an unoccupied square
                if(this.game.getMove().isAttackMove()) {
                    Piece attackingPiece = this.game.getBoard().getSquare(this.game.getMove().getStart().x, this.game.getMove().getStart().y).getPiece();
                    if(attackingPiece.getPieceType() == PieceType.SCOUT) {
                        // Check if the scout is attacking over more than one square
                        int moveX = this.game.getMove().getStart().x - this.game.getMove().getEnd().x;
                        int moveY = this.game.getMove().getStart().y - this.game.getMove().getEnd().y;

                        if(Math.abs(moveX) > 1 || Math.abs(moveY) > 1) {
                            Platform.runLater(() -> {
                                try{
                                    int shiftX = 0;
                                    int shiftY = 0;

                                    if(moveX > 0) {shiftX = 1;}
                                    else if(moveX < 0) {shiftX = -1;}
                                    else if(moveY > 0) {shiftY = 1;}
                                    else if(moveY < 0) {shiftY = -1;}

                                    // Move the scout in front of the piece it's attacking before actually fading out
                                    ClientSquare scoutSquare = this.game.getBoard().getSquare(this.game.getMove().getEnd().x+shiftX, this.game.getMove().getEnd().y+shiftY);
                                    ClientSquare startSquare = this.game.getBoard().getSquare(this.game.getMove().getStart().x, this.game.getMove().getStart().y);
                                    scoutSquare.getPiecePane().setPiece(HashTables.PIECE_MAP.get(startSquare.getPiece().getPieceSpriteKey()));
                                    startSquare.getPiecePane().setPiece(null);
                                }
                                catch (Exception e) {
                                    // TO DO Handle this somehow...
                                    logger.log( Level.SEVERE, e.toString(), e );
                                }
                            });

                            // Wait 1 second after moving the scout in front of the piece it's going to attack
                            Thread.sleep(1000);

                            int shiftX = 0;
                            int shiftY = 0;

                            if(moveX > 0) {shiftX = 1;}
                            else if(moveX < 0) {shiftX = -1;}
                            else if(moveY > 0) {shiftY = 1;}
                            else if(moveY < 0) {shiftY = -1;}
                            ClientSquare startSquare = this.game.getBoard().getSquare(this.game.getMove().getStart().x, this.game.getMove().getStart().y);

                            // Fix the clientside software boards (and move) to reflect new scout location, now attacks like a normal piece
                            this.game.getBoard().getSquare(this.game.getMove().getEnd().x+shiftX, this.game.getMove().getEnd().y+shiftY).setPiece(startSquare.getPiece());
                            this.game.getBoard().getSquare(this.game.getMove().getStart().x, this.game.getMove().getStart().y).setPiece(null);

                            this.game.getMove().setStart(this.game.getMove().getEnd().x+shiftX, this.game.getMove().getEnd().y+shiftY);
                        }
                        Platform.runLater(() -> {
                            try {
                                // Set the face images visible to both players (from the back that doesn't show piecetype)
                                ClientSquare startSquare = this.game.getBoard().getSquare(this.game.getMove().getStart().x, this.game.getMove().getStart().y);
                                ClientSquare endSquare = this.game.getBoard().getSquare(this.game.getMove().getEnd().x, this.game.getMove().getEnd().y);

                                Piece animStartPiece = startSquare.getPiece();
                                Piece animEndPiece = endSquare.getPiece();

                                startSquare.getPiecePane().setPiece(HashTables.PIECE_MAP.get(animStartPiece.getPieceSpriteKey()));
                                endSquare.getPiecePane().setPiece(HashTables.PIECE_MAP.get(animEndPiece.getPieceSpriteKey()));
                            }
                            catch (Exception e) {
                                // TO DO Handle this somehow...
                                logger.log( Level.SEVERE, e.toString(), e );
                            }
                        });
                    }


                    // Wait three seconds (the image is shown to client, then waits 2 seconds)
                    Thread.sleep(2000);

                    // Fade out pieces that lose (or draw)
                    Platform.runLater(() -> {
                        try {
                            ClientSquare startSquare = this.game.getBoard().getSquare(this.game.getMove().getStart().x, this.game.getMove().getStart().y);
                            ClientSquare endSquare = this.game.getBoard().getSquare(this.game.getMove().getEnd().x, this.game.getMove().getEnd().y);

                            // If the piece dies, fade it out (also considers a draw, where both "win" are set to false)
                            if(!this.game.getMove().isAttackWin()) {
                                FadeTransition fadeStart = new FadeTransition(Duration.millis(1500), startSquare.getPiecePane().getPiece());
                                fadeStart.setFromValue(1.0);
                                fadeStart.setToValue(0.0);
                                fadeStart.play();
                                fadeStart.setOnFinished(new ResetImageVisibility());
                            }
                            if(!this.game.getMove().isDefendWin()) {
                                FadeTransition fadeEnd = new FadeTransition(Duration.millis(1500), endSquare.getPiecePane().getPiece());
                                fadeEnd.setFromValue(1.0);
                                fadeEnd.setToValue(0.0);
                                fadeEnd.play();
                                fadeEnd.setOnFinished(new ResetImageVisibility());
                            }
                        }
                        catch (Exception e) {
                            // TO DO Handle this somehow...
                            logger.log( Level.SEVERE, e.toString(), e );
                        }
                    });

                    // Wait 1.5 seconds while the image fades out
                    Thread.sleep(1500);
                }

                // Set the piece on the software (non-GUI) board to the updated pieces (either null or the winning piece)
                this.game.getBoard().getSquare(this.game.getMove().getStart().x, this.game.getMove().getStart().y).setPiece(startPiece);
                this.game.getBoard().getSquare(this.game.getMove().getEnd().x, this.game.getMove().getEnd().y).setPiece(endPiece);

                // Update GUI.
                Platform.runLater(() -> {
                    ClientSquare endSquare = this.game.getBoard().getSquare(this.game.getMove().getEnd().x, this.game.getMove().getEnd().y);

                    // Draw
                    if(endPiece == null)
                        endSquare.getPiecePane().setPiece(null);
                    else{
                        // If not a draw, set the end piece to the PieceType face
                        if(endPiece.getPieceColor() == this.game.getPlayer().getColor()) {
                            endSquare.getPiecePane().setPiece(HashTables.PIECE_MAP.get(endPiece.getPieceSpriteKey()));
                        }
                        // ...unless it is the opponent's piece which it will display the back instead
                        else{
                            if (endPiece.getPieceColor() == PieceColor.BLUE)
                                endSquare.getPiecePane().setPiece(ImageConstants.BLUE_BACK);
                            else
                                endSquare.getPiecePane().setPiece(ImageConstants.RED_BACK);
                        }
                    }
                });

                // If it is an attack, wait 0.05 seconds to allow the arrow to be visible
                if(this.game.getMove().isAttackMove()) {
                    Thread.sleep(50);
                }

                Platform.runLater(() -> {
                    // Arrow
                    ClientSquare arrowSquare = this.game.getBoard().getSquare(this.game.getMove().getStart().x, this.game.getMove().getStart().y);

                    // Change the arrow to an image (and depending on what color the arrow should be)
                    if(this.game.getMove().getMoveColor() == PieceColor.RED)
                        arrowSquare.getPiecePane().setPiece(ImageConstants.MOVEARROW_RED);
                    else
                        arrowSquare.getPiecePane().setPiece(ImageConstants.MOVEARROW_BLUE);

                    // Rotate the arrow to show the direction of the move
                    if(this.game.getMove().getStart().x > this.game.getMove().getEnd().x)
                        arrowSquare.getPiecePane().getPiece().setRotate(0);
                    else if(this.game.getMove().getStart().y < this.game.getMove().getEnd().y)
                        arrowSquare.getPiecePane().getPiece().setRotate(90);
                    else if(this.game.getMove().getStart().x < this.game.getMove().getEnd().x)
                        arrowSquare.getPiecePane().getPiece().setRotate(180);
                    else
                        arrowSquare.getPiecePane().getPiece().setRotate(270);

                    // Fade out the arrow
                    FadeTransition ft = new FadeTransition(Duration.millis(1500), arrowSquare.getPiecePane().getPiece());
                    ft.setFromValue(1.0);
                    ft.setToValue(0.0);
                    ft.play();
                    ft.setOnFinished(new ResetSquareImage());
                });

                // Wait for fade animation to complete before continuing.
                synchronized (waitFade) { waitFade.wait(); }

                // Get game status from server.
                this.game.setStatus((GameStatus) fromServer.readObject());
            }
            catch (ClassNotFoundException | IOException | InterruptedException e) {
                // TO DO Handle this exception somehow...
                logger.log( Level.SEVERE, e.toString(), e );
                // Restore interrupted state...
                Thread.currentThread().interrupt();
            }
        }

        revealAll();
        setFinalScene();
    }

    public static Object getReceiveMove() {
        return receiveMove;
    }

    private void revealAll() {
        // End game, reveal all pieces
        Platform.runLater(() -> {
            for(int row = 0; row < 10; row++) {
                for(int col = 0; col < 10; col++) {
                    if(this.game.getBoard().getSquare(row, col).getPiece() != null && this.game.getBoard().getSquare(row, col).getPiece().getPieceColor() != this.game.getPlayer().getColor()) {
                        this.game.getBoard().getSquare(row, col).getPiecePane().setPiece(HashTables.PIECE_MAP.get(this.game.getBoard().getSquare(row, col).getPiece().getPieceSpriteKey()));
                    }
                }
            }
        });
    }

    private void setFinalScene() {
        // Show the end game screen
        Platform.runLater(() -> {
            ClientStage stage2 = new ClientStage();
            stage2.setFinalScene();
        });
    }

    // Finicky, ill-advised to edit. Resets the opacity, rotation, and piece to null
    // Duplicate "ResetImageVisibility" class was intended to not set piece to null, untested though.
    private class ResetSquareImage implements EventHandler<ActionEvent> {
        @Override
        public void handle(ActionEvent event) {
            synchronized (waitFade) {
                waitFade.notifyAll();
                ClientGameManager.this.game.getBoard().getSquare(ClientGameManager.this.game.getMove().getStart().x, ClientGameManager.this.game.getMove().getStart().y).getPiecePane().getPiece().setOpacity(1.0);
                ClientGameManager.this.game.getBoard().getSquare(ClientGameManager.this.game.getMove().getStart().x, ClientGameManager.this.game.getMove().getStart().y).getPiecePane().getPiece().setRotate(0.0);
                ClientGameManager.this.game.getBoard().getSquare(ClientGameManager.this.game.getMove().getStart().x, ClientGameManager.this.game.getMove().getStart().y).getPiecePane().setPiece(null);

                ClientGameManager.this.game.getBoard().getSquare(ClientGameManager.this.game.getMove().getEnd().x, ClientGameManager.this.game.getMove().getEnd().y).getPiecePane().getPiece().setOpacity(1.0);
                ClientGameManager.this.game.getBoard().getSquare(ClientGameManager.this.game.getMove().getEnd().x, ClientGameManager.this.game.getMove().getEnd().y).getPiecePane().getPiece().setRotate(0.0);
            }
        }
    }

    // read above comments
    private class ResetImageVisibility implements EventHandler<ActionEvent> {
        @Override
        public void handle(ActionEvent event) {
            synchronized (waitVisible) {
                waitVisible.notifyAll();
                ClientGameManager.this.game.getBoard().getSquare(ClientGameManager.this.game.getMove().getStart().x, ClientGameManager.this.game.getMove().getStart().y).getPiecePane().getPiece().setOpacity(1.0);
                ClientGameManager.this.game.getBoard().getSquare(ClientGameManager.this.game.getMove().getStart().x, ClientGameManager.this.game.getMove().getStart().y).getPiecePane().getPiece().setRotate(0.0);
                ClientGameManager.this.game.getBoard().getSquare(ClientGameManager.this.game.getMove().getStart().x, ClientGameManager.this.game.getMove().getStart().y).getPiecePane().setPiece(null);

                ClientGameManager.this.game.getBoard().getSquare(ClientGameManager.this.game.getMove().getEnd().x, ClientGameManager.this.game.getMove().getEnd().y).getPiecePane().getPiece().setOpacity(1.0);
                ClientGameManager.this.game.getBoard().getSquare(ClientGameManager.this.game.getMove().getEnd().x, ClientGameManager.this.game.getMove().getEnd().y).getPiecePane().getPiece().setRotate(0.0);
            }
        }
    }
}