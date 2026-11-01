package com.pranov.devops.menu;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "menu_items")
public class MenuItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 300)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Category category;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private boolean vegetarian;

    @Column(nullable = false)
    private boolean spicy;

    @Column(nullable = false)
    private boolean available = true;

    protected MenuItem() {
        // for JPA
    }

    public MenuItem(String name, String description, Category category, BigDecimal price,
                    boolean vegetarian, boolean spicy) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.price = price;
        this.vegetarian = vegetarian;
        this.spicy = spicy;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Category getCategory() {
        return category;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public boolean isVegetarian() {
        return vegetarian;
    }

    public boolean isSpicy() {
        return spicy;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }
}
