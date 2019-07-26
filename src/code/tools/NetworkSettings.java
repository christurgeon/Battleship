/**
 *  @author Chris Turgeon
 *  @version 1.0
 */

package code.tools;
import javax.swing.*;

public class NetworkSettings extends JPanel {

  private boolean connectToAnother;
  private String[] options = { "Connect to Another", "Wait for Player to Connect" };

  /**
   *  Constructor for a NetworkSettings JPanel that determines the
   *  connection settings for the user.
   *  @param None
   *  @return None
   */
  public NetworkSettings() {
    this.connectToAnother = false;
  }

  /**
   *  This method takes in whether the user wants to wait for another player
   *  or try to connect to another user.
   *  @param None
   *  @return None
   */
  public int getInput() {
    int optionType = JOptionPane.DEFAULT_OPTION;
    int messageType = JOptionPane.PLAIN_MESSAGE;
    int reply = JOptionPane.showOptionDialog(null, this, "Configure network settings",
                optionType, messageType, null, options, options[0]);
    if (reply == 0) {
      this.connectToAnother = true;
    } else if (reply == 1) {
      this.connectToAnother = false;
    }
    return reply;
  }

  /**
   *  This method returns a boolean that determines whether or not a player
   *  wants to wait or try to connect to another player.
   *  @param None
   *  @return true if game should connect to another player, false otherwise
   */
  public boolean connectToOtherPlayer() {
    return this.connectToAnother;
  }
}
