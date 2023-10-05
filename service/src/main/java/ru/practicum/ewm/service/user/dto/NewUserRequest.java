package ru.practicum.ewm.service.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewUserRequest {
    @NotBlank(message = "Error! Email can't be blank.")
    @Size(max = 254, min = 6, message = "Error! Email length must be between 6 and 254 characters.")
    @Email(message = "Error! Wrong email.")
    private String email;

    @NotBlank(message = "Error! Name can't be blank.")
    @Size(max = 250, min = 2, message = "Error! Name length must be between 2 and 250 characters.")
    private String name;
}
