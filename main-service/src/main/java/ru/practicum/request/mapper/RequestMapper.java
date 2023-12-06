package ru.practicum.request.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.model.RequestEntity;
import ru.practicum.user.mapper.UserMapper;

@Mapper(componentModel = "spring", uses = {UserMapper.class, EventMapper.class})
public interface RequestMapper {

    @Mapping(target = "event", source = "request.event.id")
    @Mapping(target = "requester", source = "request.requester.id")
    ParticipationRequestDto toParticipationRequestDto(RequestEntity request);

}
