package ru.maxim.gamestore.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import ru.maxim.gamestore.service.GameService;

@Controller
public class GameController {

    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @GetMapping("/games")
    public String index(Model model) {

        model.addAttribute(
                "games",
                gameService.getActiveGames()
        );

        return "games/index";
    }
}