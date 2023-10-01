package ru.practicum.ewm.service.participation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.ewm.service.constant.ParticipationRequestStatus;

import java.time.LocalDateTime;

import static ru.practicum.ewm.service.constant.DateTimeFormat.DATETIME_FORMAT;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParticipationRequestDto {
    private Long id;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATETIME_FORMAT)
    private LocalDateTime created;

    private Long event;

    private Long requester;

    private ParticipationRequestStatus status;
}
