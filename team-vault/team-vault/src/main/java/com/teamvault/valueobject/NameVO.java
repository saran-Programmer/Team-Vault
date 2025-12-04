package com.teamvault.valueobject;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NameVO {

	private String firstName;
	
	private String middleName;
	
	private String lastName;
	
	@JsonIgnore
	public String getFullName() {
		
		if(middleName == null || middleName.isBlank()) {
			
			return firstName + " " + lastName;
			
		}
			
		return firstName + " " + middleName + " " + lastName;
	}
}
