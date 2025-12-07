package com.teamvault.event.listener;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.teamvault.entity.GroupMemberLog;
import com.teamvault.event.model.GroupMemberEvent;
import com.teamvault.repository.GroupMemberLogRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class GroupMemberChangeListner {

	private final GroupMemberLogRepository groupMemberLogRepository;
	
	@EventListener
	public void handleGroupMemberInvite(GroupMemberLog log) {
		
		
		groupMemberLogRepository.save(log);
	}
}
