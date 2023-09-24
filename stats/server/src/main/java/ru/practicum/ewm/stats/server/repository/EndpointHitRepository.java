package ru.practicum.ewm.stats.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.ewm.stats.server.model.EndpointHit;
import ru.practicum.ewm.stats.server.model.ViewStatsProjection;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EndpointHitRepository extends JpaRepository<EndpointHit, Long> {
    @Query("select e.app as app, e.uri as uri, count(distinct e.ip) as hits " +
           "from EndpointHit e " +
           "where e.timestamp between :start and :end " +
           "and ((:uris) is null or e.uri in :uris) " +
           "group by e.app, e.uri " +
           "order by hits desc")
    List<ViewStatsProjection> findUniqueStats(LocalDateTime start, LocalDateTime end, List<String> uris);

    @Query("select e.app as app, e.uri as uri, count(e.ip) as hits " +
           "from EndpointHit e " +
           "where e.timestamp between :start and :end " +
           "and ((:uris) is null or e.uri in :uris) " +
           "group by e.app, e.uri " +
           "order by hits desc")
    List<ViewStatsProjection> findNotUniqueStats(LocalDateTime start, LocalDateTime end, List<String> uris);
}
