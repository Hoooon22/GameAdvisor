package com.gameadvisor.repository.vector;

import com.gameadvisor.model.vector.BaseGameKnowledge;
import com.gameadvisor.model.vector.MasterDuelKnowledge;
import com.gameadvisor.model.vector.VectorSearchResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Repository
public class MasterDuelVectorRepository implements GameVectorRepository<MasterDuelKnowledge> {
    
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;
    private final RowMapper<MasterDuelKnowledge> rowMapper;
    
    @Autowired
    public MasterDuelVectorRepository(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
        this.rowMapper = new MasterDuelKnowledgeRowMapper();
    }
    
    @Override
    public void save(MasterDuelKnowledge knowledge) {
        if (knowledge.getId() == null) {
            knowledge.setId("md_" + UUID.randomUUID().toString().substring(0, 8));
        }
        
        String sql = """
            INSERT INTO vector_knowledge_masterduel 
            (id, situation_type, format_type, archetype, card_types, title, content, advice, 
             tags, embedding, confidence, win_rate, usage_count)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        
        try {
            String tagsJson = objectMapper.writeValueAsString(knowledge.getTags());
            String embeddingJson = objectMapper.writeValueAsString(knowledge.getEmbedding());
            String cardTypesJson = objectMapper.writeValueAsString(knowledge.getCardTypes());
            
            jdbcTemplate.update(sql,
                knowledge.getId(),
                knowledge.getSituationType(),
                knowledge.getFormatType(),
                knowledge.getArchetype(),
                cardTypesJson,
                knowledge.getTitle(),
                knowledge.getContent(),
                knowledge.getAdvice(),
                tagsJson,
                embeddingJson,
                knowledge.getConfidence(),
                knowledge.getWinRate(),
                knowledge.getUsageCount() != null ? knowledge.getUsageCount() : 0
            );
            
            log.info("MasterDuel 지식 저장 완료: {}", knowledge.getId());
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON 변환 오류", e);
        }
    }
    
    @Override
    public Optional<MasterDuelKnowledge> findById(String id) {
        String sql = "SELECT * FROM vector_knowledge_masterduel WHERE id = ?";
        
        try {
            MasterDuelKnowledge knowledge = jdbcTemplate.queryForObject(sql, rowMapper, id);
            return Optional.ofNullable(knowledge);
        } catch (Exception e) {
            log.warn("MasterDuel 지식 조회 실패: {}", id);
            return Optional.empty();
        }
    }
    
    @Override
    public List<VectorSearchResult> findSimilar(List<Double> queryEmbedding, int limit) {
        return findSimilar(queryEmbedding, 0.0, limit);
    }
    
    @Override
    public List<VectorSearchResult> findSimilar(List<Double> queryEmbedding, double minSimilarity, int limit) {
        String sql = """
            SELECT * FROM vector_knowledge_masterduel 
            WHERE confidence >= 0.5 
            ORDER BY confidence DESC, win_rate DESC 
            LIMIT ?
            """;
        
        long startTime = System.currentTimeMillis();
        
        List<MasterDuelKnowledge> candidates = jdbcTemplate.query(sql, rowMapper, Math.min(limit * 5, 50));
        
        List<VectorSearchResult> results = candidates.stream()
            .map(knowledge -> {
                double similarity = calculateCosineSimilarity(queryEmbedding, knowledge.getEmbedding());
                return VectorSearchResult.builder()
                    .knowledge(knowledge)
                    .similarity(similarity)
                    .searchTimeMs(System.currentTimeMillis() - startTime)
                    .build();
            })
            .filter(result -> result.getSimilarity() >= minSimilarity)
            .sorted((a, b) -> a.compareTo(b))
            .limit(limit)
            .collect(Collectors.toList());
        
        log.info("MasterDuel 유사도 검색 완료: {} 개 결과, {}ms", results.size(), System.currentTimeMillis() - startTime);
        return results;
    }
    
    @Override
    public List<VectorSearchResult> findSimilarByType(List<Double> queryEmbedding, String situationType, int limit) {
        String sql = """
            SELECT * FROM vector_knowledge_masterduel 
            WHERE situation_type = ? AND confidence >= 0.5 
            ORDER BY confidence DESC, win_rate DESC 
            LIMIT ?
            """;
        
        long startTime = System.currentTimeMillis();
        
        List<MasterDuelKnowledge> candidates = jdbcTemplate.query(sql, rowMapper, situationType, Math.min(limit * 3, 30));
        
        return candidates.stream()
            .map(knowledge -> {
                double similarity = calculateCosineSimilarity(queryEmbedding, knowledge.getEmbedding());
                return VectorSearchResult.builder()
                    .knowledge(knowledge)
                    .similarity(similarity)
                    .searchTimeMs(System.currentTimeMillis() - startTime)
                    .build();
            })
            .sorted((a, b) -> a.compareTo(b))
            .limit(limit)
            .collect(Collectors.toList());
    }
    
    // MasterDuel 특화 검색 메서드
    public List<VectorSearchResult> findSimilarByArchetype(List<Double> queryEmbedding, String archetype, int limit) {
        String sql = """
            SELECT * FROM vector_knowledge_masterduel 
            WHERE archetype = ? AND confidence >= 0.5 
            ORDER BY confidence DESC, win_rate DESC 
            LIMIT ?
            """;
        
        long startTime = System.currentTimeMillis();
        
        List<MasterDuelKnowledge> candidates = jdbcTemplate.query(sql, rowMapper, archetype, Math.min(limit * 3, 30));
        
        return candidates.stream()
            .map(knowledge -> {
                double similarity = calculateCosineSimilarity(queryEmbedding, knowledge.getEmbedding());
                return VectorSearchResult.builder()
                    .knowledge(knowledge)
                    .similarity(similarity)
                    .searchTimeMs(System.currentTimeMillis() - startTime)
                    .build();
            })
            .sorted((a, b) -> a.compareTo(b))
            .limit(limit)
            .collect(Collectors.toList());
    }
    
    public List<VectorSearchResult> findSimilarByFormatType(List<Double> queryEmbedding, String formatType, int limit) {
        String sql = """
            SELECT * FROM vector_knowledge_masterduel 
            WHERE format_type = ? AND confidence >= 0.5 
            ORDER BY confidence DESC, win_rate DESC 
            LIMIT ?
            """;
        
        long startTime = System.currentTimeMillis();
        
        List<MasterDuelKnowledge> candidates = jdbcTemplate.query(sql, rowMapper, formatType, Math.min(limit * 3, 30));
        
        return candidates.stream()
            .map(knowledge -> {
                double similarity = calculateCosineSimilarity(queryEmbedding, knowledge.getEmbedding());
                return VectorSearchResult.builder()
                    .knowledge(knowledge)
                    .similarity(similarity)
                    .searchTimeMs(System.currentTimeMillis() - startTime)
                    .build();
            })
            .sorted((a, b) -> a.compareTo(b))
            .limit(limit)
            .collect(Collectors.toList());
    }
    
    // 코사인 유사도 계산
    private double calculateCosineSimilarity(List<Double> vec1, List<Double> vec2) {
        if (vec1 == null || vec2 == null || vec1.size() != vec2.size()) {
            return 0.0;
        }
        
        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;
        
        for (int i = 0; i < vec1.size(); i++) {
            dotProduct += vec1.get(i) * vec2.get(i);
            norm1 += vec1.get(i) * vec1.get(i);
            norm2 += vec2.get(i) * vec2.get(i);
        }
        
        if (norm1 == 0.0 || norm2 == 0.0) {
            return 0.0;
        }
        
        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }
    
    @Override
    public void incrementUsageCount(String id) {
        String sql = "UPDATE vector_knowledge_masterduel SET usage_count = usage_count + 1 WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }
    
    @Override
    public void updateSuccessMetric(String id, double successMetric) {
        String sql = "UPDATE vector_knowledge_masterduel SET win_rate = ? WHERE id = ?";
        jdbcTemplate.update(sql, successMetric, id);
    }
    
    @Override
    public long count() {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM vector_knowledge_masterduel", Long.class);
    }
    
    @Override
    public long countBySituationType(String situationType) {
        String sql = "SELECT COUNT(*) FROM vector_knowledge_masterduel WHERE situation_type = ?";
        return jdbcTemplate.queryForObject(sql, Long.class, situationType);
    }
    
    @Override
    public void update(MasterDuelKnowledge knowledge) {
        // TODO: 업데이트 로직 구현
    }
    
    @Override
    public void deleteById(String id) {
        String sql = "DELETE FROM vector_knowledge_masterduel WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }
    
    @Override
    public List<MasterDuelKnowledge> findAll() {
        String sql = "SELECT * FROM vector_knowledge_masterduel";
        return jdbcTemplate.query(sql, rowMapper);
    }
    
    @Override
    public List<VectorSearchResult> findSimilarByConfidence(List<Double> queryEmbedding, double minConfidence, int limit) {
        return findSimilar(queryEmbedding, 0.0, limit);
    }
    
    @Override
    public List<MasterDuelKnowledge> findTopByUsageCount(int limit) {
        String sql = "SELECT * FROM vector_knowledge_masterduel ORDER BY usage_count DESC LIMIT ?";
        return jdbcTemplate.query(sql, rowMapper, limit);
    }
    
    @Override
    public List<MasterDuelKnowledge> findTopBySuccessMetric(int limit) {
        String sql = "SELECT * FROM vector_knowledge_masterduel ORDER BY win_rate DESC LIMIT ?";
        return jdbcTemplate.query(sql, rowMapper, limit);
    }
    
    @Override
    public void updateEmbedding(String id, List<Double> embedding) {
        try {
            String embeddingJson = objectMapper.writeValueAsString(embedding);
            String sql = "UPDATE vector_knowledge_masterduel SET embedding = ? WHERE id = ?";
            jdbcTemplate.update(sql, embeddingJson, id);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("임베딩 업데이트 실패", e);
        }
    }
    
    // MasterDuel 특화 통계 메서드
    public long countByArchetype(String archetype) {
        String sql = "SELECT COUNT(*) FROM vector_knowledge_masterduel WHERE archetype = ?";
        return jdbcTemplate.queryForObject(sql, Long.class, archetype);
    }
    
    public long countByFormatType(String formatType) {
        String sql = "SELECT COUNT(*) FROM vector_knowledge_masterduel WHERE format_type = ?";
        return jdbcTemplate.queryForObject(sql, Long.class, formatType);
    }
    
    public void deleteAllData() {
        String sql = "DELETE FROM vector_knowledge_masterduel";
        jdbcTemplate.update(sql);
        log.info("MasterDuel 모든 데이터 삭제 완료");
    }
    
    private class MasterDuelKnowledgeRowMapper implements RowMapper<MasterDuelKnowledge> {
        @Override
        public MasterDuelKnowledge mapRow(ResultSet rs, int rowNum) throws SQLException {
            try {
                List<String> tags = objectMapper.readValue(rs.getString("tags"), new TypeReference<List<String>>() {});
                List<Double> embedding = objectMapper.readValue(rs.getString("embedding"), new TypeReference<List<Double>>() {});
                List<String> cardTypes = objectMapper.readValue(rs.getString("card_types"), new TypeReference<List<String>>() {});
                
                return MasterDuelKnowledge.builder()
                    .id(rs.getString("id"))
                    .situationType(rs.getString("situation_type"))
                    .formatType(rs.getString("format_type"))
                    .archetype(rs.getString("archetype"))
                    .cardTypes(cardTypes)
                    .title(rs.getString("title"))
                    .content(rs.getString("content"))
                    .advice(rs.getString("advice"))
                    .tags(tags)
                    .embedding(embedding)
                    .confidence(rs.getDouble("confidence"))
                    .winRate(rs.getDouble("win_rate"))
                    .usageCount(rs.getInt("usage_count"))
                    .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
                    .updatedAt(rs.getTimestamp("updated_at").toLocalDateTime())
                    .build();
            } catch (JsonProcessingException e) {
                throw new SQLException("JSON 파싱 오류", e);
            }
        }
    }
} 