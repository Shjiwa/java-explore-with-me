package ru.practicum.ewm.service.event.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import ru.practicum.ewm.service.category.model.Category;
import ru.practicum.ewm.service.event.dto.EventFullDto;
import ru.practicum.ewm.service.event.dto.EventShortDto;
import ru.practicum.ewm.service.event.dto.NewEventDto;
import ru.practicum.ewm.service.event.model.Event;
import ru.practicum.ewm.service.event.model.Location;

@Mapper
public interface EventMapper {
    EventMapper INSTANCE = Mappers.getMapper(EventMapper.class);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "eventDate", source = "dto.eventTimestamp")
    @Mapping(target = "category", source = "category")
    @Mapping(target = "location", source = "location")
    Event fromDto(NewEventDto dto, Category category, Location location);

    EventFullDto toFullDto(Event entity);

    EventShortDto toShortDto(Event entity);
}
