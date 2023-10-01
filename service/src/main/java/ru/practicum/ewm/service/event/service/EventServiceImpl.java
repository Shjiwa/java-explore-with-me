package ru.practicum.ewm.service.event.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.service.category.model.Category;
import ru.practicum.ewm.service.category.repository.CategoryRepository;
import ru.practicum.ewm.service.constant.EventState;
import ru.practicum.ewm.service.constant.SortMode;
import ru.practicum.ewm.service.error.ConflictError;
import ru.practicum.ewm.service.error.NotFoundError;
import ru.practicum.ewm.service.event.dto.*;
import ru.practicum.ewm.service.event.mapper.EventMapper;
import ru.practicum.ewm.service.event.mapper.LocationMapper;
import ru.practicum.ewm.service.event.model.Event;
import ru.practicum.ewm.service.event.model.Location;
import ru.practicum.ewm.service.event.repository.EventRepository;
import ru.practicum.ewm.service.event.repository.LocationRepository;
import ru.practicum.ewm.service.participation.dto.ParticipationRequestDto;
import ru.practicum.ewm.service.participation.mapper.ParticipationRequestMapper;
import ru.practicum.ewm.service.participation.model.ParticipationRequest;
import ru.practicum.ewm.service.participation.repository.ParticipationRequestRepository;
import ru.practicum.ewm.service.user.model.User;
import ru.practicum.ewm.service.user.repository.UserRepository;
import ru.practicum.ewm.stats.client.StatsClient;
import ru.practicum.ewm.stats.dto.EndpointHitDto;
import ru.practicum.ewm.stats.dto.ViewStatsDto;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static ru.practicum.ewm.service.constant.DateTimeFormat.DATETIME_FORMAT;
import static ru.practicum.ewm.service.constant.EventState.CANCELED;
import static ru.practicum.ewm.service.constant.EventState.PENDING;
import static ru.practicum.ewm.service.constant.ParticipationRequestStatus.CONFIRMED;
import static ru.practicum.ewm.service.constant.ParticipationRequestStatus.REJECTED;
import static ru.practicum.ewm.service.constant.StateActionAdmin.PUBLISH_EVENT;
import static ru.practicum.ewm.service.constant.StateActionAdmin.REJECT_EVENT;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final LocationRepository locationRepository;
    private final EventRepository eventRepository;
    private final ParticipationRequestRepository participationRequestRepository;

    @Value("${STATS_SERVER_URL:http://localhost:9090}")
    private String statsClientUrl;

    private StatsClient statsClient;

    @PostConstruct
    private void init() {
        statsClient = new StatsClient(statsClientUrl);
    }

    @Override
    public EventFullDto create(Long userId, NewEventDto newEventDto) {
        if (LocalDateTime.now().plusHours(2).isAfter(newEventDto.getEventTimestamp())) {
            throw new ConflictError("The date and time for which the event is scheduled cannot be earlier than " +
                    "two hours from the current moment.");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundError("User id=" + userId + " not found."));
        Category category = categoryRepository.findById(newEventDto.getCategory())
                .orElseThrow(() -> new NotFoundError("Category id=" + newEventDto.getCategory() + " not found."));
        Location location = getLocation(newEventDto.getLocation());
        Event event = EventMapper.INSTANCE.fromDto(newEventDto, category, location);

        event.setInitiator(user);
        event.setCreatedOn(LocalDateTime.now());
        event.setState(PENDING);
        event.setPaid(newEventDto.getPaid() != null && newEventDto.getPaid());
        event.setParticipantLimit(newEventDto.getParticipantLimit() == null ? 0 : newEventDto.getParticipantLimit());
        event.setRequestModeration(newEventDto.getRequestModeration() == null || newEventDto.getRequestModeration());
        return EventMapper.INSTANCE.toFullDto(eventRepository.save(event));
    }

    @Transactional(readOnly = true)
    @Override
    public List<EventFullDto> getAllAdmin(List<Long> users, List<EventState> states, List<Long> categories,
                                          LocalDateTime rangeStart, LocalDateTime rangeEnd, int from, int size) {
        Pageable pageable = PageRequest.of(from, size, Sort.by(Sort.Direction.ASC, "id"));

        if (users != null && users.size() == 1 && users.get(0).equals(0L)) {
            users = null;
        }
        if (categories != null && categories.size() == 1 && categories.get(0).equals(0L)) {
            categories = null;
        }
        if (rangeStart == null) {
            rangeStart = LocalDateTime.now();
        }
        if (rangeEnd == null) {
            rangeEnd = LocalDateTime.MAX;
        }

        List<Event> events = eventRepository.findAllByAdmin(users, states, categories, rangeStart, rangeEnd, pageable);

        List<String> eventUrls = events.stream()
                .map(event -> "/events/" + event.getId())
                .collect(Collectors.toList());

        List<ViewStatsDto> viewStatsDtos = statsClient.getStats(
                rangeStart.format(DateTimeFormatter.ofPattern(DATETIME_FORMAT)),
                rangeEnd.format(DateTimeFormatter.ofPattern(DATETIME_FORMAT)),
                eventUrls, true);
        return events.stream()
                .map(EventMapper.INSTANCE::toFullDto)
                .peek(eventFullDto -> {
                    Optional<ViewStatsDto> viewStatsDto = viewStatsDtos.stream()
                            .filter(viewStatsDto1 -> viewStatsDto1.getUri().equals("/event/" + eventFullDto.getId()))
                            .findFirst();
                    eventFullDto.setViews(viewStatsDto.map(ViewStatsDto::getHits).orElse(0L));
                }).peek(eventFullDto -> eventFullDto.setConfirmedRequests(
                        participationRequestRepository.countByEventIdAndStatus(eventFullDto.getId(), CONFIRMED)))
                .collect(Collectors.toList());
    }

    @Override
    public List<EventShortDto> getAllPrivate(Long userId, int from, int size) {
        Pageable pageable = PageRequest.of(from / size, size);
        List<Event> events = eventRepository.findAllByInitiatorId(userId, pageable);
        return events.stream()
                .map(EventMapper.INSTANCE::toShortDto)
                .collect(Collectors.toList());
    }

    @Override
    public EventFullDto getByIdPrivate(Long userId, Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundError("Event id=" + eventId + " not found."));
        if (!userId.equals(event.getInitiator().getId())) {
            throw new NotFoundError("Event id=" + eventId + " not found");
        }
        return EventMapper.INSTANCE.toFullDto(event);
    }

    @Override
    public List<ParticipationRequestDto> getParticipationRequests(Long userId, Long eventId) {
        userRepository.findById(userId).orElseThrow(() -> new NotFoundError("User id=" + userId + " not found."));
        eventRepository.findById(eventId).orElseThrow(() -> new NotFoundError("Event id=" + eventId + " not found."));
        return participationRequestRepository.findAllByEventId(eventId).stream()
                .map(ParticipationRequestMapper.INSTANCE::toDto)
                .collect(Collectors.toList());
    }

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
            rangeEnd = LocalDateTime.MAX;
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
                .map(EventMapper.INSTANCE::toShortDto)
                .peek(dto -> {
                    Optional<ViewStatsDto> matchingStats = viewStatsDtos.stream()
                            .filter(statsDto -> statsDto.getUri().equals("/events/" + dto.getId()))
                            .findFirst();
                    dto.setViews(matchingStats.map(ViewStatsDto::getHits).orElse(0L));
                })
                .peek(dto -> dto.setConfirmedRequests(participationRequestRepository
                        .countByEventIdAndStatus(dto.getId(), CONFIRMED)))
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

    @Override
    public EventFullDto getByIdPublic(Long eventId, HttpServletRequest request) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundError("Event id=" + eventId + " not found."));

        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new NotFoundError("Event id=" + eventId + " not found");
        }

        statsClient.hit(EndpointHitDto.builder()
                .app("ewm")
                .uri(request.getRequestURI())
                .ip(request.getRemoteAddr())
                .hitTimestamp(LocalDateTime.now())
                .build());

        List<String> eventUrls = Collections.singletonList("/events/" + event.getId());

        List<ViewStatsDto> viewStatsDtos = statsClient.getStats(
                LocalDateTime.MIN.format(DateTimeFormatter.ofPattern(DATETIME_FORMAT)),
                LocalDateTime.MAX.format(DateTimeFormatter.ofPattern(DATETIME_FORMAT)),
                eventUrls, true);

        EventFullDto dto = EventMapper.INSTANCE.toFullDto(event);
        dto.setViews(viewStatsDtos.isEmpty() ? 0L : viewStatsDtos.get(0).getHits());
        dto.setConfirmedRequests(participationRequestRepository.countByEventIdAndStatus(dto.getId(), CONFIRMED));

        return dto;
    }

    @Override
    public EventFullDto updateAdmin(Long eventId, UpdateEventAdminRequest updateEventAdminRequest) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundError("Event id=" + eventId + " not found."));

        if (updateEventAdminRequest.getEventTimestamp() != null
                && LocalDateTime.now().plusHours(2).isAfter(updateEventAdminRequest.getEventTimestamp())) {
            throw new ConflictError("The date and time for which the event is scheduled cannot be earlier than " +
                    "two hours from the current moment.");
        }

        if (updateEventAdminRequest.getStateAction() != null) {
            if (updateEventAdminRequest.getStateAction().equals(PUBLISH_EVENT)
                    && !event.getState().equals(EventState.PENDING)) {
                throw new ConflictError(
                        "The event cannot be published because it is in the wrong state: " + event.getState());
            }
            if (updateEventAdminRequest.getStateAction().equals(REJECT_EVENT) &&
                    event.getState().equals(EventState.PUBLISHED)) {
                throw new ConflictError(
                        "Cannot reject the event because it's not in the right state: " + event.getState());
            }
        }

        if (updateEventAdminRequest.getCategory() != null) {
            event.setCategory(categoryRepository.findById(updateEventAdminRequest.getCategory()).orElseThrow(
                    () -> new NotFoundError("Category id=" + updateEventAdminRequest.getCategory() + " not found.")));
        }

        if (updateEventAdminRequest.getLocation() != null) {
            event.setLocation(getLocation(updateEventAdminRequest.getLocation()));
        }

        Optional.ofNullable(updateEventAdminRequest.getTitle()).ifPresent(event::setTitle);
        Optional.ofNullable(updateEventAdminRequest.getAnnotation()).ifPresent(event::setAnnotation);
        Optional.ofNullable(updateEventAdminRequest.getDescription()).ifPresent(event::setDescription);
        Optional.ofNullable(updateEventAdminRequest.getEventTimestamp()).ifPresent(event::setEventDate);
        Optional.ofNullable(updateEventAdminRequest.getParticipantLimit()).ifPresent(event::setParticipantLimit);
        Optional.ofNullable(updateEventAdminRequest.getPaid()).ifPresent(event::setPaid);
        Optional.ofNullable(updateEventAdminRequest.getRequestModeration()).ifPresent(event::setRequestModeration);

        if (updateEventAdminRequest.getStateAction() != null) {
            switch (updateEventAdminRequest.getStateAction()) {
                case PUBLISH_EVENT:
                    event.setState(EventState.PUBLISHED);
                    event.setPublishedOn(LocalDateTime.now());
                    break;
                case REJECT_EVENT:
                    event.setState(EventState.CANCELED);
                    break;
            }
        }

        event = eventRepository.save(event);

        return EventMapper.INSTANCE.toFullDto(event);
    }

    @Override
    public EventFullDto updatePrivate(Long userId, Long eventId, UpdateEventUserRequest updateEventUserRequest) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundError("Event id=" + eventId + " not found."));
        if (!userId.equals(event.getInitiator().getId())) {
            throw new NotFoundError("Event id=" + eventId + " not found");
        }
        if (updateEventUserRequest.getEventTimestamp() != null
                && LocalDateTime.now().plusHours(2).isAfter(updateEventUserRequest.getEventTimestamp())) {
            throw new ConflictError("The date and time for which the event is scheduled cannot be earlier than " +
                    "two hours from the current moment.");
        }
        if (!(event.getState().equals(CANCELED) ||
                event.getState().equals(PENDING))) {
            throw new ConflictError("Only pending or canceled events can be changed");
        }
        if (updateEventUserRequest.getCategory() != null) {
            event.setCategory(categoryRepository.findById(updateEventUserRequest.getCategory()).orElseThrow(
                    () -> new NotFoundError("Category id=" + updateEventUserRequest.getCategory() + " not found.")));
        }
        if (updateEventUserRequest.getLocation() != null) {
            event.setLocation(getLocation(updateEventUserRequest.getLocation()));
        }

        Optional.ofNullable(updateEventUserRequest.getTitle()).ifPresent(event::setTitle);
        Optional.ofNullable(updateEventUserRequest.getAnnotation()).ifPresent(event::setAnnotation);
        Optional.ofNullable(updateEventUserRequest.getDescription()).ifPresent(event::setDescription);
        Optional.ofNullable(updateEventUserRequest.getEventTimestamp()).ifPresent(event::setEventDate);
        Optional.ofNullable(updateEventUserRequest.getParticipantLimit()).ifPresent(event::setParticipantLimit);
        Optional.ofNullable(updateEventUserRequest.getPaid()).ifPresent(event::setPaid);
        Optional.ofNullable(updateEventUserRequest.getRequestModeration()).ifPresent(event::setRequestModeration);

        if (updateEventUserRequest.getStateAction() != null) {
            switch (updateEventUserRequest.getStateAction()) {
                case SEND_TO_REVIEW:
                    event.setState(EventState.PENDING);
                    break;
                case CANCEL_REVIEW:
                    event.setState(EventState.CANCELED);
                    break;
            }
        }

        event = eventRepository.save(event);
        return EventMapper.INSTANCE.toFullDto(event);
    }

    @Override
    public EventRequestStatusUpdateResult updateParticipationRequests(
            Long userId, Long eventId,
            EventRequestStatusUpdateRequest eventRequestStatusUpdateRequest) {
        userRepository.findById(userId).orElseThrow(() -> new NotFoundError("User id=" + userId + " not found."));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundError("Event id=" + eventId + " not found."));

        long confirmLimit = event.getParticipantLimit() - participationRequestRepository
                .countByEventIdAndStatus(eventId, CONFIRMED);

        if (confirmLimit <= 0) {
            throw new ConflictError("The number of requests for participation has exceeded the limit.");
        }

        List<ParticipationRequest> requestList = participationRequestRepository
                .findAllByIdIn(eventRequestStatusUpdateRequest.getRequestIds());

        List<Long> notFoundIds = eventRequestStatusUpdateRequest.getRequestIds().stream()
                .filter(requestId -> requestList.stream().noneMatch(request -> request.getId().equals(requestId)))
                .collect(Collectors.toList());

        if (!notFoundIds.isEmpty()) {
            throw new NotFoundError("Participation request ids=" + notFoundIds + " not found");
        }

        EventRequestStatusUpdateResult result = EventRequestStatusUpdateResult.builder()
                .confirmedRequests(new ArrayList<>())
                .rejectedRequests(new ArrayList<>())
                .build();

        for (ParticipationRequest req : requestList) {
            if (!req.getEvent().getId().equals(eventId)) {
                throw new NotFoundError("Participation request id=" + req.getId() + " not found");
            }

            if (confirmLimit <= 0) {
                req.setStatus(REJECTED);
                result.getRejectedRequests().add(ParticipationRequestMapper.INSTANCE.toDto(req));
                continue;
            }

            switch (eventRequestStatusUpdateRequest.getStatus()) {
                case CONFIRMED:
                    req.setStatus(CONFIRMED);
                    result.getConfirmedRequests().add(ParticipationRequestMapper.INSTANCE.toDto(req));
                    confirmLimit--;
                    break;
                case REJECTED:
                    req.setStatus(REJECTED);
                    result.getRejectedRequests().add(ParticipationRequestMapper.INSTANCE.toDto(req));
                    break;
            }
        }

        participationRequestRepository.saveAll(requestList);

        return result;
    }

    /**
     * finds a location from the database or saves a new location and returns it
     * @param locationDto event coordinates
     * @return location
     */
    private Location getLocation(LocationDto locationDto) {
        Location location = locationRepository.findByLatAndLon(locationDto.getLat(), locationDto.getLon());
        return location != null ? location : locationRepository.save(LocationMapper.INSTANCE.fromDto(locationDto));
    }
}
