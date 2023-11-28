package ru.practicum.mapper;

import ru.practicum.EndpointHitDto;
import ru.practicum.model.EndpointHit;

public class StatsMapper {

    public static EndpointHit toEndpointHit(EndpointHitDto endpointHitDto) {
        EndpointHit endpointHit = new EndpointHit();
        endpointHit.setApp(endpointHitDto.getApp());
        endpointHit.setUri(endpointHitDto.getUri());
        endpointHit.setIp(endpointHitDto.getIp());
        endpointHit.setTimestamp(endpointHitDto.getTimestamp());
        return endpointHit;
    }
}
