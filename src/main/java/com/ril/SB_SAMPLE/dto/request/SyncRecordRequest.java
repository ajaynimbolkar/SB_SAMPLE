package com.ril.SB_SAMPLE.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SyncRecordRequest {
    private String syncId;
    private String crmCustomerId;
    private String marketingContactId;
    private String sourceSystem;
    private String synDirection;
    private Timestamp lastSynchedAt;
    private Timestamp lastUpdatedAt;
    private Integer retryCount;
    private String synStatus;
    private String errorMessage;
}   
