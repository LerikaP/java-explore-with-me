package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.EndpointHitDto;
import ru.practicum.ViewStatsDto;
import ru.practicum.mapper.StatsMapper;
import ru.practicum.repository.StatsRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {

    private final StatsRepository statsRepository;

    @Override
    public void createHit(EndpointHitDto endpointHitDto) {
        statsRepository.save(StatsMapper.toEndpointHit(endpointHitDto));
    }

    @Override
    public List<ViewStatsDto> getStatistics(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        List<ViewStatsDto> statistics;
        if (unique) {
            statistics = statsRepository.getStatsByUrisBetweenStartAndEndUniqueIp(start, end, uris);
        } else {
            statistics = statsRepository.getStatsByUrisBetweenStartAndEnd(start, end, uris);
        }
        return statistics;
    }
}
