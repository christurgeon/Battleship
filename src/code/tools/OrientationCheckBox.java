/**
 *  @author Chris Turgeon
 *  @version 1.0
 */

package code.tools;
import javax.swing.*;
import java.awt.event.*;

public class OrientationCheckBox extends JPanel {

  private ButtonGroup group;
  private boolean isHorizontal;
  private String orientation;

  /**
   *  The constructor for a check box that determines if ship
   *  placement should be horizontal or vertical
   *  @param None
   *  @return None
   */
  public OrientationCheckBox() {

    // Add the two choices for orientation
    this.group = new ButtonGroup();
    JRadioButton horizontalBtn = new JRadioButton("Horizontal");
    JRadioButton verticalBtn = new JRadioButton("Vertical");
    horizontalBtn.setSelected(true);
    this.group.add(horizontalBtn);
    this.group.add(verticalBtn);
    this.isHorizontal = true;

    // Register listeners for the radio buttons and add to panel
    horizontalBtn.addActionListener( new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        isHorizontal = true;
      }
    });
    verticalBtn.addActionListener( new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        isHorizontal = false;
      }
    });
    this.add(horizontalBtn);
    this.add(verticalBtn);
  }

  /**
   *  Returns whether horizontal mode is checked
   *  @param None
   *  @return a boolean, true if horizontal. false if vertical
   */
  public boolean isHorizontal() {
    return (this.isHorizontal == true);
  }

  /**
   *  Returns whether vertical mode is checked
   *  @param None
   *  @return a boolean, true if vertical, false if horizontal
   */
  public boolean isVertical() {
    return (this.isHorizontal == false);
  }

}
