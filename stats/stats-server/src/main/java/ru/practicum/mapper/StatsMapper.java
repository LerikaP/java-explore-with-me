package ru.practicum.mapper;

import ru.practicum.EndpointHitDto;
import ru.practicum.ViewStatsDto;
import ru.practicum.model.EndpointHitEntity;
import ru.practicum.model.ViewStatsEntity;

public class StatsMapper {

    public static EndpointHitEntity toEndpointHit(EndpointHitDto endpointHitDto) {
        EndpointHitEntity endpointHit = new EndpointHitEntity();
        endpointHit.setApp(endpointHitDto.getApp());
        endpointHit.setUri(endpointHitDto.getUri());
        endpointHit.setIp(endpointHitDto.getIp());
        endpointHit.setTimestamp(endpointHitDto.getTimestamp());
        return endpointHit;
    }

    public static ViewStatsDto toViewStatsDto(ViewStatsEntity viewStatsEntity) {
        ViewStatsDto viewStatsDto = new ViewStatsDto();
        viewStatsDto.setApp(viewStatsEntity.getApp());
        viewStatsDto.setUri(viewStatsEntity.getUri());
        viewStatsDto.setHits(viewStatsEntity.getHits());
        return viewStatsDto;
    }
}
