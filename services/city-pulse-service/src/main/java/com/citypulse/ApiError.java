package com.citypulse;

public record ApiError(
        String code,
        String message
) {
}
