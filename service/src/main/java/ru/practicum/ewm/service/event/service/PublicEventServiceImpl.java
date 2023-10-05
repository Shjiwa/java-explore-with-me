package ru.practicum.ewm.service.event.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.service.constant.SortMode;
import ru.practicum.ewm.service.error.NotFoundError;
import ru.practicum.ewm.service.event.dto.EventFullDto;
import ru.practicum.ewm.service.event.dto.EventShortDto;
import ru.practicum.ewm.service.event.model.Event;
import ru.practicum.ewm.service.event.repository.EventRepository;
import ru.practicum.ewm.service.participation.repository.ParticipationRequestRepository;
import ru.practicum.ewm.stats.client.StatsClient;
import ru.practicum.ewm.stats.dto.EndpointHitDto;
import ru.practicum.ewm.stats.dto.ViewStatsDto;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static ru.practicum.ewm.service.constant.DateTimeFormat.DATETIME_FORMAT;
import static ru.practicum.ewm.service.constant.EventState.PUBLISHED;
import static ru.practicum.ewm.service.constant.ParticipationRequestStatus.CONFIRMED;
import static ru.practicum.ewm.service.event.mapper.EventMapper.EVENT_MAPPER;

@Service
@RequiredArgsConstructor
public class PublicEventServiceImpl implements PublicEventService {
    private final EventRepository eventRepository;
    private final ParticipationRequestRepository participationRequestRepository;
    private final StatsClient statsClient;

    @Transactional(readOnly = true)
    @Override
    public List<EventShortDto> getAllPublic(String text, List<Long> categories, Boolean paid, LocalDateTime rangeStart,
                                            LocalDateTime rangeEnd, Boolean onlyAvailable, SortMode sort,
                                            int from, int size, HttpServletRequest request) {
        statsClient.hit(EndpointHitDto.builder()
                .app("ewm")
                .uri(request.getRequestURI())
                .ip(request.getRemoteAddr())
                .hitTimestamp(LocalDateTime.now())
                .build());

        if (categories != null && categories.size() == 1 && categories.get(0).equals(0L)) {
            categories = null;
        }
        if (rangeStart == null) {
            rangeStart = LocalDateTime.now();
        }
        if (rangeEnd == null) {
            rangeEnd = LocalDateTime.now().plusYears(100);
        }

        List<Event> events = eventRepository.findAllByPublic(text, categories, paid, rangeStart, rangeEnd);

        if (onlyAvailable) {
            events = events.stream()
                    .filter(event -> event.getParticipantLimit().equals(0)
                            || event.getParticipantLimit() < participationRequestRepository
                            .countByEventIdAndStatus(event.getId(), CONFIRMED))
                    .collect(Collectors.toList());
        }

        List<String> eventUrls = events.stream()
                .map(event -> "/events/" + event.getId())
                .collect(Collectors.toList());

        List<ViewStatsDto> viewStatsDtos = statsClient.getStats(
                rangeStart.format(DateTimeFormatter.ofPattern(DATETIME_FORMAT)),
                rangeEnd.format(DateTimeFormatter.ofPattern(DATETIME_FORMAT)),
                eventUrls, true);

        List<EventShortDto> eventShortDtos = events.stream()
                .map(EVENT_MAPPER::toShortDto)
                .peek(eventShortDto -> {
                    Optional<ViewStatsDto> viewStatsDto = viewStatsDtos.stream()
                            .filter(statsDto -> statsDto.getUri().equals("/events/" + eventShortDto.getId()))
                            .findFirst();
                    eventShortDto.setViews(viewStatsDto.map(ViewStatsDto::getHits).orElse(0L));
                }).peek(dto -> dto.setConfirmedRequests(
                        participationRequestRepository.countByEventIdAndStatus(dto.getId(), CONFIRMED)))
                .collect(Collectors.toList());

        switch (sort) {
            case EVENT_DATE:
                eventShortDtos.sort(Comparator.comparing(EventShortDto::getEventDate));
                break;
            case VIEWS:
                eventShortDtos.sort(Comparator.comparing(EventShortDto::getViews).reversed());
                break;
        }

        if (from >= eventShortDtos.size()) {
            return Collections.emptyList();
        }

        int toIndex = Math.min(from + size, eventShortDtos.size());
        return eventShortDtos.subList(from, toIndex);
    }

    @Transactional(readOnly = true)
    @Override
    public EventFullDto getByIdPublic(Long eventId, HttpServletRequest request) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundError("Event id=" + eventId + " not found."));

        if (!event.getState().equals(PUBLISHED)) {
            throw new NotFoundError("Event id=" + eventId + " is not published.");
        }

        statsClient.hit(EndpointHitDto.builder()
                .app("ewm")
                .uri(request.getRequestURI())
                .ip(request.getRemoteAddr())
                .hitTimestamp(LocalDateTime.now())
                .build());

        List<String> eventUrls = Collections.singletonList("/events/" + event.getId());

        List<ViewStatsDto> viewStatsDtos = statsClient.getStats(
                LocalDateTime.now().minusYears(100).format(DateTimeFormatter.ofPattern(DATETIME_FORMAT)),
                LocalDateTime.now().plusYears(100).format(DateTimeFormatter.ofPattern(DATETIME_FORMAT)),
                eventUrls, true);

        EventFullDto dto = EVENT_MAPPER.toFullDto(event);
        dto.setConfirmedRequests(participationRequestRepository.countByEventIdAndStatus(event.getId(), CONFIRMED));
        dto.setViews(viewStatsDtos.isEmpty() ? 0L : viewStatsDtos.get(0).getHits());

        return dto;
    }
}
