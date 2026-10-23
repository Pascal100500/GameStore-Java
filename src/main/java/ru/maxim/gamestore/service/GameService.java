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

    @Transactional
    public Game createGame(
            String title,
            String description,
            BigDecimal price,
            Integer ageLimit,
            Long categoryId,
            Boolean isActive,
            String coverKey
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

        return gameRepository.save(game);
    }
}