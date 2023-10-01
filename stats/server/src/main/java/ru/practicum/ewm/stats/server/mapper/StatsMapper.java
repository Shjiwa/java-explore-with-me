package ru.practicum.ewm.stats.server.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import ru.practicum.ewm.stats.dto.EndpointHitDto;
import ru.practicum.ewm.stats.server.model.EndpointHit;

@Mapper
public interface StatsMapper {
    StatsMapper INSTANCE = Mappers.getMapper(StatsMapper.class);

    EndpointHit fromDto(EndpointHitDto endpointHitDto);
}
