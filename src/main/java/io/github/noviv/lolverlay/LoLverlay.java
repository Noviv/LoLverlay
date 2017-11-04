package io.github.noviv.lolverlay;

import io.github.noviv.lolverlay.ni.NativeInterface;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import org.jnativehook.keyboard.NativeKeyEvent;

public class LoLverlay {

    private LoLverlayFrame frame;

    public LoLverlay() {
        initFrame();

        initNative();
    }

    private void initFrame() {
        frame = new LoLverlayFrame();
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent we) {
                NativeInterface.destroy();
            }
        });
    }

    private void initNative() {
        NativeInterface.init();

        NativeInterface.setGlobalKeyListener((keys) -> {
            if (keys[NativeKeyEvent.VC_ESCAPE]) {
                frame.dispatchEvent(new WindowEvent(frame,
                        WindowEvent.WINDOW_CLOSING));
            }
        });

        NativeInterface.setGlobalWindowFocusListener(str -> System.out.println("window: " + str));
    }

    public static void main(String[] args) {
        LoLverlay overlay = new LoLverlay();
    }
}
