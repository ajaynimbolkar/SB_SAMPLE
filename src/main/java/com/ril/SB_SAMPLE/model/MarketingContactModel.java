package com.ril.SB_SAMPLE.model;

import java.sql.Timestamp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;
import jakarta.persistence.Column;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.Entity;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "marketing_contacts")
@Entity
public class MarketingContactModel {
    @Id
    @Column(name = "mkt_contact_id")
    private String marketingContactId;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "email_address")
    private String emailAddress;

    @Column(name = "mobile")
    private String mobileNumber;

    @Column(name = "last_updated_at")
    private Timestamp lastUpdatedAt;
}
