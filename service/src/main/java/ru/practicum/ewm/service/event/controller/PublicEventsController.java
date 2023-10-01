package ru.practicum.ewm.service.event.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.service.constant.SortMode;
import ru.practicum.ewm.service.error.BadRequestError;
import ru.practicum.ewm.service.event.dto.EventFullDto;
import ru.practicum.ewm.service.event.dto.EventShortDto;
import ru.practicum.ewm.service.event.service.EventService;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.time.LocalDateTime;
import java.util.List;

import static ru.practicum.ewm.service.constant.DateTimeFormat.DATETIME_FORMAT;

@Slf4j
@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class PublicEventsController {
    private final EventService eventService;

    @GetMapping
    public List<EventShortDto> getAll(@RequestParam(defaultValue = "") String text,
                                      @RequestParam(required = false) List<Long> categories,
                                      @RequestParam(required = false) Boolean paid,
                                      @RequestParam(required = false)
                                          @DateTimeFormat(pattern = DATETIME_FORMAT) LocalDateTime rangeStart,
                                      @RequestParam(required = false)
                                          @DateTimeFormat(pattern = DATETIME_FORMAT) LocalDateTime rangeEnd,
                                      @RequestParam(defaultValue = "false") Boolean onlyAvailable,
                                      @RequestParam(defaultValue = "VIEWS") SortMode sort,
                                      @Valid @RequestParam(defaultValue = "0") @Min(0) int from,
                                      @Valid @RequestParam(defaultValue = "10") @Min(1) int size,
                                      HttpServletRequest request) {
        if (rangeStart != null && rangeEnd != null && rangeStart.isAfter(rangeEnd)) {
            throw new BadRequestError("Start date must be before end date");
        }
        return eventService.getAllPublic(
                text, categories, paid, rangeStart, rangeEnd, onlyAvailable, sort, from, size, request);
    }

    @GetMapping("/{id}")
    public EventFullDto getById(@PathVariable Long id, HttpServletRequest request) {
        return eventService.getByIdPublic(id, request);
    }
}
