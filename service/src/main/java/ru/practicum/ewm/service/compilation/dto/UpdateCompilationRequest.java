package ru.practicum.ewm.service.compilation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Size;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCompilationRequest {
    private Set<Long> events;

    private Boolean pinned;

    @Size(max = 50, min = 1, message = "Error! The compilation title must be between 1 and 50 characters.")
    private String title;
}
