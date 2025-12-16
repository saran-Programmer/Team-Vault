package com.teamvault.aws;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.teamvault.DTO.PresignedResourceResponse;
import com.teamvault.entity.Resource;
import com.teamvault.models.S3Details;

import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
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

@Service
@RequiredArgsConstructor
public class ResourceS3Service {

    private final S3Client s3Client;
    
    private final S3Presigner s3Presigner;

    @Value("${aws.resource-bucket-name}")
    private String bucketName;

    public S3Details uploadFile(MultipartFile file, String groupId, String userId) {

        Map<String, String> tags = getResourceTags(file);

        String fileName = file.getOriginalFilename();
        
        String objectKey = groupId + "/" + userId + "/" + fileName;

        try {

            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
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
                    .bucketName(bucketName)
                    .fileName(fileName)
                    .contentType(file.getContentType())
                    .size(file.getSize())
                    .tags(tags)
                    .url("https://" + bucketName + ".s3.amazonaws.com/" + objectKey)
                    .versionId(response.versionId())
                    .build();

        } catch (IOException e) {
        	
            throw new RuntimeException("Failed to upload file to S3", e);
        }
    }
    
    public PresignedResourceResponse generatePresignedUrl(Resource resource, @Nullable Long expirySeconds) {

        long effectiveExpiry = (expirySeconds == null || expirySeconds <= 0) ? 600L : expirySeconds;

        String objectKey = resource.getGroup().getId() + "/" + resource.getUser().getId() + "/" + resource.getS3Details().getFileName();

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
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
                        .bucket(bucketName)
                        .key(objectKey)
                        .build());

        String versionId = headObject.versionId(); 

        Map<String, String> tags =
                s3Client.getObjectTagging(GetObjectTaggingRequest.builder()
                        .bucket(bucketName)
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
        		.groupMemberId(resource.getGroupMemberVO().getId())
                .presignedUrl(presignedRequest.url().toString())
                .versionId(versionId)
                .tags(tags)
                .expiresInSeconds(effectiveExpiry)
                .resourceVisiblity(resource.getResourceVisiblity())
                .timestamp(Instant.now())
                .build();

    }

    public void markObjectAsDeleted(Resource resource) {

        String objectKey = resource.getGroup().getId() + "/" + resource.getUser().getId() + "/" + resource.getS3Details().getFileName();

        Map<String, String> existingTags =
                s3Client.getObjectTagging(GetObjectTaggingRequest.builder()
                        .bucket(bucketName)
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
                .bucket(bucketName)
                .key(objectKey)
                .tagging(tagging -> tagging.tagSet(tagSet)));
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
