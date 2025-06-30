package com.gameadvisor.client.ui;

import com.gameadvisor.client.model.Game;
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

import com.gameadvisor.client.network.ApiClient;
import com.gameadvisor.client.service.ProcessScanService;
import com.gameadvisor.client.model.GameWindowInfo;
import com.sun.jna.platform.win32.WinDef.RECT;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;

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

        Pane overlayPane = new Pane();
        overlayPane.setPickOnBounds(false); // 오버레이 영역만 마우스 이벤트 차단

        // StackPane 대신 BorderPane 사용하여 우측 상단 배치
        BorderPane root = new BorderPane();
        root.setCenter(statusLabel);
        BorderPane.setAlignment(statusLabel, Pos.CENTER);
        root.setTop(dateLabel);
        BorderPane.setAlignment(dateLabel, Pos.TOP_RIGHT);
        root.getChildren().add(overlayPane);

        // Scene, Stage 초기 크기/위치: 화면 중앙 400x200
        double initWidth = 400;
        double initHeight = 200;
        javafx.geometry.Rectangle2D screenBounds = javafx.stage.Screen.getPrimary().getVisualBounds();
        double centerX = (screenBounds.getWidth() - initWidth) / 2;
        double centerY = (screenBounds.getHeight() - initHeight) / 2;

        Scene scene = new Scene(root, initWidth, initHeight);
        scene.setFill(Color.rgb(30, 30, 30, 0.7));

        primaryStage.initStyle(StageStyle.TRANSPARENT);
        primaryStage.setAlwaysOnTop(true);
        primaryStage.setX(centerX);
        primaryStage.setY(centerY);
        primaryStage.setOpacity(0.9);
        primaryStage.setScene(scene);
        primaryStage.setTitle("GameAdvisor");

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
                        service.setPeriod(Duration.seconds(1)); // 1초마다 위치 갱신

                        service.setOnRunning(e -> statusLabel.setText("실행 중인 게임 탐색 중..."));
                        service.setOnSucceeded(e -> {
                            List<GameWindowInfo> infos = service.getValue();
                            if (infos == null || infos.isEmpty() || infos.get(0).getRect() == null) {
                                statusLabel.setText("게임 윈도우를 찾을 수 없습니다.");
                                overlayPane.getChildren().clear();
                                // 게임 탐지 전에는 중앙 400x200 유지 (전체화면 확장 금지)
                                if (scene.getWindow().getWidth() != initWidth || scene.getWindow().getHeight() != initHeight) {
                                    scene.getWindow().setWidth(initWidth);
                                    scene.getWindow().setHeight(initHeight);
                                    primaryStage.setX(centerX);
                                    primaryStage.setY(centerY);
                                }
                                return;
                            }
                            // === 탐지 성공 시 ===
                            GameWindowInfo info = infos.get(0);
                            statusLabel.setText("탐지 성공: " + info.getGameName() + " (" + info.getProcessName() + ")");
                            service.cancel(); // 탐지 중단

                            RECT rect = info.getRect();
                            overlayPane.getChildren().clear();
                            // 게임 윈도우 바깥 영역만 오버레이로 채움
                            double screenW = screenBounds.getWidth();
                            double screenH = screenBounds.getHeight();
                            double gx = rect.left;
                            double gy = rect.top;
                            double gw = rect.right - rect.left;
                            double gh = rect.bottom - rect.top;
                            // 전체 화면으로 확장
                            if (scene.getWindow().getWidth() != screenW || scene.getWindow().getHeight() != screenH) {
                                scene.getWindow().setWidth(screenW);
                                scene.getWindow().setHeight(screenH);
                                primaryStage.setX(0);
                                primaryStage.setY(0);
                            }
                            // 위
                            Rectangle top = new Rectangle(0, 0, screenW, gy);
                            // 아래
                            Rectangle bottom = new Rectangle(0, gy+gh, screenW, screenH-(gy+gh));
                            // 왼쪽
                            Rectangle left = new Rectangle(0, gy, gx, gh);
                            // 오른쪽
                            Rectangle right = new Rectangle(gx+gw, gy, screenW-(gx+gw), gh);
                            for (Rectangle r : new Rectangle[]{top, bottom, left, right}) {
                                r.setFill(Color.rgb(30,30,30,0.7));
                                r.setMouseTransparent(false);
                            }
                            overlayPane.getChildren().addAll(top, bottom, left, right);
                        });
                        service.setOnFailed(e -> {
                            statusLabel.setText("오류: 프로세스를 스캔할 수 없습니다.");
                            overlayPane.getChildren().clear();
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

        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}