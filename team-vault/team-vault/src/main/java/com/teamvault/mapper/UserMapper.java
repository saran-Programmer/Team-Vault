package com.teamvault.mapper;

import com.teamvault.DTO.SignUpRequest;
import com.teamvault.entity.User;
import com.teamvault.enums.UserRole;
import com.teamvault.valueobject.ContactVO;
import com.teamvault.valueobject.CredentialsVO;
import com.teamvault.valueobject.NameVO;

public class UserMapper {

    private UserMapper() {}

    public static User toUserEntity(SignUpRequest dto) {
        return User.builder()
                .name(NameVO.builder()
                        .firstName(dto.getFirstName())
                        .middleName(dto.getMiddleName())
                        .lastName(dto.getLastName())
                        .build())
                .credentials(CredentialsVO.builder()
                        .userName(dto.getUsername())
                        .email(dto.getPrimaryEmail())
                        .password(dto.getPassword())
                        .build())
                .contact(ContactVO.builder()
                        .countryCode(dto.getPhoneCountryCode())
                        .phoneNumber(dto.getPhoneNumber())
                        .secondaryEmail(dto.getSecondaryEmail())
                        .build())
                .userRole(UserRole.USER)
                .build();
    }
}
