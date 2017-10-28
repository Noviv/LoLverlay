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
    private static WinNT.HANDLE winHook;
    public static boolean cb = false;

    private NativeInterface() {
    }

    public static void init() {
        Ole32.INSTANCE.CoInitializeEx(null, 0);
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
//            char[] buf = new char[1024 * 2];
//            User32.GetWindowTextW(User32.GetForegroundWindow(), buf, 1024);
//            System.out.println("active title: " + Native.toString(buf));
            System.out.println("event proc");
            cb = true;
        };

        if (Native.getLastError() != 0) {
            System.err.println("error");
        }

        winHook = User32Ex.INSTANCE.SetWinEventHook(
                0x0003,
                0x0003,
                Pointer.NULL, testproc, 0, 0,
                2);

        WinUser.MSG msg = new WinUser.MSG();
        while (!cb) {
            if (User32.INSTANCE.PeekMessage(msg, null, 0, 0, 0)) {
                User32.INSTANCE.TranslateMessage(msg);
                User32.INSTANCE.DispatchMessage(msg);
            }
        }
    }

    public static void close() {
        System.out.println("closing native interface");

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
