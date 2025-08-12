package com.laze.flowcontrol.dto;

public record AllowUserResponse(Long requestCount, Long allowedCount) {
}
