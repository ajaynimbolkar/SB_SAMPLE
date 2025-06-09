package com.ril.SB_SAMPLE.dto.response;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.slf4j.MDC;

import com.ril.SB_SAMPLE.beans.ErrorObject;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Response {

	private static final Logger LOGGER = LogManager.getLogger(Response.class);

	private String requestId;
	private Boolean success;
	private Integer status;
	private List<ErrorObject> errors;
	private Object resource;

	public Response(Boolean success, Integer status, List<ErrorObject> error, Object resource) {
		requestId = MDC.get("requestId");
		this.success = success;
		this.status = status;
		try {
			if (error != null) {
				errors = error;
			}
			if (resource instanceof Collection<?>) {
				this.resource = Collections.singletonMap("data", resource);
			} else {
				this.resource = resource;
			}
		} catch (Exception e) {
			LOGGER.info("[ {} ] The error in the trace is :  [ {} ]", Response.class, e.getMessage());
		}
	}
}

