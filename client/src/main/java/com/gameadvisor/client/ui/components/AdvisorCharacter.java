package com.gameadvisor.client.ui.components;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.Cursor;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Duration;

/**
 * 게임 어드바이저 비서 캐릭터
 * 게임 화면 하단에서 걸어다니며 도움말과 조언을 제공하는 캐릭터
 * 마우스로 잡고 날릴 수 있는 물리 효과 포함
 */
public class AdvisorCharacter extends Group {
    
    // 캐릭터 크기 상수
    private static final double CHARACTER_WIDTH = 60;
    private static final double CHARACTER_HEIGHT = 80;
    
    // 물리 효과 상수 (더 자연스러운 값들)
    private static final double GRAVITY = 400; // 중력 가속도 (픽셀/초²)
    private static final double BOUNCE_DAMPING = 0.75; // 바운스 감쇠율
    private static final double AIR_FRICTION = 0.995; // 공기 마찰력 (더 약하게)
    private static final double GROUND_FRICTION = 0.9; // 바닥 마찰력 (더 약하게)
    private static final double MIN_VELOCITY = 1; // 최소 속도 (이하로 떨어지면 정지)
    
    // 애니메이션 상태
    public enum AnimationState {
        IDLE,       // 서있기
        WALKING,    // 걷기
        TALKING,    // 말하기
        THINKING,   // 생각하기
        DRAGGING,   // 드래그 중
        FLYING,     // 날아가는 중
        STUNNED     // 기절 (충돌 후)
    }
    
    private AnimationState currentState = AnimationState.IDLE;
    private Timeline walkAnimation;
    private Timeline idleAnimation;
    private Timeline physicsAnimation;
    private double targetX;
    private double targetY;
    
    // 드래그 관련 변수
    private boolean isDragging = false;
    private double mouseStartX, mouseStartY;
    private double dragStartX, dragStartY;
    
    // 물리 효과 변수
    private double velocityX = 0;
    private double velocityY = 0;
    private boolean isFlying = false;
    private double minX = 0, maxX = 800;  // 경계 설정 (나중에 동적으로 설정)
    private double minY = 0, maxY = 600;
    private long lastCollisionTime = 0; // 마지막 충돌 시간 (중복 방지용)
    
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
    
    // 물리 효과 완료 콜백
    private Runnable onPhysicsCompleted;
    
    public AdvisorCharacter() {
        initializeCharacter();
        setupIdleAnimation();
        setupMouseHandlers();
        setupPhysics();
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
                // 드래그 중이면 애니메이션 중지
                if (isDragging) {
                    idleAnimation.stop();
                    return;
                }
                // 원래 위치
                head.setScaleY(1.0);
                body.setScaleY(1.0);
            }),
            new KeyFrame(Duration.seconds(1), e -> {
                // 드래그 중이면 애니메이션 중지
                if (isDragging) {
                    idleAnimation.stop();
                    return;
                }
                // 살짝 위아래로 움직이는 호흡 효과
                head.setScaleY(1.05);
                body.setScaleY(1.02);
            }),
            new KeyFrame(Duration.seconds(2), e -> {
                // 드래그 중이면 애니메이션 중지
                if (isDragging) {
                    idleAnimation.stop();
                    return;
                }
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
                // 드래그 중이면 애니메이션 중지
                if (isDragging) {
                    walkAnimation.stop();
                    return;
                }
                leftLeg.setRotate(0);
                rightLeg.setRotate(0);
                leftArm.setRotate(0);
                rightArm.setRotate(0);
            }),
            new KeyFrame(Duration.millis(250), e -> {
                if (isDragging) {
                    walkAnimation.stop();
                    return;
                }
                leftLeg.setRotate(20);
                rightLeg.setRotate(-20);
                leftArm.setRotate(-15);
                rightArm.setRotate(15);
            }),
            new KeyFrame(Duration.millis(500), e -> {
                if (isDragging) {
                    walkAnimation.stop();
                    return;
                }
                leftLeg.setRotate(0);
                rightLeg.setRotate(0);
                leftArm.setRotate(0);
                rightArm.setRotate(0);
            }),
            new KeyFrame(Duration.millis(750), e -> {
                if (isDragging) {
                    walkAnimation.stop();
                    return;
                }
                leftLeg.setRotate(-20);
                rightLeg.setRotate(20);
                leftArm.setRotate(15);
                rightArm.setRotate(-15);
            }),
            new KeyFrame(Duration.millis(1000), e -> {
                if (isDragging) {
                    walkAnimation.stop();
                    return;
                }
                leftLeg.setRotate(0);
                rightLeg.setRotate(0);
                leftArm.setRotate(0);
                rightArm.setRotate(0);
            })
        );
        walkAnimation.setCycleCount(Animation.INDEFINITE);
    }
    
    /**
     * 마우스 이벤트 핸들러 설정
     */
    private void setupMouseHandlers() {
        // 캐릭터가 마우스 이벤트를 받을 수 있도록 설정
        this.setPickOnBounds(true);
        
        // 마우스 커서 변경
        this.setOnMouseEntered(e -> {
            if (!isFlying) {
                this.setCursor(Cursor.HAND);
            }
            e.consume(); // 이벤트 소비하여 게임으로 전달 방지
        });
        
        this.setOnMouseExited(e -> {
            this.setCursor(Cursor.DEFAULT);
            e.consume();
        });
        
        // 테스트용: 더블클릭으로 즉시 날리기
        this.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2 && !isFlying) {
                System.out.println("[DEBUG] 더블클릭 테스트 - 캐릭터를 오른쪽으로 날립니다!");
                velocityX = 100; // 오른쪽으로
                velocityY = -50; // 약간 위로
                startFlying();
            }
            e.consume();
        });
        
        // 마우스 드래그 시작
        this.setOnMousePressed(e -> {
            if (!isFlying) {
                isDragging = true;
                mouseStartX = e.getSceneX();
                mouseStartY = e.getSceneY();
                dragStartX = this.getLayoutX();
                dragStartY = this.getLayoutY();
                
                System.out.println("[DEBUG] 드래그 시작 - 마우스: (" + (int)mouseStartX + ", " + (int)mouseStartY + 
                                 "), 캐릭터: (" + (int)dragStartX + ", " + (int)dragStartY + ")");
                System.out.println("[DEBUG] 경계값 - minX: " + minX + ", maxX: " + maxX + ", minY: " + minY + ", maxY: " + maxY);
                
                setState(AnimationState.DRAGGING);
                
                // 드래그 중 캐릭터가 더 명확하게 보이도록
                this.setOpacity(0.9);
                this.setScaleX(1.1);
                this.setScaleY(1.1);
                
                setupDraggingAnimation();
            }
            e.consume();
        });
        
        // 마우스 드래그 중
        this.setOnMouseDragged(e -> {
            if (isDragging) {
                // 드래그 중에는 모든 다른 애니메이션 정지
                stopAllAnimations();
                
                double deltaX = e.getSceneX() - mouseStartX;
                double deltaY = e.getSceneY() - mouseStartY;
                
                // 부드러운 위치 업데이트
                Platform.runLater(() -> {
                    this.setLayoutX(dragStartX + deltaX);
                    this.setLayoutY(dragStartY + deltaY);
                });
                
                // 드래그 거리에 따른 시각적 피드백
                double distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
                double intensity = Math.min(distance / 100, 1.0);
                
                // 더 강하게 끌수록 캐릭터가 더 변형됨
                this.setScaleX(1.1 + intensity * 0.2);
                this.setScaleY(1.1 + intensity * 0.2);
                this.setRotate(Math.atan2(deltaY, deltaX) * 180 / Math.PI * 0.1); // 살짝 기울임
            }
            e.consume();
        });
        
        // 마우스 드래그 종료 (캐릭터 날리기)
        this.setOnMouseReleased(e -> {
            if (isDragging) {
                isDragging = false;
                
                // 시각적 효과 원복
                this.setOpacity(1.0);
                this.setScaleX(1.0);
                this.setScaleY(1.0);
                this.setRotate(0);
                
                // 드래그 거리와 속도 계산
                double deltaX = e.getSceneX() - mouseStartX;
                double deltaY = e.getSceneY() - mouseStartY;
                double distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
                
                System.out.println("[DEBUG] 드래그 거리: " + distance + ", deltaX: " + deltaX + ", deltaY: " + deltaY);
                
                if (distance > 15) { // 최소 드래그 거리
                    // 속도 계산 개선 (훨씬 더 강한 힘)
                    double power = Math.min(distance, 200); // 최대 속도 증가
                    velocityX = (deltaX / distance) * power;
                    velocityY = (deltaY / distance) * power;
                    
                    System.out.println("[DEBUG] 계산된 속도 - velocityX: " + velocityX + ", velocityY: " + velocityY);
                    System.out.println("[DEBUG] 정규화된 방향 - dirX: " + (deltaX / distance) + ", dirY: " + (deltaY / distance));
                    
                    startFlying();
                } else {
                    System.out.println("[DEBUG] 드래그 거리가 너무 작음, IDLE 상태로");
                    setState(AnimationState.IDLE);
                }
            }
            e.consume();
        });
    }
    
    /**
     * 물리 효과 시스템 설정
     */
    private void setupPhysics() {
        physicsAnimation = new Timeline(
            new KeyFrame(Duration.millis(30), e -> updatePhysics()) // 더 자주 업데이트
        );
        physicsAnimation.setCycleCount(Animation.INDEFINITE);
    }
    
    /**
     * 물리 효과 업데이트 (16ms마다 호출)
     */
    private void updatePhysics() {
        if (!isFlying) return;
        
        double deltaTime = 0.05; // 더 큰 시간 단위로 움직임을 더 명확하게
        
        // 중력 적용
        velocityY += GRAVITY * deltaTime;
        
        // 위치 업데이트
        double currentX = this.getLayoutX();
        double currentY = this.getLayoutY();
        double newX = currentX + velocityX * deltaTime;
        double newY = currentY + velocityY * deltaTime;
        
        // 디버깅: 처음 몇 프레임만 출력
        if (Math.abs(velocityX) > 1 || Math.abs(velocityY) > 1) {
            if (Math.random() < 0.1) { // 10% 확률로만 출력 (너무 많은 로그 방지)
                System.out.println("[DEBUG] 물리 업데이트 - 현재위치: (" + (int)currentX + ", " + (int)currentY + 
                                 "), 새 위치: (" + (int)newX + ", " + (int)newY + 
                                 "), 속도: (" + (int)velocityX + ", " + (int)velocityY + ")");
            }
        }
        
        // 벽 충돌 검사 및 처리 (개선된 버전)
        boolean collisionOccurred = false;
        
        // 좌우 벽 충돌
        if (newX <= minX) {
            newX = minX;
            if (velocityX < 0) { // 왼쪽으로 가던 중이면
                velocityX = -velocityX * BOUNCE_DAMPING;
                collisionOccurred = true;
            }
        } else if (newX >= maxX - CHARACTER_WIDTH) {
            newX = maxX - CHARACTER_WIDTH;
            if (velocityX > 0) { // 오른쪽으로 가던 중이면
                velocityX = -velocityX * BOUNCE_DAMPING;
                collisionOccurred = true;
            }
        }
        
        // 상하 벽 충돌
        if (newY <= minY) {
            newY = minY;
            if (velocityY < 0) { // 위로 가던 중이면
                velocityY = -velocityY * BOUNCE_DAMPING;
                collisionOccurred = true;
            }
        } else if (newY >= maxY - CHARACTER_HEIGHT) {
            newY = maxY - CHARACTER_HEIGHT;
            if (velocityY > 0) { // 아래로 가던 중이면
                // 바닥 충돌 시 더 강한 감쇠 적용
                velocityY = -velocityY * BOUNCE_DAMPING * 0.5; // 추가 감쇠
                collisionOccurred = true;
                
                // 속도가 너무 작으면 완전히 정지
                if (Math.abs(velocityY) < 15) {
                    velocityY = 0;
                }
                
                // 바닥 충돌 시 약간의 랜덤 효과 추가 (속도가 충분할 때만)
                if (Math.abs(velocityX) > 10) {
                    velocityX += (Math.random() - 0.5) * 2;
                }
            }
        }
        
        // 충돌 시 애니메이션 효과 (쿨다운 적용)
        if (collisionOccurred) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastCollisionTime > 200) { // 200ms 쿨다운
                createCollisionEffect();
                lastCollisionTime = currentTime;
            }
        }
        
        // 위치 적용
        this.setLayoutX(newX);
        this.setLayoutY(newY);
        
        // 마찰력 적용 (개선된 버전)
        boolean onGround = (newY >= maxY - CHARACTER_HEIGHT);
        
        if (onGround) {
            // 바닥에 있을 때: 강한 마찰력
            velocityX *= GROUND_FRICTION;
            velocityY *= GROUND_FRICTION;
            
            // 바닥에서 매우 작은 수직 속도는 완전히 제거
            if (Math.abs(velocityY) < 5) {
                velocityY = 0;
            }
        } else {
            // 공중에 있을 때: 약한 공기 마찰력
            velocityX *= AIR_FRICTION;
            velocityY *= AIR_FRICTION;
        }
        
        // 정지 조건 개선 (바닥에 있고 속도가 매우 작을 때)
        if (onGround && Math.abs(velocityX) < MIN_VELOCITY && Math.abs(velocityY) < MIN_VELOCITY) {
            stopFlying();
        } else if (!onGround && Math.abs(velocityX) < MIN_VELOCITY && Math.abs(velocityY) < MIN_VELOCITY * 2) {
            stopFlying();
        }
    }
    
    /**
     * 날아가기 시작
     */
    private void startFlying() {
        System.out.println("[DEBUG] 날아가기 시작! isFlying: " + isFlying + ", 속도: (" + (int)velocityX + ", " + (int)velocityY + ")");
        isFlying = true;
        setState(AnimationState.FLYING);
        setupFlyingAnimation();
        physicsAnimation.play();
        System.out.println("[DEBUG] 물리 애니메이션 시작됨");
    }
    
    /**
     * 날아가기 종료
     */
    private void stopFlying() {
        isFlying = false;
        velocityX = 0;
        velocityY = 0;
        physicsAnimation.stop();
        
        // 위치 동기화 콜백 호출
        if (onPhysicsCompleted != null) {
            Platform.runLater(onPhysicsCompleted);
        }
        
        // 잠시 기절 상태로 변경 후 idle로 복귀
        setState(AnimationState.STUNNED);
        
        Timeline recoveryTimer = new Timeline(
            new KeyFrame(Duration.seconds(1), e -> setState(AnimationState.IDLE))
        );
        recoveryTimer.play();
    }
    
    /**
     * 충돌 효과 생성 (더 부드럽고 재미있는 버전)
     */
    private void createCollisionEffect() {
        // 색상 변화 효과 (더 부드럽게)
        Timeline colorEffect = new Timeline(
            new KeyFrame(Duration.millis(0), e -> {
                head.setFill(Color.ORANGE);
                body.setFill(Color.ORANGE);
            }),
            new KeyFrame(Duration.millis(50), e -> {
                head.setFill(Color.YELLOW);
                body.setFill(Color.YELLOW);
            }),
            new KeyFrame(Duration.millis(150), e -> {
                head.setFill(Color.LIGHTBLUE);
                body.setFill(Color.LIGHTCYAN);
            })
        );
        colorEffect.play();
        
        // 더 부드러운 진동 효과
        Timeline shakeEffect = new Timeline();
        for (int i = 0; i < 15; i++) {
            int finalI = i;
            double intensity = 1.0 - (finalI / 15.0); // 점점 약해지는 진동
            shakeEffect.getKeyFrames().add(
                new KeyFrame(Duration.millis(i * 15), e -> {
                    double offsetX = Math.sin(finalI * 0.8) * intensity * 3;
                    double offsetY = Math.cos(finalI * 0.8) * intensity * 1.5;
                    characterBody.setTranslateX(offsetX);
                    characterBody.setTranslateY(offsetY);
                })
            );
        }
        shakeEffect.getKeyFrames().add(
            new KeyFrame(Duration.millis(225), e -> {
                characterBody.setTranslateX(0);
                characterBody.setTranslateY(0);
            })
        );
        shakeEffect.play();
        
        // 눈이 어지러워하는 효과
        Timeline eyeEffect = new Timeline(
            new KeyFrame(Duration.millis(0), e -> {
                leftEye.setScaleX(0.5);
                rightEye.setScaleX(0.5);
            }),
            new KeyFrame(Duration.millis(100), e -> {
                leftEye.setScaleX(1.5);
                rightEye.setScaleX(1.5);
            }),
            new KeyFrame(Duration.millis(200), e -> {
                leftEye.setScaleX(1.0);
                rightEye.setScaleX(1.0);
            })
        );
        eyeEffect.play();
    }
    
    /**
     * 경계 설정 (게임 창 크기에 맞춰)
     */
    public void setBounds(double minX, double minY, double maxX, double maxY) {
        this.minX = minX;
        this.minY = minY;
        this.maxX = maxX;
        this.maxY = maxY;
    }
    
    /**
     * 캐릭터가 물리 효과 중인지 확인
     */
    public boolean isInPhysicsMode() {
        return isDragging || isFlying;
    }
    
    /**
     * 캐릭터가 드래그 중인지 확인
     */
    public boolean isBeingDragged() {
        return isDragging;
    }
    
    /**
     * 물리 효과 완료 시 호출될 콜백 설정
     */
    public void setOnPhysicsCompleted(Runnable callback) {
        this.onPhysicsCompleted = callback;
    }
    
    /**
     * 지정된 위치로 걸어가기
     */
    public void walkTo(double x, double y) {
        // 드래그 중이거나 물리 효과 중일 때는 걷지 않음
        if (isDragging || isFlying) {
            return;
        }
        
        targetX = x;
        targetY = y;
        
        setState(AnimationState.WALKING);
        
        TranslateTransition moveTransition = new TranslateTransition(Duration.seconds(2), this);
        moveTransition.setToX(x);
        moveTransition.setToY(y);
        
        moveTransition.setOnFinished(e -> {
            // 완료 시에도 드래그 중이 아닐 때만 IDLE 상태로
            if (!isDragging) {
                setState(AnimationState.IDLE);
            }
        });
        moveTransition.play();
    }
    
    /**
     * 애니메이션 상태 설정
     */
    public void setState(AnimationState state) {
        // 드래그 중일 때는 외부에서 상태 변경 방지 (드래그 관련 상태는 제외)
        if (isDragging && state != AnimationState.DRAGGING && state != AnimationState.FLYING) {
            return;
        }
        
        // 기존 애니메이션 중지
        if (idleAnimation != null) idleAnimation.stop();
        if (walkAnimation != null) walkAnimation.stop();
        
        currentState = state;
        
        switch (state) {
            case IDLE:
                // 드래그 중이 아닐 때만 idle 애니메이션 시작
                if (!isDragging) {
                    idleAnimation.play();
                }
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
            case DRAGGING:
                // 드래그 애니메이션
                setupDraggingAnimation();
                break;
            case FLYING:
                // 날아가는 애니메이션
                setupFlyingAnimation();
                break;
            case STUNNED:
                // 기절 애니메이션
                setupStunnedAnimation();
                break;
        }
    }
    
    /**
     * 말하기 애니메이션 설정
     */
    private void setupTalkingAnimation() {
        Timeline talkAnimation = new Timeline(
            new KeyFrame(Duration.millis(0), e -> {
                if (isDragging) return;
                leftEye.setScaleY(1.0);
                rightEye.setScaleY(1.0);
            }),
            new KeyFrame(Duration.millis(300), e -> {
                if (isDragging) return;
                leftEye.setScaleY(0.2);
                rightEye.setScaleY(0.2);
            }),
            new KeyFrame(Duration.millis(400), e -> {
                if (isDragging) return;
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
                if (isDragging) return;
                head.setRotate(0);
            }),
            new KeyFrame(Duration.millis(500), e -> {
                if (isDragging) return;
                head.setRotate(-10);
            }),
            new KeyFrame(Duration.millis(1000), e -> {
                if (isDragging) return;
                head.setRotate(10);
            }),
            new KeyFrame(Duration.millis(1500), e -> {
                if (isDragging) return;
                head.setRotate(0);
            })
        );
        thinkAnimation.setCycleCount(2);
        thinkAnimation.setOnFinished(e -> {
            // 완료 시에도 드래그 중이 아닐 때만 IDLE 상태로
            if (!isDragging) {
                setState(AnimationState.IDLE);
            }
        });
        thinkAnimation.play();
    }
    
    /**
     * 드래그 애니메이션 설정
     */
    private void setupDraggingAnimation() {
        stopAllAnimations();
        
        // 드래그 중에는 팔다리가 벌어지는 애니메이션
        Timeline dragAnimation = new Timeline(
            new KeyFrame(Duration.millis(0), e -> {
                leftArm.setRotate(45);
                rightArm.setRotate(-45);
                leftLeg.setRotate(15);
                rightLeg.setRotate(-15);
                head.setScaleX(1.1);
                head.setScaleY(1.1);
                
                // 눈이 놀란 표정으로 (타원형)
                leftEye.setScaleX(1.3);
                leftEye.setScaleY(1.6);
                rightEye.setScaleX(1.3);
                rightEye.setScaleY(1.6);
            })
        );
        dragAnimation.play();
    }
    
    /**
     * 날아가는 애니메이션 설정
     */
    private void setupFlyingAnimation() {
        stopAllAnimations();
        
        // 날아가는 중에는 팔다리가 펼쳐진 상태로 약간씩 움직임
        Timeline flyAnimation = new Timeline(
            new KeyFrame(Duration.millis(0), e -> {
                leftArm.setRotate(60);
                rightArm.setRotate(-60);
                leftLeg.setRotate(30);
                rightLeg.setRotate(-30);
                
                // 머리가 약간 뒤로 젖혀짐
                head.setRotate(-5);
                
                // 놀란 눈 (크게)
                leftEye.setScaleX(1.6);
                leftEye.setScaleY(2.0);
                rightEye.setScaleX(1.6);
                rightEye.setScaleY(2.0);
            }),
            new KeyFrame(Duration.millis(200), e -> {
                leftArm.setRotate(70);
                rightArm.setRotate(-70);
                head.setRotate(5);
            }),
            new KeyFrame(Duration.millis(400), e -> {
                leftArm.setRotate(60);
                rightArm.setRotate(-60);
                head.setRotate(-5);
            })
        );
        flyAnimation.setCycleCount(Animation.INDEFINITE);
        flyAnimation.play();
    }
    
    /**
     * 기절 애니메이션 설정
     */
    private void setupStunnedAnimation() {
        stopAllAnimations();
        
        // 기절 상태: 별이 도는 효과와 함께 캐릭터가 흔들림
        Timeline stunnedAnimation = new Timeline(
            new KeyFrame(Duration.millis(0), e -> {
                // 팔다리가 늘어진 상태
                leftArm.setRotate(15);
                rightArm.setRotate(-15);
                leftLeg.setRotate(5);
                rightLeg.setRotate(-5);
                
                // 머리가 약간 기울어짐
                head.setRotate(10);
                
                // 눈이 X자 모양 (어지러운 표정)
                leftEye.setScaleX(0.6);
                leftEye.setScaleY(2.3);
                rightEye.setScaleX(0.6);
                rightEye.setScaleY(2.3);
            }),
            new KeyFrame(Duration.millis(250), e -> {
                head.setRotate(-10);
                characterBody.setRotate(2);
            }),
            new KeyFrame(Duration.millis(500), e -> {
                head.setRotate(10);
                characterBody.setRotate(-2);
            }),
            new KeyFrame(Duration.millis(750), e -> {
                head.setRotate(-5);
                characterBody.setRotate(0);
            }),
            new KeyFrame(Duration.millis(1000), e -> {
                // 원래 상태로 복귀
                resetCharacterPose();
            })
        );
        stunnedAnimation.play();
    }
    
    /**
     * 캐릭터 포즈를 원래 상태로 리셋
     */
    private void resetCharacterPose() {
        leftArm.setRotate(0);
        rightArm.setRotate(0);
        leftLeg.setRotate(0);
        rightLeg.setRotate(0);
        head.setRotate(0);
        head.setScaleX(1.0);
        head.setScaleY(1.0);
        characterBody.setRotate(0);
        
        // 눈을 원래 크기로
        leftEye.setScaleX(1.0);
        leftEye.setScaleY(1.0);
        rightEye.setScaleX(1.0);
        rightEye.setScaleY(1.0);
    }
    
    /**
     * 모든 애니메이션 중지
     */
    private void stopAllAnimations() {
        if (walkAnimation != null) walkAnimation.stop();
        if (idleAnimation != null) idleAnimation.stop();
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
        if (physicsAnimation != null) physicsAnimation.stop();
    }
} 