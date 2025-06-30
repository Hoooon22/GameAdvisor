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
            System.out.println("[DEBUG] EnumWindows: HWND=" + hWnd + ", PID=" + pid.getValue() + ", exeName=" + exeName + ", target=" + processName);
            if (exeName != null && exeName.equalsIgnoreCase(processName)) {
                hwndList.add(hWnd);
                System.out.println("[DEBUG] HWND 매칭 성공: " + hWnd + ", PID=" + pid.getValue());
            }
            return true;
        }, null);
        if (!hwndList.isEmpty()) {
            RECT rect = new RECT();
            boolean gotRect = User32.INSTANCE.GetWindowRect(hwndList.get(0), rect);
            System.out.println("[DEBUG] GetWindowRect: " + gotRect + ", RECT=" + (gotRect ? rect.toString() : "null"));
            return gotRect ? rect : null;
        }
        System.out.println("[DEBUG] HWND를 찾지 못함");
        return null;
    }

    private static String getProcessName(int pid) {
        // 윈도우에서 PID로 프로세스명 얻기 (tasklist 사용)
        try {
            Process process = new ProcessBuilder("tasklist", "/FI", "PID eq " + pid, "/FO", "CSV", "/NH").start();
            try (java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(process.getInputStream(), "MS949"))) {
                String line = reader.readLine();
                System.out.println("[DEBUG] getProcessName(" + pid + ") tasklist 결과: " + line);
                if (line != null && line.startsWith("\"")) {
                    String[] parts = line.split(",");
                    if (parts.length > 0) {
                        String name = parts[0].replaceAll("\"", "");
                        System.out.println("[DEBUG] getProcessName(" + pid + ") 추출: " + name);
                        return name;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // PID로 최상위 윈도우 핸들(HWND) 찾기
    public static com.sun.jna.platform.win32.WinDef.HWND findMainWindowByPid(int pid) {
        final com.sun.jna.platform.win32.WinDef.HWND[] result = new com.sun.jna.platform.win32.WinDef.HWND[1];
        User32.INSTANCE.EnumWindows((hWnd, data) -> {
            IntByReference procId = new IntByReference();
            User32.INSTANCE.GetWindowThreadProcessId(hWnd, procId);
            // 최상위 & Visible 윈도우만
            boolean isMain = User32.INSTANCE.IsWindowVisible(hWnd) && User32.INSTANCE.GetParent(hWnd) == null;
            if (procId.getValue() == pid && isMain) {
                result[0] = hWnd;
                return false; // stop
            }
            return true;
        }, null);
        return result[0];
    }

    // HWND로 RECT 구하기
    public static RECT getWindowRectByHwnd(com.sun.jna.platform.win32.WinDef.HWND hwnd) {
        if (hwnd == null) return null;
        RECT rect = new RECT();
        boolean gotRect = User32.INSTANCE.GetWindowRect(hwnd, rect);
        return gotRect ? rect : null;
    }
} 