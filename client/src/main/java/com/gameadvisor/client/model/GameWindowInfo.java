package com.gameadvisor.client.model;

import com.sun.jna.platform.win32.WinDef.RECT;

public class GameWindowInfo {
    private final String gameName;
    private final String processName;
    private final RECT rect;
    public GameWindowInfo(String gameName, String processName, RECT rect) {
        this.gameName = gameName;
        this.processName = processName;
        this.rect = rect;
    }
    public String getGameName() { return gameName; }
    public String getProcessName() { return processName; }
    public RECT getRect() { return rect; }
} 