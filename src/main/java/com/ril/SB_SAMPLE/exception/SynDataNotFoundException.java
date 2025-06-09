package com.ril.SB_SAMPLE.exception;

import com.ril.SB_SAMPLE.constants.ErrorConstant;

public class SynDataNotFoundException extends RuntimeException {
    public SynDataNotFoundException(ErrorConstant errorConstant) {
        super(errorConstant.getMessage());
    }
}
