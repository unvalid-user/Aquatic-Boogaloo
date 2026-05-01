package com.example.aquaticboogaloo.dto.response;

import com.example.aquaticboogaloo.entity.enums.JoinRequestStatus;
import lombok.Data;

@Data
public class JoinRequestResponse {
    Long id;
    UserResponse user;
    JoinRequestStatus status;
    UserResponse rejectedBy;
}
