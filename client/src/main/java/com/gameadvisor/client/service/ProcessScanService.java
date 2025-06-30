package com.gameadvisor.client.service;

import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.util.Duration;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import com.gameadvisor.client.util.WindowUtils;
import com.sun.jna.platform.win32.WinDef.RECT;
import com.gameadvisor.client.model.GameWindowInfo;
import com.gameadvisor.client.model.Game;

public class ProcessScanService extends ScheduledService<List<GameWindowInfo>> {
    private final List<Game> knownGames;

    public ProcessScanService(List<Game> knownGames) {
        this.knownGames = knownGames;
    }

    @Override
    protected Task<List<GameWindowInfo>> createTask() {
        return new Task<>() {
            @Override
            protected List<GameWindowInfo> call() throws Exception {
                return findRunningGameWindows();
            }
        };
    }

    private List<GameWindowInfo> findRunningGameWindows() throws Exception {
        List<ProcessInfo> runningProcesses = getRunningProcessesWithPid();
        List<GameWindowInfo> foundGames = new ArrayList<>();
        for (Game game : knownGames) {
            String target = game.getProcessName().toLowerCase().replace(".exe", "").trim();
            System.out.println("[DEBUG] 서버에서 받은 processName: " + game.getProcessName() + " → 비교용: " + target);
            for (ProcessInfo process : runningProcesses) {
                String proc = process.name.toLowerCase().replace(".exe", "").trim();
                System.out.println("[DEBUG] 실행중 프로세스: " + process.name + " → 비교용: " + proc);
                if (proc.equals(target) || proc.contains(target) || target.contains(proc)) {
                    System.out.println("[DEBUG] 매칭 성공: " + process.name + " <-> " + game.getProcessName());
                    // PID로 HWND, RECT 구하기
                    var hwnd = WindowUtils.findMainWindowByPid(process.pid);
                    var rect = WindowUtils.getWindowRectByHwnd(hwnd);
                    foundGames.add(new GameWindowInfo(game.getName(), process.name, rect));
                    System.out.println("[DEBUG] GameWindowInfo 추가됨(RECT 포함): " + game.getName() + ", RECT=" + (rect != null ? rect.toString() : "null"));
                }
            }
        }
        System.out.println("[DEBUG] 탐지된 게임: " + foundGames);
        return foundGames;
    }

    // 프로세스명과 PID를 함께 반환하는 구조체
    private static class ProcessInfo {
        String name;
        int pid;
        ProcessInfo(String name, int pid) { this.name = name; this.pid = pid; }
    }

    // tasklist에서 프로세스명과 PID를 모두 추출
    private List<ProcessInfo> getRunningProcessesWithPid() throws Exception {
        String os = System.getProperty("os.name").toLowerCase();
        List<ProcessInfo> result = new ArrayList<>();
        if (os.contains("win")) {
            ProcessBuilder processBuilder = new ProcessBuilder("tasklist", "/FO", "CSV", "/NH");
            Process process = processBuilder.start();
            BufferedReader reader = null;
            try {
                try {
                    reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "UTF-8"));
                    reader.mark(1);
                    if (reader.read() < 0) throw new Exception();
                    reader.reset();
                } catch (Exception e) {
                    if (reader != null) reader.close();
                    reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "MS949"));
                }
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!line.startsWith("\"")) continue;
                    String[] parts = line.split(",");
                    if (parts.length < 2) continue;
                    String name = parts[0].replaceAll("\"", "");
                    String pidStr = parts[1].replaceAll("\"", "");
                    try {
                        int pid = Integer.parseInt(pidStr);
                        result.add(new ProcessInfo(name, pid));
                    } catch (NumberFormatException ignore) {}
                }
            } finally {
                if (reader != null) reader.close();
                process.waitFor();
            }
        } else {
            // 리눅스/맥: ps -e -o pid,comm
            ProcessBuilder processBuilder = new ProcessBuilder("ps", "-e", "-o", "pid=,comm=");
            Process process = processBuilder.start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty()) continue;
                    String[] parts = line.split("\\s+", 2);
                    if (parts.length < 2) continue;
                    try {
                        int pid = Integer.parseInt(parts[0]);
                        String name = parts[1];
                        result.add(new ProcessInfo(name, pid));
                    } catch (NumberFormatException ignore) {}
                }
            } finally {
                process.waitFor();
            }
        }
        System.out.println("[DEBUG] 탐지된 프로세스명+PID: " + result);
        return result;
    }
} 