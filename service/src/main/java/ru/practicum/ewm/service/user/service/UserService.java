package ru.practicum.ewm.service.user.service;

import ru.practicum.ewm.service.user.dto.NewUserRequest;
import ru.practicum.ewm.service.user.dto.UserDto;

import java.util.List;

public interface UserService {
    UserDto create(NewUserRequest request);

    List<UserDto> getAll(List<Long> ids, int from, int size);

    void delete(Long userId);
}
