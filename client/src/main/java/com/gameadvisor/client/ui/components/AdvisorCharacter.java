package com.gameadvisor.client.ui.components;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Duration;

/**
 * 게임 어드바이저 비서 캐릭터
 * 게임 화면 하단에서 걸어다니며 도움말과 조언을 제공하는 캐릭터
 */
public class AdvisorCharacter extends Group {
    
    // 캐릭터 크기 상수
    private static final double CHARACTER_WIDTH = 60;
    private static final double CHARACTER_HEIGHT = 80;
    
    // 애니메이션 상태
    public enum AnimationState {
        IDLE,       // 서있기
        WALKING,    // 걷기
        TALKING,    // 말하기
        THINKING    // 생각하기
    }
    
    private AnimationState currentState = AnimationState.IDLE;
    private Timeline walkAnimation;
    private Timeline idleAnimation;
    private double targetX;
    private double targetY;
    
    // 캐릭터 구성 요소들
    private Group characterBody;
    private Circle head;
    private Rectangle body;
    private Rectangle leftLeg;
    private Rectangle rightLeg;
    private Rectangle leftArm;
    private Rectangle rightArm;
    private Circle leftEye;
    private Circle rightEye;
    
    public AdvisorCharacter() {
        initializeCharacter();
        setupIdleAnimation();
    }
    
    /**
     * 캐릭터 모양 초기화
     */
    private void initializeCharacter() {
        characterBody = new Group();
        
        // 머리
        head = new Circle(20);
        head.setFill(Color.LIGHTBLUE);
        head.setStroke(Color.DARKBLUE);
        head.setStrokeWidth(2);
        head.setCenterX(CHARACTER_WIDTH / 2);
        head.setCenterY(20);
        
        // 몸체
        body = new Rectangle(CHARACTER_WIDTH / 2 - 15, 35, 30, 35);
        body.setFill(Color.LIGHTCYAN);
        body.setStroke(Color.DARKBLUE);
        body.setStrokeWidth(2);
        body.setArcWidth(10);
        body.setArcHeight(10);
        
        // 팔
        leftArm = new Rectangle(CHARACTER_WIDTH / 2 - 25, 40, 8, 25);
        leftArm.setFill(Color.LIGHTBLUE);
        leftArm.setStroke(Color.DARKBLUE);
        leftArm.setStrokeWidth(1);
        leftArm.setArcWidth(4);
        leftArm.setArcHeight(4);
        
        rightArm = new Rectangle(CHARACTER_WIDTH / 2 + 17, 40, 8, 25);
        rightArm.setFill(Color.LIGHTBLUE);
        rightArm.setStroke(Color.DARKBLUE);
        rightArm.setStrokeWidth(1);
        rightArm.setArcWidth(4);
        rightArm.setArcHeight(4);
        
        // 다리
        leftLeg = new Rectangle(CHARACTER_WIDTH / 2 - 12, 70, 8, 20);
        leftLeg.setFill(Color.DARKBLUE);
        leftLeg.setStroke(Color.NAVY);
        leftLeg.setStrokeWidth(1);
        leftLeg.setArcWidth(4);
        leftLeg.setArcHeight(4);
        
        rightLeg = new Rectangle(CHARACTER_WIDTH / 2 + 4, 70, 8, 20);
        rightLeg.setFill(Color.DARKBLUE);
        rightLeg.setStroke(Color.NAVY);
        rightLeg.setStrokeWidth(1);
        rightLeg.setArcWidth(4);
        rightLeg.setArcHeight(4);
        
        // 눈
        leftEye = new Circle(CHARACTER_WIDTH / 2 - 7, 18, 3);
        leftEye.setFill(Color.BLACK);
        
        rightEye = new Circle(CHARACTER_WIDTH / 2 + 7, 18, 3);
        rightEye.setFill(Color.BLACK);
        
        // 모든 요소를 그룹에 추가
        characterBody.getChildren().addAll(
            body, head, leftArm, rightArm, leftLeg, rightLeg, leftEye, rightEye
        );
        
        this.getChildren().add(characterBody);
    }
    
    /**
     * 기본 서있기 애니메이션 설정
     */
    private void setupIdleAnimation() {
        idleAnimation = new Timeline(
            new KeyFrame(Duration.seconds(0), e -> {
                // 원래 위치
                head.setScaleY(1.0);
                body.setScaleY(1.0);
            }),
            new KeyFrame(Duration.seconds(1), e -> {
                // 살짝 위아래로 움직이는 호흡 효과
                head.setScaleY(1.05);
                body.setScaleY(1.02);
            }),
            new KeyFrame(Duration.seconds(2), e -> {
                head.setScaleY(1.0);
                body.setScaleY(1.0);
            })
        );
        idleAnimation.setCycleCount(Animation.INDEFINITE);
    }
    
    /**
     * 걷기 애니메이션 설정
     */
    private void setupWalkAnimation() {
        walkAnimation = new Timeline(
            new KeyFrame(Duration.millis(0), e -> {
                leftLeg.setRotate(0);
                rightLeg.setRotate(0);
                leftArm.setRotate(0);
                rightArm.setRotate(0);
            }),
            new KeyFrame(Duration.millis(250), e -> {
                leftLeg.setRotate(20);
                rightLeg.setRotate(-20);
                leftArm.setRotate(-15);
                rightArm.setRotate(15);
            }),
            new KeyFrame(Duration.millis(500), e -> {
                leftLeg.setRotate(0);
                rightLeg.setRotate(0);
                leftArm.setRotate(0);
                rightArm.setRotate(0);
            }),
            new KeyFrame(Duration.millis(750), e -> {
                leftLeg.setRotate(-20);
                rightLeg.setRotate(20);
                leftArm.setRotate(15);
                rightArm.setRotate(-15);
            }),
            new KeyFrame(Duration.millis(1000), e -> {
                leftLeg.setRotate(0);
                rightLeg.setRotate(0);
                leftArm.setRotate(0);
                rightArm.setRotate(0);
            })
        );
        walkAnimation.setCycleCount(Animation.INDEFINITE);
    }
    
    /**
     * 지정된 위치로 걸어가기
     */
    public void walkTo(double x, double y) {
        targetX = x;
        targetY = y;
        
        setState(AnimationState.WALKING);
        
        TranslateTransition moveTransition = new TranslateTransition(Duration.seconds(2), this);
        moveTransition.setToX(x);
        moveTransition.setToY(y);
        
        moveTransition.setOnFinished(e -> setState(AnimationState.IDLE));
        moveTransition.play();
    }
    
    /**
     * 애니메이션 상태 설정
     */
    public void setState(AnimationState state) {
        // 기존 애니메이션 중지
        if (idleAnimation != null) idleAnimation.stop();
        if (walkAnimation != null) walkAnimation.stop();
        
        currentState = state;
        
        switch (state) {
            case IDLE:
                idleAnimation.play();
                break;
            case WALKING:
                setupWalkAnimation();
                walkAnimation.play();
                break;
            case TALKING:
                // 말하기 애니메이션 (눈 깜빡이기 등)
                setupTalkingAnimation();
                break;
            case THINKING:
                // 생각하기 애니메이션
                setupThinkingAnimation();
                break;
        }
    }
    
    /**
     * 말하기 애니메이션 설정
     */
    private void setupTalkingAnimation() {
        Timeline talkAnimation = new Timeline(
            new KeyFrame(Duration.millis(0), e -> {
                leftEye.setScaleY(1.0);
                rightEye.setScaleY(1.0);
            }),
            new KeyFrame(Duration.millis(300), e -> {
                leftEye.setScaleY(0.2);
                rightEye.setScaleY(0.2);
            }),
            new KeyFrame(Duration.millis(400), e -> {
                leftEye.setScaleY(1.0);
                rightEye.setScaleY(1.0);
            })
        );
        talkAnimation.setCycleCount(3);
        talkAnimation.play();
    }
    
    /**
     * 생각하기 애니메이션 설정
     */
    private void setupThinkingAnimation() {
        Timeline thinkAnimation = new Timeline(
            new KeyFrame(Duration.millis(0), e -> {
                head.setRotate(0);
            }),
            new KeyFrame(Duration.millis(500), e -> {
                head.setRotate(-10);
            }),
            new KeyFrame(Duration.millis(1000), e -> {
                head.setRotate(10);
            }),
            new KeyFrame(Duration.millis(1500), e -> {
                head.setRotate(0);
            })
        );
        thinkAnimation.setCycleCount(2);
        thinkAnimation.setOnFinished(e -> setState(AnimationState.IDLE));
        thinkAnimation.play();
    }
    
    /**
     * 캐릭터 크기 반환
     */
    public double getCharacterWidth() {
        return CHARACTER_WIDTH;
    }
    
    public double getCharacterHeight() {
        return CHARACTER_HEIGHT;
    }
    
    /**
     * 현재 애니메이션 상태 반환
     */
    public AnimationState getCurrentState() {
        return currentState;
    }
    
    /**
     * 캐릭터 정리 (애니메이션 중지)
     */
    public void cleanup() {
        if (idleAnimation != null) idleAnimation.stop();
        if (walkAnimation != null) walkAnimation.stop();
    }
} 