package com.teamvault.valueobject;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GroupMemberVO {

	private String id;
	
	private boolean isSuperAdmin;
}
