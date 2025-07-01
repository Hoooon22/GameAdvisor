package com.gameadvisor.client.model;

import com.sun.jna.platform.win32.WinDef.RECT;
import com.sun.jna.platform.win32.WinDef.HWND;

public class GameWindowInfo {
    private final String gameName;
    private final String processName;
    private final RECT rect;
    private final HWND hwnd;
    public GameWindowInfo(String gameName, String processName, RECT rect, HWND hwnd) {
        this.gameName = gameName;
        this.processName = processName;
        this.rect = rect;
        this.hwnd = hwnd;
    }
    public String getGameName() { return gameName; }
    public String getProcessName() { return processName; }
    public RECT getRect() { return rect; }
    public HWND getHwnd() { return hwnd; }
} 