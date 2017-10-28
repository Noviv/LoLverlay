package io.github.noviv.lolverlay;

import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;

public class NativeInterface {

    private static NativeKeyListener currKeyListener;

    private NativeInterface() {
    }

    public static void init() {
        Logger l = Logger.getLogger(GlobalScreen.class.getPackage().getName());
        l.setLevel(Level.WARNING);
        l.setUseParentHandlers(false);

        try {
            GlobalScreen.registerNativeHook();
        } catch (NativeHookException nhe) {
            System.err.println("Error native hooking: " + nhe);
            System.exit(0);
        }
    }

    public static void close() {
        try {
            GlobalScreen.unregisterNativeHook();
        } catch (NativeHookException nhe) {
            System.err.println("Error native hooking: " + nhe);
            System.exit(0);
        }
    }

    public static void setJNHKeyListener(Consumer<boolean[]> callback) {
        if (callback == null) {
            if (currKeyListener != null) {
                GlobalScreen.removeNativeKeyListener(currKeyListener);
            }
        } else {
            currKeyListener = new JNHKeyListener(callback);
            GlobalScreen.addNativeKeyListener(currKeyListener);
        }
    }
}

class JNHKeyListener implements NativeKeyListener {

    private boolean[] keys = new boolean[65536];
    private Consumer<boolean[]> callback;

    public JNHKeyListener(Consumer<boolean[]> cb) {
        callback = cb;
    }

    @Override
    public void nativeKeyTyped(NativeKeyEvent nke) {
        keys[nke.getKeyCode()] = true;
        callback.accept(keys);
    }

    @Override
    public void nativeKeyPressed(NativeKeyEvent nke) {
        keys[nke.getKeyCode()] = true;
        callback.accept(keys);
    }

    @Override
    public void nativeKeyReleased(NativeKeyEvent nke) {
        keys[nke.getKeyCode()] = false;
        callback.accept(keys);
    }

    public boolean keyOn(int key) {
        return keys[key];
    }
}
