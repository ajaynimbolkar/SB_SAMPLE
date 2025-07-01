package com.ril.SB_SAMPLE.model;

import java.sql.Timestamp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;
import jakarta.persistence.Column;
import jakarta.persistence.Table;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "crm_customers")
@Entity
public class CrmCustomerModel {
    @Id
    @Column(name = "crm_customer_id")
    private String crmCustomerId;

    @Column(name = "first_name")
    private String firstName;
    
    @Column(name = "last_name")
    private String lastName;    
    
    @Column(name = "email")
    private String email;
    
    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "last_updated_at")
    private Timestamp lastUpdatedAt;
}
