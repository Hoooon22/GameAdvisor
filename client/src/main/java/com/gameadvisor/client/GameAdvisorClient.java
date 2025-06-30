package com.gameadvisor.client;

import com.gameadvisor.client.Game;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Application;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import javafx.scene.paint.Color;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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

        // 날짜/시간 표시용 라벨 생성
        Label dateLabel = new Label("");
        dateLabel.setFont(new Font("Arial", 16));
        dateLabel.setTextFill(Color.WHITE);
        dateLabel.setAlignment(Pos.TOP_RIGHT);
        dateLabel.setPadding(new Insets(10, 10, 0, 0));

        // StackPane 대신 BorderPane 사용하여 우측 상단 배치
        BorderPane root = new BorderPane();
        root.setCenter(statusLabel);
        BorderPane.setAlignment(statusLabel, Pos.CENTER);
        root.setTop(dateLabel);
        BorderPane.setAlignment(dateLabel, Pos.TOP_RIGHT);

        Scene scene = new Scene(root, 400, 200);
        // 반투명 배경 적용
        scene.setFill(Color.rgb(30, 30, 30, 0.7)); // 70% 불투명, 어두운 배경

        // 오버레이 스타일 및 위치 적용
        primaryStage.initStyle(StageStyle.TRANSPARENT); // 창 테두리/배경 투명
        primaryStage.setAlwaysOnTop(true); // 항상 최상단
        primaryStage.setX(0); // 좌측 상단
        primaryStage.setY(0);
        primaryStage.setOpacity(0.9); // 전체 창 자체도 약간 투명하게(선택)

        // 날짜/시간 주기적 갱신
        javafx.animation.Timeline timeline = new javafx.animation.Timeline(
            new javafx.animation.KeyFrame(Duration.seconds(1), e -> {
                new Thread(() -> {
                    try {
                        OkHttpClient client = new OkHttpClient();
                        Request request = new Request.Builder().url("http://localhost:8080/api/games/date").build();
                        try (Response response = client.newCall(request).execute()) {
                            if (response.isSuccessful() && response.body() != null) {
                                String dateTimeStr = response.body().string().replaceAll("\"", "");
                                // LocalDateTime 파싱 및 포맷팅
                                LocalDateTime dateTime = LocalDateTime.parse(dateTimeStr);
                                String formatted = dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                                javafx.application.Platform.runLater(() -> dateLabel.setText(formatted));
                            } else {
                                javafx.application.Platform.runLater(() -> dateLabel.setText("시간 불러오기 실패"));
                            }
                        }
                    } catch (Exception ex) {
                        javafx.application.Platform.runLater(() -> dateLabel.setText("시간 오류"));
                    }
                }).start();
            })
        );
        timeline.setCycleCount(javafx.animation.Animation.INDEFINITE);
        timeline.play();

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
                    // macOS에서는 .app 확장자를 포함하는 경우가 있으므로, 이를 제거하고 비교합니다.
                    String cleanProcess = process.replace(".app", "");
                    if (cleanProcess.equalsIgnoreCase(game.getProcessName())) {
                        foundGames.add(game.getName());
                        break; // 같은 게임 프로세스가 여러 개 실행 중일 수 있으므로, 하나만 추가하고 다음 게임으로 넘어갑니다.
                    }
                }
            }
            return foundGames;
        }

        private List<String> getRunningProcesses() throws Exception {
            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("win")) {
                // 윈도우: tasklist 사용
                ProcessBuilder processBuilder = new ProcessBuilder("tasklist");
                Process process = processBuilder.start();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "MS949"))) {
                    return reader.lines()
                        .skip(3) // 헤더 3줄 건너뜀
                        .map(line -> line.split("\\s+")[0])
                        .collect(Collectors.toList());
                } finally {
                    process.waitFor();
                }
            } else {
                // macOS/Linux: ps 사용
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

    public static void main(String[] args) {
        launch(args);
    }
}