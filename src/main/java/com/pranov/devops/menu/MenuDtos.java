package com.pranov.devops.menu;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public final class MenuDtos {

    private MenuDtos() {
    }

    public record MenuItemResponse(Long id, String name, String description, Category category,
                                   String categoryLabel, BigDecimal price, boolean vegetarian,
                                   boolean spicy, boolean available) {

        static MenuItemResponse from(MenuItem item) {
            return new MenuItemResponse(item.getId(), item.getName(), item.getDescription(),
                    item.getCategory(), item.getCategory().getLabel(), item.getPrice(),
                    item.isVegetarian(), item.isSpicy(), item.isAvailable());
        }
    }

    public record CategoryResponse(Category code, String label) {
    }

    public record NewMenuItemRequest(
            @NotBlank @Size(max = 100) String name,
            @NotBlank @Size(max = 300) String description,
            @NotNull Category category,
            @NotNull @DecimalMin("1.00") @DecimalMax("9999.00") BigDecimal price,
            boolean vegetarian,
            boolean spicy) {
    }

    public record AvailabilityRequest(@NotNull Boolean available) {
    }
}
