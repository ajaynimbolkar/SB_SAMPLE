package com.ril.SB_SAMPLE.exception;

import com.ril.SB_SAMPLE.constants.ErrorConstant;

public class DatabaseFailedException extends BaseException {

	private static final long serialVersionUID = 1L;

	public DatabaseFailedException(ErrorConstant errorConstant) {
		super(errorConstant);
	}

}
