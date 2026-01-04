package com.teamvault.mapper;

import com.teamvault.DTO.GroupRequestDTO;
import com.teamvault.DTO.GroupResponseDTO;
import com.teamvault.entity.Group;
import com.teamvault.valueobject.GroupDetailsVO;
import com.teamvault.valueobject.GroupStatisticsVO;

public class GroupMapper {

	private GroupMapper() {}
	
    public static GroupResponseDTO mapToResponse(Group group) {

        return GroupResponseDTO.builder()
                .id(group.getId())
                .groupDetailsVO(group.getGroupDetailsVO())
                .groupVisibility(group.getGroupVisibility())
                .groupStatisticsVO(group.getGroupStatisticsVO())
                .createdDate(group.getCreatedDate())
                .lastUpdatedDate(group.getLastUpdatedDate())
                .build();
    }
    
    
    public static Group mapToEntity(GroupRequestDTO dto) {
    	
    	GroupDetailsVO groupDetailsVO = GroupDetailsVO.builder()
    			.title(dto.getTitle())
    			.description(dto.getDescription())
    			.build();
    	
    	return Group.builder()
    			.groupDetailsVO(groupDetailsVO)
    			.groupVisibility(dto.getGroupVisibility())
    			.groupStatisticsVO(new GroupStatisticsVO())
    			.build();
    }
}
