package ru.practicum.ewm.service.event.service;

import ru.practicum.ewm.service.event.dto.*;
import ru.practicum.ewm.service.participation.dto.ParticipationRequestDto;

import java.util.List;

public interface PrivateEventService {
    EventFullDto create(Long userId, NewEventDto newEventDto);

    List<EventShortDto> getAllPrivate(Long userId, int from, int size);

    EventFullDto getByIdPrivate(Long userId, Long eventId);

    List<ParticipationRequestDto> getParticipationRequests(Long userId, Long eventId);

    EventFullDto updatePrivate(Long userId, Long eventId, UpdateEventUserRequest updateEventUserRequest);

    EventRequestStatusUpdateResult updateParticipationRequests(
            Long userId, Long eventId, EventRequestStatusUpdateRequest eventRequestStatusUpdateRequest);
}
