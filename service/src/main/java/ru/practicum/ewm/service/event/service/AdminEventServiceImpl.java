package ru.practicum.ewm.service.event.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.service.category.repository.CategoryRepository;
import ru.practicum.ewm.service.constant.EventState;
import ru.practicum.ewm.service.error.ConflictError;
import ru.practicum.ewm.service.error.NotFoundError;
import ru.practicum.ewm.service.event.dto.EventFullDto;
import ru.practicum.ewm.service.event.dto.LocationDto;
import ru.practicum.ewm.service.event.dto.UpdateEventAdminRequest;
import ru.practicum.ewm.service.event.model.Event;
import ru.practicum.ewm.service.event.model.Location;
import ru.practicum.ewm.service.event.repository.EventRepository;
import ru.practicum.ewm.service.event.repository.LocationRepository;
import ru.practicum.ewm.service.participation.repository.ParticipationRequestRepository;
import ru.practicum.ewm.stats.client.StatsClient;
import ru.practicum.ewm.stats.dto.ViewStatsDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static ru.practicum.ewm.service.constant.DateTimeFormat.DATETIME_FORMAT;
import static ru.practicum.ewm.service.constant.ParticipationRequestStatus.CONFIRMED;
import static ru.practicum.ewm.service.constant.StateActionAdmin.PUBLISH_EVENT;
import static ru.practicum.ewm.service.constant.StateActionAdmin.REJECT_EVENT;
import static ru.practicum.ewm.service.event.mapper.EventMapper.EVENT_MAPPER;
import static ru.practicum.ewm.service.event.mapper.LocationMapper.LOCATION_MAPPER;

@Service
@RequiredArgsConstructor
public class AdminEventServiceImpl implements AdminEventService {
    private final CategoryRepository categoryRepository;
    private final LocationRepository locationRepository;
    private final EventRepository eventRepository;
    private final ParticipationRequestRepository participationRequestRepository;
    private final StatsClient statsClient;

    @Transactional(readOnly = true)
    @Override
    public List<EventFullDto> getAllAdmin(List<Long> users, List<EventState> states, List<Long> categories,
                                          LocalDateTime rangeStart, LocalDateTime rangeEnd, int from, int size) {
        Pageable pageable = PageRequest.of(from, size);

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
            rangeEnd = LocalDateTime.now().plusYears(100);
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
                .map(EVENT_MAPPER::toFullDto)
                .peek(eventFullDto -> {
                    Optional<ViewStatsDto> viewStatsDto = viewStatsDtos.stream()
                            .filter(viewStatsDto1 -> viewStatsDto1.getUri().equals("/events/" + eventFullDto.getId()))
                            .findFirst();
                    eventFullDto.setViews(viewStatsDto.map(ViewStatsDto::getHits).orElse(0L));
                }).peek(eventFullDto -> eventFullDto.setConfirmedRequests(
                        participationRequestRepository.countByEventIdAndStatus(eventFullDto.getId(), CONFIRMED)))
                .collect(Collectors.toList());
    }

    @Transactional
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

        return EVENT_MAPPER.toFullDto(event);
    }

    /**
     * finds a location from the database or saves a new location and returns it
     * @param locationDto event coordinates
     * @return location
     */
    private Location getLocation(LocationDto locationDto) {
        Location location = locationRepository.findByLatAndLon(locationDto.getLat(), locationDto.getLon());
        return location != null ? location : locationRepository.save(LOCATION_MAPPER.fromDto(locationDto));
    }
}
