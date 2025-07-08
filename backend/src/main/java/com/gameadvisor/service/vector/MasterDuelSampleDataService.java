package com.gameadvisor.service.vector;

import com.gameadvisor.model.vector.MasterDuelKnowledge;
import com.gameadvisor.repository.vector.MasterDuelVectorRepository;
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
public class MasterDuelSampleDataService {
    
    private final MasterDuelVectorRepository repository;
    
    @Autowired
    public MasterDuelSampleDataService(MasterDuelVectorRepository repository) {
        this.repository = repository;
    }
    
    /**
     * MasterDuel 샘플 지식 데이터 생성
     */
    public void createSampleData() {
        log.info("MasterDuel 샘플 데이터 생성 시작");
        
        try {
            List<MasterDuelKnowledge> sampleKnowledge = createSampleKnowledgeList();
            
            for (MasterDuelKnowledge knowledge : sampleKnowledge) {
                repository.save(knowledge);
                log.info("샘플 지식 저장: {}", knowledge.getTitle());
            }
            
            log.info("MasterDuel 샘플 데이터 생성 완료: {} 개", sampleKnowledge.size());
            
        } catch (Exception e) {
            log.error("MasterDuel 샘플 데이터 생성 실패: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 샘플 지식 데이터 목록 생성
     */
    private List<MasterDuelKnowledge> createSampleKnowledgeList() {
        List<MasterDuelKnowledge> knowledgeList = new ArrayList<>();
        
        // 콤보 설정 전략
        knowledgeList.add(createComboSetupKnowledge());
        knowledgeList.add(createTrapCounterKnowledge());
        knowledgeList.add(createMonsterBattleKnowledge());
        knowledgeList.add(createSpellChainKnowledge());
        knowledgeList.add(createHandAdvantageKnowledge());
        knowledgeList.add(createDeckBuildKnowledge());
        knowledgeList.add(createSideDeckKnowledge());
        
        // 아키타입별 전략
        knowledgeList.add(createElementalHeroKnowledge());
        knowledgeList.add(createBlueEyesKnowledge());
        knowledgeList.add(createDragonMaidKnowledge());
        knowledgeList.add(createEldlichKnowledge());
        
        return knowledgeList;
    }
    
    private MasterDuelKnowledge createComboSetupKnowledge() {
        return MasterDuelKnowledge.builder()
            .id("md_combo_setup_001")
            .situationType(MasterDuelKnowledge.SituationType.COMBO_SETUP)
            .formatType("ranked")
            .archetype("generic")
            .cardTypes(Arrays.asList("spell", "monster"))
            .title("기본 콤보 설정 전략")
            .content("게임 초반에 안정적인 콤보 설정을 위한 기본 전략입니다. 서치 카드를 우선적으로 사용하고, 핵심 콤보 피스를 모으는 것이 중요합니다.")
            .advice("🎯 초반에는 안정성을 우선시하세요. 서치 카드로 핵심 콤보 피스를 모으고, 상대의 방해를 최소화하는 것이 중요합니다.")
            .tags(Arrays.asList("콤보", "서치", "초반", "안정성"))
            .embedding(generateRandomEmbedding())
            .confidence(0.85)
            .winRate(0.72)
            .usageCount(150)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
    }
    
    private MasterDuelKnowledge createTrapCounterKnowledge() {
        return MasterDuelKnowledge.builder()
            .id("md_trap_counter_001")
            .situationType(MasterDuelKnowledge.SituationType.TRAP_COUNTER)
            .formatType("ranked")
            .archetype("generic")
            .cardTypes(Arrays.asList("trap", "spell"))
            .title("함정 카드 대응 전략")
            .content("상대의 함정 카드를 효과적으로 대응하는 방법입니다. 타이밍을 잘 맞춰서 카운터하거나, 미리 제거하는 것이 중요합니다.")
            .advice("🛡️ 함정 카드는 미리 제거하거나 타이밍을 맞춰 카운터하세요. 상대의 백덱을 읽고 대응하는 것이 중요합니다.")
            .tags(Arrays.asList("함정", "카운터", "타이밍", "백덱"))
            .embedding(generateRandomEmbedding())
            .confidence(0.78)
            .winRate(0.65)
            .usageCount(120)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
    }
    
    private MasterDuelKnowledge createMonsterBattleKnowledge() {
        return MasterDuelKnowledge.builder()
            .id("md_monster_battle_001")
            .situationType(MasterDuelKnowledge.SituationType.MONSTER_BATTLE)
            .formatType("ranked")
            .archetype("generic")
            .cardTypes(Arrays.asList("monster"))
            .title("몬스터 전투 최적화")
            .content("몬스터 간의 전투에서 우위를 점하는 전략입니다. 공격력, 효과, 위치 등을 고려해서 최적의 전투를 진행하세요.")
            .advice("⚔️ 몬스터 전투에서는 공격력뿐만 아니라 효과와 위치도 중요합니다. 상대의 몬스터 효과를 파악하고 대응하세요.")
            .tags(Arrays.asList("전투", "몬스터", "공격력", "효과"))
            .embedding(generateRandomEmbedding())
            .confidence(0.82)
            .winRate(0.69)
            .usageCount(200)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
    }
    
    private MasterDuelKnowledge createSpellChainKnowledge() {
        return MasterDuelKnowledge.builder()
            .id("md_spell_chain_001")
            .situationType(MasterDuelKnowledge.SituationType.SPELL_CHAIN)
            .formatType("ranked")
            .archetype("generic")
            .cardTypes(Arrays.asList("spell"))
            .title("마법 카드 체인 전략")
            .content("마법 카드들을 연계해서 사용하는 고급 전략입니다. 체인 순서와 타이밍을 잘 맞춰서 최대 효과를 노리세요.")
            .advice("🔮 마법 카드 체인은 순서가 중요합니다. 상대의 대응을 예측하고 최적의 순서로 체인을 구성하세요.")
            .tags(Arrays.asList("마법", "체인", "순서", "타이밍"))
            .embedding(generateRandomEmbedding())
            .confidence(0.88)
            .winRate(0.76)
            .usageCount(180)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
    }
    
    private MasterDuelKnowledge createHandAdvantageKnowledge() {
        return MasterDuelKnowledge.builder()
            .id("md_hand_advantage_001")
            .situationType(MasterDuelKnowledge.SituationType.HAND_ADVANTAGE)
            .formatType("ranked")
            .archetype("generic")
            .cardTypes(Arrays.asList("spell", "trap"))
            .title("핸드 어드밴티지 관리")
            .content("핸드 어드밴티지를 유지하고 확보하는 전략입니다. 드로우 카드 활용과 자원 관리가 핵심입니다.")
            .advice("📋 핸드 어드밴티지는 게임의 승부를 가릅니다. 드로우 카드를 적절히 활용하고 자원을 아껴 사용하세요.")
            .tags(Arrays.asList("핸드", "어드밴티지", "드로우", "자원"))
            .embedding(generateRandomEmbedding())
            .confidence(0.90)
            .winRate(0.80)
            .usageCount(250)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
    }
    
    private MasterDuelKnowledge createDeckBuildKnowledge() {
        return MasterDuelKnowledge.builder()
            .id("md_deck_build_001")
            .situationType(MasterDuelKnowledge.SituationType.DECK_BUILD)
            .formatType("ranked")
            .archetype("generic")
            .cardTypes(Arrays.asList("monster", "spell", "trap"))
            .title("덱 구성 기본 원리")
            .content("효율적인 덱 구성을 위한 기본 원리입니다. 시너지, 안정성, 메타 대응을 고려해서 덱을 구성하세요.")
            .advice("🏗️ 덱 구성은 시너지와 안정성의 균형이 중요합니다. 현재 메타를 분석하고 대응 카드를 포함시키세요.")
            .tags(Arrays.asList("덱구성", "시너지", "안정성", "메타"))
            .embedding(generateRandomEmbedding())
            .confidence(0.85)
            .winRate(0.73)
            .usageCount(300)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
    }
    
    private MasterDuelKnowledge createSideDeckKnowledge() {
        return MasterDuelKnowledge.builder()
            .id("md_side_deck_001")
            .situationType(MasterDuelKnowledge.SituationType.SIDE_DECK)
            .formatType("ranked")
            .archetype("generic")
            .cardTypes(Arrays.asList("spell", "trap"))
            .title("사이드 덱 활용 전략")
            .content("사이드 덱을 활용해서 메타 대응력을 높이는 전략입니다. 상대 덱에 따라 적절한 카드를 교체하세요.")
            .advice("🔄 사이드 덱은 메타 대응의 핵심입니다. 상대 덱의 약점을 파악하고 적절한 카드를 교체하세요.")
            .tags(Arrays.asList("사이드", "메타", "대응", "교체"))
            .embedding(generateRandomEmbedding())
            .confidence(0.82)
            .winRate(0.70)
            .usageCount(160)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
    }
    
    private MasterDuelKnowledge createElementalHeroKnowledge() {
        return MasterDuelKnowledge.builder()
            .id("md_elemental_hero_001")
            .situationType(MasterDuelKnowledge.SituationType.COMBO_SETUP)
            .formatType("ranked")
            .archetype(MasterDuelKnowledge.Archetype.ELEMENTAL_HERO)
            .cardTypes(Arrays.asList("monster", "spell"))
            .title("엘리멘탈 히어로 콤보 가이드")
            .content("엘리멘탈 히어로 덱의 기본 콤보와 융합 패턴을 설명합니다. 스파크맨, 버스터맨 등을 활용한 융합 전략이 핵심입니다.")
            .advice("🦸 엘리멘탈 히어로는 융합이 핵심입니다. 미라클 퓨전과 폴리머라이제이션을 적절히 활용하세요.")
            .tags(Arrays.asList("엘리멘탈", "히어로", "융합", "콤보"))
            .embedding(generateRandomEmbedding())
            .confidence(0.87)
            .winRate(0.74)
            .usageCount(140)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
    }
    
    private MasterDuelKnowledge createBlueEyesKnowledge() {
        return MasterDuelKnowledge.builder()
            .id("md_blue_eyes_001")
            .situationType(MasterDuelKnowledge.SituationType.COMBO_SETUP)
            .formatType("ranked")
            .archetype(MasterDuelKnowledge.Archetype.BLUE_EYES)
            .cardTypes(Arrays.asList("monster", "spell"))
            .title("블루아이즈 전개 전략")
            .content("블루아이즈 덱의 기본 전개 패턴과 서치 루트를 설명합니다. 블루아이즈의 강력한 공격력을 최대한 활용하세요.")
            .advice("🐉 블루아이즈는 고화력이 장점입니다. 서치 카드로 안정적으로 전개하고 상대를 압도하세요.")
            .tags(Arrays.asList("블루아이즈", "전개", "서치", "고화력"))
            .embedding(generateRandomEmbedding())
            .confidence(0.83)
            .winRate(0.71)
            .usageCount(180)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
    }
    
    private MasterDuelKnowledge createDragonMaidKnowledge() {
        return MasterDuelKnowledge.builder()
            .id("md_dragon_maid_001")
            .situationType(MasterDuelKnowledge.SituationType.COMBO_SETUP)
            .formatType("ranked")
            .archetype(MasterDuelKnowledge.Archetype.DRAGON_MAID)
            .cardTypes(Arrays.asList("monster", "spell"))
            .title("드래곤메이드 순환 전략")
            .content("드래곤메이드의 변신 시스템을 활용한 순환 전략입니다. 메이드와 드래곤 형태를 적절히 변환하며 어드밴티지를 확보하세요.")
            .advice("🏠 드래곤메이드는 변신이 핵심입니다. 상황에 맞춰 메이드와 드래곤 형태를 변환하며 순환하세요.")
            .tags(Arrays.asList("드래곤메이드", "변신", "순환", "어드밴티지"))
            .embedding(generateRandomEmbedding())
            .confidence(0.86)
            .winRate(0.75)
            .usageCount(120)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
    }
    
    private MasterDuelKnowledge createEldlichKnowledge() {
        return MasterDuelKnowledge.builder()
            .id("md_eldlich_001")
            .situationType(MasterDuelKnowledge.SituationType.TRAP_COUNTER)
            .formatType("ranked")
            .archetype(MasterDuelKnowledge.Archetype.ELDLICH)
            .cardTypes(Arrays.asList("trap", "spell"))
            .title("엘드리치 컨트롤 전략")
            .content("엘드리치의 함정 카드 중심 컨트롤 전략입니다. 자원 순환과 상대 견제를 통해 게임을 장기화하며 승리하세요.")
            .advice("👑 엘드리치는 컨트롤이 핵심입니다. 함정 카드로 상대를 견제하며 자원 순환을 통해 우위를 점하세요.")
            .tags(Arrays.asList("엘드리치", "컨트롤", "함정", "견제"))
            .embedding(generateRandomEmbedding())
            .confidence(0.89)
            .winRate(0.77)
            .usageCount(200)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
    }
    
    /**
     * 랜덤 임베딩 벡터 생성 (테스트용)
     */
    private List<Double> generateRandomEmbedding() {
        List<Double> embedding = new ArrayList<>();
        for (int i = 0; i < 384; i++) {
            embedding.add(Math.random() * 2 - 1); // -1 ~ 1 사이의 랜덤 값
        }
        return embedding;
    }
    
    /**
     * 현재 지식 데이터 개수 조회
     */
    public long getKnowledgeCount() {
        return repository.count();
    }
    
    /**
     * 모든 데이터 삭제
     */
    public void clearAllData() {
        repository.deleteAllData();
        log.info("MasterDuel 모든 데이터 삭제 완료");
    }
    
    /**
     * 아키타입별 데이터 개수 조회
     */
    public long getKnowledgeCountByArchetype(String archetype) {
        return repository.countByArchetype(archetype);
    }
    
    /**
     * 포맷별 데이터 개수 조회
     */
    public long getKnowledgeCountByFormat(String formatType) {
        return repository.countByFormatType(formatType);
    }
} 