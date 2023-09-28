package com.apedano.quarkus.kafka.stream.weather;


import io.smallrye.mutiny.Multi;
import jakarta.enterprise.context.ApplicationScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Random;

import io.smallrye.reactive.messaging.kafka.Record;
import org.eclipse.microprofile.reactive.messaging.Outgoing;

/**
 * A bean producing random temperature data every second.
 * The values are written to a Kafka topic (temperature-values).
 * Another topic contains the name of weather stations (weather-stations).
 * The Kafka configuration is specified in the application configuration.
 */
@ApplicationScoped
public class WeatherRecordsGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(WeatherRecordsGenerator.class);
    private final Random random = new Random();
    private final Integer temperatureStandardDeviation = 15;
    private final List<WeatherStation> stations = List.of(
            new WeatherStation(1, "Hamburg", 13),
            new WeatherStation(2, "Snowdonia", 5),
            new WeatherStation(3, "Boston", 11),
            new WeatherStation(4, "Tokio", 16),
            new WeatherStation(5, "Cusco", 12),
            new WeatherStation(6, "Svalbard", -7),
            new WeatherStation(7, "Porthsmouth", 11),
            new WeatherStation(8, "Oslo", 7),
            new WeatherStation(9, "Marrakesh", 20));

    @Outgoing("temperature-values")
    public Multi<Record<Integer, String>> generate() { //The record is a kafka record with key and value
        return Multi.createFrom().ticks().every(Duration.ofMillis(1000))
                .onOverflow().drop()
                .map(tick -> {
                    WeatherStation station = stations.get(random.nextInt(stations.size()));
                    double temperature = BigDecimal.valueOf(
                                    random.nextGaussian() * temperatureStandardDeviation + station.averageTemperature
                            )
                            .setScale(1, RoundingMode.HALF_UP)
                            .doubleValue();

                    LOGGER.info("station: {}, temperature: {}", station.name, temperature);
                    return Record.of(station.id, Instant.now() + ";" + temperature);
                });
    }

    @Outgoing("weather-stations")
    public Multi<Record<Integer, String>> weatherStations() {
        return Multi.createFrom().items(stations.stream()
                .map(s -> Record.of(
                        s.id,
                        "{ \"id\" : " + s.id +
                                ", \"name\" : \"" + s.name + "\" }"))
        );
    }

    private static class WeatherStation {

        int id;
        String name;
        int averageTemperature;

        public WeatherStation(int id, String name, int averageTemperature) {
            this.id = id;
            this.name = name;
            this.averageTemperature = averageTemperature;
        }
    }


}
