package ru.practicum.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import ru.practicum.EndpointHitDto;
import ru.practicum.ViewStatsDto;
import ru.practicum.service.StatsService;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class StatsController {
    private final StatsService statsService;
    private static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    @PostMapping("/hit")
    public void addHit(@Valid @RequestBody EndpointHitDto endpointHitDto) {
        statsService.createHit(endpointHitDto);
    }

    @GetMapping("/stats")
    public List<ViewStatsDto> getStatistics(@RequestParam @DateTimeFormat(pattern = DATE_TIME_PATTERN)
                                                LocalDateTime start,
                                            @RequestParam @DateTimeFormat(pattern = DATE_TIME_PATTERN)
                                            LocalDateTime end,
                                            @RequestParam(required = false) List<String> uris,
                                            @RequestParam(required = false, defaultValue = "false") Boolean unique) {
        return statsService.getStatistics(start, end, uris, unique);
    }
}
