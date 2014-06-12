package it.polimi.deib.provaFinale2014.francesco.angelo_umberto.difabrizio.view;

import it.polimi.deib.provaFinale2014.francesco.angelo_umberto.difabrizio.utility.DebugLogger;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 *
 * @author Francesco
 */
public class Action extends BackgroundAndTextJPanel implements MouseListener {
    private final String toolTipText;
    
    public Action(String toolTipText) {
        setOpacity(true);
        this.setBackground(new Color(0, 0, 0, 0));
        this.addMouseListener(this);
        this.toolTipText = toolTipText;
        this.setToolTipText(toolTipText);
    }

    /**
     * se ok l'img viene visualizzata non opaca
     *
     * @param ok
     */
    protected void setOpaqueView(boolean ok) {
        super.setOpacity(ok);
    }

    public void mouseClicked(MouseEvent e) {
        DebugLogger.println("azione clickata, dentro la catch dell evento");
        revalidate();
        repaint();
    }

    public void mousePressed(MouseEvent e) {
        //not used
    }

    public void mouseReleased(MouseEvent e) {
        //not used
    }

    public void mouseEntered(MouseEvent e) {
        this.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    public void mouseExited(MouseEvent e) {
        this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    }

}
