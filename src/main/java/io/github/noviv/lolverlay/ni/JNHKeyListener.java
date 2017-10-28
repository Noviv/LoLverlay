package io.github.noviv.lolverlay.ni;

import java.util.function.Consumer;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;

public class JNHKeyListener implements NativeKeyListener {

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
