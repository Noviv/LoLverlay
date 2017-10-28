package io.github.noviv.lolverlay;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.WindowAdapter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.stage.WindowEvent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;

public class LoLverlay {

    private JFrame frame;

    public LoLverlay() {
        initNative();

        initGraphics();
    }

    private void initNative() {
        NativeInterface.init();

        NativeInterface.setJNHKeyListener((keys) -> {
            if (keys[NativeKeyEvent.VC_ESCAPE]) {
                System.exit(0);
            }
        });
    }

    private void initGraphics() {
        frame = new JFrame();
        frame.setUndecorated(true);
        frame.setBackground(new Color(0, 0, 0, 0));
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {
                NativeInterface.close();
            }
        });

        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(new JLabel("text field north"), java.awt.BorderLayout.NORTH);
        frame.setVisible(true);
        frame.pack();
    }

    public static void main(String[] args) {
        LoLverlay overlay = new LoLverlay();
    }
}
