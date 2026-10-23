package ru.maxim.gamestore.service;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import ru.maxim.gamestore.entity.Category;
import ru.maxim.gamestore.repository.CategoryRepository;

@Component
public class CategoryInitializer implements CommandLineRunner {

    private final CategoryRepository categoryRepository;

    public CategoryInitializer(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public void run(String... args) {

        createCategoryIfNotExists("Симулятор");
        createCategoryIfNotExists("RPG");
        createCategoryIfNotExists("Action");

    }

    private void createCategoryIfNotExists(String name) {

        if (categoryRepository.findByNameIgnoreCase(name).isPresent()) {
            System.out.println("Category already exists: " + name);
            return;
        }

        Category category = new Category();
        category.setName(name);

        categoryRepository.save(category);

        System.out.println("Category created: " + name);
    }
}