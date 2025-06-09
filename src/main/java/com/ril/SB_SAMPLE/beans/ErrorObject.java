package com.ril.SB_SAMPLE.beans;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ErrorObject {
	
	private String errorCode;
	private String errorMessage;
	

}
