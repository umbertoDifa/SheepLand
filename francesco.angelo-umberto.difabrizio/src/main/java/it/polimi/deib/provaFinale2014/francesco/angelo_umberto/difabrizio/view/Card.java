package it.polimi.deib.provaFinale2014.francesco.angelo_umberto.difabrizio.view;

import it.polimi.deib.provaFinale2014.francesco.angelo_umberto.difabrizio.utility.DebugLogger;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.JLabel;

/**
 * The class extends CardBoard. It adds a jlabel for the bank current value of
 * the card.
 *
 * @author Francesco
 */
public class Card extends CardBoard implements MouseListener {

    JLabel bankNum;

    /**
     * create the Card with inside a JLabel with the indicated font, text for
     * the bank value and number for the occurrence of the card. Add itself as
     * MouseListener for cursor effect
     *
     * @param font
     * @param bankNum
     * @param myCardNum
     */
    public Card(Font font, String bankNum, String myCardNum) {
        super(font, myCardNum);
        this.bankNum = new JLabel(bankNum);
        this.bankNum.setFont(FontFactory.getFont());
        this.bankNum.setForeground(Color.white);
        this.setBackground(new Color(0, 0, 0, 0));
        this.add(this.bankNum);
        this.bankNum.setBounds(10, 6, 28, 28);
        this.addMouseListener(this);
    }

    /**
     * set the bank value of the card
     *
     * @param amountToAdd
     */
    public void increase(int amountToAdd) {
        //calcolo nuovo ammount
        int newAmmount = Integer.parseInt(this.getText()) + amountToAdd;

        //setto il nuovo ammount
        this.setText(Integer.toString(newAmmount));
    }

    /**
     * se text of the card
     *
     * @param text
     */
    @Override
    public void setText(String text) {
        if (Integer.parseInt(text) < 0) {
            this.setUp(".\\images\\numFencesRed.png", 67, 77, 78, 94);
        }
        super.setText(text);
    }

    /**
     * debug method
     *
     * @param e
     */
    public void mouseClicked(MouseEvent e) {
        DebugLogger.println("carta terreno clickata, dentro la catch dell evento");
        repaint();
    }

    public void mousePressed(MouseEvent e) {

    }

    public void mouseReleased(MouseEvent e) {

    }

    /**
     * When the mouse enters, the cursor becames Hand
     *
     * @param e
     */
    public void mouseEntered(MouseEvent e) {
        this.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    /**
     * when the mouse exits the cursor return default cursor
     *
     * @param e
     */
    public void mouseExited(MouseEvent e) {
        this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    }
}
