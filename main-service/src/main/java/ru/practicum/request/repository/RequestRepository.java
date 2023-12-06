package ru.practicum.request.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import ru.practicum.request.model.RequestEntity;

public interface RequestRepository extends JpaRepository<RequestEntity, Long>,
        QuerydslPredicateExecutor<RequestEntity> {
}
