package ru.practicum.ewm.service.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.service.error.NotFoundError;
import ru.practicum.ewm.service.user.dto.NewUserRequest;
import ru.practicum.ewm.service.user.dto.UserDto;
import ru.practicum.ewm.service.user.model.User;
import ru.practicum.ewm.service.user.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.ewm.service.user.mapper.UserMapper.USER_MAPPER;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Transactional
    @Override
    public UserDto create(NewUserRequest request) {
        return USER_MAPPER.toDto(userRepository.save(USER_MAPPER.fromNewRequest(request)));
    }

    @Transactional(readOnly = true)
    @Override
    public List<UserDto> getAll(List<Long> ids, int from, int size) {
        Pageable pageable = PageRequest.of(from, size, Sort.by(Sort.Direction.ASC, "id"));
        Page<User> users;
        if (ids != null && !ids.isEmpty()) {
            users = userRepository.findAllByIdIn(ids, pageable);
        } else {
            users = userRepository.findAll(pageable);
        }
        return users.getContent().stream()
                .map(USER_MAPPER::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public void delete(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundError("User id=" + userId + "not found.");
        }
        userRepository.deleteById(userId);
    }
}
