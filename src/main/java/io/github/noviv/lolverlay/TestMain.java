package io.github.noviv.lolverlay;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.Ole32;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.platform.win32.WinUser;
import com.sun.jna.win32.StdCallLibrary.StdCallCallback;
import com.sun.jna.win32.W32APIOptions;

public class TestMain {

    public static boolean cb = false;

    public static void main(String[] args) {
        WinNT.HRESULT hr = Ole32.INSTANCE.CoInitializeEx(null, 0);
        int WINEVENT_OUTOFCONTEXT = 0;
        int WINEVENT_SKIPOWNPROCESS = 2;
        int EVENT_SYSTEM_MINIMIZEEND = 23;
        int EVENT_SYSTEM_MINIMIZESTART = 22;

        User32RW.WinEventProc testproc = (WinNT.HANDLE hWinEventHook, int event, WinDef.HWND hwnd, int idObject, int idChild, int dwEventThread, int dwmsEventTime) -> {
            System.out.println("Callback called");
            cb = true;
        };

        WinNT.HANDLE test_hook = User32RW.INSTANCE.SetWinEventHook(
                0x0003, 0x0003,
                Pointer.NULL, // Callback not in a dll                 
                testproc,
                0, 0, // Process and thread IDs of interest (0 = all)
                WINEVENT_OUTOFCONTEXT | WINEVENT_SKIPOWNPROCESS);
        int er = Kernel32.INSTANCE.GetLastError();
        System.out.println("namechange reg error " + er);
        if (er == 0) {
            WinUser.MSG msg = new WinUser.MSG();
            while (!cb) {
                if (User32.INSTANCE.PeekMessage(msg, null, 0, 0, 0)) {
                    User32.INSTANCE.TranslateMessage(msg);
                    User32.INSTANCE.DispatchMessage(msg);
                }
                try {
                    Thread.sleep(50);
                } catch (InterruptedException ex) {
                }
            }
            User32RW.INSTANCE.UnhookWinEvent(test_hook);
        }
    }

    public interface User32RW extends User32 {

        User32RW INSTANCE = (User32RW) Native.loadLibrary("user32", User32RW.class, W32APIOptions.UNICODE_OPTIONS);

        public WinNT.HANDLE SetWinEventHook(int eventMin, int eventMax, Pointer hmodWinEventProc, WinEventProc lpfnWinEventProc,
                int idProcess,
                int idThread,
                int dwflags);

        @Override
        public boolean UnhookWinEvent(WinNT.HANDLE handle);

        public static interface WinEventProc extends StdCallCallback {

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
}
