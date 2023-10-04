package ru.practicum.ewm.service.compilation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.service.compilation.dto.CompilationDto;
import ru.practicum.ewm.service.compilation.dto.NewCompilationDto;
import ru.practicum.ewm.service.compilation.dto.UpdateCompilationRequest;
import ru.practicum.ewm.service.compilation.model.Compilation;
import ru.practicum.ewm.service.compilation.repository.CompilationRepository;
import ru.practicum.ewm.service.error.NotFoundError;
import ru.practicum.ewm.service.event.model.Event;
import ru.practicum.ewm.service.event.repository.EventRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static ru.practicum.ewm.service.compilation.mapper.CompilationMapper.COMPILATION_MAPPER;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompilationServiceImpl implements CompilationService {
    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;

    @Transactional
    @Override
    public CompilationDto create(NewCompilationDto dto) {
        List<Event> events = dto.getEvents() != null && !dto.getEvents().isEmpty() ?
                eventRepository.findAllById(dto.getEvents()) : new ArrayList<>();
        if (dto.getPinned() == null) {
            dto.setPinned(false);
        }

        return COMPILATION_MAPPER .toDto(compilationRepository.save(COMPILATION_MAPPER.fromDto(dto, events)));
    }

    @Transactional(readOnly = true)
    @Override
    public List<CompilationDto> getAll(Boolean pinned, int from, int size) {
        Pageable pageable = PageRequest.of(from, size, Sort.by(Sort.Direction.ASC, "id"));
        return compilationRepository.findAllByPinnedIsNullOrPinned(pinned, pageable).stream()
                .map(COMPILATION_MAPPER::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Override
    public CompilationDto getById(Long compId) {
        return COMPILATION_MAPPER.toDto(compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundError("Compilation id=" + compId + " not found.")));
    }

    @Transactional
    @Override
    public CompilationDto update(Long compId, UpdateCompilationRequest compilationRequest) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundError("Compilation id=" + compId + " not found."));

        if (compilationRequest.getEvents() != null) {
            compilation.setEvents(eventRepository.findAllById(compilationRequest.getEvents()));
        }

        Optional.ofNullable(compilationRequest.getTitle()).ifPresent(compilation::setTitle);
        Optional.ofNullable(compilationRequest.getPinned()).ifPresent(compilation::setPinned);

        return COMPILATION_MAPPER.toDto(compilation);
    }

    @Transactional
    @Override
    public void delete(Long compId) {
        if (!compilationRepository.existsById(compId)) {
            throw new NotFoundError("Compilation id=" + compId + " not found.");
        }
        compilationRepository.deleteById(compId);
    }
}
