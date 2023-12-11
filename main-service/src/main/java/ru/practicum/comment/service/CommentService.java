package ru.practicum.comment.service;

import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.dto.NewCommentDto;

import java.time.LocalDateTime;
import java.util.List;

public interface CommentService {

    CommentDto createComment(NewCommentDto commentDto, long eventId, long userId);

    CommentDto updateComment(NewCommentDto commentDto, long commentId, long userId);

    CommentDto getCommentByUser(long commentId, long userId);

    List<CommentDto> getCommentsByUser(long eventId, long userId);

    void deleteCommentByUser(long commentId, long userId);

    List<CommentDto> getCommentsByAdmin(List<Long> users,
                                        List<Long> events,
                                        LocalDateTime rangeStart,
                                        LocalDateTime rangeEnd,
                                        int from, int size);

    void deleteCommentByAdmin(long commentId);

    List<CommentDto> getEventCommentsPublic(long eventId, int from, int size);


}
