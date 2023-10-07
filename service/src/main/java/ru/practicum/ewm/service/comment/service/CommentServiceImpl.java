package ru.practicum.ewm.service.comment.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.service.comment.dto.CommentDto;
import ru.practicum.ewm.service.comment.dto.NewCommentDto;
import ru.practicum.ewm.service.comment.model.Comment;
import ru.practicum.ewm.service.comment.repository.CommentRepository;
import ru.practicum.ewm.service.error.ForbiddenError;
import ru.practicum.ewm.service.error.NotFoundError;
import ru.practicum.ewm.service.event.model.Event;
import ru.practicum.ewm.service.event.repository.EventRepository;
import ru.practicum.ewm.service.user.model.User;
import ru.practicum.ewm.service.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static ru.practicum.ewm.service.comment.mapper.CommentMapper.COMMENT_MAPPER;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    @Transactional
    @Override
    public CommentDto create(Long userId, Long eventId, NewCommentDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundError("User id=" + userId + " not found."));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundError("Event id=" + eventId + " not found."));
        Comment comment = new Comment();
        comment.setAuthor(user);
        comment.setEvent(event);
        comment.setText(dto.getText());
        comment.setCreated(LocalDateTime.now());

        return COMMENT_MAPPER.toDto(commentRepository.save(comment));
    }

    @Transactional(readOnly = true)
    @Override
    public List<CommentDto> getAllByEventId(Long eventId) {
        return commentRepository.findAllByEventId(eventId).stream()
                .map(COMMENT_MAPPER::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public CommentDto update(Long userId, Long commentId, NewCommentDto dto) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundError("Comment id=" + commentId + " not found."));
        if (!comment.getAuthor().getId().equals(userId)) {
            throw new ForbiddenError("User id=" + userId + " is not author of comment id=" + commentId);
        }
        if (comment.getCreated().plusHours(24).isBefore(LocalDateTime.now())) {
            throw new ForbiddenError("You only have 24 hours to update your comment.");
        }
        Optional.ofNullable(dto.getText()).ifPresent(comment::setText);

        return COMMENT_MAPPER.toDto(commentRepository.save(comment));
    }

    @Transactional
    @Override
    public void deletePrivate(Long userId, Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                        .orElseThrow(() -> new NotFoundError("Comment id=" + commentId + " not found."));
        if (!comment.getAuthor().getId().equals(userId)) {
            throw new ForbiddenError("User id=" + userId + " is not author of comment id=" + commentId);
        }
        commentRepository.deleteById(commentId);
    }

    @Transactional
    @Override
    public void deleteAdmin(Long commentId) {
        if (!commentRepository.existsById(commentId)) {
            throw new NotFoundError("Comment id=" + commentId + " not found.");
        }
        commentRepository.deleteById(commentId);
    }
}
