package ru.practicum.ewm.service.category.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDto {
    private Long id;

    @NotBlank(message = "Error! Category name can't be blank.")
    @Size(max = 50, min = 1, message = "Error! The category name must be between 1 and 50 characters.")
    private String name;
}
