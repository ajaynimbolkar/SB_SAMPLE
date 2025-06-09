package com.ril.SB_SAMPLE.dto.response;

import lombok.Data;


@Data
public class ApiResponse<T> {
    private int statusCode;
    private T body;

    public ApiResponse(int statusCode, T body) {
        this.statusCode = statusCode;
        this.body = body;
    }
}
