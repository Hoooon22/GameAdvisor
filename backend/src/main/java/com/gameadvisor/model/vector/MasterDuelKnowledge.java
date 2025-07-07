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
public class MasterDuelKnowledge extends BaseGameKnowledge {
    private String formatType;      // "ranked", "casual", "event"
    private String archetype;       // "elemental_hero", "blue_eyes", "generic"
    private List<String> cardTypes; // ["spell", "trap", "monster", "xyz"]
    private Double winRate;         // 0.0 ~ 1.0
    
    @Override
    public String getGameName() {
        return "MasterDuel";
    }
    
    @Override
    public Double getSuccessMetric() {
        return winRate;
    }
    
    // 상황 유형들
    public static class SituationType {
        public static final String COMBO_SETUP = "combo_setup";
        public static final String TRAP_COUNTER = "trap_counter";
        public static final String MONSTER_BATTLE = "monster_battle";
        public static final String SPELL_CHAIN = "spell_chain";
        public static final String HAND_ADVANTAGE = "hand_advantage";
        public static final String DECK_BUILD = "deck_build";
        public static final String SIDE_DECK = "side_deck";
        public static final String TURN_OPTIMIZATION = "turn_optimization";
    }
    
    // 아키타입 정의
    public static class Archetype {
        public static final String ELEMENTAL_HERO = "elemental_hero";
        public static final String BLUE_EYES = "blue_eyes";
        public static final String DRAGON_MAID = "dragon_maid";
        public static final String ELDLICH = "eldlich";
        public static final String GENERIC = "generic";
    }
} 