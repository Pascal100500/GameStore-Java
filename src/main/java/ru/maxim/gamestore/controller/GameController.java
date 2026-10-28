package ru.maxim.gamestore.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import ru.maxim.gamestore.service.GameService;
import ru.maxim.gamestore.service.MinioService;

import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import ru.maxim.gamestore.entity.Game;

import java.io.InputStream;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class GameController {

    private final GameService gameService;
    private final MinioService minioService;

    public GameController(
            GameService gameService,
            MinioService minioService
    ) {
        this.gameService = gameService;
        this.minioService = minioService;
    }

    @GetMapping("/games")
    public String index(@RequestParam(defaultValue = "0") int page, Model model) {

        if (page < 0) {
            page = 0;
        }
        Page<Game> gamesPage = gameService.getActiveGamesPage(page);
        model.addAttribute("games", gamesPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", gamesPage.getTotalPages());

        return "games/index";
    }

    @GetMapping("/games/{id}")
    public String details(@PathVariable Long id, Model model) {

        Game game;

        try {
            game = gameService.getGameById(id);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Игра не найдена"
            );
        }

        if (!Boolean.TRUE.equals(game.getIsActive())) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Игра не найдена"
            );
        }

        model.addAttribute("game", game);

        return "games/details";
    }

    @GetMapping("/games/{id}/cover")
    public ResponseEntity<Resource> getCover(@PathVariable Long id) throws Exception {

        Game game = gameService.getGameById(id);

        String coverKey = game.getCoverKey();

        if (coverKey == null || coverKey.isBlank()) {
            return ResponseEntity.notFound().build();
        }

        InputStream imageStream = minioService.getFile(coverKey);

        MediaType mediaType;

        if (coverKey.endsWith(".png")) {
            mediaType = MediaType.IMAGE_PNG;
        } else if (coverKey.endsWith(".webp")) {
            mediaType = MediaType.parseMediaType("image/webp");
        } else {
            mediaType = MediaType.IMAGE_JPEG;
        }

        return ResponseEntity.ok()
                .contentType(mediaType)
                .body(new InputStreamResource(imageStream));
    }
    @GetMapping("/games/{id}/screenshots/{number}")
    public ResponseEntity<Resource> getScreenshot(
            @PathVariable Long id,
            @PathVariable int number
    ) throws Exception {

        Game game;

        try {
            game = gameService.getGameById(id);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }

        if (!Boolean.TRUE.equals(game.getIsActive())) {
            return ResponseEntity.notFound().build();
        }

        String screenshotKey;

        switch (number) {
            case 1 -> screenshotKey = game.getScreenshot1Key();
            case 2 -> screenshotKey = game.getScreenshot2Key();
            case 3 -> screenshotKey = game.getScreenshot3Key();
            default -> {
                return ResponseEntity.notFound().build();
            }
        }

        if (screenshotKey == null || screenshotKey.isBlank()) {
            return ResponseEntity.notFound().build();
        }

        InputStream imageStream = minioService.getFile(screenshotKey);

        MediaType mediaType;

        String lowerKey = screenshotKey.toLowerCase(
                java.util.Locale.ROOT
        );

        if (lowerKey.endsWith(".png")) {
            mediaType = MediaType.IMAGE_PNG;
        } else if (lowerKey.endsWith(".webp")) {
            mediaType = MediaType.parseMediaType("image/webp");
        } else {
            mediaType = MediaType.IMAGE_JPEG;
        }

        return ResponseEntity.ok()
                .contentType(mediaType)
                .body(new InputStreamResource(imageStream));
    }
}