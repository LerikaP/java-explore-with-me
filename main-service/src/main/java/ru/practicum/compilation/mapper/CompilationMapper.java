package ru.practicum.compilation.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.NewCompilationDto;
import ru.practicum.compilation.model.CompilationEntity;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.model.EventEntity;

import java.util.List;

@Mapper(componentModel = "spring", uses = EventMapper.class)
public interface CompilationMapper {

    @Mapping(target = "events", source = "events")
    CompilationEntity toCompilation(NewCompilationDto newCompilationDto, List<EventEntity> events);

    CompilationDto toCompilationDto(CompilationEntity compilation);
}
