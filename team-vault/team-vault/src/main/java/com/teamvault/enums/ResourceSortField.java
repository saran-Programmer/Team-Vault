package com.teamvault.enums;

public enum ResourceSortField {

	UPLOADED_AT(""),
	TITLE(""),
	DESCRIPTION(""),
	LIKES(""),
	DISLIKES(""),
	COMMENTS(""),
	RATING("");
	
	public static final String DEFAULT_SORT_FIELD = "UPLOADED_AT";
	
	private final String field;
	
	ResourceSortField(String field) {
		
		this.field = field;
	}
	
	public String getField() {
		
		return field;
	}
	
}
