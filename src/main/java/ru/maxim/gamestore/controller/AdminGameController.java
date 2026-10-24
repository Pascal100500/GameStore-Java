package ru.maxim.gamestore.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import ru.maxim.gamestore.service.CategoryService;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.maxim.gamestore.service.GameService;
import java.math.BigDecimal;
import ru.maxim.gamestore.service.MinioService;

import org.springframework.web.multipart.MultipartFile;

@Controller
public class AdminGameController {

    private final CategoryService categoryService;
    private final GameService gameService;
    private final MinioService minioService;

    public AdminGameController(
            CategoryService categoryService,
            GameService gameService,
            MinioService minioService
    ) {
        this.categoryService = categoryService;
        this.gameService = gameService;
        this.minioService = minioService;
    }

    @GetMapping("/admin/games/create")
    public String create(Model model) {

        model.addAttribute(
                "categories",
                categoryService.getAllCategories()
        );

        return "admin/games/create";
    }
    @PostMapping("/admin/games/create")
    public String store(
            @RequestParam String title,
            @RequestParam(required = false) String description,
            @RequestParam BigDecimal price,
            @RequestParam(required = false) Integer ageLimit,
            @RequestParam Long categoryId,
            @RequestParam(defaultValue = "false") Boolean isActive,
            @RequestParam(value = "cover", required = false) MultipartFile cover,
            RedirectAttributes redirectAttributes
    ) {

        if (title.isBlank() || title.length() > 150
                || price.signum() < 0
                || price.compareTo(new BigDecimal("99999999.99")) > 0
                || price.scale() > 2
                || (description != null && description.length() > 2000)
                || (ageLimit != null && ageLimit < 0)) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Проверьте правильность заполнения полей."
            );

            return "redirect:/admin/games/create";
        }
        String coverKey = null;
        if (cover != null && !cover.isEmpty()) {

            try {
                coverKey = minioService.uploadFile(cover);

                System.out.println(
                        "MinIO: обложка загружена — " + coverKey
                );

            } catch (Exception e) {

                redirectAttributes.addFlashAttribute(
                        "error",
                        "Не удалось загрузить обложку: " + e.getMessage()
                );

                return "redirect:/admin/games/create";
            }
        }

        gameService.createGame(
                title,
                description,
                price,
                ageLimit,
                categoryId,
                isActive,
                coverKey
        );

        redirectAttributes.addFlashAttribute(
                "success",
                "Игра успешно добавлена!"
        );

        return "redirect:/admin/games/create";
    }
}