/**
 *  @author Chris Turgeon
 *  @version 1.0
 */

package code.tools;
import javax.swing.*;

public class GetTime extends JPanel {

  private static final JLabel label = new JLabel("Enter a time: ");
  private JTextField textField;

  /**
   *  Constructor for the JPanel to retrieve the countdown timer value
   *  entered by the user
   *  @param None
   */
  public GetTime() {
    this.add(label);
    this.textField = new JTextField("30", 6);
    this.add(textField);
  }

  /**
   *  Retrieve the time value entered by the user
   *  @param None
   *  @return the time entered by the user if it is valid, 30 otherwise
   */
  public int getTime() {
    try {
      if (this.textField != null) {
        String t = this.textField.getText();
        int time = Integer.valueOf(t);
        if (time < 5) {
          JOptionPane.showMessageDialog(null, "That is too fast...");
          return 30;
        } else {
          return time;
        }
      } else {
        JOptionPane.showMessageDialog(null, "Using default time 30 seconds");
        return 30;
      }
    } catch(Exception e) {
      JOptionPane.showMessageDialog(null, "Invalid time... using default 30s");
      return 30;
    }
  }
}
