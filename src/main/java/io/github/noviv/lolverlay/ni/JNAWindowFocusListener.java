package io.github.noviv.lolverlay.ni;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.platform.win32.WinUser;
import java.util.function.Consumer;

public class JNAWindowFocusListener {

    private final Consumer<String> callback;
    private Thread winHookThread;
    private boolean winHookRunning;
    private WinNT.HANDLE winHook;

    public JNAWindowFocusListener(Consumer<String> cb) {
        callback = cb;

        initWinHook();
    }

    private void initWinHook() {
        int EVENT_SYSTEM_FOREGROUND = 0x0003;
        int WINEVENT_SKIPOWNPROCESS = 0x0002;

        User32.WinEventProc testproc = (hWinEventHook,
                event, hwnd, idObject, idChild,
                dwEventThread, dwmsEventTime) -> {
            char[] buf = new char[1024 * 2];
            User32.INSTANCE.GetWindowText(User32.INSTANCE.GetForegroundWindow(), buf, 1024);
            callback.accept(Native.toString(buf));
        };

        winHookThread = new Thread(() -> {
            //default includes WINEVENT_OUTOFCONTEXT
            winHook = User32.INSTANCE.SetWinEventHook(
                    EVENT_SYSTEM_FOREGROUND,
                    EVENT_SYSTEM_FOREGROUND,
                    null, testproc, 0, 0,
                    WINEVENT_SKIPOWNPROCESS);
            winHookRunning = true;

            WinUser.MSG msg = new WinUser.MSG();
            while (winHookRunning) {
                while (User32.INSTANCE.PeekMessage(msg, null, 0, 0, 0)) {
                    User32.INSTANCE.TranslateMessage(msg);
                    User32.INSTANCE.DispatchMessage(msg);
                }

                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    System.err.println("thread sleep failed");
                }
            }
        }, "LoLverlay - WinHook Thread");
        winHookThread.setPriority(Thread.MIN_PRIORITY);
        winHookThread.start();
    }

    public void destroy() {
        winHookRunning = false;

        try {
            winHookThread.join(500);
        } catch (InterruptedException ex) {
            System.err.println("joining error: " + ex);
        }

        if (winHook != null) {
            User32.INSTANCE.UnhookWinEvent(winHook);
        }
    }
}
