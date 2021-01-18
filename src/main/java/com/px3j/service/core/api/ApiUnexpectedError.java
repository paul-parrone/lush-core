package com.px3j.service.core.api;

import lombok.Data;
import lombok.NonNull;

@Data
public class ApiUnexpectedError {
    private @NonNull String contextKey;
    private @NonNull String status;
    private @NonNull String message;
    private boolean unexpectedError = true;
}
