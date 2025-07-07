package com.gameadvisor.model.vector;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class BloonsTDKnowledge extends BaseGameKnowledge {
    private String roundRange;      // "early_game", "mid_game", "late_game", "freeplay"
    private String difficulty;      // "easy", "medium", "hard", "expert"
    private List<String> towerTypes; // ["dart_monkey", "spike_factory", "alchemist"]
    private Double successRate;     // 0.0 ~ 1.0
    private String sourceUrl;       // 웹에서 수집된 자료의 원본 URL
    
    @Override
    public String getGameName() {
        return "BloonsTD";
    }
    
    @Override
    public Double getSuccessMetric() {
        return successRate;
    }
    
    // 라운드 번호로 범위 판단
    public static String getRoundRange(int round) {
        if (round <= 40) return "early_game";
        if (round <= 80) return "mid_game";
        if (round <= 100) return "late_game";
        return "freeplay";
    }
    
    // 상황 유형들
    public static class SituationType {
        public static final String CERAMIC_DEFENSE = "ceramic_defense";
        public static final String MOAB_BATTLE = "moab_battle";
        public static final String CAMO_DETECTION = "camo_detection";
        public static final String LEAD_POPPING = "lead_popping";
        public static final String BOSS_BATTLE = "boss_battle";
        public static final String TOWER_COMBO = "tower_combo";
        public static final String ECONOMY_BUILD = "economy_build";
        public static final String GENERAL_STRATEGY = "general_strategy";
    }
} 