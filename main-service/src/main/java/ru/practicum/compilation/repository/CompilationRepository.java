package ru.practicum.compilation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import ru.practicum.compilation.model.CompilationEntity;

public interface CompilationRepository extends JpaRepository<CompilationEntity, Long>,
        QuerydslPredicateExecutor<CompilationEntity> {
}
