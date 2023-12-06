package ru.practicum.event.mapper;

import org.mapstruct.Mapper;
import ru.practicum.event.dto.LocationDto;
import ru.practicum.event.model.LocationEntity;

@Mapper(componentModel = "spring")
public interface LocationMapper {

    LocationEntity toLocation(LocationDto locationDto);
}
