package com.hisense.gateway.library.web.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OriginalError {
    long timestamp;
    int status;
    String error;
    String exception;
    String message;
    String path;
}
