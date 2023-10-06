package ru.practicum.ewm.service.category.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.service.category.dto.CategoryDto;
import ru.practicum.ewm.service.category.model.Category;
import ru.practicum.ewm.service.category.repository.CategoryRepository;
import ru.practicum.ewm.service.error.NotFoundError;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static ru.practicum.ewm.service.category.mapper.CategoryMapper.CATEGORY_MAPPER;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;

    @Override
    public CategoryDto create(CategoryDto dto) {
        return CATEGORY_MAPPER.toDto(categoryRepository.save(CATEGORY_MAPPER.fromDto(dto)));
    }

    @Override
    public List<CategoryDto> getAll(int from, int size) {
        return categoryRepository.findAll(PageRequest.of(from, size)).getContent().stream()
                .map(CATEGORY_MAPPER::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Override
    public CategoryDto getById(Long catId) {
        return CATEGORY_MAPPER.toDto(categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundError("Category id=" + catId + " not found.")));
    }

    @Transactional
    @Override
    public CategoryDto update(Long catId, CategoryDto dto) {
        Category category = categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundError("Category id=" + catId + " not found."));
        Optional.ofNullable(dto.getName()).ifPresent(category::setName);
        return CATEGORY_MAPPER.toDto(categoryRepository.save(category));
    }

    @Transactional
    @Override
    public void delete(Long catId) {
        if (!categoryRepository.existsById(catId)) {
            throw new NotFoundError("Category id=" + catId + " not found.");
        }
        categoryRepository.deleteById(catId);
    }
}
