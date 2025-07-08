package com.gameadvisor.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "games")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String name;

    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;

    @Column(name = "process_name", length = 100)
    private String processName; // e.g., "BloonsTD6", "YuGiOh"

    @Column(name = "vector_table_name", nullable = false, length = 100)
    private String vectorTableName; // e.g., "vector_knowledge_bloonstd"

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // 편의 메서드들
    public boolean isSupported() {
        return Boolean.TRUE.equals(isActive);
    }

    public String getFullDisplayName() {
        return displayName != null ? displayName : name;
    }

    // 게임별 특화 정보를 위한 메서드들
    public boolean isBloonsTD() {
        return "BloonsTD".equalsIgnoreCase(name);
    }

    public boolean isMasterDuel() {
        return "MasterDuel".equalsIgnoreCase(name);
    }

    @Override
    public String toString() {
        return "Game{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", displayName='" + displayName + '\'' +
                ", processName='" + processName + '\'' +
                ", isActive=" + isActive +
                '}';
    }
} 