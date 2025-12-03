package com.teamvault.valueobject;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ContactVO {

    private String countryCode;
    
    private String phoneNumber;
    
    private String secondaryEmail;
}
