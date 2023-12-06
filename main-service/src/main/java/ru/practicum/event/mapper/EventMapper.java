package ru.practicum.event.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.category.mapper.CategoryMapper;
import ru.practicum.category.model.CategoryEntity;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.dto.NewEventDto;
import ru.practicum.event.model.EventEntity;
import ru.practicum.event.model.LocationEntity;
import ru.practicum.user.mapper.UserMapper;
import ru.practicum.user.model.UserEntity;

@Mapper(componentModel = "spring", uses = {CategoryMapper.class, UserMapper.class})
public interface EventMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(source = "category", target = "category")
    @Mapping(source = "location", target = "location")
    EventEntity toEvent(NewEventDto newEventDto, CategoryEntity category, UserEntity initiator,
                        LocationEntity location);

    EventFullDto toEventFullDto(EventEntity eventEntity);

    EventShortDto toEventShortDto(EventEntity eventEntity);
}
