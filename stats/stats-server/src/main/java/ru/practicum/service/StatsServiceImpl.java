package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.EndpointHitDto;
import ru.practicum.ViewStatsDto;
import ru.practicum.mapper.StatsMapper;
import ru.practicum.model.ViewStatsEntity;
import ru.practicum.repository.StatsRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {

    private final StatsRepository statsRepository;

    @Transactional
    @Override
    public void createHit(EndpointHitDto endpointHitDto) {
        statsRepository.save(StatsMapper.toEndpointHit(endpointHitDto));
    }

    @Override
    public List<ViewStatsDto> getStatistics(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        List<ViewStatsEntity> statistics;
        if (unique) {
            statistics = statsRepository.getStatsByUrisBetweenStartAndEndUniqueIp(start, end, uris);
        } else {
            statistics = statsRepository.getStatsByUrisBetweenStartAndEnd(start, end, uris);
        }
        return statistics.stream()
                .map(StatsMapper::toViewStatsDto)
                .collect(Collectors.toList());
    }
}
