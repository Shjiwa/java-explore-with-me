package ru.practicum.ewm.service.event.service;

import ru.practicum.ewm.service.constant.EventState;
import ru.practicum.ewm.service.event.dto.EventFullDto;
import ru.practicum.ewm.service.event.dto.UpdateEventAdminRequest;

import java.time.LocalDateTime;
import java.util.List;

public interface AdminEventService {
    List<EventFullDto> getAllAdmin(List<Long> users, List<EventState> states, List<Long> categories,
                                   LocalDateTime rangeStart, LocalDateTime rangeEnd, int from, int size);

    EventFullDto updateAdmin(Long eventId, UpdateEventAdminRequest updateEventAdminRequest);

}
