package ru.practicum.participationRequest.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import ru.practicum.participationRequest.model.ParticipationRequestEntity;

public interface ParticipationRequestRepository extends JpaRepository<ParticipationRequestEntity, Long>,
        QuerydslPredicateExecutor<ParticipationRequestEntity> {
}
