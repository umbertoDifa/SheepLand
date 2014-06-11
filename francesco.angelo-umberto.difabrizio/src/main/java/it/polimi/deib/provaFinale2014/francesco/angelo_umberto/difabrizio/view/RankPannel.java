package it.polimi.deib.provaFinale2014.francesco.angelo_umberto.difabrizio.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 *
 * @author Francesco
 */
public class RankPannel extends JPanel {

    private final JLabel textLabel;
    private final Image image;

    public RankPannel(String text) {
        this.textLabel = new JLabel(text);
        this.setBackground(new Color(0, 0, 0, 0));
        if (text.contains("vinto")) {
            image = ImagePool.getByName("cup");
        } else {
            image = ImagePool.getByName("lose");
        }
        this.setPreferredSize(new Dimension(326, 444));
        this.setLayout(null);
        this.add(textLabel);
        Font biggerFont = FontFactory.getFont().deriveFont(new Float(30.0));
        textLabel.setFont(biggerFont);
        textLabel.setSize(new Dimension(326, 50));
        textLabel.setBounds(33, 31, 326, 50);
        this.setBounds(235, 160, 326, 444);
    }

    @Override
    protected void paintComponent(Graphics g) {
        g.drawImage(image, 0, 0, new Color(0, 0, 0, 0), this);
        super.paintComponent(g);
    }

}
