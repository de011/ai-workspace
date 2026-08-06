package com.aiworkspace.kafka;

import com.aiworkspace.event.UserCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class KafkaConsumerService {

    @KafkaListener(
            topics = "user-created",
            groupId = "aiworkspace-group"
    )
    public void consume(UserCreatedEvent event) {

        log.info("===============================");
        log.info("User Created Event Received");
        log.info("User ID : {}", event.getUserId());
        log.info("Email : {}", event.getEmail());
        log.info("First Name : {}", event.getFirstName());
        log.info("===============================");
    }

}