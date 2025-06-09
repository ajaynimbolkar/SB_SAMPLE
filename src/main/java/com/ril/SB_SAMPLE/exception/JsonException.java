package com.ril.SB_SAMPLE.exception;

import com.ril.SB_SAMPLE.constants.ErrorConstant;

public class JsonException extends BaseException {

	private static final long serialVersionUID = 1L;

	public JsonException(ErrorConstant errorConstant) {
		super(errorConstant);
	}

}
