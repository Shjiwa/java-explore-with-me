package ru.practicum.ewm.service.participation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.ewm.service.constant.ParticipationRequestStatus;
import ru.practicum.ewm.service.participation.model.ParticipationRequest;

import java.util.List;

@Repository
public interface ParticipationRequestRepository extends JpaRepository<ParticipationRequest, Long> {
    Long countByEventIdAndStatus(Long eventId, ParticipationRequestStatus status);

    List<ParticipationRequest> findAllByRequesterId(Long id);

    List<ParticipationRequest> findAllByEventId(Long id);

    List<ParticipationRequest> findAllByIdIn(List<Long> ids);
}
