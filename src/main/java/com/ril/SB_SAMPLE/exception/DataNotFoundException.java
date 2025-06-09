package com.ril.SB_SAMPLE.exception;

import com.ril.SB_SAMPLE.constants.ErrorConstant;

public class DataNotFoundException extends BaseException{

	private static final long serialVersionUID = 1L;

	public DataNotFoundException(ErrorConstant errorConstant) {
		super(errorConstant);
	}

}
