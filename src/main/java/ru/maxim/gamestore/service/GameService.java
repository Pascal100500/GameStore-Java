package ru.maxim.gamestore.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ru.maxim.gamestore.entity.Game;
import ru.maxim.gamestore.repository.GameRepository;

import java.util.List;
import ru.maxim.gamestore.entity.Category;
import java.math.BigDecimal;
import ru.maxim.gamestore.repository.CategoryRepository;

@Service
public class GameService {

    private final GameRepository gameRepository;
    private final CategoryRepository categoryRepository;

    public GameService(
            GameRepository gameRepository,
            CategoryRepository categoryRepository
    ) {
        this.gameRepository = gameRepository;
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    public List<Game> getActiveGames() {
        return gameRepository.findByIsActiveTrue();
    }

    @Transactional(readOnly = true)
    public Game getGameById(Long id) {

        return gameRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Game not found: " + id
                        )
                );
    }

    @Transactional
    public Game createGame(
            String title,
            String description,
            BigDecimal price,
            Integer ageLimit,
            Long categoryId,
            Boolean isActive,
            String coverKey,
            String screenshot1Key,
            String screenshot2Key,
            String screenshot3Key
    ) {

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Category not found: " + categoryId
                        )
                );

        Game game = new Game();

        game.setTitle(title);
        game.setDescription(description);
        game.setPrice(price);
        game.setAgeLimit(ageLimit);
        game.setCategory(category);
        game.setIsActive(Boolean.TRUE.equals(isActive));
        game.setCoverKey(coverKey);
        game.setScreenshot1Key(screenshot1Key);
        game.setScreenshot2Key(screenshot2Key);
        game.setScreenshot3Key(screenshot3Key);

        return gameRepository.save(game);
    }

    @Transactional
    public Game updateGame(
            Long id,
            String title,
            String description,
            BigDecimal price,
            Integer ageLimit,
            Long categoryId,
            Boolean isActive,
            String coverKey,
            String screenshot1Key,
            String screenshot2Key,
            String screenshot3Key
    ) {
        Game game = gameRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Game not found: " + id
                        )
                );

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Category not found: " + categoryId
                        )
                );

        game.setTitle(title);
        game.setDescription(description);
        game.setPrice(price);
        game.setAgeLimit(ageLimit);
        game.setCategory(category);
        game.setIsActive(Boolean.TRUE.equals(isActive));

        if (coverKey != null) {
            game.setCoverKey(coverKey);
        }
        if (screenshot1Key != null) {
            game.setScreenshot1Key(screenshot1Key);
        }

        if (screenshot2Key != null) {
            game.setScreenshot2Key(screenshot2Key);
        }

        if (screenshot3Key != null) {
            game.setScreenshot3Key(screenshot3Key);
        }

        return gameRepository.save(game);
    }

    @Transactional(readOnly = true)
    public Game getGameForEdit(Long id) {

        return gameRepository.findWithCategoryById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Game not found: " + id
                        )
                );
    }
}