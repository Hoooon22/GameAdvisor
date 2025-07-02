package com.gameadvisor.client.ui.components.character;

import javafx.animation.FadeTransition;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.util.Duration;

/**
 * 캐릭터의 말풍선 컴포넌트
 * 다양한 스타일과 애니메이션 효과를 지원
 */
public class SpeechBubble extends Group {
    
    public enum BubbleType {
        NORMAL,     // 일반 대화
        ADVICE,     // 조언
        WARNING,    // 경고
        SUCCESS,    // 성공
        THINKING,   // 생각
        STRATEGY    // 공략 (지속 표시)
    }
    
    private StackPane bubbleContainer;
    private Label textLabel;
    private Polygon bubbleTail;
    private Button closeButton;
    private Button minimizeButton;
    private StackPane minimizedBar;
    private BubbleType currentType;
    private Timeline showAnimation;
    private Runnable onCloseCallback;
    private boolean isMinimized = false;
    private String currentMessage = "";
    
    public SpeechBubble() {
        initializeBubble();
    }
    
    /**
     * 말풍선 초기화
     */
    private void initializeBubble() {
        // 텍스트 라벨 생성
        textLabel = new Label();
        textLabel.setWrapText(true);
        textLabel.setMaxWidth(280); // 더 넓게 설정하여 긴 텍스트 수용
        textLabel.setMaxHeight(200); // 최대 높이 제한으로 너무 길어지는 것 방지
        textLabel.setPadding(new Insets(12, 40, 12, 15)); // 오른쪽 패딩을 늘려 X 버튼 공간 확보
        textLabel.setAlignment(Pos.CENTER);
        textLabel.setStyle("-fx-font-family: 'Malgun Gothic'; -fx-font-size: 13px; -fx-text-fill: white; -fx-font-weight: bold; -fx-effect: dropshadow(gaussian, black, 2, 0.8, 1, 1);");
        
        // X 버튼 생성 (공략 조언용)
        closeButton = new Button("✕");
        closeButton.setPrefSize(28, 28); // 크기를 더 크게
        closeButton.setMinSize(28, 28);
        closeButton.setMaxSize(28, 28);
        closeButton.setStyle(
            "-fx-background-color: rgba(220,20,20,0.9); " + // 빨간색 배경으로 눈에 띄게
            "-fx-text-fill: white; " +
            "-fx-font-size: 14px; " +
            "-fx-font-weight: bold; " +
            "-fx-background-radius: 14; " +
            "-fx-border-radius: 14; " +
            "-fx-border-color: rgba(180,0,0,0.8); " +
            "-fx-border-width: 2px; " +
            "-fx-cursor: hand; " +
            "-fx-padding: 0; " +
            "-fx-effect: dropshadow(gaussian, black, 3, 0.8, 1, 1);" // 그림자 효과 추가
        );
        
        // 호버 효과 추가
        closeButton.setOnMouseEntered(e -> {
            closeButton.setStyle(
                "-fx-background-color: rgba(255,30,30,1.0); " +
                "-fx-text-fill: white; " +
                "-fx-font-size: 14px; " +
                "-fx-font-weight: bold; " +
                "-fx-background-radius: 14; " +
                "-fx-border-radius: 14; " +
                "-fx-border-color: rgba(200,0,0,1.0); " +
                "-fx-border-width: 2px; " +
                "-fx-cursor: hand; " +
                "-fx-padding: 0; " +
                "-fx-effect: dropshadow(gaussian, black, 5, 1.0, 2, 2);"
            );
        });
        
        closeButton.setOnMouseExited(e -> {
            closeButton.setStyle(
                "-fx-background-color: rgba(220,20,20,0.9); " +
                "-fx-text-fill: white; " +
                "-fx-font-size: 14px; " +
                "-fx-font-weight: bold; " +
                "-fx-background-radius: 14; " +
                "-fx-border-radius: 14; " +
                "-fx-border-color: rgba(180,0,0,0.8); " +
                "-fx-border-width: 2px; " +
                "-fx-cursor: hand; " +
                "-fx-padding: 0; " +
                "-fx-effect: dropshadow(gaussian, black, 3, 0.8, 1, 1);"
            );
        });
        closeButton.setOnAction(e -> {
            System.out.println("[DEBUG] X 버튼 클릭됨 - 말풍선 닫기 시작");
            hide();
            if (onCloseCallback != null) {
                System.out.println("[DEBUG] 콜백 함수 실행");
                onCloseCallback.run();
            } else {
                System.out.println("[DEBUG] 콜백 함수가 설정되지 않음");
            }
        });
        
        // 마우스 클릭 이벤트도 추가 (더 확실한 처리를 위해)
        closeButton.setOnMouseClicked(e -> {
            System.out.println("[DEBUG] X 버튼 마우스 클릭 감지");
            e.consume(); // 이벤트 소비하여 다른 핸들러로 전파 방지
            hide();
            if (onCloseCallback != null) {
                onCloseCallback.run();
            }
        });
        closeButton.setVisible(false); // 기본적으로 숨김
        
        // 최소화 버튼 생성 (공략 조언용)
        minimizeButton = new Button("−");
        minimizeButton.setPrefSize(28, 28);
        minimizeButton.setMinSize(28, 28);
        minimizeButton.setMaxSize(28, 28);
        minimizeButton.setStyle(
            "-fx-background-color: rgba(50,150,50,0.9); " + // 초록색 배경
            "-fx-text-fill: white; " +
            "-fx-font-size: 16px; " +
            "-fx-font-weight: bold; " +
            "-fx-background-radius: 14; " +
            "-fx-border-radius: 14; " +
            "-fx-border-color: rgba(30,120,30,0.8); " +
            "-fx-border-width: 2px; " +
            "-fx-cursor: hand; " +
            "-fx-padding: 0; " +
            "-fx-effect: dropshadow(gaussian, black, 3, 0.8, 1, 1);"
        );
        
        // 최소화 버튼 호버 효과
        minimizeButton.setOnMouseEntered(e -> {
            minimizeButton.setStyle(
                "-fx-background-color: rgba(70,170,70,1.0); " +
                "-fx-text-fill: white; " +
                "-fx-font-size: 16px; " +
                "-fx-font-weight: bold; " +
                "-fx-background-radius: 14; " +
                "-fx-border-radius: 14; " +
                "-fx-border-color: rgba(50,140,50,1.0); " +
                "-fx-border-width: 2px; " +
                "-fx-cursor: hand; " +
                "-fx-padding: 0; " +
                "-fx-effect: dropshadow(gaussian, black, 5, 1.0, 2, 2);"
            );
        });
        
        minimizeButton.setOnMouseExited(e -> {
            minimizeButton.setStyle(
                "-fx-background-color: rgba(50,150,50,0.9); " +
                "-fx-text-fill: white; " +
                "-fx-font-size: 16px; " +
                "-fx-font-weight: bold; " +
                "-fx-background-radius: 14; " +
                "-fx-border-radius: 14; " +
                "-fx-border-color: rgba(30,120,30,0.8); " +
                "-fx-border-width: 2px; " +
                "-fx-cursor: hand; " +
                "-fx-padding: 0; " +
                "-fx-effect: dropshadow(gaussian, black, 3, 0.8, 1, 1);"
            );
        });
        
        minimizeButton.setOnAction(e -> {
            System.out.println("[DEBUG] 최소화 버튼 클릭됨");
            minimizeBubble();
        });
        
        minimizeButton.setOnMouseClicked(e -> {
            System.out.println("[DEBUG] 최소화 버튼 마우스 클릭 감지");
            e.consume();
            minimizeBubble();
        });
        minimizeButton.setVisible(false); // 기본적으로 숨김
        
        // 최소화된 바 생성 (윈도우 작업표시줄처럼)
        minimizedBar = new StackPane();
        Label minimizedLabel = new Label("📋 공략 조언");
        minimizedLabel.setStyle(
            "-fx-font-family: 'Malgun Gothic'; " +
            "-fx-font-size: 12px; " +
            "-fx-text-fill: white; " +
            "-fx-font-weight: bold; " +
            "-fx-effect: dropshadow(gaussian, black, 2, 0.8, 1, 1);"
        );
        minimizedLabel.setPadding(new Insets(6, 12, 6, 12));
        
        minimizedBar.getChildren().add(minimizedLabel);
        minimizedBar.setPrefWidth(150);
        minimizedBar.setPrefHeight(30);
        minimizedBar.setMaxWidth(150);
        minimizedBar.setMaxHeight(30);
        minimizedBar.setStyle(
            "-fx-background-color: rgba(100,100,100,0.9); " +
            "-fx-background-radius: 15; " +
            "-fx-border-color: rgba(70,70,70,0.8); " +
            "-fx-border-width: 2px; " +
            "-fx-border-radius: 15; " +
            "-fx-cursor: hand;"
        );
        
        // 최소화된 바 호버 효과
        minimizedBar.setOnMouseEntered(e -> {
            minimizedBar.setStyle(
                "-fx-background-color: rgba(120,120,120,1.0); " +
                "-fx-background-radius: 15; " +
                "-fx-border-color: rgba(90,90,90,1.0); " +
                "-fx-border-width: 2px; " +
                "-fx-border-radius: 15; " +
                "-fx-cursor: hand; " +
                "-fx-effect: dropshadow(gaussian, black, 5, 1.0, 2, 2);"
            );
        });
        
        minimizedBar.setOnMouseExited(e -> {
            minimizedBar.setStyle(
                "-fx-background-color: rgba(100,100,100,0.9); " +
                "-fx-background-radius: 15; " +
                "-fx-border-color: rgba(70,70,70,0.8); " +
                "-fx-border-width: 2px; " +
                "-fx-border-radius: 15; " +
                "-fx-cursor: hand;"
            );
        });
        
        minimizedBar.setOnMouseClicked(e -> {
            System.out.println("[DEBUG] 최소화 바 클릭됨 - 복원 시작");
            e.consume();
            restoreBubble();
        });
        
        minimizedBar.setVisible(false); // 기본적으로 숨김
        
        // 말풍선 컨테이너 생성
        bubbleContainer = new StackPane();
        bubbleContainer.getChildren().addAll(textLabel, closeButton, minimizeButton);
        bubbleContainer.setMaxWidth(320); // 긴 텍스트를 위해 더 넓게
        bubbleContainer.setMaxHeight(220); // 최대 높이 제한
        
        // X 버튼과 최소화 버튼을 오른쪽 위에 위치시키기
        StackPane.setAlignment(closeButton, Pos.TOP_RIGHT);
        StackPane.setMargin(closeButton, new Insets(3, 3, 0, 0));
        
        StackPane.setAlignment(minimizeButton, Pos.TOP_RIGHT);
        StackPane.setMargin(minimizeButton, new Insets(3, 35, 0, 0)); // X 버튼 왼쪽에 배치
        
        // 더 강한 그림자 효과 추가
        DropShadow dropShadow = new DropShadow();
        dropShadow.setRadius(12.0);
        dropShadow.setOffsetX(4.0);
        dropShadow.setOffsetY(4.0);
        dropShadow.setColor(Color.color(0, 0, 0, 0.6));
        bubbleContainer.setEffect(dropShadow);
        
        // 말풍선 꼬리 생성 (아래쪽 삼각형)
        bubbleTail = new Polygon();
        bubbleTail.getPoints().addAll(new Double[]{
            0.0, 0.0,   // 위쪽 중앙
            -12.0, 18.0, // 왼쪽 아래
            12.0, 18.0   // 오른쪽 아래
        });
        
        // 꼬리에도 더 강한 그림자 효과 추가
        DropShadow tailShadow = new DropShadow();
        tailShadow.setRadius(10.0);
        tailShadow.setOffsetX(3.0);
        tailShadow.setOffsetY(3.0);
        tailShadow.setColor(Color.color(0, 0, 0, 0.5));
        bubbleTail.setEffect(tailShadow);
        
        this.getChildren().addAll(bubbleContainer, bubbleTail, minimizedBar);
        
        // 기본적으로 숨김
        this.setVisible(false);
        this.setOpacity(0.0);
    }
    
    /**
     * 말풍선 표시
     */
    public void showMessage(String message, BubbleType type) {
        currentType = type;
        currentMessage = message;
        textLabel.setText(message);
        
        // 최소화 상태 초기화
        isMinimized = false;
        bubbleContainer.setVisible(true);
        bubbleTail.setVisible(true);
        minimizedBar.setVisible(false);
        
        // 타입에 따른 스타일 설정
        setupBubbleStyle(type);
        
        // 꼬리 위치 조정 (말풍선 중앙 하단)
        bubbleTail.setLayoutX(bubbleContainer.getWidth() / 2);
        bubbleTail.setLayoutY(bubbleContainer.getHeight());
        
        // X 버튼과 최소화 버튼 표시 여부 설정 (STRATEGY 타입만 표시)
        closeButton.setVisible(type == BubbleType.STRATEGY);
        minimizeButton.setVisible(type == BubbleType.STRATEGY);
        
        // 페이드 인 애니메이션 (95% 투명도로 설정하여 배경이 약간 보이도록)
        this.setVisible(true);
        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), this);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(0.95);
        fadeIn.play();
        
        // 공략 조언이 아닌 경우만 자동으로 숨기기
        if (type != BubbleType.STRATEGY) {
            // 텍스트 길이에 따른 표시 시간 계산
            double displayDuration = calculateDisplayDuration(message);
            Timeline autoHide = new Timeline(
                new javafx.animation.KeyFrame(Duration.seconds(displayDuration), e -> hide())
            );
            autoHide.play();
        }
    }
    
    /**
     * 타입에 따른 말풍선 스타일 설정
     */
    private void setupBubbleStyle(BubbleType type) {
        Color bubbleColor;
        Color borderColor;
        String textColor = "#FFFFFF"; // 기본 흰색 텍스트
        String shadowColor = "#000000"; // 텍스트 그림자 색상
        
        switch (type) {
            case ADVICE:
                bubbleColor = Color.web("#1565C0", 0.9); // 진한 블루, 90% 불투명
                borderColor = Color.web("#0D47A1"); // 더 진한 블루
                break;
            case WARNING:
                bubbleColor = Color.web("#EF6C00", 0.9); // 진한 오렌지, 90% 불투명
                borderColor = Color.web("#BF360C"); // 더 진한 오렌지
                break;
            case SUCCESS:
                bubbleColor = Color.web("#2E7D32", 0.9); // 진한 그린, 90% 불투명
                borderColor = Color.web("#1B5E20"); // 더 진한 그린
                break;
            case THINKING:
                bubbleColor = Color.web("#6A1B9A", 0.9); // 진한 퍼플, 90% 불투명
                borderColor = Color.web("#4A148C"); // 더 진한 퍼플
                // 생각하는 말풍선은 원형으로 변경
                bubbleContainer.setStyle("-fx-background-radius: 50%; -fx-border-radius: 50%;");
                break;
            case STRATEGY:
                bubbleColor = Color.web("#D32F2F", 0.9); // 진한 빨강, 90% 불투명 (공략 전용)
                borderColor = Color.web("#B71C1C"); // 더 진한 빨강
                break;
            case NORMAL:
            default:
                bubbleColor = Color.web("#424242", 0.9); // 진한 그레이, 90% 불투명
                borderColor = Color.web("#212121"); // 더 진한 그레이
                break;
        }
        
        // 배경 설정 (반투명 효과)
        BackgroundFill backgroundFill = new BackgroundFill(
            bubbleColor, 
            new CornerRadii(18), 
            Insets.EMPTY
        );
        bubbleContainer.setBackground(new Background(backgroundFill));
        
        // 텍스트 색상 및 그림자 효과 업데이트
        textLabel.setStyle(
            "-fx-font-family: 'Malgun Gothic'; " +
            "-fx-font-size: 13px; " +
            "-fx-text-fill: " + textColor + "; " +
            "-fx-font-weight: bold; " +
            "-fx-effect: dropshadow(gaussian, " + shadowColor + ", 2, 0.8, 1, 1);"
        );
        
        // 테두리 설정 (더 굵게)
        if (type != BubbleType.THINKING) {
            bubbleContainer.setStyle(
                "-fx-border-color: " + toRgbString(borderColor) + "; " +
                "-fx-border-width: 3px; " +
                "-fx-border-radius: 18px; " +
                "-fx-background-radius: 18px;"
            );
        }
        
        // 꼬리 색상 설정 (투명도 적용)
        bubbleTail.setFill(bubbleColor);
        bubbleTail.setStroke(borderColor);
        bubbleTail.setStrokeWidth(3);
    }
    
    /**
     * 텍스트 길이에 따른 표시 시간 계산
     * 최소 2초, 최대 12초, 글자 수에 따라 조정
     */
    public double calculateDisplayDuration(String message) {
        if (message == null || message.trim().isEmpty()) {
            return 2.0; // 기본 최소 시간
        }
        
        // 실제 표시될 텍스트 길이 계산 (공백 제거)
        String cleanText = message.trim();
        int textLength = cleanText.length();
        
        // 기본 시간 (2초) + 글자당 추가 시간
        // 한국어 평균 읽기 속도: 분당 350글자 정도
        // 1글자당 약 0.17초 (60초 ÷ 350글자)
        double baseTime = 2.0;
        double readingTimePerChar = 0.1; // 조금 여유있게 설정
        
        double calculatedTime = baseTime + (textLength * readingTimePerChar);
        
        // 최소 2초, 최대 12초로 제한
        return Math.max(2.0, Math.min(12.0, calculatedTime));
    }
    
    /**
     * Color를 RGB 문자열로 변환
     */
    private String toRgbString(Color color) {
        return String.format("rgb(%d,%d,%d)", 
            (int)(color.getRed() * 255),
            (int)(color.getGreen() * 255),
            (int)(color.getBlue() * 255)
        );
    }
    
    /**
     * 말풍선 숨기기
     */
    public void hide() {
        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), this);
        fadeOut.setFromValue(0.95);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(e -> this.setVisible(false));
        fadeOut.play();
    }
    
    /**
     * 즉시 숨기기
     */
    public void hideImmediately() {
        this.setVisible(false);
        this.setOpacity(0.0);
    }
    
    /**
     * 말풍선이 표시 중인지 확인
     */
    public boolean isShowing() {
        return this.isVisible() && this.getOpacity() > 0.1;
    }
    
    /**
     * 현재 말풍선 타입 반환
     */
    public BubbleType getCurrentType() {
        return currentType;
    }

    /**
     * 말풍선이 닫힐 때 호출될 콜백 설정
     */
    public void setOnCloseCallback(Runnable callback) {
        this.onCloseCallback = callback;
    }
    
    /**
     * 캐릭터 위에 말풍선 위치시키기 (화면 경계 고려)
     */
    public void positionAboveCharacter(double characterX, double characterY, double characterWidth, 
                                     double screenMinX, double screenMinY, double screenMaxX, double screenMaxY) {
        
        // 말풍선 크기 (실제 렌더링 후 크기 적용을 위해 최소값 사용)
        double bubbleWidth = Math.max(bubbleContainer.getWidth(), 320);
        double bubbleHeight = Math.max(bubbleContainer.getHeight(), 100);
        
        // 기본 위치: 캐릭터 위쪽 중앙
        double bubbleX = characterX + (characterWidth / 2) - (bubbleWidth / 2);
        double bubbleY = characterY - bubbleHeight - 20; // 20픽셀 간격
        
        // X 좌표 경계 조정 (좌우 화면 밖으로 나가지 않게)
        if (bubbleX < screenMinX + 10) {
            bubbleX = screenMinX + 10; // 왼쪽 여백
        } else if (bubbleX + bubbleWidth > screenMaxX - 10) {
            bubbleX = screenMaxX - bubbleWidth - 10; // 오른쪽 여백
        }
        
        // Y 좌표 경계 조정 (위쪽으로 잘리지 않게)
        if (bubbleY < screenMinY + 10) {
            // 위쪽에 공간이 없으면 캐릭터 아래쪽에 표시
            bubbleY = characterY + 80; // 캐릭터 높이 추정값 + 여백
            
            // 아래쪽에도 공간이 없으면 캐릭터 옆에 표시
            if (bubbleY + bubbleHeight > screenMaxY - 10) {
                bubbleY = characterY - (bubbleHeight / 2); // 캐릭터 옆 중앙
                
                // 캐릭터 오른쪽에 공간이 있으면 오른쪽에, 없으면 왼쪽에
                if (characterX + characterWidth + bubbleWidth + 30 < screenMaxX) {
                    bubbleX = characterX + characterWidth + 20; // 오른쪽
                } else {
                    bubbleX = characterX - bubbleWidth - 20; // 왼쪽
                }
            }
        }
        
        // 최종 위치 적용
        this.setLayoutX(bubbleX);
        this.setLayoutY(bubbleY);
        
        System.out.println("[DEBUG] 말풍선 위치 조정: 캐릭터(" + (int)characterX + "," + (int)characterY + 
                          ") → 말풍선(" + (int)bubbleX + "," + (int)bubbleY + ") 크기(" + (int)bubbleWidth + "x" + (int)bubbleHeight + ")");
        
        // 꼬리 위치 조정 (말풍선이 캐릭터 위에 있을 때만 하단 중앙)
        if (bubbleY < characterY) {
            // 말풍선이 캐릭터 위에 있음 - 꼬리를 하단 중앙에
            double tailX = Math.max(20, Math.min(bubbleWidth - 20, 
                (characterX + characterWidth/2) - bubbleX)); // 캐릭터 중앙을 향하도록
            bubbleTail.setLayoutX(tailX);
            bubbleTail.setLayoutY(bubbleHeight);
        } else {
            // 말풍선이 캐릭터 옆이나 아래에 있음 - 꼬리를 캐릭터 방향으로
            bubbleTail.setLayoutX(bubbleWidth / 2);
                         bubbleTail.setLayoutY(0); // 상단에
         }
    }
    
    /**
     * 캐릭터 위에 말풍선 위치시키기 (기본 화면 크기 사용)
     * @deprecated 화면 경계를 전달하는 오버로드 메서드 사용 권장
     */
    @Deprecated
    public void positionAboveCharacter(double characterX, double characterY, double characterWidth) {
        // 기본 게임 창 크기로 호출
        positionAboveCharacter(characterX, characterY, characterWidth, 0, 0, 1600, 1000);
    }
    
    /**
     * 말풍선을 최소화 (작은 바 형태로)
     */
    private void minimizeBubble() {
        if (isMinimized) return;
        
        System.out.println("[DEBUG] 말풍선 최소화 시작");
        isMinimized = true;
        
        // 전체 말풍선과 꼬리 숨기기
        bubbleContainer.setVisible(false);
        bubbleTail.setVisible(false);
        
        // 최소화된 바 표시
        minimizedBar.setVisible(true);
        
        // 부드러운 전환 효과
        FadeTransition fadeOut = new FadeTransition(Duration.millis(200), bubbleContainer);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(e -> {
            FadeTransition fadeIn = new FadeTransition(Duration.millis(200), minimizedBar);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(0.95);
            fadeIn.play();
        });
        fadeOut.play();
        
        System.out.println("[DEBUG] 말풍선 최소화 완료");
    }
    
    /**
     * 말풍선을 복원 (최소화에서 전체 표시로)
     */
    private void restoreBubble() {
        if (!isMinimized) return;
        
        System.out.println("[DEBUG] 말풍선 복원 시작");
        isMinimized = false;
        
        // 최소화된 바 숨기기
        minimizedBar.setVisible(false);
        
        // 전체 말풍선과 꼬리 표시
        bubbleContainer.setVisible(true);
        bubbleTail.setVisible(true);
        
        // 부드러운 전환 효과
        FadeTransition fadeOut = new FadeTransition(Duration.millis(200), minimizedBar);
        fadeOut.setFromValue(0.95);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(e -> {
            FadeTransition fadeIn = new FadeTransition(Duration.millis(200), bubbleContainer);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(0.95);
            fadeIn.play();
        });
        fadeOut.play();
        
        System.out.println("[DEBUG] 말풍선 복원 완료");
    }
    
    /**
     * 현재 최소화 상태인지 확인
     */
    public boolean isMinimized() {
        return isMinimized;
    }
} 