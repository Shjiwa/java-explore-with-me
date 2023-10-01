package ru.practicum.ewm.service.user.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserShortDto {
    private Long id;

    private String name;
}
