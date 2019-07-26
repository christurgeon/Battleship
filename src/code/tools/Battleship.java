/**
 *  @author Chris Turgeon
 *  @version 1.0
 */

package code.tools;

public class Battleship {

  private int numRows;
  private int numCols;
  private int numBoats;
  private int aliveCount;   // Number of places still left on board

  private int[][] shipInfo; // id -> [size][orientation][start_r][start_c]
  private int[][] board;

  /**
   *  Create a board and place all of the boats
   *  @param numRows - number of rows
   *  @param numCols - number of columns
   *  @param numBoats - number of boats
   *  @param shipInfo - location, size, and orientation of every boat
   *  @return None
   */
  public Battleship(int numRows, int numCols, int numBoats, int[][] shipInfo) {
    // Set variables and initialize empty board
    this.numRows = numRows;
    this.numCols = numCols;
    this.numBoats = numBoats;
    this.board = new int[numRows][numCols];
    for (int i = 0; i < numRows; i++) {
      for (int j = 0; j < numCols; j++) {
        board[i][j] = 0;
      }
    }
    // Create the board
    this.aliveCount = 0;
    this.shipInfo = new int[numBoats][4];
    for (int i = 0; i < numBoats; i++) {
      this.shipInfo[i][0] = shipInfo[i][0];
      this.shipInfo[i][1] = shipInfo[i][1];
      this.shipInfo[i][2] = shipInfo[i][2];
      this.shipInfo[i][3] = shipInfo[i][3];
      aliveCount += shipInfo[i][0];
      int id = i + 1;
      placeShip(shipInfo[i][0], shipInfo[i][1], shipInfo[i][2], shipInfo[i][3], id);
    }
  }

  /**
   *  Reset the board and ship information
   *  @param None
   *  @return None
   */
  public void clearShipData() {
    this.shipInfo = new int[numBoats][4];
  }

  /**
   *  Take in a location of a shot, change the board accordingly,
   *  then return with information for the GUI.
   *  @param row - the row in the grid of the shot
   *  @param col - the col in the grid of the shot
   *  @return an int[] with information about the status of the shot
   *  Indexes and their correspondence:
   *  0 --> 0 if horizontal, 1 if vertical
   *  1 --> starting loop idx
   *  2 --> ending loop idx
   *  3 --> offset in row or column
   *  4 --> hit status (-1 if no hit, 0 if hit, 1 if sinking hit)
   *  5 --> the number that are alive
   */
  public int[] setAndGetHitInfo(int row, int col) {
    int id = this.board[row][col];
    int[] returnData = { -1, -1, -1, -1, -1, -1 };
    if (id == -1 || id == 0) {
      return returnData;
    }
    id--;
    this.aliveCount--;
    returnData[5] = aliveCount;
    returnData[0] = shipInfo[id][1];
    if (returnData[0] == 0) { // if horizontal
      int start = returnData[1] = shipInfo[id][3];
      int end = returnData[2] = shipInfo[id][3] + shipInfo[id][0];
      returnData[3] = shipInfo[id][2];
      int r = shipInfo[id][2];
      for (int i = start; i < end; i++) {
        if (this.board[r][i] == id+1) {
          returnData[4] = 0;
          this.board[row][col] = 0;
          return returnData;
        }
      }
    } else { // if vertical
      int start = returnData[1] = shipInfo[id][2];
      int end = returnData[2] = shipInfo[id][2] + shipInfo[id][0];
      returnData[3] = shipInfo[id][3];
      int c = shipInfo[id][3];
      for (int i = start; i < end; i++) {
        if (this.board[i][c] == id+1) {
          returnData[4] = 0;
          this.board[row][col] = 0;
          return returnData;
        }
      }
    }
    returnData[4] = 1;
    this.board[row][col] = 0;
    return returnData;
  }

  /**
   *  Method to place a ship and its relevant information
   *  -- PRIVATE --
   */
  private void placeShip(int size, int orientation, int row, int col, int id) {
    if (orientation == 0) { // horizontal
      for (int i = col; i < col + size; i++) {
        this.board[row][i] = id;
      }
    } else { // vertical
      for (int i = row; i < row + size; i++) {
        this.board[i][col] = id;
      }
    }
  }

  /**
   *  Print the 2D board
   *  @param None
   *  @return None
   */
  public void print() {
    for (int r = 0; r < numRows; r++) {
      for (int c = 0; c < numCols; c++) {
        System.out.printf("%d ", board[r][c]);
      }
      System.out.println();
    }
    System.out.println("\n\n");
  }
}
