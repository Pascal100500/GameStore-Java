package ru.maxim.gamestore.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.maxim.gamestore.entity.Game;

import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import java.util.Optional;

public interface GameRepository extends JpaRepository<Game, Long> {

    List<Game> findByIsActiveTrue();
    @EntityGraph(attributePaths = {"category"})
    Optional<Game> findWithCategoryById(Long id);

}