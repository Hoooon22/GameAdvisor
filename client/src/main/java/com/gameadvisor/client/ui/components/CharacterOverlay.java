package com.gameadvisor.client.ui.components;

import com.gameadvisor.client.model.GameWindowInfo;
import com.sun.jna.platform.win32.WinDef.RECT;
import javafx.application.Platform;
import javafx.scene.layout.Pane;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.util.Duration;
import java.util.Random;

/**
 * 캐릭터 오버레이 관리 클래스
 * 게임 창 하단에 캐릭터를 배치하고 관리
 */
public class CharacterOverlay {
    
    private Pane overlayPane;
    private AdvisorCharacter character;
    private SpeechBubble speechBubble;
    private GameWindowInfo currentGameInfo;
    
    // 캐릭터 위치 및 상태
    private double characterX = 0;
    private double characterY = 0;
    private boolean isCharacterActive = false;
    
    // 자동 활동 타이머
    private Timeline idleActivityTimer;
    private Random random = new Random();
    
    // 물리 효과 완료 후 위치 업데이트 방지용 쿨다운
    private long lastPhysicsCompletedTime = 0;
    private static final long POSITION_UPDATE_COOLDOWN = 3000; // 3초
    
    public CharacterOverlay(Pane overlayPane) {
        this.overlayPane = overlayPane;
        initializeComponents();
        setupIdleActivity();
    }
    
    /**
     * 컴포넌트 초기화
     */
    private void initializeComponents() {
        character = new AdvisorCharacter();
        speechBubble = new SpeechBubble();
        
        // 물리 효과 완료 시 위치 동기화 콜백 설정
        character.setOnPhysicsCompleted(() -> {
            syncCharacterPosition();
            lastPhysicsCompletedTime = System.currentTimeMillis(); // 쿨다운 시작
            System.out.println("[DEBUG] 물리 효과 완료 - 위치 동기화됨, 쿨다운 시작");
        });
        
        // 캐릭터가 마우스 이벤트를 받을 수 있도록 설정
        character.setMouseTransparent(false);
        speechBubble.setMouseTransparent(true); // 말풍선은 클릭 불가
        
        // 오버레이에 추가
        overlayPane.getChildren().addAll(character, speechBubble);
        
        // 초기에는 숨김
        character.setVisible(false);
        speechBubble.setVisible(false);
    }
    
    /**
     * 게임 감지 시 캐릭터 활성화
     */
    public void activateCharacter(GameWindowInfo gameInfo) {
        GameWindowInfo previousGameInfo = this.currentGameInfo;
        this.currentGameInfo = gameInfo;
        
        if (!isCharacterActive) {
            isCharacterActive = true;
            positionCharacterAtGameBottom(gameInfo);
            character.setVisible(true);
            
            // 환영 메시지 표시
            Platform.runLater(() -> {
                character.setState(AdvisorCharacter.AnimationState.TALKING);
                speechBubble.showMessage(
                    gameInfo.getGameName() + " 플레이를 시작하셨네요!\n도움이 필요하면 언제든 말씀하세요!",
                    SpeechBubble.BubbleType.NORMAL
                );
            });
            
            // 자동 활동 시작
            startIdleActivity();
        } else {
            // 게임 창 정보가 실제로 변경되었는지 확인
            boolean gameWindowChanged = false;
            if (previousGameInfo != null) {
                gameWindowChanged = !gameInfo.getRect().equals(previousGameInfo.getRect()) ||
                                  !gameInfo.getGameName().equals(previousGameInfo.getGameName());
            }
            
            // 게임 창이 실제로 변경되었을 때만 위치 업데이트
            if (gameWindowChanged) {
                System.out.println("[DEBUG] 게임 창 변경 감지 - 위치 업데이트 실행");
                updateCharacterPosition(gameInfo);
            } else {
                // 변경되지 않았다면 경계만 업데이트
                character.setBounds(gameInfo.getRect().left, gameInfo.getRect().top, 
                                  gameInfo.getRect().right, gameInfo.getRect().bottom);
            }
        }
    }
    
    /**
     * 캐릭터 비활성화
     */
    public void deactivateCharacter() {
        isCharacterActive = false;
        character.setVisible(false);
        speechBubble.hideImmediately();
        stopIdleActivity();
    }
    
    /**
     * 게임 창 하단에 캐릭터 배치
     */
    private void positionCharacterAtGameBottom(GameWindowInfo gameInfo) {
        // 캐릭터가 물리 효과 중이거나 드래그 중일 때는 위치를 강제로 변경하지 않음
        if (character.isInPhysicsMode() || character.isBeingDragged()) {
            // 경계만 업데이트
            RECT rect = gameInfo.getRect();
            character.setBounds(rect.left, rect.top, rect.right, rect.bottom);
            System.out.println("[DEBUG] 물리 모드 중 - 경계만 업데이트: minX=" + rect.left + ", minY=" + rect.top + 
                             ", maxX=" + rect.right + ", maxY=" + rect.bottom);
            return;
        }
        
        // 물리 효과 완료 후 쿨다운 중인지 확인
        long currentTime = System.currentTimeMillis();
        boolean inCooldown = (currentTime - lastPhysicsCompletedTime) < POSITION_UPDATE_COOLDOWN;
        
        if (inCooldown) {
            System.out.println("[DEBUG] 물리 효과 쿨다운 중 - 위치 재설정 방지");
            // 경계만 업데이트
            RECT rect = gameInfo.getRect();
            character.setBounds(rect.left, rect.top, rect.right, rect.bottom);
            return;
        }
        
        RECT rect = gameInfo.getRect();
        
        // 게임 창의 실제 크기와 위치 계산
        double gameLeft = rect.left;
        double gameTop = rect.top;
        double gameWidth = rect.right - rect.left;
        double gameHeight = rect.bottom - rect.top;
        
        // 캐릭터를 게임 창 하단 중앙에 배치
        characterX = gameLeft + (gameWidth / 2) - (character.getCharacterWidth() / 2);
        characterY = rect.bottom - character.getCharacterHeight() - 5; // 게임 창 바닥에서 5px 위
        
        // 캐릭터가 게임 창 범위 내에 있는지 확인
        if (characterX < gameLeft) {
            characterX = gameLeft + 10; // 왼쪽 여백
        } else if (characterX + character.getCharacterWidth() > rect.right) {
            characterX = rect.right - character.getCharacterWidth() - 10; // 오른쪽 여백
        }
        
        Platform.runLater(() -> {
            character.setLayoutX(characterX);
            character.setLayoutY(characterY);
            
            // 캐릭터 물리 효과 경계 설정 (게임 창에 맞춤)
            character.setBounds(gameLeft, gameTop, rect.right, rect.bottom);
            
            System.out.println("[DEBUG] 캐릭터 경계값 설정: minX=" + gameLeft + ", minY=" + gameTop + 
                             ", maxX=" + rect.right + ", maxY=" + rect.bottom);
            
            // 말풍선 위치도 업데이트
            updateSpeechBubblePosition();
        });
        
        System.out.println("[DEBUG] 캐릭터 위치 설정: (" + characterX + ", " + characterY + ")");
        System.out.println("[DEBUG] 게임 창 정보: " + gameLeft + ", " + gameTop + ", " + gameWidth + "x" + gameHeight);
    }
    
    /**
     * 캐릭터 위치 업데이트 (게임 창 크기 변경 시)
     */
    private void updateCharacterPosition(GameWindowInfo gameInfo) {
        // 물리 효과 완료 후 쿨다운 중인지 확인
        long currentTime = System.currentTimeMillis();
        boolean inCooldown = (currentTime - lastPhysicsCompletedTime) < POSITION_UPDATE_COOLDOWN;
        
        if (inCooldown) {
            System.out.println("[DEBUG] 물리 효과 쿨다운 중 - 위치 업데이트 건너뜀 (남은 시간: " + 
                             (POSITION_UPDATE_COOLDOWN - (currentTime - lastPhysicsCompletedTime)) + "ms)");
            // 경계만 업데이트
            RECT rect = gameInfo.getRect();
            character.setBounds(rect.left, rect.top, rect.right, rect.bottom);
            return;
        }
        
        if (isCharacterActive && !character.isInPhysicsMode() && !character.isBeingDragged()) {
            positionCharacterAtGameBottom(gameInfo);
        } else if (isCharacterActive) {
            // 물리 모드 중이거나 드래그 중일 때는 경계만 업데이트
            RECT rect = gameInfo.getRect();
            character.setBounds(rect.left, rect.top, rect.right, rect.bottom);
        }
    }
    
    /**
     * 말풍선 위치 업데이트 (캐릭터의 실제 위치 기반)
     */
    private void updateSpeechBubblePosition() {
        Platform.runLater(() -> {
            speechBubble.positionAboveCharacter(
                character.getLayoutX(), 
                character.getLayoutY(), 
                character.getCharacterWidth()
            );
        });
    }
    
    /**
     * 캐릭터의 실제 위치로 추적 변수 동기화
     */
    public void syncCharacterPosition() {
        characterX = character.getLayoutX();
        characterY = character.getLayoutY();
        System.out.println("[DEBUG] 캐릭터 위치 동기화: (" + characterX + ", " + characterY + ")");
    }
    
    /**
     * 캐릭터에게 메시지 말하게 하기
     */
    public void makeCharacterSpeak(String message, SpeechBubble.BubbleType bubbleType) {
        if (!isCharacterActive) return;
        
        Platform.runLater(() -> {
            // 물리 모드가 아닐 때만 상태 변경
            if (!character.isInPhysicsMode()) {
                character.setState(AdvisorCharacter.AnimationState.TALKING);
            }
            updateSpeechBubblePosition();
            speechBubble.showMessage(message, bubbleType);
            
            // 물리 모드 중일 때는 말풍선 위치를 지속적으로 업데이트
            if (character.isInPhysicsMode()) {
                Timeline bubbleUpdateTimer = new Timeline(
                    new KeyFrame(Duration.millis(50), e -> updateSpeechBubblePosition())
                );
                bubbleUpdateTimer.setCycleCount(60); // 3초간 업데이트 (50ms × 60)
                bubbleUpdateTimer.play();
            }
        });
    }
    
    /**
     * 캐릭터 걷기 (게임 창 내에서)
     */
    public void makeCharacterWalk() {
        if (!isCharacterActive || currentGameInfo == null || character.isInPhysicsMode() || character.isBeingDragged()) return;
        
        RECT rect = currentGameInfo.getRect();
        double gameWidth = rect.right - rect.left;
        
        // 게임 창 내에서 랜덤한 위치로 이동 (하단 고정)
        double minX = rect.left + 20;
        double maxX = rect.right - character.getCharacterWidth() - 20;
        
        if (maxX <= minX) return; // 게임 창이 너무 작으면 이동하지 않음
        
        double targetX = minX + random.nextDouble() * (maxX - minX);
        double targetY = rect.bottom - character.getCharacterHeight() - 5;
        
        Platform.runLater(() -> {
            // 캐릭터의 현재 실제 위치 기반으로 상대적 이동 계산
            double currentX = character.getLayoutX();
            double deltaX = targetX - currentX;
            
            character.walkTo(deltaX, 0);
            
            // 걷기가 완료된 후 위치 동기화
            Timeline syncAfterWalk = new Timeline(
                new KeyFrame(Duration.seconds(2.1), e -> syncCharacterPosition()) // 걷기 완료 후 동기화
            );
            syncAfterWalk.play();
            
            // 걷는 동안 말풍선 위치 지속 업데이트
            Timeline updateBubblePosition = new Timeline(
                new KeyFrame(Duration.millis(100), e -> updateSpeechBubblePosition())
            );
            updateBubblePosition.setCycleCount(20); // 2초간 업데이트
            updateBubblePosition.play();
        });
    }
    
    /**
     * 자동 활동 설정
     */
    private void setupIdleActivity() {
        idleActivityTimer = new Timeline(
            new KeyFrame(Duration.seconds(10), e -> performRandomActivity()) // 10초마다 활동
        );
        idleActivityTimer.setCycleCount(Timeline.INDEFINITE);
    }
    
    /**
     * 자동 활동 시작
     */
    private void startIdleActivity() {
        if (idleActivityTimer != null) {
            idleActivityTimer.play();
        }
    }
    
    /**
     * 자동 활동 중지
     */
    private void stopIdleActivity() {
        if (idleActivityTimer != null) {
            idleActivityTimer.stop();
        }
    }
    
    /**
     * 랜덤 활동 수행
     */
    private void performRandomActivity() {
        if (!isCharacterActive) return;
        
        // 캐릭터가 물리 효과 중이거나 드래그 중이면 자동 활동하지 않음
        if (character.isInPhysicsMode() || character.isBeingDragged()) {
            return;
        }
        
        int activity = random.nextInt(4);
        
        switch (activity) {
            case 0:
                // 걷기
                makeCharacterWalk();
                break;
            case 1:
                // 생각하기
                Platform.runLater(() -> {
                    character.setState(AdvisorCharacter.AnimationState.THINKING);
                });
                break;
            case 2:
                // 조언 말하기
                String[] tips = {
                    "열심히 플레이하고 계시네요! 👍",
                    "잠깐 휴식을 취하는 것도 좋아요! ☕",
                    "집중해서 플레이하고 계시는군요! 🎯",
                    "게임을 즐기고 계신가요? 😊",
                    "어려운 부분이 있으면 도움을 요청하세요! 🆘"
                };
                makeCharacterSpeak(tips[random.nextInt(tips.length)], SpeechBubble.BubbleType.ADVICE);
                break;
            case 3:
                // 가만히 서있기 (기본 상태)
                Platform.runLater(() -> {
                    character.setState(AdvisorCharacter.AnimationState.IDLE);
                });
                break;
        }
    }
    
    /**
     * 게임별 특별 조언 제공
     */
    public void provideGameSpecificAdvice(String gameName) {
        if (!isCharacterActive) return;
        
        String advice = getGameSpecificAdvice(gameName);
        makeCharacterSpeak(advice, SpeechBubble.BubbleType.ADVICE);
    }
    
    /**
     * 게임별 조언 생성
     */
    private String getGameSpecificAdvice(String gameName) {
        String lowerGameName = gameName.toLowerCase();
        
        if (lowerGameName.contains("league") || lowerGameName.contains("lol")) {
            return "LoL 플레이 중이시네요! 미니맵을 자주 확인하세요! 🗺️";
        } else if (lowerGameName.contains("overwatch")) {
            return "오버워치! 팀과의 협력이 중요해요! 🤝";
        } else if (lowerGameName.contains("minecraft")) {
            return "마인크래프트에서 창의력을 발휘해보세요! ⛏️";
        } else if (lowerGameName.contains("valorant")) {
            return "발로란트! 정확한 에임과 전략이 중요해요! 🎯";
        } else if (lowerGameName.contains("steam")) {
            return "Steam 게임을 플레이 중이시네요! 즐거운 시간 되세요! 🎮";
        } else {
            return "멋진 게임이네요! 즐거운 플레이 되세요! 🎉";
        }
    }
    
    /**
     * 리소스 정리
     */
    public void cleanup() {
        stopIdleActivity();
        if (character != null) {
            character.cleanup();
        }
        if (speechBubble != null) {
            speechBubble.hideImmediately();
        }
    }
    
    /**
     * 캐릭터 활성 상태 반환
     */
    public boolean isCharacterActive() {
        return isCharacterActive;
    }
} 