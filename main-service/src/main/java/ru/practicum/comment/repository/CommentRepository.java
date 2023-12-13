package ru.practicum.comment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import ru.practicum.comment.model.CommentEntity;

public interface CommentRepository extends JpaRepository<CommentEntity, Long>,
        QuerydslPredicateExecutor<CommentEntity> {
}
