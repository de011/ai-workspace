package com.aiworkspace.event;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCreatedEvent {

    private Long userId;

    private String firstName;

    private String lastName;

    private String email;
}