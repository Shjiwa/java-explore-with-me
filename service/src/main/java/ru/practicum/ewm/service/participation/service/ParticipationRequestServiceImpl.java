package ru.practicum.ewm.service.participation.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.service.error.ConflictError;
import ru.practicum.ewm.service.error.NotFoundError;
import ru.practicum.ewm.service.event.model.Event;
import ru.practicum.ewm.service.event.repository.EventRepository;
import ru.practicum.ewm.service.participation.dto.ParticipationRequestDto;
import ru.practicum.ewm.service.participation.model.ParticipationRequest;
import ru.practicum.ewm.service.participation.repository.ParticipationRequestRepository;
import ru.practicum.ewm.service.user.model.User;
import ru.practicum.ewm.service.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.ewm.service.constant.EventState.PUBLISHED;
import static ru.practicum.ewm.service.constant.ParticipationRequestStatus.*;
import static ru.practicum.ewm.service.participation.mapper.ParticipationRequestMapper.REQUEST_MAPPER;

@Service
@RequiredArgsConstructor
public class ParticipationRequestServiceImpl implements ParticipationRequestService {
    private final ParticipationRequestRepository participationRequestRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    @Override
    public ParticipationRequestDto create(Long userId, Long eventId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundError("User id=" + userId + " not found."));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundError("Event id=" + eventId + " not found."));
        if (event.getInitiator().getId().equals(userId)) {
            throw new ConflictError("The event initiator cannot apply to participate in his own event.");
        }
        if (!event.getState().equals(PUBLISHED)) {
            throw new ConflictError("Unable to participate in an unpublished event.");
        }
        if (event.getParticipantLimit() > 0) {
            if (event.getParticipantLimit() <= participationRequestRepository
                    .countByEventIdAndStatus(eventId, CONFIRMED)) {
                throw new ConflictError("The number of requests for participation has exceeded the limit.");
            }
        }
        ParticipationRequest participationRequest = new ParticipationRequest();
        participationRequest.setCreated(LocalDateTime.now());
        participationRequest.setEvent(event);
        participationRequest.setRequester(user);
        participationRequest.setStatus(
                event.getRequestModeration() && !event.getParticipantLimit().equals(0) ? PENDING : CONFIRMED);
        return REQUEST_MAPPER.toDto(participationRequestRepository.save(participationRequest));
    }

    @Override
    public List<ParticipationRequestDto> getAllRequests(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundError("User id=" + userId + "not found.");
        }
        return participationRequestRepository.findAllByRequesterId(userId).stream()
                .map(REQUEST_MAPPER::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public ParticipationRequestDto update(Long userId, Long requestId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundError("User id=" + userId + "not found.");
        }
        ParticipationRequest request = participationRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundError("Participation request id=" + requestId + " not found."));
        if (!request.getRequester().getId().equals(userId)) {
            throw new NotFoundError("Request id=" + requestId + " not found.");
        }
        request.setStatus(CANCELED);
        return REQUEST_MAPPER.toDto(participationRequestRepository.save(request));
    }
}
