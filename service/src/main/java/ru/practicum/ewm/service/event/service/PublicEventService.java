package ru.practicum.ewm.service.event.service;

import ru.practicum.ewm.service.constant.SortMode;
import ru.practicum.ewm.service.event.dto.EventFullDto;
import ru.practicum.ewm.service.event.dto.EventShortDto;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;

public interface PublicEventService {
    List<EventShortDto> getAllPublic(String text, List<Long> categories, Boolean paid, LocalDateTime rangeStart,
                                     LocalDateTime rangeEnd, Boolean onlyAvailable, SortMode sort,
                                     int from, int size, HttpServletRequest request);

    EventFullDto getByIdPublic(Long id, HttpServletRequest request);
}
