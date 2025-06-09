package com.ril.SB_SAMPLE.constants;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public enum ErrorConstant {	

	HARMFUL_REQUEST_ERROR("LMS_MT_00", "Request Contains Harmfull Data", HttpStatus.BAD_REQUEST),
	USER_NOT_FOUND("LMS_ERR_001", "User is missing", HttpStatus.UNAUTHORIZED), //ErrorCode, ErrorMessage, HTTPStausCode
	DATA_NOT_FOUND("LMS_ERR_001", "Data is not found", HttpStatus.INTERNAL_SERVER_ERROR),
	ELASTIC_CLIENT_EXCEPTION("LMS_ERR_001", "ELSTAIC CLIENT EXCEPTION", HttpStatus.INTERNAL_SERVER_ERROR),
	SYNC_DATA_NOT_FOUND("LMS_ERR_001", "Sync Data is not found", HttpStatus.INTERNAL_SERVER_ERROR),
	JSON_PROCESSING_EXCEPTION("LMS_ERR_001", "JSON Processing Exception", HttpStatus.INTERNAL_SERVER_ERROR);
	
	String errorCode;
	String message;
	HttpStatus httpStatus;
	
	ErrorConstant(String errorCode, String message, HttpStatus httpStatus){
		this.errorCode = errorCode;
		this.message = message;
		this.httpStatus = httpStatus;
	}
	
	
	
}
