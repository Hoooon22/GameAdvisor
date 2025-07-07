package com.gameadvisor.service.vector;

import com.gameadvisor.model.vector.BaseGameKnowledge;
import com.gameadvisor.model.vector.MasterDuelKnowledge;
import com.gameadvisor.model.vector.VectorSearchResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class MasterDuelVectorService implements GameVectorService {
    
    // TODO: Repository 주입 예정
    
    @Override
    public List<VectorSearchResult> searchSimilar(String situation, int limit) {
        log.info("Master Duel 상황 검색: {}", situation);
        
        // 상황 유형 추출
        String situationType = extractSituationType(situation);
        
        // 아키타입 추출
        String archetype = extractArchetype(situation);
        
        // 포맷 유형 추출
        String formatType = extractFormatType(situation);
        
        log.info("추출된 정보 - 유형: {}, 아키타입: {}, 포맷: {}", 
                situationType, archetype, formatType);
        
        // TODO: Repository를 통한 실제 검색 구현
        return List.of();
    }
    
    @Override
    public void saveKnowledge(BaseGameKnowledge knowledge) {
        if (knowledge instanceof MasterDuelKnowledge mdKnowledge) {
            log.info("Master Duel 지식 저장: {}", mdKnowledge.getTitle());
            // TODO: Repository를 통한 저장 구현
        }
    }
    
    @Override
    public void incrementUsage(String knowledgeId) {
        log.info("Master Duel 지식 사용량 증가: {}", knowledgeId);
        // TODO: Repository를 통한 사용량 업데이트
    }
    
    @Override
    public void updateSuccessMetric(String knowledgeId, double successMetric) {
        log.info("Master Duel 지식 승률 업데이트: {} -> {}", knowledgeId, successMetric);
        // TODO: Repository를 통한 승률 업데이트
    }
    
    @Override
    public String getGameName() {
        return "MasterDuel";
    }
    
    @Override
    public boolean supports(String gameName) {
        return gameName != null && 
               (gameName.toLowerCase().contains("master") || 
                gameName.toLowerCase().contains("duel") ||
                gameName.toLowerCase().contains("masterduel") ||
                gameName.toLowerCase().contains("yugioh") ||
                gameName.toLowerCase().contains("유기오"));
    }
    
    // 상황 유형 추출
    private String extractSituationType(String situation) {
        String lower = situation.toLowerCase();
        
        if (lower.contains("combo") || lower.contains("콤보")) {
            return MasterDuelKnowledge.SituationType.COMBO_SETUP;
        }
        if (lower.contains("trap") || lower.contains("트랩")) {
            return MasterDuelKnowledge.SituationType.TRAP_COUNTER;
        }
        if (lower.contains("monster") || lower.contains("몬스터") || lower.contains("battle") || lower.contains("전투")) {
            return MasterDuelKnowledge.SituationType.MONSTER_BATTLE;
        }
        if (lower.contains("spell") || lower.contains("마법") || lower.contains("chain") || lower.contains("체인")) {
            return MasterDuelKnowledge.SituationType.SPELL_CHAIN;
        }
        if (lower.contains("hand") || lower.contains("핸드") || lower.contains("어드밴티지")) {
            return MasterDuelKnowledge.SituationType.HAND_ADVANTAGE;
        }
        if (lower.contains("deck") || lower.contains("덱") || lower.contains("build") || lower.contains("구성")) {
            return MasterDuelKnowledge.SituationType.DECK_BUILD;
        }
        if (lower.contains("side") || lower.contains("사이드")) {
            return MasterDuelKnowledge.SituationType.SIDE_DECK;
        }
        if (lower.contains("turn") || lower.contains("턴") || lower.contains("최적화")) {
            return MasterDuelKnowledge.SituationType.TURN_OPTIMIZATION;
        }
        
        return "general"; // 기본값
    }
    
    // 아키타입 추출
    private String extractArchetype(String situation) {
        String lower = situation.toLowerCase();
        
        if (lower.contains("elemental") || lower.contains("hero") || lower.contains("엘리멘탈") || lower.contains("히어로")) {
            return MasterDuelKnowledge.Archetype.ELEMENTAL_HERO;
        }
        if (lower.contains("blue") || lower.contains("eyes") || lower.contains("블루") || lower.contains("아이즈")) {
            return MasterDuelKnowledge.Archetype.BLUE_EYES;
        }
        if (lower.contains("dragon") || lower.contains("maid") || lower.contains("드래곤") || lower.contains("메이드")) {
            return MasterDuelKnowledge.Archetype.DRAGON_MAID;
        }
        if (lower.contains("eldlich") || lower.contains("엘드리치")) {
            return MasterDuelKnowledge.Archetype.ELDLICH;
        }
        
        return MasterDuelKnowledge.Archetype.GENERIC; // 기본값
    }
    
    // 포맷 유형 추출
    private String extractFormatType(String situation) {
        String lower = situation.toLowerCase();
        
        if (lower.contains("ranked") || lower.contains("랭크")) {
            return "ranked";
        }
        if (lower.contains("casual") || lower.contains("캐주얼")) {
            return "casual";
        }
        if (lower.contains("event") || lower.contains("이벤트")) {
            return "event";
        }
        
        return "ranked"; // 기본값
    }
} 