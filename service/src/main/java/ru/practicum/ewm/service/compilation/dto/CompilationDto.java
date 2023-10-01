package ru.practicum.ewm.service.compilation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.ewm.service.event.dto.EventShortDto;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompilationDto {
    private Long id;

    private Set<EventShortDto> events;

    private Boolean pinned;

    private String title;
}
