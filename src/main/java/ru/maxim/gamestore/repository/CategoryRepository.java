package ru.maxim.gamestore.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.maxim.gamestore.entity.Category;

import java.util.Optional;

public interface CategoryRepository
        extends JpaRepository<Category, Long> {

    Optional<Category> findByNameIgnoreCase(String name);

}