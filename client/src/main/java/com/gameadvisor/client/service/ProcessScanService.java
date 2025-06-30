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
        List<String> runningProcesses = getRunningProcesses();
        List<GameWindowInfo> foundGames = new ArrayList<>();
        for (Game game : knownGames) {
            for (String process : runningProcesses) {
                String cleanProcess = process.replace(".app", "");
                if (cleanProcess.equalsIgnoreCase(game.getProcessName())) {
                    RECT rect = WindowUtils.getWindowRectByProcessName(game.getProcessName());
                    foundGames.add(new GameWindowInfo(game.getName(), game.getProcessName(), rect));
                    break;
                }
            }
        }
        return foundGames;
    }

    private List<String> getRunningProcesses() throws Exception {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            ProcessBuilder processBuilder = new ProcessBuilder("tasklist");
            Process process = processBuilder.start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "MS949"))) {
                return reader.lines()
                    .skip(3)
                    .map(line -> line.split("\\s+")[0])
                    .collect(Collectors.toList());
            } finally {
                process.waitFor();
            }
        } else {
            ProcessBuilder processBuilder = new ProcessBuilder("ps", "-e", "-o", "comm=");
            Process process = processBuilder.start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                return reader.lines()
                    .map(line -> line.substring(line.lastIndexOf('/') + 1))
                    .collect(Collectors.toList());
            } finally {
                process.waitFor();
            }
        }
    }
} 