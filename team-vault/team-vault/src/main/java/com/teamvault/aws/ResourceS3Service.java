package com.teamvault.aws;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
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
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectTaggingRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.model.Tag;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResourceS3Service {

    private final S3Client s3Client;
    
    private final S3Presigner s3Presigner;

    @Value("${aws.resource-bucket-name}")
    private String mainBucketName;

    @Value("${aws.deleted-resource-bucket-name}")
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

        PutObjectResponse response = s3Client.putObject(
                putRequest,
                RequestBody.fromInputStream(
                        file.getInputStream(),
                        file.getSize()));

        return S3Details.builder()
                .bucketName(mainBucketName)
                .fileName(fileName)
                .contentType(file.getContentType())
                .size(file.getSize())
                .tags(tags)
                .url("https://" + mainBucketName + ".s3.amazonaws.com/" + objectKey)
                .versionId(response.versionId())
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

    @Recover
    public PresignedResourceResponse recoverGeneratePresignedUrl(Exception e, Resource resource, @Nullable Long expirySeconds) {
    	
        throw new S3Exception("Failed to generate presigned URL after retries for resource " + resource.getId());
    }
    
    @Recover
    public S3Details recoverUploadFile(Exception e, MultipartFile file, String groupId, String userId) {
    	
        throw new S3Exception("Failed to upload file after retries for file " + file.getOriginalFilename());
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
                .contentType(resource.getS3Details().getContentType())
                .size(resource.getS3Details().getSize())
                .url("https://" + deletedBucketName + ".s3.amazonaws.com/" + objectKey)
                .versionId(resource.getS3Details().getVersionId())
                .tags(resource.getS3Details().getTags())
                .build();
    }

    private Map<String, String> getResourceTags(MultipartFile file) {
    	
        Map<String, String> tags = new HashMap<>();
        
        long sizeInBytes = file.getSize();
        
        String sizeTag;

        if (sizeInBytes < 5 * 1024 * 1024) sizeTag = "SMALL";
        else if (sizeInBytes <= 15 * 1024 * 1024) sizeTag = "MEDIUM";
        else sizeTag = "LARGE";

        tags.put("size", sizeTag);

        return tags;
    }

    private String buildTagString(Map<String, String> tags) {
    	
        StringBuilder sb = new StringBuilder();
        tags.forEach((key, value) -> sb.append(key).append("=").append(value).append("&"));
        if (sb.length() > 0) sb.setLength(sb.length() - 1);
        return sb.toString();
    }
}
