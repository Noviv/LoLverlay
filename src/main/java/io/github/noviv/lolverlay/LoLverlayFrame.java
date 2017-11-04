package io.github.noviv.lolverlay;

import java.awt.Color;
import javax.swing.JFrame;

public class LoLverlayFrame extends JFrame {

    private boolean trigger;

    public LoLverlayFrame() {
        setUndecorated(true);
        setSize(640, 480);
        getContentPane().setBackground(Color.RED);
        setOpacity(0.5f);
    }

    public void trigger() {
        getContentPane().setBackground(trigger ? Color.RED : Color.GREEN);
        trigger = !trigger;
    }
}
