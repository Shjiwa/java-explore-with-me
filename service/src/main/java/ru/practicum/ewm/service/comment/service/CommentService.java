package ru.practicum.ewm.service.comment.service;

import ru.practicum.ewm.service.comment.dto.CommentDto;
import ru.practicum.ewm.service.comment.dto.NewCommentDto;

import java.util.List;

public interface CommentService {
    CommentDto create(Long userId, Long eventId, NewCommentDto dto);

    List<CommentDto> getAllByEventId(Long eventId);

    CommentDto update(Long userId, Long commentId, NewCommentDto dto);

    void deletePrivate(Long userId, Long commentId);

    void deleteAdmin(Long commentId);
}
