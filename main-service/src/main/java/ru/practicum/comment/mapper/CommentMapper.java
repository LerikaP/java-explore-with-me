package ru.practicum.comment.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.dto.NewCommentDto;
import ru.practicum.comment.model.CommentEntity;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.model.EventEntity;
import ru.practicum.user.mapper.UserMapper;
import ru.practicum.user.model.UserEntity;

@Mapper(componentModel = "spring", uses = {EventMapper.class, UserMapper.class})
public interface CommentMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(source = "user", target = "author")
    @Mapping(source = "event", target = "event")
    CommentEntity toComment(NewCommentDto commentDto, UserEntity user, EventEntity event);

    CommentDto toCommentDto(CommentEntity comment);

}
