package ru.practicum.ewm.service.compilation.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.service.compilation.dto.CompilationDto;
import ru.practicum.ewm.service.compilation.service.CompilationService;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/compilations")
@RequiredArgsConstructor
public class PublicCompilationsController {
    private final CompilationService compilationService;

    @GetMapping
    public List<CompilationDto> getAll(@RequestParam(required = false) Boolean pinned,
                                       @Valid @RequestParam(required = false, defaultValue = "0") @Min(0) int from,
                                       @Valid @RequestParam(required = false, defaultValue = "10") @Min(1) int size) {
        log.info("Поступил запрос на получение подборок событий.");
        return compilationService.getAll(pinned, from, size);
    }

    @GetMapping("/{compId}")
    public CompilationDto getById(@PathVariable Long compId) {
        log.info("Поступил запрос на получение информации о подборке с id={}", compId);
        return compilationService.getById(compId);
    }
}
