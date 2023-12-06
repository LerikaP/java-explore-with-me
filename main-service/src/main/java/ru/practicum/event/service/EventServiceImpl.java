package ru.practicum.event.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.util.CustomPageRequest;
import ru.practicum.EndpointHitDto;
import ru.practicum.StatsClient;
import ru.practicum.ViewStatsDto;
import ru.practicum.category.model.CategoryEntity;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.event.dto.*;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.mapper.LocationMapper;
import ru.practicum.event.model.EventEntity;
import ru.practicum.event.model.EventState;
import ru.practicum.event.model.LocationEntity;
import ru.practicum.event.model.QEventEntity;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.BadRequestException;
import ru.practicum.exception.ValidationException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.dto.RequestStatusForUpdate;
import ru.practicum.request.mapper.RequestMapper;
import ru.practicum.request.model.QRequestEntity;
import ru.practicum.request.model.RequestEntity;
import ru.practicum.request.model.RequestStatus;
import ru.practicum.request.repository.RequestRepository;
import ru.practicum.user.model.UserEntity;
import ru.practicum.user.repository.UserRepository;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final RequestRepository requestRepository;
    private final CategoryRepository categoryRepository;
    private final EventMapper eventMapper;
    private final RequestMapper requestMapper;
    private final LocationMapper locationMapper;
    private final StatsClient statsClient;
    private final ObjectMapper objectMapper = new ObjectMapper();


    @Override
    public List<EventShortDto> getEventsByUser(long userId, int from, int size) {
        getUserById(userId);
        BooleanExpression selectById = QEventEntity.eventEntity.initiator.id.eq(userId);
        PageRequest pageRequest = new CustomPageRequest(from, size, Sort.by(Sort.Direction.DESC, "id"));
        return eventRepository.findAll(selectById, pageRequest)
                .getContent()
                .stream()
                .map(eventMapper::toEventShortDto)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public EventFullDto createEvent(NewEventDto newEventDto, long userId) {
        UserEntity user = getUserById(userId);
        CategoryEntity category = getCategoryById(newEventDto.getCategory());
        LocationEntity location = locationMapper.toLocation(newEventDto.getLocation());
        checkTimeForEvent(newEventDto.getEventDate());
        EventEntity event = eventMapper.toEvent(newEventDto, category, user, location);
        event.setConfirmedRequests(0L);
        event.setCreatedOn(LocalDateTime.now());
        event.setState(EventState.PENDING);
        return eventMapper.toEventFullDto(eventRepository.save(event));
    }

    @Override
    public EventFullDto getEventByUser(long eventId, long userId) {
        EventEntity event = getEventByid(eventId);
        return eventMapper.toEventFullDto(event);
    }

    @Transactional
    @Override
    public EventFullDto updateEventByUser(UpdateEventUserRequest updateEventDto, long eventId, long userId) {
        getUserById(userId);
        EventEntity event = getEventByid(eventId);
        if (event.getState().equals(EventState.PUBLISHED)) {
            throw new ValidationException("Only pending or canceled events can be updated");
        }
        if (updateEventDto.getAnnotation() != null) {
            event.setAnnotation(updateEventDto.getAnnotation());
        }
        if (updateEventDto.getCategory() != null) {
            event.setCategory(getCategoryById(updateEventDto.getCategory()));
        }
        if (updateEventDto.getDescription() != null) {
            event.setDescription(updateEventDto.getDescription());
        }
        if (updateEventDto.getEventDate() != null) {
            checkTimeForEvent(updateEventDto.getEventDate());
            event.setEventDate(updateEventDto.getEventDate());
        }
        if (updateEventDto.getLocation() != null) {
            event.setLocation(locationMapper.toLocation(updateEventDto.getLocation()));
        }
        if (updateEventDto.getPaid() != null) {
            event.setPaid(updateEventDto.getPaid());
        }
        if (updateEventDto.getParticipantLimit() != null) {
            event.setParticipantLimit(updateEventDto.getParticipantLimit());
        }
        if (updateEventDto.getRequestModeration() != null) {
            event.setRequestModeration(updateEventDto.getRequestModeration());
        }
        if (updateEventDto.getTitle() != null) {
            event.setTitle(updateEventDto.getTitle());
        }
        if (updateEventDto.getStateAction() != null &&
                updateEventDto.getStateAction().equals(StateActionUser.SEND_TO_REVIEW)) {
            event.setState(EventState.PENDING);
        }
        if (updateEventDto.getStateAction() != null &&
                updateEventDto.getStateAction().equals(StateActionUser.CANCEL_REVIEW)) {
            event.setState(EventState.CANCELED);
        }
        return eventMapper.toEventFullDto(eventRepository.save(event));
    }

    @Override
    public List<ParticipationRequestDto> getRequestsForEventByUser(long eventId, long userId) {
        getEventByid(eventId);
        getUserById(userId);
        BooleanExpression selectById = QRequestEntity.requestEntity.event.id.eq(eventId);
        return StreamSupport.stream(requestRepository.findAll(selectById).spliterator(), false)
                .map(requestMapper::toParticipationRequestDto)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public EventRequestStatusUpdateResult changeRequestStatus(EventRequestStatusUpdateRequest updateDto, long eventId,
                                                              long userId) {
        getUserById(userId);
        EventEntity event = getEventByid(eventId);
        RequestStatusForUpdate status = updateDto.getStatus();
        long confirmedRequests = event.getConfirmedRequests();
        long participantLimit = event.getParticipantLimit();

        if (event.getParticipantLimit() == 0) {
            throw new ValidationException("The event does not have a limit on participation, " +
                    "confirmation is not required");
        }
        if (!event.getRequestModeration()) {
            throw new ValidationException("The event does not have pre-moderation");
        }
        if (status.equals(RequestStatusForUpdate.CONFIRMED) && participantLimit - confirmedRequests <= 0) {
            throw new ValidationException("The limit on participation for this event has been reached");
        }

        BooleanExpression selectByIdsRequest = QRequestEntity.requestEntity.id.in(updateDto.getRequestIds());
        BooleanExpression selectByIdEvent = QRequestEntity.requestEntity.event.id.eq(eventId);
        List<RequestEntity> requests = StreamSupport
                .stream(requestRepository.findAll(selectByIdsRequest.and(selectByIdEvent)).spliterator(), false)
                .collect(Collectors.toList());

        List<RequestEntity> listOfConfirmedRequests = new ArrayList<>();
        List<RequestEntity> listOfRejectedRequests = new ArrayList<>();

        if (status.equals(RequestStatusForUpdate.CONFIRMED)) {
            for (RequestEntity request : requests) {
                if (!request.getStatus().equals(RequestStatus.PENDING)) {
                    throw new ValidationException("The status can only be changed for requests " +
                            "that are in a pending state");
                }
                if (participantLimit - confirmedRequests <= 0) {
                    request.setStatus(RequestStatus.REJECTED);
                    listOfRejectedRequests.add(request);
                } else {
                    request.setStatus(RequestStatus.CONFIRMED);
                    confirmedRequests = confirmedRequests + 1;
                    listOfConfirmedRequests.add(request);
                }
            }
        } else if (status.equals(RequestStatusForUpdate.REJECTED)) {
            for (RequestEntity request : requests) {
                if (!request.getStatus().equals(RequestStatus.PENDING)) {
                    throw new ValidationException("The status can only be changed for requests " +
                            "that are in a pending state");
                }
                request.setStatus(RequestStatus.REJECTED);
                listOfRejectedRequests.add(request);
            }
        }
        event.setConfirmedRequests(confirmedRequests);
        eventRepository.save(event);
        return new EventRequestStatusUpdateResult(
                (listOfConfirmedRequests
                        .stream()
                        .map(requestMapper::toParticipationRequestDto)
                        .collect(Collectors.toList())),
                (listOfRejectedRequests
                        .stream()
                        .map(requestMapper::toParticipationRequestDto)
                        .collect(Collectors.toList())));
    }

    @Override
    public List<EventFullDto> getEventsByAdmin(List<Long> users, List<EventState> states, List<Long> categories,
                                               LocalDateTime rangeStart, LocalDateTime rangeEnd, int from, int size) {
        if (rangeStart != null && rangeEnd != null && rangeEnd.isBefore(rangeStart)) {
            throw new BadRequestException("The end date cannot be earlier than the start date");
        }
        BooleanBuilder booleanBuilder = new BooleanBuilder();
        if (users != null && !users.isEmpty()) {
            booleanBuilder.and(QEventEntity.eventEntity.initiator.id.in(users));
        }
        if (states != null && !states.isEmpty()) {
            booleanBuilder.and(QEventEntity.eventEntity.state.in(states));
        }
        if (categories != null && !categories.isEmpty()) {
            booleanBuilder.and(QEventEntity.eventEntity.category.id.in(categories));
        }
        if (rangeStart != null && rangeEnd != null) {
            booleanBuilder.and(QEventEntity.eventEntity.eventDate.between(rangeStart, rangeEnd));
        }
        PageRequest pageRequest = new CustomPageRequest(from, size, Sort.by(Sort.Direction.DESC, "id"));

        return eventRepository.findAll(booleanBuilder, pageRequest)
                .getContent()
                .stream()
                .map(eventMapper::toEventFullDto)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public EventFullDto updateEventByAdmin(UpdateEventAdminRequest updateEventDto, long eventId) {
        EventEntity event = getEventByid(eventId);
        StateActionAdmin stateAction = updateEventDto.getStateAction();
        if (stateAction != null) {
            if (stateAction.equals(StateActionAdmin.PUBLISH_EVENT) && !event.getState().equals(EventState.PENDING)) {
                throw new ValidationException("An event can only be published if it is in a pending state");
            }
            if (stateAction.equals(StateActionAdmin.REJECT_EVENT) && event.getState().equals(EventState.PUBLISHED)) {
                throw new ValidationException("An event can only be rejected if it has not yet been published");
            }
            if (stateAction.equals(StateActionAdmin.PUBLISH_EVENT)) {
                event.setState(EventState.PUBLISHED);
                event.setPublishedOn(LocalDateTime.now());
            }
            if (stateAction.equals(StateActionAdmin.REJECT_EVENT)) {
                event.setState(EventState.REJECTED);
            }
        }
        if (event.getPublishedOn() != null && event.getEventDate().isBefore(event.getPublishedOn().minusHours(1))) {
            throw new ValidationException("The event date must be no earlier than an hour from the published date");
        }
        if (updateEventDto.getAnnotation() != null) {
            event.setAnnotation(updateEventDto.getAnnotation());
        }
        if (updateEventDto.getCategory() != null) {
            event.setCategory(getCategoryById(updateEventDto.getCategory()));
        }
        if (updateEventDto.getDescription() != null) {
            event.setDescription(updateEventDto.getDescription());
        }
        if (updateEventDto.getEventDate() != null) {
            checkTimeForEvent(updateEventDto.getEventDate());
            event.setEventDate(updateEventDto.getEventDate());
        }
        if (updateEventDto.getLocation() != null) {
            event.setLocation(locationMapper.toLocation(updateEventDto.getLocation()));
        }
        if (updateEventDto.getPaid() != null) {
            event.setPaid(updateEventDto.getPaid());
        }
        if (updateEventDto.getParticipantLimit() != null) {
            event.setParticipantLimit(updateEventDto.getParticipantLimit());
        }
        if (updateEventDto.getRequestModeration() != null) {
            event.setRequestModeration(updateEventDto.getRequestModeration());
        }
        if (updateEventDto.getTitle() != null) {
            event.setTitle(updateEventDto.getTitle());
        }
        return eventMapper.toEventFullDto(eventRepository.save(event));
    }

    @Override
    public List<EventShortDto> getEventsPublic(String text, List<Long> categories, Boolean paid,
                                               LocalDateTime rangeStart, LocalDateTime rangeEnd, Boolean onlyAvailable,
                                               String sort, int from, int size, HttpServletRequest httpServletRequest) {
        if (rangeStart != null && rangeEnd != null && rangeEnd.isBefore(rangeStart)) {
            throw new BadRequestException("The end date cannot be earlier than the start date");
        }
        BooleanBuilder booleanBuilder = new BooleanBuilder();
        if (text != null && !text.isEmpty()) {
            booleanBuilder.and(QEventEntity.eventEntity.annotation.likeIgnoreCase(text)).or(
                    QEventEntity.eventEntity.description.likeIgnoreCase(text));
        }
        if (categories != null && !categories.isEmpty()) {
            booleanBuilder.and(QEventEntity.eventEntity.category.id.in(categories));
        }
        if (paid != null) {
            booleanBuilder.and(QEventEntity.eventEntity.paid.eq(paid));
        }
        if (rangeStart != null && rangeEnd != null) {
            booleanBuilder.and(QEventEntity.eventEntity.eventDate.between(rangeStart, rangeEnd));
        } else {
            booleanBuilder.and(QEventEntity.eventEntity.eventDate.after(LocalDateTime.now()));
        }
        if (onlyAvailable != null && onlyAvailable) {
            booleanBuilder.and((QEventEntity.eventEntity.participantLimit
                    .subtract(QEventEntity.eventEntity.confirmedRequests)).loe(1));
        }
        booleanBuilder.and(QEventEntity.eventEntity.state.eq(EventState.PUBLISHED));
        PageRequest pageRequest = new CustomPageRequest(from, size, Sort.by(Sort.Direction.DESC, "id"));
        if (sort != null) {
            if (sort.equals("EVENT_DATE")) {
                pageRequest = new CustomPageRequest(from, size, Sort.by(Sort.Direction.ASC, "eventDate"));
            } else {
                pageRequest = new CustomPageRequest(from, size, Sort.by(Sort.Direction.DESC, "views"));
            }
        }
        addHit(httpServletRequest);
        List<EventEntity> events = eventRepository.findAll(booleanBuilder, pageRequest).getContent();

        Map<Long, Long> views = getViewsForEvents(events);
        for (EventEntity event : events) {
            Long viewsFromStatistic = views.getOrDefault(event.getId(), 0L);
            event.setViews(viewsFromStatistic);
        }
        return events
                .stream()
                .map(eventMapper::toEventShortDto)
                .collect(Collectors.toList());
    }

    @Override
    public EventFullDto getEventPublic(long eventId, HttpServletRequest httpServletRequest) {
        EventEntity event = getEventByid(eventId);
        if (event.getState() != EventState.PUBLISHED) {
            throw new NotFoundException(String.format("Event with id %s is not published", eventId));
        }
        addHit(httpServletRequest);
        Map<Long, Long> viewsForEvent = getViewsForEvents(List.of(event));
        Long views = viewsForEvent.getOrDefault(eventId, 0L);
        event.setViews(views);
        return eventMapper.toEventFullDto(event);
    }

    private UserEntity getUserById(long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("User with id %s was not found", userId)));
    }

    private CategoryEntity getCategoryById(long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException(String.format("Category with id %s was " +
                        "not found", categoryId)));
    }

    private EventEntity getEventByid(long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(String.format("Event with id %s was not found", eventId)));
    }

    private void checkTimeForEvent(LocalDateTime eventDate) {
        if (eventDate.isBefore(LocalDateTime.now().minusHours(2))) {
            throw new BadRequestException("The event date must be no earlier than two hours from the current moment");
        }
    }

    private Map<Long, Long> getViewsForEvents(List<EventEntity> events) {
        Map<Long, Long> views = new HashMap<>();

        List<String> uris = events.stream().map(event -> "/events/" + event.getId()).collect(Collectors.toList());

        List<LocalDateTime> startDates = events.stream()
                .map(EventEntity::getCreatedOn)
                .collect(Collectors.toList());
        LocalDateTime earliestDate = startDates.stream()
                .min(LocalDateTime::compareTo)
                .orElse(null);

        if (earliestDate != null) {
            ResponseEntity<Object> response = statsClient.getStats(earliestDate, LocalDateTime.now(),
                    uris, true);
            List<ViewStatsDto> viewStats = objectMapper.convertValue(response.getBody(), new TypeReference<>() {
            });

            for (ViewStatsDto view : viewStats) {
                String uri = view.getUri();
                String[] split = uri.split("/");
                String id = split[2];
                Long eventId = Long.parseLong(id);
                views.put(eventId, view.getHits());
            }
        }
        return views;
    }

    private void addHit(HttpServletRequest httpServletRequest) {
        EndpointHitDto endpointHitDto = new EndpointHitDto(
                "evm-main-service",
                httpServletRequest.getRequestURI(),
                httpServletRequest.getRemoteAddr(),
                LocalDateTime.now());
        statsClient.createHit(endpointHitDto);
    }

}
