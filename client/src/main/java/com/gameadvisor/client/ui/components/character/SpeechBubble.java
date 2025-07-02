package com.gameadvisor.client.ui.components.character;

import javafx.animation.FadeTransition;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
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
        THINKING    // 생각
    }
    
    private StackPane bubbleContainer;
    private Label textLabel;
    private Polygon bubbleTail;
    private BubbleType currentType;
    private Timeline showAnimation;
    
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
        textLabel.setMaxWidth(200);
        textLabel.setPadding(new Insets(12, 15, 12, 15));
        textLabel.setAlignment(Pos.CENTER);
        textLabel.setStyle("-fx-font-family: 'Malgun Gothic'; -fx-font-size: 13px; -fx-text-fill: white; -fx-font-weight: bold; -fx-effect: dropshadow(gaussian, black, 2, 0.8, 1, 1);");
        
        // 말풍선 컨테이너 생성
        bubbleContainer = new StackPane();
        bubbleContainer.getChildren().add(textLabel);
        bubbleContainer.setMaxWidth(230);
        
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
        
        this.getChildren().addAll(bubbleContainer, bubbleTail);
        
        // 기본적으로 숨김
        this.setVisible(false);
        this.setOpacity(0.0);
    }
    
    /**
     * 말풍선 표시
     */
    public void showMessage(String message, BubbleType type) {
        currentType = type;
        textLabel.setText(message);
        
        // 타입에 따른 스타일 설정
        setupBubbleStyle(type);
        
        // 꼬리 위치 조정 (말풍선 중앙 하단)
        bubbleTail.setLayoutX(bubbleContainer.getWidth() / 2);
        bubbleTail.setLayoutY(bubbleContainer.getHeight());
        
        // 페이드 인 애니메이션 (95% 투명도로 설정하여 배경이 약간 보이도록)
        this.setVisible(true);
        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), this);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(0.95);
        fadeIn.play();
        
        // 텍스트 길이에 따른 표시 시간 계산
        double displayDuration = calculateDisplayDuration(message);
        Timeline autoHide = new Timeline(
            new javafx.animation.KeyFrame(Duration.seconds(displayDuration), e -> hide())
        );
        autoHide.play();
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
     * 캐릭터 위에 말풍선 위치시키기
     */
    public void positionAboveCharacter(double characterX, double characterY, double characterWidth) {
        // 말풍선을 캐릭터 위쪽에 위치
        double bubbleX = characterX + (characterWidth / 2) - (bubbleContainer.getWidth() / 2);
        double bubbleY = characterY - bubbleContainer.getHeight() - 20; // 20픽셀 간격
        
        this.setLayoutX(bubbleX);
        this.setLayoutY(bubbleY);
        
        // 꼬리를 말풍선 중앙 하단에 위치
        bubbleTail.setLayoutX(bubbleContainer.getWidth() / 2);
        bubbleTail.setLayoutY(bubbleContainer.getHeight());
    }
} 