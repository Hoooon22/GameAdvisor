package com.gameadvisor.client.util;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinDef.RECT;
import com.sun.jna.platform.win32.WinNT.HANDLE;
import com.sun.jna.ptr.IntByReference;
import java.util.ArrayList;
import java.util.List;

public class WindowUtils {
    public static RECT getWindowRectByProcessName(String processName) {
        List<HWND> hwndList = new ArrayList<>();
        User32.INSTANCE.EnumWindows((hWnd, data) -> {
            char[] windowText = new char[512];
            User32.INSTANCE.GetWindowText(hWnd, windowText, 512);
            char[] className = new char[512];
            User32.INSTANCE.GetClassName(hWnd, className, 512);
            // 프로세스 ID 얻기
            IntByReference pid = new IntByReference();
            User32.INSTANCE.GetWindowThreadProcessId(hWnd, pid);
            String exeName = getProcessName(pid.getValue());
            if (exeName != null && exeName.equalsIgnoreCase(processName)) {
                hwndList.add(hWnd);
            }
            return true;
        }, null);
        if (!hwndList.isEmpty()) {
            RECT rect = new RECT();
            User32.INSTANCE.GetWindowRect(hwndList.get(0), rect);
            return rect;
        }
        return null;
    }

    private static String getProcessName(int pid) {
        // 윈도우에서 PID로 프로세스명 얻기 (tasklist 사용)
        try {
            Process process = new ProcessBuilder("tasklist", "/FI", "PID eq " + pid, "/FO", "CSV", "/NH").start();
            try (java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(process.getInputStream(), "MS949"))) {
                String line = reader.readLine();
                if (line != null && line.startsWith("\"")) {
                    String[] parts = line.split(",");
                    if (parts.length > 0) {
                        return parts[0].replaceAll("\"", "");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
} 