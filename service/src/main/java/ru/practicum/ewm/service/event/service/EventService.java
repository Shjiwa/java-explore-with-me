package ru.practicum.ewm.service.event.service;

import ru.practicum.ewm.service.constant.EventState;
import ru.practicum.ewm.service.constant.SortMode;
import ru.practicum.ewm.service.event.dto.*;
import ru.practicum.ewm.service.participation.dto.ParticipationRequestDto;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;

public interface EventService {
    EventFullDto create(Long userId, NewEventDto newEventDto);

    List<EventFullDto> getAllAdmin(List<Long> users, List<EventState> states, List<Long> categories,
                                   LocalDateTime rangeStart, LocalDateTime rangeEnd, int from, int size);

    List<EventShortDto> getAllPrivate(Long userId, int from, int size);

    EventFullDto getByIdPrivate(Long userId, Long eventId);

    List<ParticipationRequestDto> getParticipationRequests(Long userId, Long eventId);

    List<EventShortDto> getAllPublic(String text, List<Long> categories, Boolean paid, LocalDateTime rangeStart,
                                     LocalDateTime rangeEnd, Boolean onlyAvailable, SortMode sort,
                                     int from, int size, HttpServletRequest request);

    EventFullDto getByIdPublic(Long id, HttpServletRequest request);

    EventFullDto updateAdmin(Long eventId, UpdateEventAdminRequest updateEventAdminRequest);

    EventFullDto updatePrivate(Long userId, Long eventId, UpdateEventUserRequest updateEventUserRequest);

    EventRequestStatusUpdateResult updateParticipationRequests(
            Long userId, Long eventId, EventRequestStatusUpdateRequest eventRequestStatusUpdateRequest);
}
