package com.teamvault.query.processor;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.teamvault.DTO.GroupRequestDTO;
import com.teamvault.entity.Group;
import com.teamvault.repository.GroupRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GroupQueryProcessor {

	private final GroupRepository groupRepository;
	
	public Optional<Group> getConflictingGroup(GroupRequestDTO request) {
		
		return groupRepository.findByGroupDetailsVO_Title(request.getTitle())
                .filter(g -> !g.isDeleted());
	}
}
