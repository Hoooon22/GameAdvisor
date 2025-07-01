package com.gameadvisor.client.ui.components;

import javafx.animation.FadeTransition;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.Label;
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
        textLabel.setPadding(new Insets(10));
        textLabel.setAlignment(Pos.CENTER);
        textLabel.setStyle("-fx-font-family: 'Malgun Gothic'; -fx-font-size: 12px; -fx-text-fill: black;");
        
        // 말풍선 컨테이너 생성
        bubbleContainer = new StackPane();
        bubbleContainer.getChildren().add(textLabel);
        bubbleContainer.setMaxWidth(220);
        
        // 말풍선 꼬리 생성 (아래쪽 삼각형)
        bubbleTail = new Polygon();
        bubbleTail.getPoints().addAll(new Double[]{
            0.0, 0.0,   // 위쪽 중앙
            -10.0, 15.0, // 왼쪽 아래
            10.0, 15.0   // 오른쪽 아래
        });
        
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
        
        // 페이드 인 애니메이션
        this.setVisible(true);
        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), this);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.play();
        
        // 3초 후 자동 숨김
        Timeline autoHide = new Timeline(
            new javafx.animation.KeyFrame(Duration.seconds(3), e -> hide())
        );
        autoHide.play();
    }
    
    /**
     * 타입에 따른 말풍선 스타일 설정
     */
    private void setupBubbleStyle(BubbleType type) {
        Color bubbleColor;
        Color borderColor;
        
        switch (type) {
            case ADVICE:
                bubbleColor = Color.LIGHTBLUE;
                borderColor = Color.BLUE;
                break;
            case WARNING:
                bubbleColor = Color.LIGHTYELLOW;
                borderColor = Color.ORANGE;
                break;
            case SUCCESS:
                bubbleColor = Color.LIGHTGREEN;
                borderColor = Color.GREEN;
                break;
            case THINKING:
                bubbleColor = Color.LAVENDER;
                borderColor = Color.PURPLE;
                // 생각하는 말풍선은 원형으로 변경
                bubbleContainer.setStyle("-fx-background-radius: 50%; -fx-border-radius: 50%;");
                break;
            case NORMAL:
            default:
                bubbleColor = Color.WHITE;
                borderColor = Color.GRAY;
                break;
        }
        
        // 배경 설정
        BackgroundFill backgroundFill = new BackgroundFill(
            bubbleColor, 
            new CornerRadii(15), 
            Insets.EMPTY
        );
        bubbleContainer.setBackground(new Background(backgroundFill));
        
        // 테두리 설정
        if (type != BubbleType.THINKING) {
            bubbleContainer.setStyle(
                "-fx-border-color: " + toRgbString(borderColor) + "; " +
                "-fx-border-width: 2px; " +
                "-fx-border-radius: 15px; " +
                "-fx-background-radius: 15px;"
            );
        }
        
        // 꼬리 색상 설정
        bubbleTail.setFill(bubbleColor);
        bubbleTail.setStroke(borderColor);
        bubbleTail.setStrokeWidth(2);
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
        fadeOut.setFromValue(1.0);
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
        return this.isVisible() && this.getOpacity() > 0;
    }
    
    /**
     * 현재 말풍선 타입 반환
     */
    public BubbleType getCurrentType() {
        return currentType;
    }
    
    /**
     * 말풍선 위치를 캐릭터 위에 설정
     */
    public void positionAboveCharacter(double characterX, double characterY, double characterWidth) {
        // 말풍선을 캐릭터 중앙 위에 배치
        this.setLayoutX(characterX + characterWidth / 2 - bubbleContainer.getWidth() / 2);
        this.setLayoutY(characterY - bubbleContainer.getHeight() - 20); // 20px 간격
        
        // 꼬리 위치 재조정
        bubbleTail.setLayoutX(bubbleContainer.getWidth() / 2);
        bubbleTail.setLayoutY(bubbleContainer.getHeight());
    }
} 