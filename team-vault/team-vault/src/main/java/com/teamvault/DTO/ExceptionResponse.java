package com.teamvault.DTO;

import java.time.Instant;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ExceptionResponse {

	private String message;
	
	private String path;
	
	private String method;
	
	private String errorType;
	
	private Instant timestamp;
}
