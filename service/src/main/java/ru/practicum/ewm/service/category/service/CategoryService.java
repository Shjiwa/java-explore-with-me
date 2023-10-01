package ru.practicum.ewm.service.category.service;

import ru.practicum.ewm.service.category.dto.CategoryDto;

import java.util.List;

public interface CategoryService {
    CategoryDto create(CategoryDto dto);

    List<CategoryDto> getAll(int from, int size);

    CategoryDto getById(Long catId);

    CategoryDto update(Long catId, CategoryDto dto);

    void delete(Long catId);
}
