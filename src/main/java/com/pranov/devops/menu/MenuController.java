package com.pranov.devops.menu;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.pranov.devops.menu.MenuDtos.AvailabilityRequest;
import com.pranov.devops.menu.MenuDtos.CategoryResponse;
import com.pranov.devops.menu.MenuDtos.MenuItemResponse;
import com.pranov.devops.menu.MenuDtos.NewMenuItemRequest;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/menu")
public class MenuController {

    private final MenuService menuService;

    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    @GetMapping
    public List<MenuItemResponse> list(@RequestParam(required = false) Category category) {
        return menuService.list(category);
    }

    @GetMapping("/categories")
    public List<CategoryResponse> categories() {
        return menuService.categories();
    }

    @GetMapping("/{id}")
    public MenuItemResponse get(@PathVariable Long id) {
        return menuService.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MenuItemResponse add(@Valid @RequestBody NewMenuItemRequest request) {
        return menuService.add(request);
    }

    @PatchMapping("/{id}/availability")
    public MenuItemResponse setAvailability(@PathVariable Long id,
                                            @Valid @RequestBody AvailabilityRequest request) {
        return menuService.setAvailability(id, request.available());
    }
}
