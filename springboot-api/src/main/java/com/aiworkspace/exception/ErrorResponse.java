package com.aiworkspace.exception;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ErrorResponse {

    private boolean success;

    private String message;

    private String errorCode;

    private String timestamp;

}