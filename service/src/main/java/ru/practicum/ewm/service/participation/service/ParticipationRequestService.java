package ru.practicum.ewm.service.participation.service;

import ru.practicum.ewm.service.participation.dto.ParticipationRequestDto;

import java.util.List;

public interface ParticipationRequestService {
    ParticipationRequestDto create(Long userId, Long eventId);

    List<ParticipationRequestDto> getAllRequests(Long userId);

    ParticipationRequestDto update(Long userId, Long requestId);
}
