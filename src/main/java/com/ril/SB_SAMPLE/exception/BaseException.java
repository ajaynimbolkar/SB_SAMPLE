package com.ril.SB_SAMPLE.exception;

import org.springframework.http.HttpStatus;

import com.ril.SB_SAMPLE.constants.ErrorConstant;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class BaseException extends RuntimeException {

	private static final long serialVersionUID = 7626893836669722031L;

	ErrorConstant errorConstant;

	public BaseException(ErrorConstant errorConstant) {
		super(errorConstant.getMessage());
		this.errorConstant = errorConstant;
	}

	public String getErrorCode() {
		return errorConstant.getErrorCode();
	}

	public String getMessage() {
		return errorConstant.getMessage();
	}

	public HttpStatus getHttpStatus() {
		return errorConstant.getHttpStatus();
	}

}
