package ru.practicum.event.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.event.model.LocationEntity;

public interface LocationRepository extends JpaRepository<LocationEntity, Long> {
}
