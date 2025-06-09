package com.ril.SB_SAMPLE.dto.request;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MarketingContactPostRequest {
    private String marketingContactId;
    private String fullName;
    private String emailAddress;
    private String mobileNumber;
}
