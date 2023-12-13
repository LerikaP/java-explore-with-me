package ru.practicum.comment.service;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.dto.NewCommentDto;
import ru.practicum.comment.mapper.CommentMapper;
import ru.practicum.comment.model.CommentEntity;
import ru.practicum.comment.model.QCommentEntity;
import ru.practicum.comment.repository.CommentRepository;
import ru.practicum.event.model.EventEntity;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.BadRequestException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;
import ru.practicum.user.model.UserEntity;
import ru.practicum.user.repository.UserRepository;
import ru.practicum.util.CustomPageRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CommentMapper commentMapper;

    @Transactional
    @Override
    public CommentDto createComment(NewCommentDto commentDto, long eventId, long userId) {
        EventEntity event = getEventById(eventId);
        UserEntity user = getUserById(userId);
        CommentEntity comment = commentMapper.toComment(commentDto, user, event);
        comment.setEdited(false);
        return commentMapper.toCommentDto(commentRepository.save(comment));
    }

    @Transactional
    @Override
    public CommentDto updateComment(NewCommentDto commentDto, long commentId, long userId) {
        getUserById(userId);
        CommentEntity comment = getCommentById(commentId);
        checkCommentAuthor(comment, userId);
        comment.setText(commentDto.getText());
        comment.setEdited(true);
        return commentMapper.toCommentDto(commentRepository.save(comment));
    }

    @Override
    public CommentDto getCommentByUser(long commentId, long userId) {
        CommentEntity comment = getCommentById(commentId);
        getUserById(userId);
        checkCommentAuthor(comment, userId);
        return commentMapper.toCommentDto(comment);
    }

    @Override
    public List<CommentDto> getCommentsByUser(long eventId, long userId) {
        getEventById(eventId);
        getUserById(userId);
        BooleanExpression selectByIdEvent = QCommentEntity.commentEntity.event.id.eq(eventId);
        BooleanExpression selectByIdUser = QCommentEntity.commentEntity.author.id.eq(userId);
        List<CommentEntity> comments = StreamSupport
                .stream(commentRepository.findAll(selectByIdEvent.and(selectByIdUser)).spliterator(), false)
                .collect(Collectors.toList());
        return comments.stream().map(commentMapper::toCommentDto).collect(Collectors.toList());
    }

    @Transactional
    @Override
    public void deleteCommentByUser(long commentId, long userId) {
        getUserById(userId);
        CommentEntity comment = getCommentById(commentId);
        checkCommentAuthor(comment, userId);
        commentRepository.deleteById(commentId);
    }

    @Override
    public List<CommentDto> getCommentsByAdmin(List<Long> users, List<Long> events,
                                               LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                               int from, int size) {
        if (rangeStart != null && rangeEnd != null && rangeEnd.isBefore(rangeStart)) {
            throw new BadRequestException("The end date cannot be earlier than the start date");
        }
        BooleanBuilder booleanBuilder = new BooleanBuilder();
        if (users != null && !users.isEmpty()) {
            booleanBuilder.and(QCommentEntity.commentEntity.author.id.in(users));
        }
        if (events != null && !events.isEmpty()) {
            booleanBuilder.and(QCommentEntity.commentEntity.event.id.in(events));
        }
        if (rangeStart != null && rangeEnd != null) {
            booleanBuilder.and(QCommentEntity.commentEntity.created.between(rangeStart, rangeEnd));
        }
        PageRequest pageRequest = new CustomPageRequest(from, size, Sort.by(Sort.Direction.ASC, "id"));
        return commentRepository.findAll(booleanBuilder, pageRequest)
                .getContent()
                .stream()
                .map(commentMapper::toCommentDto)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public void deleteCommentByAdmin(long commentId) {
        getCommentById(commentId);
        commentRepository.deleteById(commentId);
    }

    @Override
    public List<CommentDto> getEventCommentsPublic(long eventId, int from, int size) {
        getEventById(eventId);
        BooleanExpression selectByIdEvent = QCommentEntity.commentEntity.event.id.eq(eventId);
        PageRequest pageRequest = new CustomPageRequest(from, size, Sort.by(Sort.Direction.ASC, "id"));
        return commentRepository.findAll(selectByIdEvent, pageRequest)
                .getContent()
                .stream()
                .map(commentMapper::toCommentDto)
                .collect(Collectors.toList());
    }

    private EventEntity getEventById(long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(String.format("Event with id %s was not found", eventId)));
    }

    private UserEntity getUserById(long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("User with id %s was not found", userId)));
    }

    private CommentEntity getCommentById(long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException(String.format("Comment with id %s was not found", commentId)));
    }

    private void checkCommentAuthor(CommentEntity comment, long userId) {
        if (comment.getAuthor().getId() != userId) {
            throw new ValidationException("Only the author can edit his comment");
        }
    }


}
