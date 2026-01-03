package com.teamvault.DTO;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MultipartUploadStartResponse {

    private String uploadId;
    
    private String fileName;
    
    private Long fileSize;
    
    private Long chunkSize;
    
    private List<MultipartUploadPart> partPresignedUrls;
    
    private String objectKey;
    
    private int totalParts;

    @Data
    @Builder
    public static class MultipartUploadPart {
    	
        private int partNumber;
        
        private String presignedUrl;
    }
}