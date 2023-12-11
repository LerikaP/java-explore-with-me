package ru.practicum.comment.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.dto.NewCommentDto;
import ru.practicum.comment.service.CommentService;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping(path = "/users/{userId}/comments")
@RequiredArgsConstructor
@Validated
public class PrivateCommentController {
    private final CommentService commentService;

    @PostMapping("/{eventId}")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto createComment(@RequestBody @Valid NewCommentDto commentDto, @PathVariable long eventId,
                                    @PathVariable long userId) {
        return commentService.createComment(commentDto, eventId, userId);
    }

    @PatchMapping("/{commentId}")
    public CommentDto updateComment(@RequestBody @Valid NewCommentDto commentDto, @PathVariable long commentId,
                                    @PathVariable long userId) {
        return commentService.updateComment(commentDto, commentId, userId);
    }

    @GetMapping("/{commentId}")
    public CommentDto getCommentByUser(@PathVariable long commentId, @PathVariable long userId) {
        return commentService.getCommentByUser(commentId, userId);
    }

    @GetMapping("/events/{eventId}")
    public List<CommentDto> getCommentsByUser(@PathVariable long eventId, @PathVariable long userId) {
        return commentService.getCommentsByUser(eventId, userId);
    }

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCommentByUser(@PathVariable long commentId, @PathVariable long userId) {
        commentService.deleteCommentByUser(commentId, userId);
    }
}
