package ru.practicum.ewm.service.event.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.service.event.dto.*;
import ru.practicum.ewm.service.event.service.PrivateEventService;
import ru.practicum.ewm.service.participation.dto.ParticipationRequestDto;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/users/{userId}/events")
@RequiredArgsConstructor
public class PrivateEventsController {
    private final PrivateEventService privateEventService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto create(@PathVariable Long userId, @Valid @RequestBody NewEventDto newEventDto) {
        log.info("Поступил запрос на создание события от пользователя с id={}, event={}", userId, newEventDto);
        return privateEventService.create(userId, newEventDto);
    }

    @GetMapping
    public List<EventShortDto> getAll(@PathVariable Long userId,
                                      @Valid @RequestParam(defaultValue = "0") @Min(0) int from,
                                      @Valid @RequestParam(defaultValue = "10") @Min(1) int size) {
        log.info("Поступил запрос на получение списка событий, добавленных пользователем с id={}", userId);
        return privateEventService.getAllPrivate(userId, from, size);
    }

    @GetMapping("/{eventId}")
    public EventFullDto getById(@PathVariable Long userId, @PathVariable Long eventId) {
        log.info("Поступил запрос на получение информации о событии с id={}, " +
                "добавленном пользователем с id={}", eventId, userId);
        return privateEventService.getByIdPrivate(userId, eventId);
    }

    @GetMapping("/{eventId}/requests")
    public List<ParticipationRequestDto> getParticipationRequests(@PathVariable Long userId,
                                                                  @PathVariable Long eventId) {
        log.info("Поступил запрос на получения списка запросов на участие в событии с id={}, " +
                "от пользователя с id={}", userId, eventId);
        return privateEventService.getParticipationRequests(userId, eventId);
    }

    @PatchMapping("/{eventId}")
    public EventFullDto update(@PathVariable Long userId, @PathVariable Long eventId,
                               @Valid @RequestBody UpdateEventUserRequest updateEventUserRequest) {
        log.info("Поступил запрос на изменение события с id={} от пользователя с id={}, updateData={}",
                eventId, userId, updateEventUserRequest);
        return privateEventService.updatePrivate(userId, eventId, updateEventUserRequest);
    }

    @PatchMapping("/{eventId}/requests")
    public EventRequestStatusUpdateResult updateParticipationRequests(
            @PathVariable Long userId, @PathVariable Long eventId,
            @Valid @RequestBody EventRequestStatusUpdateRequest eventRequestStatusUpdateRequest) {
        log.info("Поступил запрос на изменение статуса заявок на участие в событии с id={} " +
                "от пользователя с id={}, updateRequest={}", eventId, userId, eventRequestStatusUpdateRequest);
        return privateEventService.updateParticipationRequests(userId, eventId, eventRequestStatusUpdateRequest);
    }
}
