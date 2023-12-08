package ru.practicum.participationRequest.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.participationRequest.dto.ParticipationRequestDto;
import ru.practicum.participationRequest.model.ParticipationRequestEntity;
import ru.practicum.user.mapper.UserMapper;

@Mapper(componentModel = "spring", uses = {UserMapper.class, EventMapper.class})
public interface ParticipationRequestMapper {

    @Mapping(target = "event", source = "request.event.id")
    @Mapping(target = "requester", source = "request.requester.id")
    ParticipationRequestDto toParticipationRequestDto(ParticipationRequestEntity request);

}
