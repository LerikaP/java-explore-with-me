package ru.practicum.participationRequest.service;

import ru.practicum.participationRequest.dto.ParticipationRequestDto;

import java.util.List;

public interface ParticipationRequestService {

    ParticipationRequestDto createRequest(long userId, long eventId);

    List<ParticipationRequestDto> getRequests(long userId);

    ParticipationRequestDto cancelRequest(long userId, long requestId);
}
