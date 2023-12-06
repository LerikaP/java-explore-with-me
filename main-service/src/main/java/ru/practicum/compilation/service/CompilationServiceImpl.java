package ru.practicum.compilation.service;

import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.util.CustomPageRequest;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.NewCompilationDto;
import ru.practicum.compilation.dto.UpdateCompilationRequest;
import ru.practicum.compilation.mapper.CompilationMapper;
import ru.practicum.compilation.model.CompilationEntity;
import ru.practicum.compilation.model.QCompilationEntity;
import ru.practicum.compilation.repository.CompilationRepository;
import ru.practicum.event.model.EventEntity;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.NotFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CompilationServiceImpl implements CompilationService {
    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;
    private final CompilationMapper compilationMapper;

    @Override
    public List<CompilationDto> getCompilations(Boolean pinned, int from, int size) {
        PageRequest pageRequest = new CustomPageRequest(from, size, Sort.by(Sort.Direction.DESC, "id"));
        BooleanExpression selectByPinned = QCompilationEntity.compilationEntity.pinned.eq(pinned);
        if (pinned != null) {
            return compilationRepository.findAll(selectByPinned, pageRequest)
                    .getContent()
                    .stream()
                    .map(compilationMapper::toCompilationDto)
                    .collect(Collectors.toList());
        }
        return compilationRepository.findAll(pageRequest)
                .stream()
                .map(compilationMapper::toCompilationDto)
                .collect(Collectors.toList());
    }

    @Override
    public CompilationDto getCompilation(long compId) {
        return compilationMapper.toCompilationDto(getCompilationById(compId));
    }

    @Transactional
    @Override
    public CompilationDto createCompilation(NewCompilationDto newCompilationDto) {
        List<EventEntity> events;
        if (newCompilationDto.getEvents() != null && !newCompilationDto.getEvents().isEmpty()) {
            events = eventRepository.findAllById(newCompilationDto.getEvents());
        } else {
            events = new ArrayList<>();
        }
        CompilationEntity compilation = compilationMapper.toCompilation(newCompilationDto, events);
        return compilationMapper.toCompilationDto(compilationRepository.save(compilation));
    }

    @Transactional
    @Override
    public CompilationDto updateCompilation(UpdateCompilationRequest updateCompilation, long compId) {
        CompilationEntity compilation = getCompilationById(compId);
        if (updateCompilation.getPinned() != null) {
            compilation.setPinned(updateCompilation.getPinned());
        }
        if (updateCompilation.getTitle() != null && !updateCompilation.getTitle().isBlank()) {
            compilation.setTitle(updateCompilation.getTitle());
        }
        if (updateCompilation.getEvents() != null && !updateCompilation.getEvents().isEmpty()) {
            List<EventEntity> events = eventRepository.findAllById(updateCompilation.getEvents());
            compilation.setEvents(events);
        }
        return compilationMapper.toCompilationDto(compilationRepository.save(compilation));
    }

    @Transactional
    @Override
    public void deleteCompilation(long compId) {
        getCompilationById(compId);
        compilationRepository.deleteById(compId);
    }

    private CompilationEntity getCompilationById(long compId) {
        return compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Compilation with id %s was not found", compId)));
    }
}
