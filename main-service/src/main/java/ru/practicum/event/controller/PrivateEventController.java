package ru.practicum.event.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.event.dto.*;
import ru.practicum.event.service.EventService;
import ru.practicum.request.dto.ParticipationRequestDto;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping(path = "/users/{userId}/events")
@RequiredArgsConstructor
@Validated
public class PrivateEventController {
    private final EventService eventService;

    @GetMapping
    public List<EventShortDto> getEventsByUser(@PathVariable long userId, @RequestParam(defaultValue = "0") int from,
                                               @RequestParam(defaultValue = "10") int size) {
        return eventService.getEventsByUser(userId, from, size);
    }

    @GetMapping("/{eventId}")
    public EventFullDto getEvent(@PathVariable long eventId, @PathVariable long userId) {
        return eventService.getEventByUser(eventId, userId);
    }

    @GetMapping("/{eventId}/requests")
    public List<ParticipationRequestDto> getRequestsForEventByUser(@PathVariable long eventId,
                                                                   @PathVariable long userId) {
        return eventService.getRequestsForEventByUser(eventId, userId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto createEvent(@RequestBody @Valid NewEventDto newEventDto, @PathVariable long userId) {
        return eventService.createEvent(newEventDto, userId);
    }

    @PatchMapping("/{eventId}")
    public EventFullDto updateEventByUser(@RequestBody @Valid UpdateEventUserRequest updateEventDto,
                                          @PathVariable long eventId, @PathVariable long userId) {
        return eventService.updateEventByUser(updateEventDto, eventId, userId);
    }

    @PatchMapping("/{eventId}/requests")
    public EventRequestStatusUpdateResult changeRequestStatus(@RequestBody
                                                                  @Valid EventRequestStatusUpdateRequest eventDto,
                                                              @PathVariable long eventId, @PathVariable long userId) {
        return eventService.changeRequestStatus(eventDto, eventId, userId);
    }

}
