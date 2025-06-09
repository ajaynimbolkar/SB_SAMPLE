package com.ril.SB_SAMPLE.exception;

import com.ril.SB_SAMPLE.constants.ErrorConstant;

public class JsonProcessingException extends RuntimeException {
    public JsonProcessingException(ErrorConstant errorConstant) {
        super(errorConstant.getMessage());
    }
}
