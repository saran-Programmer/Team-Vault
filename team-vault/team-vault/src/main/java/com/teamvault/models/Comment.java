package com.teamvault.models;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Comment {

    private String comment;
	
    private Instant commentedAt;
    
    private boolean isEdited;
    
    private Instant editedAt;
    
    private boolean isDeleted;
    
    private Instant deletedAt;
}
