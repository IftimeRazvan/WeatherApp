package org.example.data_source.model;

import lombok.Data;
import jakarta.persistence.*;

@Entity
@Data
@Table(name = "weather")
public class WeatherEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @OneToOne
    @JoinColumn(name = "location_id")
    private LocationEntity location;

    @Column(name = "temperature")
    private Double temperature;

    @Column(name = "weather_description")
    private String weatherDescription;

    @Column(name = "tommorow_temperature")
    private Double tommorowTemperature;

    @Column(name = "day_after_tommorow_temperature")
    private Double dayAfterTommorowTemperature;

    @Column(name = "third_day_temperature")
    private Double thirdDayTemperature;
}
