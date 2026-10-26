package ru.maxim.gamestore.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "games")
@Getter
@Setter
@NoArgsConstructor
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(length = 2000)
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "age_limit")
    private Integer ageLimit;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "cover_key", length = 255)
    private String coverKey;

    @Column(name = "screenshot1_key", length = 255)
    private String screenshot1Key;

    @Column(name = "screenshot2_key", length = 255)
    private String screenshot2Key;

    @Column(name = "screenshot3_key", length = 255)
    private String screenshot3Key;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;
}