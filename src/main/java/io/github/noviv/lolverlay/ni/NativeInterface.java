package io.github.noviv.lolverlay.ni;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Ole32;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.platform.win32.WinUser;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyListener;

public class NativeInterface {

    public interface User32Ex extends User32 {

        User32Ex INSTANCE = (User32Ex) Native.loadLibrary("user32", User32Ex.class, W32APIOptions.UNICODE_OPTIONS);

        //dw flags
        public static int WINEVENT_OUTOFCONTEXT = 0x0000;
        public static int WINEVENT_INCONTEXT = 0x0004;
        public static int WINEVENT_SKIPOWNPROCESS = 0x0002;
        public static int WINEVENT_SKIPOWNTHREAD = 0x0001;

        //event constants
        public static int EVENT_SYSTEM_MINIMIZEEND = 0x0017;
        public static int EVENT_SYSTEM_MINIMIZESTART = 0x0016;
        public static int EVENT_SYSTEM_FOREGROUND = 0x0003;

        public WinNT.HANDLE SetWinEventHook(int eventMin, int eventMax, Pointer hmodWinEventProc, WinEventProc lpfnWinEventProc,
                int idProcess,
                int idThread,
                int dwflags);

        public boolean UnhookWinEvent(WinNT.HANDLE handle);

        public static interface WinEventProc extends StdCallLibrary.StdCallCallback {

            void callback(
                    WinNT.HANDLE hWinEventHook,
                    int event,
                    WinDef.HWND hwnd,
                    int idObject,
                    int idChild,
                    int dwEventThread,
                    int dwmsEventTime
            );
        }
    }

    private static NativeKeyListener currKeyListener;
    private static boolean winHookRunning;
    private static WinNT.HANDLE winHook;
    private static Thread winHookThread;

    private NativeInterface() {
    }

    public static void init() {
        System.out.println("initializing native interface");

        Logger l = Logger.getLogger(GlobalScreen.class.getPackage().getName());
        l.setLevel(Level.WARNING);
        l.setUseParentHandlers(false);

        try {
            GlobalScreen.registerNativeHook();
        } catch (NativeHookException nhe) {
            System.err.println("Error native hooking: " + nhe);
            System.exit(0);
        }

        User32Ex.WinEventProc testproc = (hWinEventHook,
                event, hwnd, idObject, idChild,
                dwEventThread, dwmsEventTime) -> {
            char[] buf = new char[1024 * 2];
            User32.INSTANCE.GetWindowText(User32.INSTANCE.GetForegroundWindow(), buf, 1024);
            System.out.println("active title: " + Native.toString(buf));
        };

        winHookThread = new Thread(() -> {
            winHook = User32Ex.INSTANCE.SetWinEventHook(
                    0x0003,
                    0x0003,
                    Pointer.NULL, testproc, 0, 0,
                    2);
            winHookRunning = true;

            WinUser.MSG msg = new WinUser.MSG();
            while (winHookRunning) {
                if (User32.INSTANCE.PeekMessage(msg, null, 0, 0, 0)) {
                    User32.INSTANCE.TranslateMessage(msg);
                    User32.INSTANCE.DispatchMessage(msg);
                }
                try {
                    Thread.sleep(50);
                } catch (InterruptedException ex) {
                    System.err.println("sleep err: " + ex);
                }
            }
        });
        winHookThread.setPriority(Thread.MIN_PRIORITY);
        winHookThread.start();
    }

    public static void close() {
        System.out.println("closing native interface");

        winHookRunning = false;
        try {
            winHookThread.join(500);
        } catch (InterruptedException ex) {
            System.err.println("joining error: " + ex);
        }

        if (winHook != null) {
            User32.INSTANCE.UnhookWinEvent(winHook);
        }

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
