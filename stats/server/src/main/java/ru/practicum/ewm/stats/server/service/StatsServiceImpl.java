package ru.practicum.ewm.stats.server.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.dto.EndpointHitDto;
import ru.practicum.ewm.stats.dto.ViewStatsDto;
import ru.practicum.ewm.stats.server.mapper.StatsMapper;
import ru.practicum.ewm.stats.server.model.ViewStatsProjection;
import ru.practicum.ewm.stats.server.repository.EndpointHitRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {
    private final EndpointHitRepository endpointHitRepository;

    public void hit(EndpointHitDto endpointHitDto) {
        endpointHitRepository.save(StatsMapper.INSTANCE.fromDto(endpointHitDto));
    }

    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        List<ViewStatsProjection> stats;

        if (unique) {
            stats = endpointHitRepository.findUniqueStats(start, end, uris);
        } else {
            stats = endpointHitRepository.findNotUniqueStats(start, end, uris);
        }

        return stats.stream()
                .map(result -> ViewStatsDto.builder()
                        .app(result.getApp())
                        .uri(result.getUri())
                        .hits(result.getHits())
                        .build())
                .collect(Collectors.toList());
    }
}
