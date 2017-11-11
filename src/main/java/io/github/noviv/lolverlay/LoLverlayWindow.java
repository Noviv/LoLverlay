package io.github.noviv.lolverlay;

import java.awt.GraphicsEnvironment;
import javax.swing.JDialog;

public class LoLverlayWindow extends JDialog {

    public LoLverlayWindow() {
        setUndecorated(true);
        setBounds(GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds());
        setOpacity(0.6f);
    }
}
