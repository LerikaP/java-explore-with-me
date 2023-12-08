package ru.practicum.participationRequest.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.participationRequest.dto.ParticipationRequestDto;
import ru.practicum.participationRequest.service.ParticipationRequestService;

import java.util.List;

@RestController
@RequestMapping(path = "/users/{userId}/requests")
@RequiredArgsConstructor
public class PrivateParticipationRequestController {
    private final ParticipationRequestService requestService;

    @GetMapping
    public List<ParticipationRequestDto> getRequests(@PathVariable long userId) {
        return requestService.getRequests(userId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ParticipationRequestDto createRequest(@RequestParam long eventId, @PathVariable long userId) {
        return requestService.createRequest(userId, eventId);
    }

    @PatchMapping("/{requestId}/cancel")
    public ParticipationRequestDto cancelRequest(@PathVariable long userId, @PathVariable long requestId) {
        return requestService.cancelRequest(userId, requestId);
    }
}
