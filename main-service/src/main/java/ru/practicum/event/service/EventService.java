package ru.practicum.event.service;

import ru.practicum.event.dto.*;
import ru.practicum.event.model.EventState;
import ru.practicum.request.dto.ParticipationRequestDto;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;

public interface EventService {

    List<EventShortDto> getEventsByUser(long userId, int from, int size);

    EventFullDto createEvent(NewEventDto newEventDto, long userId);

    EventFullDto getEventByUser(long eventId, long userId);

    EventFullDto updateEventByUser(UpdateEventUserRequest updateEventDto, long eventId, long userId);

    List<ParticipationRequestDto> getRequestsForEventByUser(long eventId, long userId);

    EventRequestStatusUpdateResult changeRequestStatus(EventRequestStatusUpdateRequest updateDto,
                                                                long eventId, long userId);

    List<EventFullDto> getEventsByAdmin(List<Long> users, List<EventState> states, List<Long> categories,
                                        LocalDateTime rangeStart, LocalDateTime rangeEnd, int from, int size);

    EventFullDto updateEventByAdmin(UpdateEventAdminRequest updateEventDto, long eventId);

    List<EventShortDto> getEventsPublic(String text, List<Long> categories, Boolean paid, LocalDateTime rangeStart,
                                        LocalDateTime rangeEnd, Boolean onlyAvailable, String sort, int from, int size,
                                        HttpServletRequest httpServletRequest);

    EventFullDto getEventPublic(long eventId, HttpServletRequest httpServletRequest);
}
