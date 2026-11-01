package com.pranov.devops.menu;

import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pranov.devops.common.NotFoundException;
import com.pranov.devops.menu.MenuDtos.CategoryResponse;
import com.pranov.devops.menu.MenuDtos.MenuItemResponse;
import com.pranov.devops.menu.MenuDtos.NewMenuItemRequest;

@Service
@Transactional(readOnly = true)
public class MenuService {

    private final MenuItemRepository repository;

    public MenuService(MenuItemRepository repository) {
        this.repository = repository;
    }

    public List<MenuItemResponse> list(Category category) {
        List<MenuItem> items = category == null
                ? repository.findAllByOrderByCategoryAscNameAsc()
                : repository.findByCategoryOrderByNameAsc(category);
        return items.stream().map(MenuItemResponse::from).toList();
    }

    public MenuItemResponse get(Long id) {
        return MenuItemResponse.from(find(id));
    }

    public List<CategoryResponse> categories() {
        return Arrays.stream(Category.values())
                .map(c -> new CategoryResponse(c, c.getLabel()))
                .toList();
    }

    @Transactional
    public MenuItemResponse add(NewMenuItemRequest request) {
        MenuItem item = new MenuItem(request.name().trim(), request.description().trim(),
                request.category(), request.price(), request.vegetarian(), request.spicy());
        return MenuItemResponse.from(repository.save(item));
    }

    @Transactional
    public MenuItemResponse setAvailability(Long id, boolean available) {
        MenuItem item = find(id);
        item.setAvailable(available);
        return MenuItemResponse.from(item);
    }

    MenuItem find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Menu item " + id + " not found"));
    }
}
