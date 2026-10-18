package ru.maxim.gamestore.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.maxim.gamestore.entity.AppUser;

import java.util.Optional;

public interface AppUserRepository
        extends JpaRepository<AppUser, Long> {

    Optional<AppUser> findByUsername(String username);
}