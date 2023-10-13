package ru.practicum.ewm.service.comment.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.service.comment.dto.CommentDto;
import ru.practicum.ewm.service.comment.dto.NewCommentDto;
import ru.practicum.ewm.service.comment.service.CommentService;

import javax.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/users/{userId}/comments")
@RequiredArgsConstructor
public class PrivateCommentController {
    private final CommentService commentService;

    @PostMapping("/{eventId}")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto create(@PathVariable Long userId, @PathVariable Long eventId,
                             @Valid @RequestBody NewCommentDto dto) {
        log.info("Поступил запрос на создание комментария от пользователя с id={}, к ивенту с id={}, комментарий={}",
                userId, eventId, dto.getText());
        return commentService.create(userId, eventId, dto);
    }

    @PatchMapping("/{commentId}")
    public CommentDto patch(@PathVariable Long userId,
                            @PathVariable Long commentId,
                            @Valid @RequestBody NewCommentDto dto) {
        log.info("Поступил запрос на изменение комментария от пользователя с id={}, новый текст комментария={}",
                userId, dto.getText());
        return commentService.update(userId, commentId, dto);
    }

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long userId, @PathVariable Long commentId) {
        log.info("Поступил запрос на удаление комментария от пользователя с id={}, comment id={}", userId, commentId);
        commentService.deletePrivate(userId, commentId);
    }
}
