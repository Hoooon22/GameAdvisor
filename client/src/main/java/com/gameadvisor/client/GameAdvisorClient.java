package com.gameadvisor.client;

import com.gameadvisor.client.Game;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Application;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class GameAdvisorClient extends Application {

    private List<Game> knownGames = new ArrayList<>();

    @Override
    public void start(Stage primaryStage) {
        Label statusLabel = new Label("초기화 중...");
        statusLabel.setFont(new Font("Arial", 24));
        statusLabel.setAlignment(Pos.CENTER);

        StackPane root = new StackPane(statusLabel);
        Scene scene = new Scene(root, 800, 600);

        // 1. 애플리케이션 시작 시 서버에서 게임 목록 가져오기
        new Thread(() -> {
            ApiClient apiClient = new ApiClient();
            try {
                knownGames = apiClient.getGames();
                // UI 스레드에서 서비스 시작
                javafx.application.Platform.runLater(() -> {
                    if (knownGames.isEmpty()) {
                        statusLabel.setText("서버에서 게임 목록을 불러오지 못했습니다.\n서버가 실행 중인지 확인하세요.");
                    } else {
                        ProcessScanService service = new ProcessScanService(knownGames);
                        service.setPeriod(Duration.seconds(5));

                        service.setOnRunning(e -> statusLabel.setText("실행 중인 게임 탐색 중..."));
                        service.setOnSucceeded(e -> statusLabel.setText(service.getValue()));
                        service.setOnFailed(e -> {
                            statusLabel.setText("오류: 프로세스를 스캔할 수 없습니다.");
                            if (service.getException() != null) {
                                service.getException().printStackTrace();
                            }
                        });

                        service.start();

                        primaryStage.setOnCloseRequest(event -> service.cancel());
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                javafx.application.Platform.runLater(() -> statusLabel.setText("서버 연결 오류."));
            }
        }).start();

        primaryStage.setTitle("GameAdvisor");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private static class ApiClient {
        private final OkHttpClient client = new OkHttpClient();
        private final ObjectMapper mapper = new ObjectMapper();
        private static final String BASE_URL = "http://localhost:8080/api/games";

        public List<Game> getGames() throws Exception {
            Request request = new Request.Builder().url(BASE_URL).build();
            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    System.err.println("Failed to fetch game list: " + response);
                    return new ArrayList<>();
                }
                return mapper.readValue(response.body().string(), new TypeReference<List<Game>>() {
                });
            }
        }
    }

    private static class ProcessScanService extends ScheduledService<String> {
        private final List<Game> knownGames;

        public ProcessScanService(List<Game> knownGames) {
            this.knownGames = knownGames;
        }

        @Override
        protected Task<String> createTask() {
            return new Task<>() {
                @Override
                protected String call() throws Exception {
                    Optional<String> gameName = findRunningGame();
                    return gameName.map(name -> "현재 플레이 중:\n" + name)
                            .orElse("실행 중인 게임을 찾을 수 없습니다.");
                }
            };
        }

        private Optional<String> findRunningGame() throws Exception {
            List<String> runningProcesses = getRunningProcesses();
            for (Game game : knownGames) {
                for (String process : runningProcesses) {
                    if (process.equalsIgnoreCase(game.getProcessName())) {
                        return Optional.of(game.getName());
                    }
                }
            }
            return Optional.empty();
        }

        private List<String> getRunningProcesses() throws Exception {
            ProcessBuilder processBuilder = new ProcessBuilder("ps", "-e", "-o", "comm=");
            Process process = processBuilder.start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                // 프로세스 경로에서 마지막 부분(실행 파일 이름)만 추출
                return reader.lines()
                        .map(line -> line.substring(line.lastIndexOf('/') + 1))
                        .collect(Collectors.toList());
            } finally {
                process.waitFor();
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}