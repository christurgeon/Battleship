/**
 *  @author Chris Turgeon
 *  @version 1.0
 */

package code.tools;
import javax.swing.*;
import java.net.*;
import java.io.*;

public class Server extends Thread {

  private ServerSocket ss;
  private Socket clientSocket;
  private OutputStream outStream;
  private InputStream inStream;

  private GUI gui;
  private int port;
  protected int rowClicked, colClicked;

  protected boolean sendInitialBoard, readInitialBoard;
  protected boolean sendMove, readMove;
  protected boolean yourTurn;


  /**
   *  Constructor for the server object
   *  @param gui - a reference to the GUI so that the server
   *               can interact with the game when events happen
   *  @param portNumber - the port number used to connect to client
   *  @return None
   */
  public Server(GUI gui, int portNumber) {
    this.gui = gui;
    this.port = portNumber;
    this.rowClicked = -1;
    this.colClicked = -1;
    this.sendInitialBoard = true;
    this.readInitialBoard = true;
    this.sendMove = false;
    this.readMove = false;
    this.yourTurn = false;
  }

  /**
   *  The main loop for the server, it sends the players
   *  board to the client, it reads the clients board
   *  and can send and read in moves made throughout the game.
   *  @param None
   *  @return None
   */
  public void serverStart() {
    try {
      gui.connected = true;
      this.ss = new ServerSocket(this.port);
      this.clientSocket = this.ss.accept();
    } catch(Exception e) {
      System.out.println("Sever error: " + e.getMessage());
    }
    while (true) {
      try {
        // Create an input and output stream
        this.outStream = this.clientSocket.getOutputStream();
        this.inStream = this.clientSocket.getInputStream();

        // Send the board that the user set to the other player
        if (sendInitialBoard) {
          OutputStreamWriter outStreamWriter = new OutputStreamWriter(outStream);
          BufferedWriter writer = new BufferedWriter(outStreamWriter);
          String time = Integer.toString(gui.timer.getTime()) + "\n";
          String numBoats = Integer.toString(gui.shipInfoSize) + "\n";
          String numRows = Integer.toString(gui.boardData[0]) + "\n";
          String numCols = Integer.toString(gui.boardData[1]) + "\n";
          writer.write(time);
          writer.write(numBoats);
          writer.write(numRows);
          writer.write(numCols);
          for (int i = 0; i < gui.shipInfoSize; i++) {
            for (int j = 0; j < 4; j++) {
              writer.write(Integer.toString(gui.shipInfo[i][j]) + "\n");
            }
          }
          for (int i = 2; i < 12; i++) {
            writer.write(Integer.toString(gui.boardData[i]) + "\n");
          }
          writer.flush();
          sendInitialBoard = false;
          System.out.println("Successfully sent the board to Player 2");
        }

        // Slowing dows the thread to catch booleans
        try { Thread.sleep(300); }
        catch(Exception e) { System.out.println("Thread error..."); }

        // Take in the ship placement information from the other player
        if (readInitialBoard) {
          InputStreamReader inStreamReader = new InputStreamReader(inStream);
          BufferedReader reader = new BufferedReader(inStreamReader);
          String boatCount;
          while ( (boatCount = reader.readLine()) == null ) { }
          int numBoats = Integer.valueOf(boatCount);
          int numRows = Integer.valueOf( reader.readLine() );
          int numCols = Integer.valueOf( reader.readLine() );
          int[][] opponentBoardData = new int[numBoats][4];
          for (int i = 0; i < numBoats; i++) {
            opponentBoardData[i][0] = Integer.valueOf( reader.readLine() );
            opponentBoardData[i][1] = Integer.valueOf( reader.readLine() );
            opponentBoardData[i][2] = Integer.valueOf( reader.readLine() );
            opponentBoardData[i][3] = Integer.valueOf( reader.readLine() );
          }
          gui.buildOpponentsBoard(opponentBoardData);
          readInitialBoard = false;
          String msg = "The game is about to begin! You go first!";
          JOptionPane.showMessageDialog(null, msg);
          this.yourTurn = true;
          gui.timer.startTimer();
        }

        // Send the move to the other user
        if (sendMove) {
          if (this.rowClicked == -1 || this.colClicked == -1) { continue; }
          try {
            OutputStreamWriter outStreamWriter = new OutputStreamWriter(outStream);
            BufferedWriter writer = new BufferedWriter(outStreamWriter);
            String row = Integer.toString(this.rowClicked) + "\n";
            String col = Integer.toString(this.colClicked) + "\n";
            writer.write(row);
            writer.write(col);
            writer.flush();
            gui.processSendMove(this.rowClicked, this.colClicked);
            this.readMove = true;
            this.sendMove = false;
            this.yourTurn = false;
            this.rowClicked = -1;
            this.colClicked = -1;
            gui.timer.reset();
          } catch(IOException e) {
            System.err.format("Connection lost with client...");
            System.exit(1);
          }
        }

        // Try to read the move from the other user
        if (readMove) {
          try {
            InputStreamReader inStreamReader = new InputStreamReader(inStream);
            BufferedReader reader = new BufferedReader(inStreamReader);
            String row;
            while ( (row = reader.readLine()) == null) { }
            int r = Integer.valueOf(row);
            int c = Integer.valueOf( reader.readLine() );
            this.readMove = false;
            this.yourTurn = true;
            System.out.printf("Player 2 sent move to %d,%d\n", r, c);
            gui.processReadMove(r, c);
            gui.timer.startTimer();
          } catch(IOException e) {
            System.err.format("Connection lost with client...");
            System.exit(1);
          }
        }
      } catch(IOException ex) {
        System.err.format("Connection lost with client...");
        System.exit(1);
      }
    }
  }

  /**
   *  Method to close the socket and the server
   *  @param None
   *  @return None
   */
  public void serverStop() {
    if (clientSocket != null) {
      try {
        clientSocket.close();
      } catch(Exception e) {
        e.printStackTrace();
      }
    }
  }

  /**
   *  Overrided method for thread run()
   *  @param None
   *  @return None
   */
  @Override
  public void run() {
    serverStart();
  }
}
