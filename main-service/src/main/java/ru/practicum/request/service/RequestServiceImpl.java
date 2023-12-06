package ru.practicum.request.service;

import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.event.model.EventEntity;
import ru.practicum.event.model.EventState;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.ValidationException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.mapper.RequestMapper;
import ru.practicum.request.model.QRequestEntity;
import ru.practicum.request.model.RequestEntity;
import ru.practicum.request.model.RequestStatus;
import ru.practicum.request.repository.RequestRepository;
import ru.practicum.user.model.UserEntity;
import ru.practicum.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
public class RequestServiceImpl implements RequestService {
    private final RequestRepository requestRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final RequestMapper requestMapper;

    @Override
    public List<ParticipationRequestDto> getRequests(long userId) {
        UserEntity user = getUserById(userId);
        BooleanExpression selectById = QRequestEntity.requestEntity.requester.eq(user);
        return StreamSupport.stream(requestRepository.findAll(selectById).spliterator(), false)
                .map(requestMapper::toParticipationRequestDto)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public ParticipationRequestDto createRequest(long userId, long eventId) {
        UserEntity user = getUserById(userId);
        EventEntity event = getEventById(eventId);
        if (event.getInitiator().getId() == userId) {
            throw new ValidationException("Event initiator cannot add a request to participate in his event");
        }
        if (event.getState() != EventState.PUBLISHED) {
            throw new ValidationException("You cannot participate in an unpublished event");
        }
        if (event.getParticipantLimit() != 0 && event.getParticipantLimit() - event.getConfirmedRequests() <= 0) {
            throw new ValidationException("The event has reached the limit of requests for participation");
        }
        BooleanExpression selectByEventId = QRequestEntity.requestEntity.event.id.eq(eventId);
        BooleanExpression selectBuUserId = QRequestEntity.requestEntity.requester.id.eq(userId);
        if (requestRepository.count(selectByEventId.and(selectBuUserId)) > 0) {
            throw new ValidationException("You cannot send the same request");
        }
        RequestEntity request = gatherRequest(user, event);
        if (!event.getRequestModeration() || event.getParticipantLimit() == 0) {
            request.setStatus(RequestStatus.CONFIRMED);
            event.setConfirmedRequests(event.getConfirmedRequests() + 1);
            eventRepository.save(event);
        }
        return requestMapper.toParticipationRequestDto(requestRepository.save(request));
    }

    @Transactional
    @Override
    public ParticipationRequestDto cancelRequest(long userId, long requestId) {
        getUserById(userId);
        RequestEntity request = getRequestById(requestId);
        request.setStatus(RequestStatus.CANCELED);
        return requestMapper.toParticipationRequestDto(requestRepository.save(request));
    }

    private UserEntity getUserById(long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("User with id %s was not found", userId)));
    }

    private EventEntity getEventById(long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(String.format("Event with id %s was not found", eventId)));
    }

    private RequestEntity getRequestById(long requestId) {
        return requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException(String.format("Request with id %s was not found", requestId)));
    }

    private RequestEntity gatherRequest(UserEntity user, EventEntity event) {
        RequestEntity request = new RequestEntity();
        request.setCreated(LocalDateTime.now());
        request.setEvent(event);
        request.setRequester(user);
        request.setStatus(RequestStatus.PENDING);
        return request;
    }
}
