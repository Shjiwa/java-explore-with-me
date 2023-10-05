package ru.practicum.ewm.stats.server.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "hits")
public class EndpointHit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "hits_id")
    private Long id;

    @Column(name = "hits_app")
    private String app;

    @Column(name = "hits_uri")
    private String uri;

    @Column(name = "hits_ip")
    private String ip;

    @Column(name = "hits_timestamp")
    private LocalDateTime hitTimestamp;
}
