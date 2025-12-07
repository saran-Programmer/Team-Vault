package com.teamvault.valueobject;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupStatisticsVO {

    private int users; 
    
    @Builder.Default
    // when creating a group a default admin will be added
    private int admins = 1;
    
    private int superAdmins;
    
    private int posts;
    
    private int members;
}