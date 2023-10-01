package ru.practicum.ewm.service.constant;

public enum ParticipationRequestStatus {
    PENDING,
    CONFIRMED,
    /**
     * "CANCELED" means canceled by initiator
     */
    CANCELED,
    /**
     * "REJECTED" means rejected by moderator
     */
    REJECTED
}
