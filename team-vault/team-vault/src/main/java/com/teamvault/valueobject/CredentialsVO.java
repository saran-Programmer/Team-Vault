package com.teamvault.valueobject;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class CredentialsVO {

	private String userName;
	
	private String email;
	
	private String password;
}
