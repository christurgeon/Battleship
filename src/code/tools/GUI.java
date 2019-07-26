/**
 *  @author Chris Turgeon
 *  @version 1.0
 */

package code.tools;
import java.util.LinkedList;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.io.*;
import javax.swing.border.EmptyBorder;

public class GUI {

  protected Client client;
  protected Server server;
  protected int portNumber;
  protected String host;
  protected boolean connected;
  private boolean connectToAnother;

  protected MainFrame frame;
  private JPanel containerPanel;
  private GameMenuBar gameMenu;
  private OrientationCheckBox checkBox;
  private NetworkSettings networkSettings;
  private BoatInitializer boatInit;
  private NetworkDialogPanel networkPanel;
  private JButton readyToPlay;
  private Color color;
  protected Board leftBoard, rightBoard;
  protected ScoringPanel score;

  private LinkedList<Integer> boatList;
  protected int[] boardData;
  protected int shipInfoSize, clientTime;
  protected int[][] shipInfo;
  protected Battleship opponentBoard;
  protected TimerPanel timer;
  protected int seconds;


  /**
   *  Method used to read in user gameplay information and
   *  build the GUI for the client and server.
   *  @param None
   *  @return None
   */
  public void createAndShowGUI() {

    // Prompt the user for network settings
    this.networkSettings = new NetworkSettings();
    int reply = this.networkSettings.getInput();
    if (reply == -1) { System.exit(1); }
    this.connectToAnother = this.networkSettings.connectToOtherPlayer();
    this.connected = false;

    portNumber = 25000;
    host = "localhost";

    if (this.connectToAnother) {
      // Prompt the user for a port number
      this.host = null;
      reply = -1;
      while (this.host == null || reply == -1) {
        this.networkPanel = new NetworkDialogPanel();
        String[] options = { "Submit" };
        int optionType = JOptionPane.DEFAULT_OPTION;
        int messageType = JOptionPane.PLAIN_MESSAGE;
        reply = JOptionPane.showOptionDialog(null, networkPanel, "", optionType,
                            messageType, null, options, options[0]);
        if (reply == 0) {
          this.portNumber = this.networkPanel.getPort();
          this.host = this.networkPanel.getInetAddress();
        }
      }
      connectToUser();
    } else {
      File configFile = new File("_CONFIG_.txt");
      if (configFile.exists()) {
        loadConfig();
      } else {
        // Retrieve the board data and set the dimensions
        this.boatInit = new BoatInitializer();
        while (!this.boatInit.validInput()) {
          this.boatInit = new BoatInitializer();
        }
        this.boardData = boatInit.getGameAttributes();
        this.leftBoard = new Board(boardData[0], boardData[1]);
        this.rightBoard = new Board(boardData[0], boardData[1]);
      }
      buildBoardsPanel();
    }
  }

  /**
   *  Write a configuration file to store game date
   *  @param None
   *  @return None
   */
  public void writeConfig() {
    if (connectToAnother) // Return if client
      return;
    PrintWriter writer = null;
    try {
      writer = new PrintWriter("_CONFIG_.txt", "UTF-8");
      for (int i = 0; i < this.boardData.length; i++) { // Write out ships
        writer.println(this.boardData[i]);
      }
      writer.println(timer.getTime()); // Write out default timer time
    } catch(Exception e) {
      System.out.println("Something went wrong while trying to write to config file...");
    } finally {
      if (writer != null)
        writer.close();
    }
  }

  /**
   *  Load the configuration file if it exists
   *  @param None
   *  @return None
   */
  public void loadConfig() {
    BufferedReader br = null;
    try {
      File file = new File("_CONFIG_.txt");
      br = new BufferedReader(new FileReader(file));
      this.boardData = new int[12];
      for (int i = 0; i < 12; i++) {
        this.boardData[i] = Integer.valueOf( br.readLine() );
      }
      this.leftBoard = new Board(boardData[0], boardData[1]);
      this.rightBoard = new Board(boardData[0], boardData[1]);
      int time = Integer.valueOf( br.readLine() );
      timer = new TimerPanel(time);
    } catch(Exception e) {
      System.out.println("No config file found");
    } finally {
      try { br.close(); }
      catch (Exception e) { System.out.println("Couldn't close reader"); }
    }
  }

  /**
   *  Builds the main frame, the board panels, and additional features
   *  and then displays the GUI.
   *  @param None
   *  @return None
   */
  public void buildBoardsPanel() {

    // Create the main frame
    this.frame = new MainFrame();
    this.color = Color.RED;
    this.frame.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent windowEvent) {
        writeConfig();
        if (server != null) {
          server.serverStop();
        }
      }
    });

    // Add both of the boards to a panel
    JPanel boardsPanel = new JPanel();
    boardsPanel.setLayout(new BoxLayout(boardsPanel, BoxLayout.Y_AXIS));
    this.rightBoard.setRight();
    boardsPanel.add(this.leftBoard);
    boardsPanel.add(this.rightBoard);

    // Create a panel to hold all of the game elements
    this.containerPanel = new JPanel();
    this.containerPanel.setLayout(new BorderLayout());
    this.containerPanel.add(boardsPanel, BorderLayout.CENTER);

    // Add an orientation selector and score board
    JPanel southPanel = new JPanel();
    southPanel.setLayout(new FlowLayout());
    this.checkBox = new OrientationCheckBox();
    this.score = new ScoringPanel();
    southPanel.add(this.score);
    southPanel.add(this.checkBox);
    this.containerPanel.add(southPanel, BorderLayout.SOUTH);

    // Add the game menu bar
    this.gameMenu = new GameMenuBar();
    this.containerPanel.add(gameMenu, BorderLayout.NORTH);

    // Display the board
    this.frame.setJMenuBar(this.gameMenu.menuBar);
    this.frame.getContentPane().add(containerPanel);
    this.frame.pack();
    this.frame.setVisible(true);
  }

  /**
   *  This method checks to see if a boat can be placed at a certain location
   *  and it places it if possible, if not then it sends the player a message
   *  and returns false.
   *  @param size - the size of the boat to be placed
   *  @param r - the starting row of the boat
   *  @param c - the starting column of the boat
   *  @param horizontal - true if the boat is horizontal, false otherwise
   *  @return true if the boat was successfully placed, false otherwise
   */
  private boolean checkLocationAndPlace(int size, int r, int c, boolean horizontal) {
    int idx = this.shipInfoSize;

    // Check to see if a ship can be placed horizontally and if so place it
    if (horizontal) {
      try {
        for (int col = c; col < c + size; col++) {
          if (this.leftBoard.shipExists(r, col)) {
            JOptionPane.showMessageDialog(null, "Cannot place a ship over another!");
            return false;
          }
        }
        this.shipInfo[idx][0] = size;
        this.shipInfo[idx][1] = 0;
        this.shipInfo[idx][2] = r;
        this.shipInfo[idx][3] = c;
        this.shipInfoSize++;
        for (int col = c; col < c + size; col++) {
          this.leftBoard.changeButtonColor(r, col, this.color);
        }
      } catch(IndexOutOfBoundsException e) {
        JOptionPane.showMessageDialog(null, "Ship went off the board, try again!");
        return false;
      }
    }

    // Check to see if a ship can be placed vertically and if so place it
    else {
      try {
        for (int row = r; row < r + size; row++) {
          if (this.leftBoard.shipExists(row, c)) {
            JOptionPane.showMessageDialog(null, "Cannot place a ship over another!");
            return false;
          }
        }
        this.shipInfo[idx][0] = size;
        this.shipInfo[idx][1] = 1;
        this.shipInfo[idx][2] = r;
        this.shipInfo[idx][3] = c;
        this.shipInfoSize++;
        for (int row = r; row < r + size; row++) {
          this.leftBoard.changeButtonColor(row, c, this.color);
        }
      } catch(IndexOutOfBoundsException e) {
        JOptionPane.showMessageDialog(null, "Ship went off the board, try again!");
        return false;
      }
    }
    return true;
  }

  /**
   *  This method is called on the server side and it starts the
   *  server in a separate thread.
   *  @param None
   *  @return None
   */
  private void waitToConnect() {
    try {
      // Configure the server
      System.out.printf("Server started and listening to the port %d using IP %s\n", this.portNumber, this.host);
      this.server = new Server(this, this.portNumber);
      this.server.start();
    } catch (Exception e) {
      System.out.println("Could not start the server, try again!");
      System.exit(1);
    }
  }

  /**
   *  Initialize a client to connect and play with the host server
   *  @param None
   *  @return None
   */
  private void connectToUser() {
    this.client = new Client(this);
    this.client.start();
  }

  /**
   *  Takes in the other player's board and puts it into the Battleship
   *  object which is used to compute hits and misses
   *  @param enemyBoard - a 2D grid of boat locations
   *  @return None
   */
  public void buildOpponentsBoard(int[][] enemyBoard) {
    this.opponentBoard = new Battleship(this.boardData[0], this.boardData[1],
                                        enemyBoard.length, enemyBoard);
  }

  /**
   *  This method takes in a location of a shot and updates the
   *  board with relevant information such as hit status. It processes
   *  the send move so it updates the right hand (bottom) board.
   *  @param r - the row location in board
   *  @param c - the column location in the board
   *  @return None
   */
  public void processSendMove(int r, int c) {
    int[] shotInfo = this.opponentBoard.setAndGetHitInfo(r, c);
    int hitStatus = shotInfo[4];
    this.rightBoard.changeButtonColor(r, c, Color.BLACK);
    if (shotInfo[5] == 0) {
      processEndGame();
    }
    if (hitStatus == -1) {
      JOptionPane.showMessageDialog(null, "Sorry! It's a MISS.");
    } else if (hitStatus == 0) {
      JOptionPane.showMessageDialog(null, "Nice shot! It's a HIT.");
    }
  }

  /**
   *  This method takes in a shot from the opponent and updates
   *  the left hand side (or top) board.
   *  @param r - the row location in the board
   *  @param c - the column location in the board
   *  @return None
   */
  public void processReadMove(int r, int c) {
    int[] shotInfo = this.opponentBoard.setAndGetHitInfo(r, c);
    int hitStatus = shotInfo[4];
    this.leftBoard.changeButtonColor(r, c, Color.BLACK);
    boolean allDestroyed = this.leftBoard.lost();
    if (allDestroyed) {
      JOptionPane.showMessageDialog(null, "You have lost the game!");
      this.frame.dispatchEvent(new WindowEvent(this.frame, WindowEvent.WINDOW_CLOSING));
      System.exit(1);
    }
  }

  /**
   *  This method is called when one of the users destroys
   *  all of his/her opponents ships and the game is over.
   *  @param None
   *  @return None
   */
   public void processEndGame() {
     if (connectToAnother) { // Notify server
       score.updatePlayerTwoScore();
     } else { // Notify client
       score.updatePlayerOneScore();
     }
     JOptionPane.showMessageDialog(null, "The game is over! You won!");
     this.frame.dispatchEvent(new WindowEvent(this.frame, WindowEvent.WINDOW_CLOSING));
     System.exit(1);
   }


  // ============================================================================
  // ============================================================================


  public class Client extends Thread {

    private Socket socket;
    private GUI gui;
    private InputStream inStream;
    private OutputStream outStream;

    protected boolean sendInitialBoard, readInitialBoard;
    protected boolean sendMove, readMove;
    protected boolean gameDone, yourTurn;

    protected int rowClicked, colClicked;

    /**
     *  Client code that serves as the second player
     *  @param gui - a reference to the GUI so that the server can
     *               interact with the game when events happen
     *
     */
    public Client(GUI gui) {
      this.gui = gui;
      this.readInitialBoard = true;
      this.gameDone = false;
      this.sendMove = false;
      this.readMove = false;
      this.yourTurn = false;
      this.rowClicked = -1;
      this.colClicked = -1;
    }

    /**
     *  This is the driving method for the client thread.
     *  It runs in a loop and repeatedly takes in board inforation
     *  and sends out board inforation to the server (opponent)
     *  @param None
     *  @return None
     */
    public void runClient() {
      try {
        InetAddress address = InetAddress.getByName(gui.host);
        System.out.printf("Trying to connect to port %d at IP %s\n", gui.portNumber, address);
        this.socket = new Socket(address, gui.portNumber);
        this.inStream = socket.getInputStream();
        this.outStream =  socket.getOutputStream();
      } catch(Exception e) {
        System.err.format("Connection could not be made...");
        System.exit(1);
      }
      try {
        while (!gameDone) {

          // Read the board details from the other user
          if (readInitialBoard) {
            InputStreamReader inStreamReader = new InputStreamReader(inStream);
            BufferedReader reader = new BufferedReader(inStreamReader);
            int time = Integer.valueOf( reader.readLine() );
            int numBoats = Integer.valueOf( reader.readLine() );
            int numRows = Integer.valueOf( reader.readLine() );
            int numCols = Integer.valueOf( reader.readLine() );
            gui.leftBoard = new Board(numRows, numCols);
            gui.rightBoard = new Board(numRows, numCols);
            gui.boardData = new int[12];
            gui.boardData[0] = numRows;
            gui.boardData[1] = numCols;
            gui.clientTime = time;
            int[][] opponentBoardData = new int[numBoats][4];
            for (int i = 0; i < numBoats; i++) {
              opponentBoardData[i][0] = Integer.valueOf( reader.readLine() );
              opponentBoardData[i][1] = Integer.valueOf( reader.readLine() );
              opponentBoardData[i][2] = Integer.valueOf( reader.readLine() );
              opponentBoardData[i][3] = Integer.valueOf( reader.readLine() );
            }
            for (int i = 2; i < 12; i++) {
              gui.boardData[i] = Integer.valueOf( reader.readLine() );
            }
            buildBoardsPanel();
            buildOpponentsBoard(opponentBoardData);
            gui.connected = true;
            readInitialBoard = false;
          }

          // Slowing dows the thread to catch booleans
          try { Thread.sleep(300); }
          catch(Exception e) { System.out.println("Thread error..."); }

          // Write the players ship placement to the host of the game
          if (sendInitialBoard) {
            OutputStreamWriter outStreamWriter = new OutputStreamWriter(outStream);
            BufferedWriter writer = new BufferedWriter(outStreamWriter);
            String numBoats = Integer.toString(gui.shipInfoSize) + "\n";
            String numRows = Integer.toString(gui.boardData[0]) + "\n";
            String numCols = Integer.toString(gui.boardData[1]) + "\n";
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
            System.out.println("Successfully sent the board to Player 1");
            String msg = "The game is about to begin! Your opponent goes first!";
            JOptionPane.showMessageDialog(null, msg);
            this.readMove = true;
          }

          // Send the move to the other user
          if (sendMove) {
            if (this.rowClicked == -1 || this.colClicked == -1) { continue; }
            OutputStreamWriter outStreamWriter = new OutputStreamWriter(outStream);
            BufferedWriter writer = new BufferedWriter(outStreamWriter);
            String row = Integer.toString(this.rowClicked) + "\n";
            String col = Integer.toString(this.colClicked) + "\n";
            try {
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
              gui.timer.startTimer();
              gui.processReadMove(r, c);
            } catch(IOException e) {
              System.out.println("Connection has been broken...");
              System.exit(1);
            }
          }
        }
      } catch(Exception e2) {
        JOptionPane.showMessageDialog(null, "The connection was refused... try again");
      } finally {
        try {
          if (this.socket != null) {
            this.socket.close();
          }
        } catch(Exception e3) {
          System.exit(1);
        }
      }
    }

    /**
     *  Allow the client player to send his/her board
     *  @param None
     *  @return None
     */
    public void clientSendBoard() {
      this.sendInitialBoard = true;
    }

    /**
     *  Override run() method for client thread
     *  @param None
     *  @return None
     */
    @Override
    public void run() {
      runClient();
    }
  }


// ============================================================================
// ============================================================================


  public class Board extends JPanel {

    private JButton[][] board;
    private LinkedList<Integer> boats;
    private int numRows;
    private int numCols;
    private boolean placeMode, isRight;
    protected boolean donePlacing;

    /**
     *  Takes in the number of rows and columns and builds a board.
     *  @param rows - the number of rows in the board
     *  @param cols - the number of columns in the board
     *  @return None
     */
    public Board(int rows, int cols) {
      this.board = new JButton[rows][cols];
      this.boats = new LinkedList<Integer>();
      this.numRows = rows;
      this.numCols = cols;
      this.placeMode = false;
      this.isRight = false;
      setLayout(new GridLayout(rows, cols));
      for (int i = 0; i < numRows; i++) {
        for (int j = 0; j < numCols; j++) {
          JButton button = new JButton();
          button.setBackground(Color.LIGHT_GRAY);
          button.setActionCommand(String.format("%d,%d", i, j));
          button.addActionListener( new ActionListener() {

            // Add action listener to get most recently clicked button
            public void actionPerformed(ActionEvent e) {
              String location = e.getActionCommand();
              String[] values = location.split(",");
              int r, c;
              try {
                r = Integer.valueOf(values[0]);
                c = Integer.valueOf(values[1]);

                // User is trying to place a ship horizontally
                if (placeMode && GUI.this.checkBox.isHorizontal()) {
                  int boatListLength = GUI.this.boatList.size();
                  if (boatListLength-1 == 0) {
                    placeMode = false;
                  } else if (boatListLength == 0) {
                    return;
                  }
                  int boatSize = GUI.this.boatList.remove(boatListLength - 1);
                  if (checkLocationAndPlace(boatSize, r, c, true) == false) {
                    GUI.this.boatList.add(boatSize);
                  }
                }

                // User is trying to place a ship vertically
                else if (placeMode && !GUI.this.checkBox.isHorizontal()) {
                  int boatListLength = GUI.this.boatList.size();
                  if (boatListLength-1 == 0) {
                    placeMode = false;
                  } else if (boatListLength == 0) {
                    return;
                  }
                  int boatSize = GUI.this.boatList.remove(boatListLength - 1);
                  if (checkLocationAndPlace(boatSize, r, c, false) == false) {
                    GUI.this.boatList.add(boatSize);
                  }
                }

                // Clients move, choose a location in rightBoard and send coordinates
                else if (connected && connectToAnother && GUI.this.client.yourTurn && isRight) {
                  if (GUI.this.rightBoard.board[r][c].getBackground() != Color.BLACK) {
                    GUI.this.client.rowClicked = r;
                    GUI.this.client.colClicked = c;
                  }
                }

                // Servers move, choose a location in rightBoard and send coordinates
                else if (connected && !connectToAnother && GUI.this.server.yourTurn && isRight) {
                  if (GUI.this.rightBoard.board[r][c].getBackground() != Color.BLACK) {
                    GUI.this.server.rowClicked = r;
                    GUI.this.server.colClicked = c;
                  }
                }
              } catch(Exception exception) {
                exception.printStackTrace();
              }
            }
          });
          this.board[i][j] = button;
          add(this.board[i][j]);
        }
      }
    }

    /**
     *  Checks to see if all ships are sunk on the board
     *  @param None
     *  @return true if all ships are sunk, false otherwise
     */
    public boolean lost() {
      for (int i = 0; i < numRows; i++) {
        for (int j = 0; j < numCols; j++) {
          if (board[i][j].getBackground() == Color.BLACK) {
            continue;
          } else if (board[i][j].getBackground() == Color.LIGHT_GRAY) {
            continue;
          } else {
            return false;
          }
        }
      }
      return true;
    }

    /**
     *  This method allows click access to the right board
     *  when the user needs to place moves.
     *  @param None
     *  @return None
     */
    public void setRight() {
      this.isRight = true;
    }

    /**
     *  End the ship placement mode of the board
     *  @param place - true if user can place ships, false otherwise
     *  @return None
     */
    public void placeShips(boolean place) { this.placeMode = place; }

    /**
     *  Returns whether the user is allowed to place ships or not
     *  @param None
     *  @return true if player can place ships, false otherwise
     */
    public boolean getPlaceMode() {
      return this.placeMode;
    }

    /**
     *  Check to see whether a ship exists at a given location
     *  @param r - the row in the grid
     *  @param c - the column in the grid
     *  @return true if ship exists at that location, false otherwise
     */
    public boolean shipExists(int r, int c) {
      if (this.board[r][c].getBackground() == Color.LIGHT_GRAY) {
        return false;
      }
      return true;
    }

    /**
     *  Change the color of a button at a certain location
     *  @param r - the row in the grid
     *  @param c - the column in the grid
     *  @param color - the new color for the button at location (r, c)
     *  @return None
     */
    public void changeButtonColor(int r, int c, Color color) {
      this.board[r][c].setBackground(color);
    }

    /**
     *  Change the color of all ships on the board
     *  @param color - a new color for the grid
     *  @return None
     */
    public void changeColor(Color color) {
      for (int r = 0; r < numRows; r++) {
        for (int c = 0; c < numCols; c++) {
          if (this.board[r][c].getBackground() != Color.LIGHT_GRAY) {
            this.board[r][c].setBackground(color);
          }
        }
      }
    }

    /**
     *  Clear the current board, add the game boats to a list,
     *  and reset the color of the boat to a light gray
     *  @param None
     *  @return None
     */
    public void resetBoard() {
      this.boats.clear();
      this.boats = new LinkedList<Integer>(GUI.this.boatList);
      for (int r = 0; r < numRows; r++) {
        for (int c = 0; c < numCols; c++) {
          this.board[r][c].setBackground(Color.LIGHT_GRAY);
        }
      }
    }
  }


// ============================================================================
// ============================================================================


public class GameMenuBar extends JPanel {

  protected JMenuBar menuBar;
  protected JMenuBar toolBar;
  protected JMenu game;
  private boolean sentBoard;

  /**
   *  Constructor for the menu bar at the top of the GUI
   *  @param None
   *  @return None
   */
  public GameMenuBar() {
    this.sentBoard = false;

    // Configure the menu bar items
    setLayout(new FlowLayout());
    this.menuBar = new JMenuBar();
    this.toolBar = new JMenuBar();
    this.game = new JMenu("Game");
    this.game.setMnemonic(KeyEvent.VK_A);
    JMenuItem play = new JMenuItem("Place Ships");
    JMenuItem network = new JMenuItem("Network Settings");
    JMenuItem time = new JMenuItem("Countdown Time");

    // Configure an action to allow the users to place boats
    GUI.this.boatList = new LinkedList<Integer>();
    play.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (GUI.this.leftBoard.donePlacing) {
          JOptionPane.showMessageDialog(null, "The game is already in play...");
          return;
        }
        leftBoard.placeShips(true);
        leftBoard.resetBoard();
        int numBoats = 0;
        for (int i = 2; i < 7; i++) {
          for (int j = boardData[i]; j > 0; j--) {
            boatList.add(boardData[i+5]);
            numBoats++;
          }
        }
        shipInfo = new int[numBoats][4];
        shipInfoSize = 0;
      }
    });

    // Configure an action listener for network settings
    network.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (connected) {
          JOptionPane.showMessageDialog(null, "There is already an established connection...");
          return;
        }
        GUI.this.networkPanel = new NetworkDialogPanel();
        String[] options = { "Submit" };
        int optionType = JOptionPane.DEFAULT_OPTION;
        int messageType = JOptionPane.PLAIN_MESSAGE;
        int reply = JOptionPane.showOptionDialog(null, networkPanel, "Configure network settings", optionType,
                                messageType, null, options, options[0]);

        if (reply == -1) {
          return;
        } else if (reply == 0) {
          GUI.this.portNumber = networkPanel.getPort();
          GUI.this.host = networkPanel.getInetAddress();
        }
      }
    });

    // Configure an action listener for the timer
    time.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (connected) {
          JOptionPane.showMessageDialog(null, "The game is already in play...");
          return;
        }
        GetTime newTime = new GetTime();
        String[] options = { "Submit" };
        int optionType = JOptionPane.DEFAULT_OPTION;
        int messageType = JOptionPane.PLAIN_MESSAGE;
        int reply = JOptionPane.showOptionDialog(null, newTime, "Enter a new countdown time",
                                optionType, messageType, null, options, options[0]);
        if (reply == 0) {
          timer.updateTime(newTime.getTime());
        }
      }
    });

    // Create a JComboBox to take in user selected colors
    String[] colors = new String[] { "Red", "Blue", "White", "Yellow", "Green" };
    JComboBox<String> colorSelector = new JComboBox<String>(colors);
    colorSelector.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        String selectedColor = (String) colorSelector.getSelectedItem();
        Color c;
        if (selectedColor.equals("Green"))
          c = Color.GREEN;
        else if (selectedColor.equals("Red"))
          c = Color.RED;
        else if (selectedColor.equals("White"))
          c = Color.WHITE;
        else if (selectedColor.equals("Yellow"))
          c = Color.YELLOW;
        else
          c = Color.BLUE;
        GUI.this.leftBoard.changeColor(c);
        GUI.this.rightBoard.changeColor(c);
        GUI.this.color = c;
      }
    });

    // Add a JButton to know when the user is done placing ships
    JButton readyToPlay = new JButton();
    readyToPlay.setText("Done Placing Ships");
    readyToPlay.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (sentBoard) {
          JOptionPane.showMessageDialog(null, "You have already sent your board!");
          return;
        }
        // Calculate the number of boats
        int numBoats = 0;
        for (int i = 2; i < 7; i++) {
          for (int j = boardData[i]; j > 0; j--) {
            numBoats++;
          }
        }
        // If there are still boats to be placed, tell user
        if (GUI.this.shipInfoSize < numBoats) {
          JOptionPane.showMessageDialog(null, "Place all ships on the board first!");
          return;
        }
        // If not, then send board to the other player
        else if (!GUI.this.leftBoard.getPlaceMode()) {
          GUI.this.leftBoard.donePlacing = true;
          if (GUI.this.connectToAnother == true) {
            sentBoard = true;
            GUI.this.client.clientSendBoard();
          } else if (GUI.this.connected) {
            JOptionPane.showMessageDialog(null, "The game is already in play!");
          } else {
            sentBoard = true;
            waitToConnect();
          }
        }
        // If something unexpected happens, the game is probably in play
        else {
          JOptionPane.showMessageDialog(null, "Something unexepected happened!");
        }
      }
    });

    // Add a JButton to send move to an opponent on click
    JButton sendShotButton = new JButton("Send Shot");
    sendShotButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (connectToAnother) {
          if (!connected) {
            JOptionPane.showMessageDialog(null, "Wait until you are connected!");
            return;
          } else if (!GUI.this.client.yourTurn) {
            JOptionPane.showMessageDialog(null, "Wait for your opponent to make their move!");
            return;
          } else if (GUI.this.client.rowClicked == -1 || GUI.this.client.colClicked == -1) {
            JOptionPane.showMessageDialog(null, "Click a location on the bottom board first!");
            return;
          } else {
            GUI.this.client.sendMove = true;
          }
        } else {
          if (!connected) {
            JOptionPane.showMessageDialog(null, "Wait until you are connected!");
            return;
          } else if (!GUI.this.server.yourTurn) {
            JOptionPane.showMessageDialog(null, "Wait for your opponent to make their move!");
            return;
          } else if (GUI.this.server.rowClicked == -1 || GUI.this.server.colClicked == -1) {
            JOptionPane.showMessageDialog(null, "Click a location on the bottom board first!");
            return;
          } else {
            GUI.this.server.sendMove = true;
          }
        }
      }
    });

    // Add components to the menu bar
    this.game.add(play);
    this.game.add(network);
    this.game.add(time);
    if (GUI.this.connectToAnother) {
      JLabel label = new JLabel("Player 2");
      this.add(label);
      label.setBorder(new EmptyBorder(0, 0, 0, 40));
    } else {
      JLabel label = new JLabel("Player 1");
      this.add(label);
      label.setBorder(new EmptyBorder(0, 0, 0, 40));
    }
    if (timer == null)    // If not loaded from config
      timer = new TimerPanel(30);
    if (connectToAnother) // Client time also needs a value
      timer.updateTime(GUI.this.clientTime);

    this.menuBar.add(game);
    this.toolBar.add(colorSelector);
    this.toolBar.add(new JMenuItem());
    this.toolBar.add(readyToPlay);
    this.toolBar.add(new JMenuItem());
    this.toolBar.add(sendShotButton);
    add(toolBar);
    add(timer);
  }
}


// ============================================================================
// ============================================================================


  /**
   *  Driving method for the GUI and game
   *  @param args - all are ignored
   *  @return None
   */
  public static void main(String[] args) {
    EventQueue.invokeLater(() -> {
      GUI battleshipGUI = new GUI();
      battleshipGUI.createAndShowGUI();
    });
  }
}
