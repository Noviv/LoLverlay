package io.github.noviv.lolverlay.ni;

import java.util.function.Consumer;

public class JNAWindowFocusTracker {

    private Consumer<String> callback;

    public JNAWindowFocusTracker(Consumer<String> cb) {
        callback = cb;
    }
}
