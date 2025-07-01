package com.gameadvisor.controller;

import com.gameadvisor.model.Game;
import com.gameadvisor.service.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/games")
public class GameController {
    private final GameService gameService;

    @Autowired
    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @GetMapping
    public List<Game> getGames() {
        return gameService.getAllGames();
    }

    @GetMapping("/api/gemini-test")
    public String geminiTest() {
        return "제미나이 API 테스트: 게임 도우미가 정상적으로 동작합니다.";
    }
} 