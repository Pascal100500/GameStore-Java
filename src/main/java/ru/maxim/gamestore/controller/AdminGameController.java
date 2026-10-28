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

import org.springframework.web.bind.annotation.PathVariable;
import ru.maxim.gamestore.entity.Game;

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

    @GetMapping("/admin/games/{id}/edit")
    public String edit(
            @PathVariable Long id,
            Model model
    ) {

        Game game = gameService.getGameForEdit(id);

        model.addAttribute("game", game);

        model.addAttribute(
                "categories",
                categoryService.getAllCategories()
        );

        return "admin/games/edit";
    }

    @PostMapping("/admin/games/{id}/edit")
    public String update(
            @PathVariable Long id,
            @RequestParam String title,
            @RequestParam(required = false) String description,
            @RequestParam BigDecimal price,
            @RequestParam(required = false) Integer ageLimit,
            @RequestParam Long categoryId,
            @RequestParam(defaultValue = "false") Boolean isActive,
            @RequestParam(value = "cover", required = false) MultipartFile cover,
            @RequestParam(value = "screenshot1", required = false)
            MultipartFile screenshot1,
            @RequestParam(value = "screenshot2", required = false)
            MultipartFile screenshot2,
            @RequestParam(value = "screenshot3", required = false)
            MultipartFile screenshot3,
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

            return "redirect:/admin/games/" + id + "/edit";
        }

        String coverKey = null;

        if (cover != null && !cover.isEmpty()) {

            try {
                coverKey = minioService.uploadFile(cover);

            } catch (Exception e) {

                redirectAttributes.addFlashAttribute(
                        "error",
                        "Не удалось загрузить обложку."
                );

                return "redirect:/admin/games/" + id + "/edit";
            }
        }

        String screenshot1Key = null;
        String screenshot2Key = null;
        String screenshot3Key = null;

        try {
            if (screenshot1 != null && !screenshot1.isEmpty()) {
                screenshot1Key = minioService.uploadFile(screenshot1);
            }

            if (screenshot2 != null && !screenshot2.isEmpty()) {
                screenshot2Key = minioService.uploadFile(screenshot2);
            }

            if (screenshot3 != null && !screenshot3.isEmpty()) {
                screenshot3Key = minioService.uploadFile(screenshot3);
            }

        } catch (Exception e) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Не удалось загрузить скриншоты."
            );

            return "redirect:/admin/games/" + id + "/edit";
        }

        gameService.updateGame(
                id,
                title,
                description,
                price,
                ageLimit,
                categoryId,
                isActive,
                coverKey,
                screenshot1Key,
                screenshot2Key,
                screenshot3Key
        );

        redirectAttributes.addFlashAttribute(
                "success",
                "Изменения успешно сохранены!"
        );

        return "redirect:/admin/games/" + id + "/edit";
    }

    @PostMapping("/admin/games/{id}/delete")
    public String delete(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes
    ) {

        gameService.deactivateGame(id);

        redirectAttributes.addFlashAttribute(
                "success",
                "Игра успешно скрыта из каталога!"
        );

        return "redirect:/games";
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
            @RequestParam(value = "screenshot1", required = false)
            MultipartFile screenshot1,
            @RequestParam(value = "screenshot2", required = false)
            MultipartFile screenshot2,
            @RequestParam(value = "screenshot3", required = false)
            MultipartFile screenshot3,
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

        String screenshot1Key = null;
        String screenshot2Key = null;
        String screenshot3Key = null;

        try {
            if (screenshot1 != null && !screenshot1.isEmpty()) {
                screenshot1Key = minioService.uploadFile(screenshot1);
            }

            if (screenshot2 != null && !screenshot2.isEmpty()) {
                screenshot2Key = minioService.uploadFile(screenshot2);
            }

            if (screenshot3 != null && !screenshot3.isEmpty()) {
                screenshot3Key = minioService.uploadFile(screenshot3);
            }

        } catch (Exception e) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Не удалось загрузить скриншоты."
            );

            return "redirect:/admin/games/create";
        }


        gameService.createGame(
                title,
                description,
                price,
                ageLimit,
                categoryId,
                isActive,
                coverKey,
                screenshot1Key,
                screenshot2Key,
                screenshot3Key
        );

        redirectAttributes.addFlashAttribute(
                "success",
                "Игра успешно добавлена!"
        );

        return "redirect:/admin/games/create";
    }
}