package com.teamvault.aws;

import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.teamvault.DTO.PresignedResourceResponse;
import com.teamvault.entity.Resource;
import com.teamvault.exception.S3Exception;
import com.teamvault.models.S3Details;

import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CompleteMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CompletedMultipartUpload;
import software.amazon.awssdk.services.s3.model.CompletedPart;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectTaggingRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.Tag;
import software.amazon.awssdk.services.s3.model.UploadPartRequest;
import software.amazon.awssdk.services.s3.model.UploadPartResponse;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedUploadPartRequest;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadResponse;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResourceS3Service {

    private final S3Client s3Client;
    
    private final S3Presigner s3Presigner;

    @Value("${aws.s3.resource-bucket}")
    private String mainBucketName;

    @Value("${aws.s3.deleted-resource-bucket}")
    private String deletedBucketName;
    

    @Retryable(
    	retryFor = { IOException.class, SdkException.class },
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2.0)
    )
    public S3Details uploadFile(MultipartFile file, String groupId, String userId) throws IOException {

        Map<String, String> tags = getResourceTags(file);

        String fileName = file.getOriginalFilename();
        
        String objectKey = groupId + "/" + userId + "/" + fileName;

        PutObjectRequest putRequest = PutObjectRequest.builder()
                .bucket(mainBucketName)
                .key(objectKey)
                .contentType(file.getContentType())
                .contentLength(file.getSize())  
                .tagging(buildTagString(tags))
                .build();

        s3Client.putObject(
                putRequest,
                RequestBody.fromInputStream(
                        file.getInputStream(),
                        file.getSize()));

        return S3Details.builder()
                .bucketName(mainBucketName)
                .fileName(fileName)
                .size(file.getSize())
                .tags(tags)
                .url("https://" + mainBucketName + ".s3.amazonaws.com/" + objectKey)
                .build();
    }
    
    @Retryable(
    	retryFor = { SdkException.class, IOException.class },
    	maxAttempts = 3,
    	backoff = @Backoff(delay = 1000, multiplier = 2.0))
    public PresignedResourceResponse generatePresignedUrl(Resource resource, @Nullable Long expirySeconds) {

        long effectiveExpiry = (expirySeconds == null || expirySeconds <= 0) ? 600L : expirySeconds;

        String objectKey = resource.getGroup().getId() + "/" + resource.getUser().getId() + "/" + resource.getS3Details().getFileName();

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(mainBucketName)
                .key(objectKey)
                .responseContentDisposition("inline")
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofSeconds(effectiveExpiry))
                .getObjectRequest(getObjectRequest)
                .build();

        PresignedGetObjectRequest presignedRequest =
                s3Presigner.presignGetObject(presignRequest);

        HeadObjectResponse headObject =
                s3Client.headObject(HeadObjectRequest.builder()
                        .bucket(mainBucketName)
                        .key(objectKey)
                        .build());

        String versionId = headObject.versionId(); 

        Map<String, String> tags =
                s3Client.getObjectTagging(GetObjectTaggingRequest.builder()
                        .bucket(mainBucketName)
                        .key(objectKey)
                        .build())
                .tagSet()
                .stream()
                .collect(Collectors.toMap(Tag::key, Tag::value));


        return PresignedResourceResponse.builder()
        		.resourceId(resource.getId())
        		.resourceTitle(resource.getResourceDetails().getTitle())
        		.resourceOwnerId(resource.getUser().getId())
        		.resourceDescription(resource.getResourceDetails().getDescription())
        		.groupMemberId(resource.getGroupMember().getId())
                .presignedUrl(presignedRequest.url().toString())
                .versionId(versionId)
                .tags(tags)
                .expiresInSeconds(effectiveExpiry)
                .resourceVisiblity(resource.getResourceVisiblity())
                .timestamp(Instant.now())
                .build();

    }

    @Retryable(
    	retryFor = { SdkException.class, IOException.class },
    	maxAttempts = 3,
    	backoff = @Backoff(delay = 1000, multiplier = 2.0)
    )
    public void markObjectAsDeleted(Resource resource) {

        String objectKey = resource.getGroup().getId() + "/" + resource.getUser().getId() + "/" + resource.getS3Details().getFileName();

        Map<String, String> existingTags =
                s3Client.getObjectTagging(GetObjectTaggingRequest.builder()
                        .bucket(mainBucketName)
                        .key(objectKey)
                        .build())
                .tagSet()
                .stream()
                .collect(Collectors.toMap(Tag::key, Tag::value));

        existingTags.put("deleted", "true");
        existingTags.put("deletedAt", Instant.now().toString());

        var tagSet = existingTags.entrySet()
                .stream()
                .map(e -> Tag.builder()
                        .key(e.getKey())
                        .value(e.getValue())
                        .build())
                .toList();

        s3Client.putObjectTagging(builder -> builder
                .bucket(mainBucketName)
                .key(objectKey)
                .tagging(tagging -> tagging.tagSet(tagSet)));
    }
    
    @Retryable(retryFor = { SdkException.class, IOException.class },
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2.0))
    public S3Details moveToDeletedBucket(Resource resource) {

        String objectKey = resource.getGroup().getId() + "/" + resource.getUser().getId() + "/" + resource.getS3Details().getFileName();

        log.info("Starting S3 move for resource {}", resource.getId());

        s3Client.copyObject(CopyObjectRequest.builder()
                .sourceBucket(mainBucketName)
                .sourceKey(objectKey)
                .destinationBucket(deletedBucketName)
                .destinationKey(objectKey)
                .build());

        log.info("Resource {} copied to deleted bucket", resource.getId());

        s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(mainBucketName)
                .key(objectKey)
                .build());

        log.info("Resource {} deleted from main bucket", resource.getId());
        
        return S3Details.builder()
                .bucketName(deletedBucketName)
                .fileName(resource.getS3Details().getFileName())
                .size(resource.getS3Details().getSize())
                .url("https://" + deletedBucketName + ".s3.amazonaws.com/" + objectKey)
                .tags(resource.getS3Details().getTags())
                .build();
    }
    
    @Retryable(retryFor = { SdkException.class, IOException.class },
    	    maxAttempts = 3, backoff = @Backoff(delay = 1000, multiplier = 2.0))
    	public String createMultipartUpload(String objectKey, long fileSize, String contentType) {
    	    try {
    	        Map<String, String> tags = getResourceTags(fileSize);

    	        CreateMultipartUploadRequest request = CreateMultipartUploadRequest.builder()
    	                .bucket(mainBucketName)
    	                .key(objectKey)
    	                .tagging(buildTagString(tags))
    	                .contentType(contentType)
    	                .build();

    	        CreateMultipartUploadResponse response = s3Client.createMultipartUpload(request);
    	        return response.uploadId();

    	    } catch (Exception e) {
    	    	
    	        log.error("Failed to create multipart upload for objectKey {}: {}", objectKey, e.getMessage());
    	        
    	        throw new S3Exception("Failed to initiate multipart upload for " + objectKey);
    	    }
    	}


    @Retryable(retryFor = { SdkException.class, IOException.class },
    	    maxAttempts = 3,
    	    backoff = @Backoff(delay = 1000, multiplier = 2.0))
    public String generatePresignedUrlForPart(String objectKey, String uploadId, int partNumber, long partSize) {

    	    UploadPartRequest uploadPartRequest = UploadPartRequest.builder()
    	            .bucket(mainBucketName)
    	            .key(objectKey)
    	            .uploadId(uploadId)
    	            .partNumber(partNumber)
    	            .contentLength(partSize)
    	            .build();

    	    PresignedUploadPartRequest presignedRequest = s3Presigner.presignUploadPart(r -> r
    	            .signatureDuration(Duration.ofHours(1))
    	            .uploadPartRequest(uploadPartRequest));

    	    URL url = presignedRequest.url();
    	    
    	    return url.toString();
    	}
    
    @Retryable(retryFor = { SdkException.class, IOException.class },
    		maxAttempts = 3,
    		backoff = @Backoff(delay = 1000, multiplier = 2.0))
    public String uploadPart(String objectKey, String uploadId, int partNumber, byte[] partBytes) {

    	UploadPartRequest uploadPartRequest = UploadPartRequest.builder()
    			.bucket(mainBucketName)
    			.key(objectKey)
    			.uploadId(uploadId)
    			.partNumber(partNumber)
    			.contentLength((long) partBytes.length)
    			.build();

    	UploadPartResponse response =
    			s3Client.uploadPart(uploadPartRequest, RequestBody.fromBytes(partBytes));

    	return response.eTag();

    }

    
    @Retryable(retryFor = { SdkException.class, IOException.class },
    		maxAttempts = 3,
    		backoff = @Backoff(delay = 1000, multiplier = 2.0))
    public void completeMultipartUpload(String objectKey, String uploadId, List<CompletedPart> completedParts) {

    	CompletedMultipartUpload multipartUpload = CompletedMultipartUpload.builder()
    			.parts(completedParts)
    			.build();

    	CompleteMultipartUploadRequest completeRequest =
    			CompleteMultipartUploadRequest.builder()
    			.bucket(mainBucketName)
    			.key(objectKey)
    			.uploadId(uploadId)
    			.multipartUpload(multipartUpload)
    			.build();

    	s3Client.completeMultipartUpload(completeRequest);
    }

    private Map<String, String> getResourceTags(long sizeInBytes) {
        Map<String, String> tags = new HashMap<>();
        
        String sizeTag;
        if (sizeInBytes < 5 * 1024 * 1024) {
        	
            sizeTag = "SMALL";
        } else if (sizeInBytes <= 15 * 1024 * 1024) {
        	
            sizeTag = "MEDIUM";
        } else {
        	
            sizeTag = "LARGE";
        }
        
        tags.put("size", sizeTag);
        
        return tags;
    }

    private Map<String, String> getResourceTags(MultipartFile file) {
    	
        return getResourceTags(file.getSize());
    }


    private String buildTagString(Map<String, String> tags) {
    	
        StringBuilder sb = new StringBuilder();
        tags.forEach((key, value) -> sb.append(key).append("=").append(value).append("&"));
        if (sb.length() > 0) sb.setLength(sb.length() - 1);
        return sb.toString();
    }
    
    public S3Details buildS3Details(MultipartFile file, String objectKey) {
    	
        return S3Details.builder()
                .bucketName(mainBucketName)
                .fileName(file.getOriginalFilename())
                .size(file.getSize())
                .tags(getResourceTags(file))
                .url("https://" + mainBucketName + ".s3.amazonaws.com/" + objectKey)
                .build();
    }
    
    @Recover
    public PresignedResourceResponse recoverGeneratePresignedUrl(Exception e, Resource resource, @Nullable Long expirySeconds) {
    	
        throw new S3Exception("Failed to generate presigned URL after retries for resource " + resource.getId());
    }
    
    @Recover
    public S3Details recoverUploadFile(Exception e, MultipartFile file, String groupId, String userId) {
    	
        throw new S3Exception("Failed to upload file after retries for file " + file.getOriginalFilename());
    }

    
    @Recover
    public String recoverCreateMultipartUpload(Exception e, String objectKey, long fileSize) {
    	
        throw new S3Exception("Failed to create multipart upload after retries for objectKey: " + objectKey);
    }

    @Recover
    public String recoverGeneratePresignedUrlForPart(Exception e, String objectKey, String uploadId, int partNumber, long partSize) {
    	
        throw new S3Exception("Failed to generate presigned URL for part " + partNumber + " after retries.");
    }

    @Recover
    public String recoverUploadPart(Exception e, String objectKey, String uploadId, int partNumber, byte[] partBytes) {
    	
        throw new S3Exception("Failed to upload part " + partNumber + " after retries for " + objectKey);
    }
}
