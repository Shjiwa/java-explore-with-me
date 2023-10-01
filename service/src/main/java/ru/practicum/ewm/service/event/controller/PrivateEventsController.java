package ru.practicum.ewm.service.event.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.service.event.dto.*;
import ru.practicum.ewm.service.event.service.EventService;
import ru.practicum.ewm.service.participation.dto.ParticipationRequestDto;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/users/{userId}/events")
@RequiredArgsConstructor
public class PrivateEventsController {
    private final EventService eventService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto create(@PathVariable Long userId, @Valid @RequestBody NewEventDto newEventDto) {
        return eventService.create(userId, newEventDto);
    }

    @GetMapping
    public List<EventShortDto> getAll(@PathVariable Long userId,
                                      @Valid @RequestParam(defaultValue = "0") @Min(0) int from,
                                      @Valid @RequestParam(defaultValue = "10") @Min(1) int size) {
        return eventService.getAllPrivate(userId, from, size);
    }

    @GetMapping("/{eventId}")
    public EventFullDto getById(@PathVariable Long userId, @PathVariable Long eventId) {
        return eventService.getByIdPrivate(userId, eventId);
    }

    @GetMapping("/{eventId}/requests")
    public List<ParticipationRequestDto> getParticipationRequests(@PathVariable Long userId,
                                                                  @PathVariable Long eventId) {
        return eventService.getParticipationRequests(userId, eventId);
    }

    @PatchMapping("/{eventId}")
    public EventFullDto update(@PathVariable Long userId, @PathVariable Long eventId,
                               @Valid @RequestBody UpdateEventUserRequest updateEventUserRequest) {
        return eventService.updatePrivate(userId, eventId, updateEventUserRequest);
    }

    @PatchMapping("/{eventId}/requests")
    public EventRequestStatusUpdateResult updateParticipationRequests(
            @PathVariable Long userId, @PathVariable Long eventId,
            @Valid @RequestBody EventRequestStatusUpdateRequest eventRequestStatusUpdateRequest) {
        return eventService.updateParticipationRequests(userId, eventId, eventRequestStatusUpdateRequest);
    }
}
