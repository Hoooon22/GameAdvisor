package com.gameadvisor.service.vector;

import com.gameadvisor.model.vector.BloonsTDKnowledge;
import com.gameadvisor.repository.vector.BloonsTDVectorRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class BTDAdvancedDataService {
    
    private final BloonsTDVectorRepository repository;
    
    @Autowired
    public BTDAdvancedDataService(BloonsTDVectorRepository repository) {
        this.repository = repository;
    }
    
    /**
     * 고급 BTD 전략 지식 데이터 생성
     */
    public void createAdvancedStrategies() {
        log.info("BTD 고급 전략 지식 데이터 생성 시작");
        
        try {
            List<BloonsTDKnowledge> advancedKnowledge = createAdvancedKnowledgeList();
            
            for (BloonsTDKnowledge knowledge : advancedKnowledge) {
                repository.save(knowledge);
                log.info("고급 지식 저장: {}", knowledge.getTitle());
            }
            
            log.info("BTD 고급 전략 데이터 생성 완료: {} 개", advancedKnowledge.size());
            
        } catch (Exception e) {
            log.error("BTD 고급 전략 데이터 생성 실패: {}", e.getMessage(), e);
        }
    }
    
    private List<BloonsTDKnowledge> createAdvancedKnowledgeList() {
        List<BloonsTDKnowledge> knowledgeList = new ArrayList<>();
        
        // === 초반 전략 (라운드 1-20) ===
        knowledgeList.add(BloonsTDKnowledge.builder()
                .id(UUID.randomUUID().toString())
                .situationType(BloonsTDKnowledge.SituationType.ECONOMY_BUILD)
                .roundRange("early_game")
                .difficulty("easy")
                .towerTypes(Arrays.asList("Dart Monkey", "Banana Farm"))
                .title("초보자를 위한 효율적인 시작 전략")
                .content("게임 시작 시 가장 안정적이고 효율적인 초반 진행 방법")
                .advice("첫 타워는 다트 몽키를 트랙 시작 부분에 배치하고, 라운드 3-4부터 바나나 농장을 건설하여 경제력을 확보하세요. 0-0-3 다트 몽키로 업그레이드하면 초반 블룬들을 안정적으로 처리할 수 있습니다.")
                .tags(Arrays.asList("초반", "다트몽키", "바나나농장", "경제", "초보자"))
                .embedding(generateAdvancedEmbedding("초반 시작 다트몽키 바나나농장 경제"))
                .confidence(0.92)
                .successRate(0.85)
                .usageCount(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());

        knowledgeList.add(BloonsTDKnowledge.builder()
                .id(UUID.randomUUID().toString())
                .situationType(BloonsTDKnowledge.SituationType.LEAD_POPPING)
                .roundRange("early_game")
                .difficulty("medium")
                .towerTypes(Arrays.asList("Wizard", "Monkey Ace", "Mortar"))
                .title("라운드 28 납 블룬 완벽 대응법")
                .content("라운드 28에 처음 등장하는 납 블룬을 효과적으로 처리하는 전략")
                .advice("0-1-2 위저드의 파이어볼이나 2-0-0 몽키 에이스의 폭탄 공격을 준비하세요. 또는 0-2-2 모르타르로 납 블룬을 효과적으로 처리할 수 있습니다. 연금술사 3-0-0의 산성 공격도 훌륭한 대안입니다.")
                .tags(Arrays.asList("납블룬", "라운드28", "위저드", "에이스", "모르타르"))
                .embedding(generateAdvancedEmbedding("라운드 28 납 블룬 위저드 에이스 모르타르"))
                .confidence(0.88)
                .successRate(0.82)
                .usageCount(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());

        // === 중반 전략 (라운드 21-60) ===
        knowledgeList.add(BloonsTDKnowledge.builder()
                .id(UUID.randomUUID().toString())
                .situationType(BloonsTDKnowledge.SituationType.CAMO_DETECTION)
                .roundRange("mid_game")
                .difficulty("medium")
                .towerTypes(Arrays.asList("Ninja", "Monkey Village", "Submarine"))
                .title("라운드 42, 45 카모 러시 완벽 방어")
                .content("중반의 강력한 카모 블룬 러시를 안전하게 막는 전략")
                .advice("라운드 40 전에 2-0-0 몽키 빌리지나 0-0-2 닌자로 카모 탐지를 확보하세요. 4-0-2 닌자 몽키는 카모 탐지와 함께 강력한 공격력으로 카모 블룬을 처리할 수 있습니다. 물 맵에서는 2-0-3 잠수함이 매우 효과적입니다.")
                .tags(Arrays.asList("카모", "라운드42", "라운드45", "닌자", "빌리지", "잠수함"))
                .embedding(generateAdvancedEmbedding("라운드 42 45 카모 닌자 빌리지 잠수함"))
                .confidence(0.90)
                .successRate(0.88)
                .usageCount(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());

        knowledgeList.add(BloonsTDKnowledge.builder()
                .id(UUID.randomUUID().toString())
                .situationType(BloonsTDKnowledge.SituationType.CERAMIC_DEFENSE)
                .roundRange("mid_game")
                .difficulty("hard")
                .towerTypes(Arrays.asList("Boomerang", "Ice", "Glue Gunner"))
                .title("라운드 63 세라믹 러시 특수 대응법")
                .content("가장 어려운 라운드 중 하나인 63라운드 세라믹 러시 대응 전략")
                .advice("4-0-2 부메랑 몽키를 여러 개 배치하거나, 0-2-5 아이스 몽키의 절대영도로 세라믹을 즉시 파괴할 수 있습니다. 2-3-0 글루 거너로 세라믹을 느리게 만든 후 집중 공격하는 것도 효과적입니다.")
                .tags(Arrays.asList("세라믹", "라운드63", "부메랑", "아이스", "글루", "절대영도"))
                .embedding(generateAdvancedEmbedding("라운드 63 세라믹 부메랑 아이스 글루 절대영도"))
                .confidence(0.87)
                .successRate(0.79)
                .usageCount(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());

        // === 후반 전략 (라운드 61-100) ===
        knowledgeList.add(BloonsTDKnowledge.builder()
                .id(UUID.randomUUID().toString())
                .situationType(BloonsTDKnowledge.SituationType.MOAB_BATTLE)
                .roundRange("late_game")
                .difficulty("hard")
                .towerTypes(Arrays.asList("Super Monkey", "Monkey Ace", "Heli Pilot"))
                .title("라운드 80 첫 BAD 공략 전략")
                .content("게임에서 가장 강력한 블룬인 BAD를 처음 만나는 라운드 80 대응법")
                .advice("2-0-5 슈퍼 몽키의 다크 챔피언이나 5-2-0 몽키 에이스의 그라운드 제로가 효과적입니다. 5-0-2 헬리콥터 파일럿의 콤만치 커맨더로 지속적인 화력을 제공하세요. 여러 타워의 조합이 중요합니다.")
                .tags(Arrays.asList("BAD", "라운드80", "슈퍼몽키", "에이스", "헬리콥터", "다크챔피언"))
                .embedding(generateAdvancedEmbedding("라운드 80 BAD 슈퍼몽키 에이스 헬리콥터"))
                .confidence(0.85)
                .successRate(0.76)
                .usageCount(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());

        // === 보스 전략 ===
        knowledgeList.add(BloonsTDKnowledge.builder()
                .id(UUID.randomUUID().toString())
                .situationType(BloonsTDKnowledge.SituationType.BOSS_BATTLE)
                .roundRange("late_game")
                .difficulty("expert")
                .towerTypes(Arrays.asList("Super Monkey", "Monkey Village", "Alchemist", "Engineer"))
                .title("보스 블룬 (블룬아리우스) 공략 전략")
                .content("특별 이벤트 보스 블룬을 효과적으로 처리하는 고급 전략")
                .advice("5-2-0 슈퍼 몽키를 5-0-0 몽키 빌리지와 5-0-0 연금술사로 강화하세요. 5-2-0 엔지니어의 센트리 파라곤으로 추가 화력을 제공하고, 여러 개의 0-2-5 아이스 몽키로 보스를 느리게 만드는 것이 핵심입니다.")
                .tags(Arrays.asList("보스", "블룬아리우스", "슈퍼몽키", "빌리지", "연금술사", "엔지니어"))
                .embedding(generateAdvancedEmbedding("보스 블룬아리우스 슈퍼몽키 빌리지 연금술사"))
                .confidence(0.83)
                .successRate(0.72)
                .usageCount(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());

        // === 타워 조합 전략 ===
        knowledgeList.add(BloonsTDKnowledge.builder()
                .id(UUID.randomUUID().toString())
                .situationType(BloonsTDKnowledge.SituationType.TOWER_COMBO)
                .roundRange("mid_game")
                .difficulty("medium")
                .towerTypes(Arrays.asList("Ninja", "Alchemist", "Monkey Village"))
                .title("닌자-연금술사-빌리지 최강 조합")
                .content("중반부터 후반까지 활용 가능한 안정적인 타워 조합 전략")
                .advice("4-0-1 닌자 몽키를 4-2-0 연금술사와 2-3-0 몽키 빌리지 근처에 배치하세요. 연금술사의 버프와 빌리지의 추가 공격력으로 닌자의 칼리즈가 모든 블룬 타입을 관통할 수 있습니다. 비용 대비 효율이 매우 높습니다.")
                .tags(Arrays.asList("닌자", "연금술사", "빌리지", "조합", "칼리즈", "버프"))
                .embedding(generateAdvancedEmbedding("닌자 연금술사 빌리지 조합 칼리즈 버프"))
                .confidence(0.91)
                .successRate(0.87)
                .usageCount(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());

        knowledgeList.add(BloonsTDKnowledge.builder()
                .id(UUID.randomUUID().toString())
                .situationType(BloonsTDKnowledge.SituationType.TOWER_COMBO)
                .roundRange("late_game")
                .difficulty("hard")
                .towerTypes(Arrays.asList("Druid", "Monkey Village"))
                .title("드루이드 스톰 집중 공격 전략")
                .content("드루이드의 강력한 스톰 능력을 최대화하는 후반 전략")
                .advice("5-0-0 드루이드 5마리를 한 곳에 모으고 5-0-0 몽키 빌리지 근처에 배치하세요. 드루이드들의 스톰이 겹쳐서 엄청난 데미지를 입힐 수 있습니다. 특히 밀집된 블룬 그룹에게 매우 효과적입니다.")
                .tags(Arrays.asList("드루이드", "스톰", "집중공격", "빌리지", "후반"))
                .embedding(generateAdvancedEmbedding("드루이드 스톰 집중공격 빌리지 후반"))
                .confidence(0.84)
                .successRate(0.78)
                .usageCount(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());

        // === 맵별 특화 전략 ===
        knowledgeList.add(BloonsTDKnowledge.builder()
                .id(UUID.randomUUID().toString())
                .situationType("map_strategy")
                .roundRange("early_game")
                .difficulty("medium")
                .towerTypes(Arrays.asList("Monkey Buccaneer", "Submarine", "Monkey Village"))
                .title("물이 있는 맵에서의 특화 전략")
                .content("물이 포함된 맵에서만 사용할 수 있는 특별한 전략")
                .advice("3-2-0 몽키 버커니어를 물에 배치하여 추가 수입을 얻고, 2-0-4 잠수함으로 카모 탐지와 함께 강력한 공격을 하세요. 5-0-0 잠수함의 프리 차지는 세라믹과 MOAB에게 매우 효과적입니다.")
                .tags(Arrays.asList("물맵", "버커니어", "잠수함", "수입", "카모", "프리차지"))
                .embedding(generateAdvancedEmbedding("물맵 버커니어 잠수함 수입 카모"))
                .confidence(0.86)
                .successRate(0.81)
                .usageCount(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());

        return knowledgeList;
    }
    
    /**
     * 고급 임베딩 생성 (더 정교한 벡터 생성)
     */
    private List<Double> generateAdvancedEmbedding(String text) {
        List<Double> embedding = new ArrayList<>();
        
        // 텍스트를 단어별로 분리하여 더 정교한 임베딩 생성
        String[] words = text.toLowerCase().split("\\s+");
        int baseHash = text.hashCode();
        
        for (int i = 0; i < 768; i++) {
            double value = 0.0;
            
            // 각 단어의 영향을 임베딩에 반영
            for (int j = 0; j < words.length; j++) {
                int wordHash = words[j].hashCode();
                value += Math.sin((baseHash + wordHash + i * 13 + j * 7) / 1000.0) * (0.3 + 0.4 / (j + 1));
            }
            
            // 정규화
            value = value / words.length;
            
            // 범위를 [-1, 1]로 제한
            value = Math.max(-1.0, Math.min(1.0, value));
            
            embedding.add(value);
        }
        
        return embedding;
    }
    
    /**
     * 특정 상황별 맞춤 지식 데이터 생성
     */
    public void createSituationSpecificKnowledge() {
        log.info("상황별 맞춤 BTD 지식 데이터 생성 시작");
        
        try {
            List<BloonsTDKnowledge> situationKnowledge = createSituationSpecificList();
            
            for (BloonsTDKnowledge knowledge : situationKnowledge) {
                repository.save(knowledge);
                log.info("상황별 지식 저장: {}", knowledge.getTitle());
            }
            
            log.info("상황별 맞춤 BTD 지식 데이터 생성 완료: {} 개", situationKnowledge.size());
            
        } catch (Exception e) {
            log.error("상황별 BTD 지식 데이터 생성 실패: {}", e.getMessage(), e);
        }
    }
    
    private List<BloonsTDKnowledge> createSituationSpecificList() {
        List<BloonsTDKnowledge> knowledgeList = new ArrayList<>();
        
        // 예산 부족 상황
        knowledgeList.add(BloonsTDKnowledge.builder()
                .id(UUID.randomUUID().toString())
                .situationType("budget_constraint")
                .roundRange("mid_game")
                .difficulty("medium")
                .towerTypes(Arrays.asList("Dart Monkey", "Tack Shooter", "Sniper"))
                .title("돈이 부족할 때의 저비용 고효율 전략")
                .content("예산이 제한적일 때 최소 비용으로 최대 효과를 내는 방법")
                .advice("0-2-3 다트 몽키나 2-0-3 택 슈터가 비용 대비 효율이 좋습니다. 0-3-2 스나이퍼로 경제를 보완하고, 가능하면 바나나 농장으로 수입을 늘리세요.")
                .tags(Arrays.asList("저비용", "효율", "다트몽키", "택슈터", "스나이퍼"))
                .embedding(generateAdvancedEmbedding("저비용 효율 다트몽키 택슈터 스나이퍼"))
                .confidence(0.89)
                .successRate(0.83)
                .usageCount(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());
        
        return knowledgeList;
    }
} 