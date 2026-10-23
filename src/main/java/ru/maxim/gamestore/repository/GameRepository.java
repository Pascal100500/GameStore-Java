package ru.maxim.gamestore.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.maxim.gamestore.entity.Game;

import java.util.List;

public interface GameRepository extends JpaRepository<Game, Long> {

    List<Game> findByIsActiveTrue();

}