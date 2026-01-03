package com.teamvault.service;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.teamvault.fields.CacheName;

@Service
public class UploadProgressService {

    @CachePut(value = CacheName.RESOURCE_PROGRESS, key = "#path")
    public Double initUploadCache(String path) {
    	
        return 0.0;
    }

    @CachePut(value = CacheName.RESOURCE_PROGRESS, key = "#path")
    public Double updateUploadProgress(String path, Double progressPercentage) {

        double capped = Math.min(progressPercentage, 100.0);

        return Math.floor(capped * 10) / 10.0;
    }

    @CacheEvict(value = CacheName.RESOURCE_PROGRESS, key = "#path")
    public void clearUploadProgress(String path) {
    }
    
    @Cacheable(value = CacheName.RESOURCE_PROGRESS, key = "#path", unless = "true")
    public Double getUploadProgress(String path) {
    	
        return null;
    }

}
