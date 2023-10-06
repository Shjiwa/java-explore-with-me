package ru.practicum.ewm.service.event.model;

import lombok.Getter;
import lombok.Setter;
import ru.practicum.ewm.service.category.model.Category;
import ru.practicum.ewm.service.constant.EventState;
import ru.practicum.ewm.service.user.model.User;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "events")
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "event_id")
    private Long id;

    @Column(name = "event_annotation", nullable = false)
    private String annotation;

    @ManyToOne
    @JoinColumn(name = "event_category_id", nullable = false)
    private Category category;

    @Column(name = "event_confirmed_requests")
    private Long confirmedRequests;

    @Column(name = "event_created_on")
    private LocalDateTime createdOn;

    @Column(name = "event_description", nullable = false)
    private String description;

    @Column(name = "event_event_date", nullable = false)
    private LocalDateTime eventDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_initiator_id", nullable = false)
    private User initiator;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_location_id", nullable = false)
    private Location location;

    @Column(name = "event_paid", nullable = false)
    private Boolean paid;

    @Column(name = "event_participant_limit", nullable = false)
    private Integer participantLimit;

    @Column(name = "event_published_on")
    private LocalDateTime publishedOn;

    @Column(name = "event_request_moderation")
    private Boolean requestModeration;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_state", nullable = false)
    private EventState state;

    @Column(name = "event_title", nullable = false)
    private String title;
}
