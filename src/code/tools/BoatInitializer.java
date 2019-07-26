/**
 *  @author Chris Turgeon
 *  @version 1.0
 */

package code.tools;
import java.util.HashMap;
import javax.swing.*;

public class BoatInitializer extends JPanel {

  private int reply;
  private static final int COLS = 4;
  private int height, width, numCarriers, numCruisers, numBships, numSubmarines, numDestroyers;
  private int carrierSize, cruiserSize, bshipSize, subSize, destroyerSize;
  private HashMap<String, JTextField> labelFieldMap = new HashMap<String, JTextField>();
  private static final String[] LABEL_TEXTS = { "Board Height ","Board Width ","Number of Carriers ",
                                                "Carrier Size ","Number of Battleships ","Battleship Size ",
                                                "Number of Cruisers ","Cruiser Size ","Number of Submarines ",
                                                "Submarine Size ","Number of Destroyers ","Destroyer Size " };

  /**
   *  Creates a window to take in the information of the boats
   *  used in the game
   *  @param None
   *  @return None
   */
  public BoatInitializer() {
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    setBorder(BorderFactory.createTitledBorder("Configure the initial board..."));
    for (int i = 0; i < LABEL_TEXTS.length; i++) {
       String msgPrompt = LABEL_TEXTS[i];
       add(new JLabel(msgPrompt));
       JTextField textField = new JTextField(COLS);
       labelFieldMap.put(msgPrompt, textField);
       add(textField);
    }
    String[] options = { "Submit" };
    int optionType = JOptionPane.DEFAULT_OPTION;
    int messageType = JOptionPane.PLAIN_MESSAGE;
    int reply = JOptionPane.showOptionDialog(null, this, "Game Settings",
                optionType, messageType, null, options, options[0]);
    this.reply = reply;
    if (reply == 0) {
      try {
        this.height = Integer.valueOf(labelFieldMap.get("Board Height ").getText());
        this.width = Integer.valueOf(labelFieldMap.get("Board Width ").getText());
        this.numCarriers = Integer.valueOf(labelFieldMap.get("Number of Carriers ").getText());
        this.numCruisers = Integer.valueOf(labelFieldMap.get("Number of Battleships ").getText());
        this.numBships = Integer.valueOf(labelFieldMap.get("Number of Cruisers ").getText());
        this.numSubmarines = Integer.valueOf(labelFieldMap.get("Number of Submarines ").getText());
        this.numDestroyers = Integer.valueOf(labelFieldMap.get("Number of Destroyers ").getText());
        this.carrierSize = Integer.valueOf(labelFieldMap.get("Carrier Size ").getText());
        this.cruiserSize = Integer.valueOf(labelFieldMap.get("Cruiser Size ").getText());
        this.bshipSize = Integer.valueOf(labelFieldMap.get("Battleship Size ").getText());
        this.subSize = Integer.valueOf(labelFieldMap.get("Submarine Size ").getText());
        this.destroyerSize = Integer.valueOf(labelFieldMap.get("Destroyer Size ").getText());
      } catch(Exception e) {
        this.reply = -1;
      }
    }
  }

  /**
   *  Returns configuration settings that the user set.
   *  @param None
   *  @return an integer array with the height, width, and the number
   *          of each boat that the game will have and their sizes
   */
  public int[] getGameAttributes() {
    int[] attributes = new int[12];
    attributes[0] = height;
    attributes[1] = width;
    attributes[2] = numCarriers;
    attributes[3] = numCruisers;
    attributes[4] = numBships;
    attributes[5] = numSubmarines;
    attributes[6] = numDestroyers;
    attributes[7] = carrierSize;
    attributes[8] = cruiserSize;
    attributes[9] = bshipSize;
    attributes[10] = subSize;
    attributes[11] = destroyerSize;
    return attributes;
  }

  /**
   *  Validates input taken in from the user
   *  @param None
   *  @return true - if the user entered information is correct,
   *                 false otherwise
   */
  public boolean validInput() {
    if (reply == -1) {
      JOptionPane.showMessageDialog(null, "You must provide all input to begin the game [CTRL-C to abandon game]");
      return false;
    }
    if (height < 5 || width < 5) {
      JOptionPane.showMessageDialog(null, "Both height and width must be larger than 5");
      return false;
    }
    if (numCarriers < 0 || numBships < 0 || numCruisers < 0 || numDestroyers < 0 || numSubmarines < 0) {
      JOptionPane.showMessageDialog(null, "Cannot have a negative number of a certain vessel");
      return false;
    }
    if (carrierSize < 0 || bshipSize < 0 || cruiserSize < 0 || destroyerSize < 0 || subSize < 0) {
      JOptionPane.showMessageDialog(null, "Cannot have negative board sizes");
      return false;
    }
    if (carrierSize + bshipSize + cruiserSize + destroyerSize + subSize == 0) {
      JOptionPane.showMessageDialog(null, "Cannot have no boats");
      return false;
    }
    boolean tooLargeHeight = carrierSize > height || bshipSize > height || cruiserSize > height ||
                             destroyerSize > height || subSize > height;
    boolean tooLargeWidth = carrierSize > width || bshipSize > width || cruiserSize > width ||
                            destroyerSize > width || subSize > width;
    if (tooLargeWidth && tooLargeHeight) {
      JOptionPane.showMessageDialog(null, "One or more of the boat sizes is too large");
      return false;
    }
    if (carrierSize*numCarriers + bshipSize*numBships + cruiserSize*numCruisers +
        destroyerSize*numDestroyers + subSize*numSubmarines > height*width) {
      JOptionPane.showMessageDialog(null, "Too many vessels for the board");
      return false;
    }
    return true;
  }
}
