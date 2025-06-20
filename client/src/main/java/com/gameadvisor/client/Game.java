package com.gameadvisor.client;

// 서버의 Game 엔티티와 동일한 구조를 가지는 DTO 클래스
public class Game {

    private Long id;
    private String name;
    private String processName;

    // Getters
    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getProcessName() {
        return processName;
    }

    // Setters
    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setProcessName(String processName) {
        this.processName = processName;
    }

    @Override
    public String toString() {
        return "Game{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", processName='" + processName + '\'' +
                '}';
    }
} 