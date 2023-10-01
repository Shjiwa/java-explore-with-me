package ru.practicum.ewm.service.compilation.service;

import ru.practicum.ewm.service.compilation.dto.CompilationDto;
import ru.practicum.ewm.service.compilation.dto.NewCompilationDto;
import ru.practicum.ewm.service.compilation.dto.UpdateCompilationRequest;

import java.util.List;

public interface CompilationService {
    CompilationDto create(NewCompilationDto dto);

    List<CompilationDto> getAll(Boolean pinned, int from, int size);

    CompilationDto getById(Long compId);

    CompilationDto update(Long compId, UpdateCompilationRequest compilationRequest);

    void delete(Long compId);
}
