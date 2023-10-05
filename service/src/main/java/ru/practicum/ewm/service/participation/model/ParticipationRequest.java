package ru.practicum.ewm.service.participation.model;

import lombok.Getter;
import lombok.Setter;
import ru.practicum.ewm.service.constant.ParticipationRequestStatus;
import ru.practicum.ewm.service.event.model.Event;
import ru.practicum.ewm.service.user.model.User;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "participation_requests")
public class ParticipationRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "participation_request_id")
    private Long id;

    @Column(name = "participation_request_created")
    private LocalDateTime created;

    @ManyToOne
    @JoinColumn(name = "participation_request_event_id", nullable = false)
    private Event event;

    @ManyToOne
    @JoinColumn(name = "participation_request_requester_id", nullable = false)
    private User requester;

    @Enumerated(EnumType.STRING)
    @Column(name = "participation_request_status")
    private ParticipationRequestStatus status;
}
