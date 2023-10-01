package ru.practicum.ewm.service.event.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.ewm.service.constant.EventRequestStatus;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventRequestStatusUpdateRequest {
    @NotNull(message = "Error! The ids of requests can't be empty.")
    private List<Long> requestIds;

    @NotNull(message = "Error! The status of the update request can't be empty.")
    private EventRequestStatus status;
}
