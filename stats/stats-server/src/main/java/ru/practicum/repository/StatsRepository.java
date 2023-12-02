package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.model.EndpointHitEntity;
import ru.practicum.model.ViewStatsEntity;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsRepository extends JpaRepository<EndpointHitEntity, Long> {

    @Query("SELECT NEW ru.practicum.model.ViewStatsEntity(eh.app, eh.uri, COUNT(eh.ip))" +
            "FROM EndpointHitEntity AS eh " +
            "WHERE eh.timestamp BETWEEN :start AND :end " +
            "AND (eh.uri IN :uris OR :uris IS NULL ) " +
            "GROUP BY eh.app, eh.uri " +
            "ORDER BY COUNT(eh.uri) DESC")
    List<ViewStatsEntity> getStatsByUrisBetweenStartAndEnd(LocalDateTime start, LocalDateTime end,
                                                           List<String> uris);

    @Query("SELECT NEW ru.practicum.model.ViewStatsEntity(eh.app, eh.uri, COUNT(DISTINCT(eh.ip)))" +
            "FROM EndpointHitEntity AS eh " +
            "WHERE eh.timestamp BETWEEN :start AND :end " +
            "AND (eh.uri IN :uris OR :uris IS NULL ) " +
            "GROUP BY eh.app, eh.uri " +
            "ORDER BY COUNT(DISTINCT(eh.ip)) DESC")
    List<ViewStatsEntity> getStatsByUrisBetweenStartAndEndUniqueIp(LocalDateTime start, LocalDateTime end,
                                                                   List<String> uris);
}
