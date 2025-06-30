package com.gameadvisor.client;

import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.util.Duration;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ProcessScanService extends ScheduledService<String> {
    private final List<Game> knownGames;

    public ProcessScanService(List<Game> knownGames) {
        this.knownGames = knownGames;
    }

    @Override
    protected Task<String> createTask() {
        return new Task<>() {
            @Override
            protected String call() throws Exception {
                List<String> runningGames = findRunningGames();
                if (runningGames.isEmpty()) {
                    return "실행 중인 게임을 찾을 수 없습니다.";
                } else {
                    return "현재 플레이 중:\n" + String.join("\n", runningGames);
                }
            }
        };
    }

    private List<String> findRunningGames() throws Exception {
        List<String> runningProcesses = getRunningProcesses();
        List<String> foundGames = new ArrayList<>();
        for (Game game : knownGames) {
            for (String process : runningProcesses) {
                String cleanProcess = process.replace(".app", "");
                if (cleanProcess.equalsIgnoreCase(game.getProcessName())) {
                    foundGames.add(game.getName());
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