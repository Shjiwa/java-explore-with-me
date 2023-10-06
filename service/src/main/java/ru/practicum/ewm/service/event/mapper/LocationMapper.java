package ru.practicum.ewm.service.event.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import ru.practicum.ewm.service.event.dto.LocationDto;
import ru.practicum.ewm.service.event.model.Location;

@Mapper
public interface LocationMapper {
    LocationMapper LOCATION_MAPPER = Mappers.getMapper(LocationMapper.class);

    Location fromDto(LocationDto dto);
}
