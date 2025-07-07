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
public class BloonsTDSampleDataService {
    
    private final BloonsTDVectorRepository repository;
    
    @Autowired
    public BloonsTDSampleDataService(BloonsTDVectorRepository repository) {
        this.repository = repository;
    }
    
    /**
     * 샘플 BTD 지식 데이터 생성 및 저장
     */
    public void createSampleData() {
        log.info("BTD 샘플 데이터 생성 시작");
        
        try {
            List<BloonsTDKnowledge> sampleKnowledge = createSampleKnowledgeList();
            
            for (BloonsTDKnowledge knowledge : sampleKnowledge) {
                repository.save(knowledge);
                log.info("샘플 지식 저장: {}", knowledge.getTitle());
            }
            
            log.info("BTD 샘플 데이터 생성 완료: {} 개", sampleKnowledge.size());
            
        } catch (Exception e) {
            log.error("BTD 샘플 데이터 생성 실패: {}", e.getMessage(), e);
        }
    }
    
    private List<BloonsTDKnowledge> createSampleKnowledgeList() {
        List<BloonsTDKnowledge> knowledgeList = new ArrayList<>();
        
        // 1. 세라믹 방어 전략
        knowledgeList.add(BloonsTDKnowledge.builder()
                .id(UUID.randomUUID().toString())
                .situationType(BloonsTDKnowledge.SituationType.CERAMIC_DEFENSE)
                .roundRange("61-80")
                .difficulty("hard")
                .towerTypes(Arrays.asList("Ninja", "Alchemist", "Super Monkey"))
                .title("세라믹 블룬 방어를 위한 닌자-연금술사 조합")
                .content("라운드 63-76에서 대량으로 나오는 세라믹 블룬을 효율적으로 처리하는 전략")
                .advice("4-0-1 닌자 몽키와 4-2-0 연금술사를 조합하세요. 닌자의 칼리즈를 연금술사가 강화하여 세라믹을 쉽게 뚫을 수 있습니다.")
                .tags(Arrays.asList("세라믹", "닌자", "연금술사", "중반전"))
                .embedding(generateSampleEmbedding("세라믹 블룬 닌자 연금술사 방어"))
                .confidence(0.85)
                .successRate(0.78)
                .usageCount(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());
        
        // 2. MOAB 공략
        knowledgeList.add(BloonsTDKnowledge.builder()
                .id(UUID.randomUUID().toString())
                .situationType(BloonsTDKnowledge.SituationType.MOAB_BATTLE)
                .roundRange("81-100")
                .difficulty("hard")
                .towerTypes(Arrays.asList("Super Monkey", "Monkey Village", "Spike Factory"))
                .title("MOAB 및 BFB 공략을 위한 슈퍼 몽키 전략")
                .content("고레벨 MOAB류 블룬 처리를 위한 집중 화력 전략")
                .advice("2-0-3 슈퍼 몽키를 4-2-0 몽키 빌리지 버프로 강화하고, 4-0-2 스파이크 팩토리로 누수 방지하세요.")
                .tags(Arrays.asList("MOAB", "BFB", "슈퍼몽키", "후반전"))
                .embedding(generateSampleEmbedding("MOAB BFB 슈퍼몽키 빌리지 스파이크"))
                .confidence(0.82)
                .successRate(0.73)
                .usageCount(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());
        
        // 3. 카모 탐지 전략
        knowledgeList.add(BloonsTDKnowledge.builder()
                .id(UUID.randomUUID().toString())
                .situationType(BloonsTDKnowledge.SituationType.CAMO_DETECTION)
                .roundRange("21-40")
                .difficulty("medium")
                .towerTypes(Arrays.asList("Ninja", "Monkey Village", "Wizard"))
                .title("카모 블룬 탐지 및 처리 전략")
                .content("초중반 카모 블룬 대응을 위한 탐지 타워 배치")
                .advice("0-0-2 닌자 몽키나 2-0-0 몽키 빌리지로 카모 탐지를 확보하고, 0-2-3 위저드로 디카모 효과를 활용하세요.")
                .tags(Arrays.asList("카모", "탐지", "닌자", "빌리지", "위저드"))
                .embedding(generateSampleEmbedding("카모 블룬 탐지 닌자 빌리지 위저드"))
                .confidence(0.88)
                .successRate(0.82)
                .usageCount(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());
        
        // 4. 납 블룬 처리
        knowledgeList.add(BloonsTDKnowledge.builder()
                .id(UUID.randomUUID().toString())
                .situationType(BloonsTDKnowledge.SituationType.LEAD_POPPING)
                .roundRange("21-40")
                .difficulty("medium")
                .towerTypes(Arrays.asList("Monkey Ace", "Wizard", "Alchemist"))
                .title("납 블룬 처리를 위한 관통 공격 전략")
                .content("납 블룬의 특성을 이해하고 효율적으로 처리하는 방법")
                .advice("2-0-3 몽키 에이스의 폭탄 공격이나 0-1-2 위저드의 파이어볼, 3-0-0 연금술사의 산성 공격이 효과적입니다.")
                .tags(Arrays.asList("납블룬", "관통", "에이스", "위저드", "연금술사"))
                .embedding(generateSampleEmbedding("납 블룬 관통 에이스 위저드 연금술사"))
                .confidence(0.80)
                .successRate(0.75)
                .usageCount(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());
        
        // 5. 경제 구축 전략
        knowledgeList.add(BloonsTDKnowledge.builder()
                .id(UUID.randomUUID().toString())
                .situationType(BloonsTDKnowledge.SituationType.ECONOMY_BUILD)
                .roundRange("1-20")
                .difficulty("easy")
                .towerTypes(Arrays.asList("Banana Farm", "Monkey Village", "Druid"))
                .title("초반 경제 구축을 위한 바나나 농장 전략")
                .content("게임 초반부터 안정적인 수입원을 확보하는 방법")
                .advice("3-2-0 바나나 농장을 2-3-0 몽키 빌리지 근처에 배치하여 수입을 증가시키고, 0-4-0 드루이드로 농장을 추가 강화하세요.")
                .tags(Arrays.asList("경제", "바나나농장", "빌리지", "드루이드", "초반"))
                .embedding(generateSampleEmbedding("경제 바나나 농장 빌리지 드루이드 수입"))
                .confidence(0.75)
                .successRate(0.68)
                .usageCount(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());
        
        return knowledgeList;
    }
    
    // 샘플 임베딩 생성 (실제로는 AI 모델 사용)
    private List<Double> generateSampleEmbedding(String text) {
        List<Double> embedding = new ArrayList<>();
        int hash = text.hashCode();
        
        for (int i = 0; i < 768; i++) {
            // 텍스트 해시를 기반으로 의사 임베딩 생성
            double value = Math.sin((hash + i * 7) / 1000.0) * 0.5;
            embedding.add(value);
        }
        
        return embedding;
    }
    
    /**
     * 저장된 지식 개수 확인
     */
    public long getKnowledgeCount() {
        return repository.count();
    }
    
    /**
     * 모든 지식 데이터 삭제
     */
    public void clearAllData() {
        try {
            List<BloonsTDKnowledge> allKnowledge = repository.findAll();
            for (BloonsTDKnowledge knowledge : allKnowledge) {
                repository.deleteById(knowledge.getId());
            }
            log.info("모든 BTD 지식 데이터 삭제 완료");
        } catch (Exception e) {
            log.error("BTD 지식 데이터 삭제 실패: {}", e.getMessage(), e);
        }
    }
} 