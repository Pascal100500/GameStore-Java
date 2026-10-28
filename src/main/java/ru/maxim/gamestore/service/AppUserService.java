package ru.maxim.gamestore.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ru.maxim.gamestore.entity.AppUser;
import ru.maxim.gamestore.entity.Role;
import ru.maxim.gamestore.repository.AppUserRepository;

import java.util.List;

@Service
public class AppUserService {

    private final AppUserRepository appUserRepository;

    public AppUserService(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    @Transactional(readOnly = true)
    public List<AppUser> getAllUsers() {
        return appUserRepository.findAll();
    }

    @Transactional
    public void changeUserRole(
            Long userId,
            Role newRole,
            String currentUsername
    ) {

        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Пользователь не найден"
                        )
                );

        // Администратор не может изменить собственную роль
        if (user.getUsername().equals(currentUsername)) {
            throw new IllegalArgumentException(
                    "Нельзя изменять собственную роль"
            );
        }

        user.setRole(newRole);
    }
}
