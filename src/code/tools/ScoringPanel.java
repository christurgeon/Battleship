/**
 *  @author Chris Turgeon
 *  @version 1.0
 */

package code.tools;
import javax.swing.*;
import java.awt.GridLayout;

public class ScoringPanel extends JPanel {

  JLabel playerOne, playerOneScore;
  JLabel playerTwo, playerTwoScore;

  /**
   *  Create a JPanel to hold the scores of each player
   *  @param None
   */
  public ScoringPanel() {
    setLayout(new GridLayout(2, 2));
    this.playerOne = new JLabel("Player One:  ");
    this.playerTwo = new JLabel("Player Two:  ");
    this.playerOneScore = new JLabel("0");
    this.playerTwoScore = new JLabel("0");
    add(playerOne); add(playerOneScore);
    add(playerTwo); add(playerTwoScore);
  }

  /**
   *  Increment player one's score
   *  @param None
   *  @return None
   */
  public void updatePlayerOneScore() {
    try {
      String s = playerOneScore.getText();
      int currentScore = Integer.valueOf(s) + 1;
      playerOneScore.setText(s);
    } catch(Exception e) {
      playerOneScore.setText("-1");
    }
  }

  /**
   *  Increment player two's score
   *  @param None
   *  @return None
   */
  public void updatePlayerTwoScore() {
    try {
      String s = playerTwoScore.getText();
      int currentScore = Integer.valueOf(s) + 1;
      playerTwoScore.setText(s);
    } catch(Exception e) {
      playerTwoScore.setText("-1");
    }
  }
}
