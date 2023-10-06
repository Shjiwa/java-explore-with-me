package ru.practicum.ewm.service.event.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.service.constant.EventState;
import ru.practicum.ewm.service.event.dto.EventFullDto;
import ru.practicum.ewm.service.event.dto.UpdateEventAdminRequest;
import ru.practicum.ewm.service.event.service.AdminEventService;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.time.LocalDateTime;
import java.util.List;

import static ru.practicum.ewm.service.constant.DateTimeFormat.DATETIME_FORMAT;

@Slf4j
@RestController
@RequestMapping("/admin/events")
@RequiredArgsConstructor
public class AdminEventsController {
    private final AdminEventService adminEventService;

    @GetMapping
    public List<EventFullDto> getAll(@RequestParam(required = false) List<Long> users,
                                     @RequestParam(required = false) List<EventState> states,
                                     @RequestParam(required = false) List<Long> categories,
                                     @RequestParam(required = false)
                                         @DateTimeFormat(pattern = DATETIME_FORMAT) LocalDateTime rangeStart,
                                     @RequestParam(required = false)
                                         @DateTimeFormat(pattern = DATETIME_FORMAT) LocalDateTime rangeEnd,
                                     @Valid @RequestParam(defaultValue = "0") @Min(0) int from,
                                     @Valid @RequestParam(defaultValue = "10") @Min(1) int size) {
        log.info("Поступил запрос на получение списка событий: " +
                "users={}, states={}, categories={}, rangeStart={}, rangeEnd={}, from={}, size={}",
                users, states, categories, rangeStart, rangeEnd, from, size);
        return adminEventService.getAllAdmin(users, states, categories, rangeStart, rangeEnd, from, size);
    }

    @PatchMapping("/{eventId}")
    public EventFullDto update(@PathVariable Long eventId,
                               @Valid @RequestBody UpdateEventAdminRequest updateEventAdminRequest) {
        log.info("Поступил запрос на изменение события с id={}, updateData={}", eventId, updateEventAdminRequest);
        return adminEventService.updateAdmin(eventId, updateEventAdminRequest);
    }
}
