package com.teamvault.service;

import java.time.Instant;

import org.springframework.stereotype.Service;

import com.teamvault.DTO.CommentRequest;
import com.teamvault.DTO.RatingRequest;
import com.teamvault.entity.GroupMember;
import com.teamvault.entity.Resource;
import com.teamvault.entity.ResourceEngagement;
import com.teamvault.exception.InvalidActionException;
import com.teamvault.mapper.ResourceEngagementMapper;
import com.teamvault.models.Comment;
import com.teamvault.models.CommentThread;
import com.teamvault.queryprocessor.GroupMemberQueryProcessor;
import com.teamvault.queryprocessor.ResourceQueryProcessor;
import com.teamvault.repository.ResourceEngagementRepository;
import com.teamvault.security.filter.SecurityUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ResourceEngagementService {
	
    private final GroupMemberQueryProcessor groupMemberQueryProcessor;
    
    private final ResourceQueryProcessor resourceQueryProcessor;

	private final ResourceEngagementRepository resourceEngagementRepository;
	
	public void toggleLike(String resourceId) {
		
		toggleLikeOrDislike(resourceId, true);
	}

	public void toggleDislike(String resourceId) {
		
		toggleLikeOrDislike(resourceId, false);
	}


	private void toggleLikeOrDislike(String resourceId, boolean isLike) {

	    Resource resource = resourceQueryProcessor.getResourceOrThrow(resourceId);
	    String groupId = resource.getGroup().getId();
	    String userId = SecurityUtil.getCurrentUser().getUserId();

	    GroupMember groupMember = groupMemberQueryProcessor
	            .getByUserIdAndGroupId(userId, groupId)
	            .orElseThrow();

	    ResourceEngagement engagement = resourceEngagementRepository
	            .findByResource_IdAndUser_Id(resourceId, userId)
	            .orElse(ResourceEngagementMapper.groupMemberResourceEngagement(resourceId, groupMember));

	    boolean currentValue = isLike
	            ? Boolean.TRUE.equals(engagement.getLiked())
	            : Boolean.TRUE.equals(engagement.getDisliked());

	    boolean newValue = !currentValue;

	    if (isLike) {
	    	
	        engagement.setLiked(newValue);

	        int count = resource.getResourceMeta().getNoLikes() + (newValue ? 1 : -1);
	        resource.getResourceMeta().setNoLikes(count);

	        if (newValue && Boolean.TRUE.equals(engagement.getDisliked())) {
	        	
	            engagement.setDisliked(false);
	            resource.getResourceMeta().setNoDislikes(resource.getResourceMeta().getNoDislikes() - 1);
	        }

	    } else {
	    	
	        engagement.setDisliked(newValue);
	        
	        int count = resource.getResourceMeta().getNoDislikes() + (newValue ? 1 : -1);
	        resource.getResourceMeta().setNoDislikes(count);

	        if (newValue && Boolean.TRUE.equals(engagement.getLiked())) {
	        	
	            engagement.setLiked(false);
	            resource.getResourceMeta().setNoLikes(resource.getResourceMeta().getNoLikes() - 1);
	        }
	    }

	    resourceEngagementRepository.save(engagement);
	    
	    resourceQueryProcessor.saveUpdatedResource(resource);
	}

	public void rateResource(String resourceId, RatingRequest request) {

	    Resource resource = resourceQueryProcessor.getResourceOrThrow(resourceId);
	    String groupId = resource.getGroup().getId();
	    String userId = SecurityUtil.getCurrentUser().getUserId();

	    GroupMember groupMember = groupMemberQueryProcessor
	            .getByUserIdAndGroupId(userId, groupId)
	            .orElseThrow();

	    ResourceEngagement engagement = resourceEngagementRepository
	            .findByResource_IdAndUser_Id(resourceId, userId)
	            .orElse(ResourceEngagementMapper.groupMemberResourceEngagement(resourceId, groupMember));

	    double currentAvg = resource.getResourceMeta().getAvgRating();
	    int noUsersRated = resource.getResourceMeta().getNoUsersRated();
	    Double previousRating = engagement.getRating();
	    Double newRating = request.getRating();

	    boolean firstTimeRating = (previousRating == null || previousRating == 0);

	    engagement.setRating(newRating);

	    double updatedAvg;

	    if (firstTimeRating) {
	    	
	        noUsersRated++;

	        if (noUsersRated == 1) updatedAvg = newRating;
	        else updatedAvg = (currentAvg * (noUsersRated - 1) + newRating) / noUsersRated;

	        resource.getResourceMeta().setNoUsersRated(noUsersRated);
	        
	    } else {
	    	
	        updatedAvg = ((currentAvg * noUsersRated) - previousRating + newRating) / noUsersRated;
	    }
	    
	    updatedAvg = normalizeRating(updatedAvg);

	    resource.getResourceMeta().setAvgRating(updatedAvg);

	    resourceEngagementRepository.save(engagement);
	    resourceQueryProcessor.saveUpdatedResource(resource);
	}


	public void createOrUpdateResourceComment(String resourceId, CommentRequest request) {
		
		Instant currentTime = Instant.now();
		
	    Resource resource = resourceQueryProcessor.getResourceOrThrow(resourceId);
	    String groupId = resource.getGroup().getId();
	    String userId = SecurityUtil.getCurrentUser().getUserId();

	    GroupMember groupMember = groupMemberQueryProcessor
	            .getByUserIdAndGroupId(userId, groupId)
	            .orElseThrow();

	    ResourceEngagement engagement = resourceEngagementRepository
	            .findByResource_IdAndUser_Id(resourceId, userId)
	            .orElse(ResourceEngagementMapper.groupMemberResourceEngagement(resourceId, groupMember));
	    
	    CommentThread commentThread = engagement.getCommentThread();
	    
	    boolean isNewComment = false;
	    
	    isNewComment = (commentThread.getCurrentComment() == null);
	    
	    moveCommentToHistory(engagement, currentTime, true);
	    	    
	    if(isNewComment) { 
	    	
	    	int noComments = resource.getResourceMeta().getNoComments();
	    	
	    	resource.getResourceMeta().setNoComments(noComments + 1);
	    }
	    
	    Comment comment = Comment.builder().comment(request.getComment())
	    		.commentedAt(currentTime)
	    		.build();
	    
	    commentThread.setCurrentComment(comment);
	    
	    resourceEngagementRepository.save(engagement);
	    
	    resourceQueryProcessor.saveUpdatedResource(resource);
	}

	public void deleteResourceComment(String resourceId) {
		
		Instant currentTime = Instant.now();
		
	    Resource resource = resourceQueryProcessor.getResourceOrThrow(resourceId);
	    String userId = SecurityUtil.getCurrentUser().getUserId();

	    ResourceEngagement engagement = resourceEngagementRepository
	            .findByResource_IdAndUser_Id(resourceId, userId)
	            .orElseThrow(() -> new InvalidActionException("No Comment To Delete"));
	    
	    moveCommentToHistory(engagement, currentTime, false);
	    
	    resource.getResourceMeta().setNoComments(resource.getResourceMeta().getNoComments() - 1);
	    
	    resourceEngagementRepository.save(engagement);
	    
	    resourceQueryProcessor.saveUpdatedResource(resource);
	}
	
	private void moveCommentToHistory(ResourceEngagement engagement, Instant currentTime, boolean isUpdate) {
		
	    CommentThread commentThread = engagement.getCommentThread();
		
	    if(commentThread.getCurrentComment() != null) {
	    	
	    	if(isUpdate) {
	    		
		    	commentThread.getCurrentComment().setEdited(true);
		    	commentThread.getCurrentComment().setEditedAt(currentTime);
		    	
	    	} else {
	    		
	    		commentThread.getCurrentComment().setDeleted(true);
	    		commentThread.getCurrentComment().setDeletedAt(currentTime);
	    		
	    	}
	    	
	    	commentThread.getHistory().add(commentThread.getCurrentComment());
	    	commentThread.setCurrentComment(null);
	    }
	}
	
	private double normalizeRating(double value) {
		
	    double fractional = value - Math.floor(value);
	    double whole = Math.floor(value);

	    if (fractional < 0.5)  return whole;
	    else if (fractional == 0.5) return value;
	    else  return whole + 1;
	}

}