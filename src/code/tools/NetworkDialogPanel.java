/**
 *  @author Chris Turgeon
 *  @version 1.0
 */

package code.tools;
import java.awt.GridBagLayout;
import javax.swing.*;

public class NetworkDialogPanel extends JPanel {

  private JTextField textField1;
  private JTextField textField2;

  /**
   *  Create a panel to take in network information
   *  @param None
   */
  public NetworkDialogPanel() {
    setLayout(new GridBagLayout());
    add(new JLabel("Port Number: "));
    this.textField1 = new JTextField("25000", 6);
    add(textField1);
    add(new JLabel(" IP Address: "));
    this.textField2 = new JTextField("localhost", 6);
    add(textField2);
  }

  /**
   *  Returns the internet address of the host entered by the user
   *  @param None
   *  @return the internet address of the host as a string
   */
  public String getInetAddress() {
    try {
      if (this.textField2 != null) {
        return this.textField2.getText();
      } else {
        JOptionPane.showMessageDialog(null, "No address input, using default \"localhost\"");
        return "localhost";
      }
    } catch(Exception e) {
      JOptionPane.showMessageDialog(null, "Invalid input for IP address");
      return null;
    }
  }

  /**
   *  Returns the port number for the sockets, uses default 25000 if error
   *  @param None
   *  @return the port number entered by the user
   */
  public int getPort() {
    try {
      if (this.textField1 != null) {
        String number = this.textField1.getText();
        int port = Integer.valueOf(number);
        if (port < 1) {
          JOptionPane.showMessageDialog(null, "Port number is too low, using default [25000]");
          return 25000;
        } else {
          return port;
        }
      } else {
        JOptionPane.showMessageDialog(null, "Using default port [25000]");
        return 25000;
      }
    } catch(Exception e) {
      JOptionPane.showMessageDialog(null, "Invalid port number. Using default [25000]");
      return 25000;
    }
  }
}
