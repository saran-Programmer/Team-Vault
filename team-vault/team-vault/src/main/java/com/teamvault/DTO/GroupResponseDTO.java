package com.teamvault.DTO;

import java.time.Instant;

import com.teamvault.enums.GroupVisibility;
import com.teamvault.valueobject.GroupDetailsVO;
import com.teamvault.valueobject.GroupStatisticsVO;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GroupResponseDTO {

    private String id;
    
	private GroupDetailsVO groupDetailsVO;
	
	private GroupVisibility groupVisibility;
	
	private GroupStatisticsVO groupStatisticsVO;
        
    private Instant createdDate;
    
    private Instant lastUpdatedDate;
}
