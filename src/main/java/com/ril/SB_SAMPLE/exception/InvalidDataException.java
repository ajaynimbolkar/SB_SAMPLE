package com.ril.SB_SAMPLE.exception;

import com.ril.SB_SAMPLE.constants.ErrorConstant;

public class InvalidDataException extends BaseException{

	private static final long serialVersionUID = 1L;

	public InvalidDataException(ErrorConstant errorConstant) {
		super(errorConstant);
	}

}
