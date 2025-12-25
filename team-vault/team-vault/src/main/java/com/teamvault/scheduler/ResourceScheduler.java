package com.teamvault.scheduler;

import java.time.Instant;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.teamvault.aws.ResourceS3Service;
import com.teamvault.entity.Resource;
import com.teamvault.models.S3Details;
import com.teamvault.repository.ResourceRepository;
import com.teamvault.valueobject.ResourceDeletionStatus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ResourceScheduler {
	
	private final ResourceS3Service resourceS3Service;
	
	private final ResourceRepository resourceRepository;

    @Scheduled(cron = "0 0 0 ? * SUN", zone = "UTC")
    public void weeklyResourceMaintenance() {
    	
    	long start = System.currentTimeMillis();
    	
    	int success = 0;
    	int failure = 0;

    	log.info("Weekly resource maintenance job started");
    	
    	List<Resource> resourceMarkedForDeletion = resourceRepository.findByDeletionStatus(ResourceDeletionStatus.MARKED_FOR_DELETION);
    	
    	for(Resource resource : resourceMarkedForDeletion) {
    		
    	    log.info("Processing resource {} (user: {}, group: {})", resource.getId(), resource.getUser().getId(), resource.getGroup().getId());
    	    
    	    try {
    	    	
    	    	S3Details deletedS3Details = resourceS3Service.moveToDeletedBucket(resource);
    	    	
    	    	resource.setS3Details(deletedS3Details);
    	        resource.setDeletionStatus(ResourceDeletionStatus.S3_MOVED);
    	        resource.setDeletedAt(Instant.now());
    	        
    	        success++;
    	        
    	        log.info("S3 move completed for resource {}", resource.getId());
    	    	
    	    } catch (Exception ex) {
    	    	
    	    	resource.setDeletionStatus(ResourceDeletionStatus.S3_MOVE_FAILED);
    	    	
    	    	failure++;
    	    	
    	    	log.error("S3 move failed for resource {}", resource.getId(), ex);
    	    }
    	}
    	
    	long end = System.currentTimeMillis();
    	
    	log.info("Job completed in {} ms | Success: {}, Failure: {}", (end - start), success, failure);
    	
    	resourceRepository.saveAll(resourceMarkedForDeletion);
    	
    	log.info("Updated All resource closing the job");
    }
}
