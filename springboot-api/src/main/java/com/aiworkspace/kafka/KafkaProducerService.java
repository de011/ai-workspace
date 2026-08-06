package com.aiworkspace.kafka;

import com.aiworkspace.event.UserCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaProducerService {

    private final KafkaTemplate<String, UserCreatedEvent> kafkaTemplate;

    private static final String TOPIC = "user-created";

    public void publishUserCreated(UserCreatedEvent event) {

        log.info("Publishing UserCreatedEvent : {}", event);

        kafkaTemplate.send(TOPIC, event);

        log.info("Event Published Successfully");
    }
}