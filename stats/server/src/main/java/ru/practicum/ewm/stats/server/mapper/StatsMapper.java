package ru.practicum.ewm.stats.server.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.ewm.stats.dto.EndpointHitDto;
import ru.practicum.ewm.stats.server.model.EndpointHit;

@UtilityClass
public class StatsMapper {
    public EndpointHit fromDto(EndpointHitDto dto) {
        EndpointHit entity = new EndpointHit();
        entity.setApp(dto.getApp());
        entity.setUri(dto.getUri());
        entity.setIp(dto.getIp());
        entity.setTimestamp(dto.getHitTimestamp());
        return entity;
    }

    public EndpointHitDto toDto(EndpointHit entity) {
        return EndpointHitDto.builder()
                .app(entity.getApp())
                .uri(entity.getUri())
                .ip(entity.getIp())
                .hitTimestamp(entity.getTimestamp())
                .build();
    }
}
