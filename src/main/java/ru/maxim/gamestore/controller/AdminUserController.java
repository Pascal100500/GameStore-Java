package ru.maxim.gamestore.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import ru.maxim.gamestore.entity.Role;
import ru.maxim.gamestore.service.AppUserService;

@Controller
@RequestMapping("/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final AppUserService appUserService;

    public AdminUserController(AppUserService appUserService) {
        this.appUserService = appUserService;
    }

    @GetMapping
    public String index(Model model) {

        model.addAttribute(
                "users",
                appUserService.getAllUsers()
        );

        model.addAttribute("roles", Role.values());

        return "admin/users/index";
    }

    @PostMapping("/{id}/role")
    public String changeRole(
            @PathVariable Long id,
            @RequestParam Role role,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {

        try {
            appUserService.changeUserRole(
                    id,
                    role,
                    authentication.getName()
            );

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Роль пользователя успешно изменена"
            );

        } catch (IllegalArgumentException ex) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    ex.getMessage()
            );
        }

        return "redirect:/admin/users";
    }
}