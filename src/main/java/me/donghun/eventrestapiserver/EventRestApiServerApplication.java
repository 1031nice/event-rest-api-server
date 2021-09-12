package me.donghun.eventrestapiserver;

import me.donghun.eventrestapiserver.events.Event;
import me.donghun.eventrestapiserver.events.EventRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.stream.IntStream;

@SpringBootApplication
public class EventRestApiServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(EventRestApiServerApplication.class, args);
    }

    @Autowired
    EventRepository eventRepository;

    @Bean
    public ApplicationRunner applicationRunner() {
        return new ApplicationRunner() {
            @Override
            public void run(ApplicationArguments args) throws Exception {
                IntStream.range(0, 30).forEach(i -> {
                    Event event = Event.builder()
                            .name("event" + i)
                            .description("test event")
                            .build();

                    eventRepository.save(event);
                });
            }
        };
    }

    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }

}
