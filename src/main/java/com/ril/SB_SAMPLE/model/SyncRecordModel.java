package com.ril.SB_SAMPLE.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;
import java.sql.Timestamp;
import jakarta.persistence.Column;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.Entity;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "synced_records")
@Entity
public class SyncRecordModel {
    @Id
    @Column(name = "syn_id")
    private String syncId;

    @Column(name = "crm_record_id")
    private String crmCustomerId;

    @Column(name = "mkt_record_id")
    private String marketingContactId;

    @Column(name = "source_system")
    private String sourceSystem;

    @Column(name = "sync_direction")
    private String synDirection;

    @Column(name = "last_synced_at")
    private Timestamp lastSychedAt;   

    @Column(name = "last_updated_at")
    private Timestamp lastUpdatedAt; 

    @Column(name = "retry_count")
    private Integer retryCount;

    @Column(name = "sync_status")
    private String synStatus;

    @Column(name = "error_message")
    private String errorMessage;
}
