package ru.practicum.ewm.service.user.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import ru.practicum.ewm.service.user.dto.NewUserRequest;
import ru.practicum.ewm.service.user.dto.UserDto;
import ru.practicum.ewm.service.user.model.User;

@Mapper
public interface UserMapper {
    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    UserDto toDto(User user);

    User fromNewRequest(NewUserRequest request);
}
