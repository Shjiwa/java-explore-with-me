package ru.practicum.ewm.service.event.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@Entity
@Table(name = "locations")
public class Location {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "location_id")
    private Long id;

    /**
     * широта
     */
    @Column(name = "location_lat")
    private Float lat;

    /**
     * долгота
     */
    @Column(name = "location_lon")
    private Float lon;
}
