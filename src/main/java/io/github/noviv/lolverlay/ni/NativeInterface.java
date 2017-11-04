package io.github.noviv.lolverlay.ni;

import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyListener;

public class NativeInterface {

    private static NativeKeyListener currKeyListener;
    private static JNAWindowFocusListener wfListener;

    private NativeInterface() {
    }

    /**
     * Call any methods required to interface with native system calls.
     */
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

    /**
     * Set a callback for global key events. Replaces and frees the current
     * callback, if it exists.
     *
     * @param callback Callback to handle the array representing pressed keys.
     */
    public synchronized static void setGlobalKeyListener(Consumer<boolean[]> callback) {
        if (callback == null) {
            if (currKeyListener != null) {
                GlobalScreen.removeNativeKeyListener(currKeyListener);
            }
        } else {
            currKeyListener = new JNHKeyListener(callback);
            GlobalScreen.addNativeKeyListener(currKeyListener);
        }
    }

    /**
     * Set a callback for global window focus change events. Replaces and frees
     * the current callback, if it exists.
     *
     * @param callback Callback to handle the name of the new currently active
     * window.
     */
    public synchronized static void setGlobalWindowFocusListener(Consumer<String> callback) {
        if (wfListener != null) {
            wfListener.destroy();
        }
        wfListener = new JNAWindowFocusListener(callback);
    }

    /**
     * Destroy all associated native and JVM resources. Essentially resets to be
     * like a new NativeInterface.
     */
    public synchronized static void destroy() {
        currKeyListener = null;

        if (wfListener != null) {
            wfListener.destroy();
            wfListener = null;
        }

        try {
            GlobalScreen.unregisterNativeHook();
        } catch (NativeHookException nhe) {
            System.err.println("Error native hooking: " + nhe);
            System.exit(0);
        }
    }
}
