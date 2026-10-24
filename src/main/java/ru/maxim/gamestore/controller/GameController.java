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
    public String index(Model model) {

        model.addAttribute(
                "games",
                gameService.getActiveGames()
        );

        return "games/index";
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
}