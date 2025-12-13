package com.teamvault.valueobject;

import java.time.Instant;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GroupAccessMetadataVO {

    private Instant lastAccessed;
    
    private Instant lastWrite;
}