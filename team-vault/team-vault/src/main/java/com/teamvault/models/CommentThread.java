package com.teamvault.models;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CommentThread {

	private Comment currentComment;
	
	// Once the comment gets deleted or updated the old comments goes into history
	@Builder.Default
    private List<Comment> history = new ArrayList<>();
}
