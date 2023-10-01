package ru.practicum.ewm.service.participation.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import ru.practicum.ewm.service.participation.dto.ParticipationRequestDto;
import ru.practicum.ewm.service.participation.model.ParticipationRequest;

@Mapper
public interface ParticipationRequestMapper {
    ParticipationRequestMapper INSTANCE = Mappers.getMapper(ParticipationRequestMapper.class);

    @Mapping(target = "requester", source = "requester.id")
    @Mapping(target = "event", source = "event.id")
    ParticipationRequestDto toDto(ParticipationRequest participationRequest);
}
