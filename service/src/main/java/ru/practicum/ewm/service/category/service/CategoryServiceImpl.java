package ru.practicum.ewm.service.category.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.service.category.dto.CategoryDto;
import ru.practicum.ewm.service.category.mapper.CategoryMapper;
import ru.practicum.ewm.service.category.model.Category;
import ru.practicum.ewm.service.category.repository.CategoryRepository;
import ru.practicum.ewm.service.error.NotFoundError;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;

    @Override
    public CategoryDto create(CategoryDto dto) {
        return CategoryMapper.INSTANCE.toDto(categoryRepository.save(CategoryMapper.INSTANCE.fromDto(dto)));
    }

    @Override
    public List<CategoryDto> getAll(int from, int size) {
        return categoryRepository.findAll(PageRequest.of(from / size, size)).getContent().stream()
                .map(CategoryMapper.INSTANCE::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public CategoryDto getById(Long catId) {
        return CategoryMapper.INSTANCE.toDto(categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundError("Category id=" + catId + " not found.")));
    }

    @Override
    public CategoryDto update(Long catId, CategoryDto dto) {
        Category category = categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundError("Category id=" + catId + " not found."));
        Optional.ofNullable(dto.getName()).ifPresent(category::setName);
        return CategoryMapper.INSTANCE.toDto(categoryRepository.save(category));
    }

    @Override
    public void delete(Long catId) {
        categoryRepository.findById(catId).orElseThrow(() -> new NotFoundError("Category id=" + catId + " not found."));
        categoryRepository.deleteById(catId);
    }
}
