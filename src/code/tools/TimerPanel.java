/**
 *  @author Chris Turgeon
 *  @version 1.0
 */

package code.tools;
import javax.swing.*;
import java.awt.event.*;
import javax.swing.border.EmptyBorder;
import java.awt.GridBagLayout;

public class TimerPanel extends JPanel {

  private JLabel label1, label2;
  private Timer timer;
  private int seconds;
  private int secondsCopy;

  /**
   *  The constructfor the countdown timer JPanel
   *  @param time - the default time for the timer
   */
  public TimerPanel(int time) {
    this.seconds = time;
    this.secondsCopy = time;
    setLayout(new GridBagLayout());
    label1 = new JLabel("Timer: ");
    label2 = new JLabel(Integer.toString(time));
    label1.setBorder(new EmptyBorder(0, 40, 0, 0));
    add(label1);
    add(label2);
    configure();
  }

  /**
   *  Start the timer
   *  @param None
   *  @return None
   */
  public void startTimer() {
    timer.start();
  }

  /**
   *  Takes in a new value for seconds and sets the timer to it
   *  @param seconds - the number of seconds for each move
   *  @return None
   */
  public void updateTime(int seconds) {
    String s = Integer.toString(seconds);
    this.label2.setText(s);
    this.seconds = seconds;
    this.secondsCopy = seconds;
    configure();
  }

  /**
   *  Resets the timer and stops it
   *  @param None
   *  @return None
   */
  public void reset() {
    timer.stop();
    this.label2.setText( Integer.toString(this.secondsCopy) );
    this.seconds = this.secondsCopy;
    configure();
  }

  /**
   *  Accessor for currently set timer
   *  @param None
   *  @return the time of the timer
   */
  public int getTime() {
    return this.secondsCopy;
  }

  /**
   *  Create and intitialize the countdown timer
   *  @param None
   *  @return None
   */
  private void configure() {
    this.timer = new Timer(1000, new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        TimerPanel.this.seconds--;
        if (TimerPanel.this.seconds > 0) {
          label2.setText(Integer.toString(TimerPanel.this.seconds));
        } else {
          ((Timer) (e.getSource())).stop();
          JOptionPane.showMessageDialog(null, "You ran out of time... the game is over!");
          System.exit(1);
        }
      }
    });
  }
}
