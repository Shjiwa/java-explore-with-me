package ru.practicum.ewm.service.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.ewm.service.constant.StateActionAdmin;

import javax.validation.constraints.Future;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

import static ru.practicum.ewm.service.constant.DateTimeFormat.DATETIME_FORMAT;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateEventAdminRequest {
    @Size(min = 20, max = 2000, message = "Error! Annotation length must be between 20 and 2000 chars.")
    private String annotation;

    private Long category;

    @Size(min = 20, max = 7000, message = "Error! Description length must be between 20 and 7000 chars.")
    private String description;

    @Future(message = "Error! Event date must be in future.")
    @JsonProperty("eventDate")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATETIME_FORMAT)
    private LocalDateTime eventTimestamp;

    private LocationDto location;

    private Boolean paid;

    private Integer participantLimit;

    private Boolean requestModeration;

    private StateActionAdmin stateAction;

    @Size(min = 3, max = 120, message = "Error! Title length must be between 3 and 120 chars.")
    private String title;
}
