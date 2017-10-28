package io.github.noviv.lolverlay;

import io.github.noviv.lolverlay.ni.NativeInterface;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JFrame;
import javax.swing.JLabel;
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
                frame.dispatchEvent(new WindowEvent(frame,
                        WindowEvent.WINDOW_CLOSING));
                System.exit(0);
            }
        });
    }

    private void initGraphics() {
        frame = new JFrame();
        frame.setUndecorated(true);
        frame.setBackground(new Color(0, 0, 0, 0));
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent we) {
                NativeInterface.close();
            }
        });
        frame.setAlwaysOnTop(true);

        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(new JLabel("text field north"), java.awt.BorderLayout.NORTH);
        frame.setVisible(true);
        frame.pack();
    }

    public static void main(String[] args) {
        LoLverlay overlay = new LoLverlay();
    }
}
