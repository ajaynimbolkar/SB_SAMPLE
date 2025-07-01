package com.ril.SB_SAMPLE.service.impl;

import org.springframework.stereotype.Service;
import com.ril.SB_SAMPLE.service.MarketingContactService;
import com.ril.SB_SAMPLE.dto.response.MarketingContactPostResponse;
import com.ril.SB_SAMPLE.dto.request.MarketingContactPostRequest;
import org.springframework.http.HttpHeaders;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.ril.SB_SAMPLE.model.MarketingContactModel;

import java.sql.Timestamp;
import com.ril.SB_SAMPLE.repository.MarketingContactRepository;
import org.springframework.beans.factory.annotation.Autowired;
import com.ril.SB_SAMPLE.repository.SyncRecordRepository;
import com.ril.SB_SAMPLE.repository.CrmCustomerRepository;
import com.ril.SB_SAMPLE.model.SyncRecordModel;
import com.ril.SB_SAMPLE.model.CrmCustomerModel;
import com.ril.SB_SAMPLE.exception.SynDataNotFoundException;
import com.ril.SB_SAMPLE.constants.ErrorConstant;
   
@Service
public class MarketingContactServiceImpl implements MarketingContactService{
    private static final Logger LOGGER = LogManager.getLogger(MarketingContactServiceImpl.class);
    
    @Autowired
    private MarketingContactRepository marketingContactRepository;

    @Autowired
    private SyncRecordRepository syncRecordRepository;

    @Autowired
    private CrmCustomerRepository crmCustomerRepository;
    
    @Override
    public MarketingContactPostResponse createMarketingContact(HttpHeaders httpHeaders, MarketingContactPostRequest marketingContactPostRequest, String requestId) {
        String currentMethodName = new Throwable().getStackTrace()[0].getMethodName();
        LOGGER.info("[ {} ] :: STARTED :: [ {} ]", 
                                currentMethodName,
                                requestId);
        MarketingContactPostResponse marketingContactPostResponse = null;
        try {
            // Create the marketing contact.
            MarketingContactModel marketingContactModel = MarketingContactModel.builder()
                .marketingContactId(marketingContactPostRequest.getMarketingContactId())
                .fullName(marketingContactPostRequest.getFullName())
                .emailAddress(marketingContactPostRequest.getEmailAddress())
                .mobileNumber(marketingContactPostRequest.getMobileNumber())
                .lastUpdatedAt(new Timestamp(System.currentTimeMillis()))
                .build();

            // Save the marketing contact.
            marketingContactRepository.save(marketingContactModel);

            marketingContactPostResponse = MarketingContactPostResponse.builder()
                .id(marketingContactModel.getMarketingContactId())
                .message("Marketing Contact created successfully")
                .build();

            // If marketing contact is created successfully then we need to create a crm customer and update the sync record with status pending.
            if(marketingContactModel.getMarketingContactId() != null) {
                LOGGER.info("mktCustomerId :: [ {} ]", marketingContactModel.getMarketingContactId());
                // Get the marketing contact id.
                String mktCustomerId = marketingContactPostRequest.getMarketingContactId().replace("mkt_", "");
                LOGGER.info("mktCustomerId1 :: [ {} ]", mktCustomerId);

                // Get data not present in sync table from it will cause circular problem.apiService    
                SyncRecordModel syncRecordModel1 = syncRecordRepository.findBySyncId("sync-" + mktCustomerId);
                LOGGER.info("syncRecordModel1 :: [ {} ]", syncRecordModel1);

                if(syncRecordModel1 == null) {
                    String[] parts = marketingContactPostRequest.getFullName().split(" ");
                    String crmFirstName = parts[0];
                    String crmLastName = parts[1];
                    LOGGER.info("crmFirstName :: [ {} ]", crmFirstName);
                    LOGGER.info("crmLastName :: [ {} ]", crmLastName);
                    String crmEmail = marketingContactPostRequest.getEmailAddress();
                    String crmPhone = marketingContactPostRequest.getMobileNumber();
                    String crmCustomerId =  "crm_"  + mktCustomerId;

                    // Once record is created in marketing then we need to update sync record with status pending.
                    SyncRecordModel syncRecordModel = SyncRecordModel.builder()
                    .syncId("sync-" + mktCustomerId)
                    .marketingContactId("mkt_" + mktCustomerId)
                    .sourceSystem("MARKETING")
                    .synDirection("MARKETING-CRM")
                    .synStatus("PENDING")
                    .lastUpdatedAt(new Timestamp(System.currentTimeMillis()))
                    .lastSychedAt(null)
                    .retryCount(0)
                    .errorMessage(null)
                    .build();
                    LOGGER.info("syncRecordModel :: [ {} ]", syncRecordModel);

                    // Save the sync record.
                    try {
                        LOGGER.info("Before saving sync record for mkt to crm");
                        syncRecordRepository.save(syncRecordModel);
                        LOGGER.info("After saving sync record for mkt to crm");
                    } catch (Exception e) {
                        LOGGER.error("Exception during save: ", e);
                    }

                    // Log the sync record.
                    LOGGER.info("Sync Record :: [ {} ]", syncRecordModel);

                    // Create the crm customer.
                    CrmCustomerModel crmCustomerModel = CrmCustomerModel.builder()
                        .crmCustomerId("crm_" + mktCustomerId)
                        .firstName(crmFirstName)
                        .lastName(crmLastName)
                        .email(crmEmail)
                        .phoneNumber(crmPhone)
                        .lastUpdatedAt(new Timestamp(System.currentTimeMillis()))
                        .build();

                    // Save the sync record.
                    try {
                        LOGGER.info("Before Data save to crm customer");
                        crmCustomerRepository.save(crmCustomerModel);

                        String syncId = "sync-" + crmCustomerId;
                        LOGGER.info("syncId :: [ {} ]", syncId);
                        // Find the sync record and update the status.
                        SyncRecordModel updatedSyncRecord = syncRecordRepository.findById(syncId).orElseThrow(() -> new SynDataNotFoundException(ErrorConstant.SYNC_DATA_NOT_FOUND));
                        updatedSyncRecord.setCrmCustomerId("crm_" + crmCustomerId);
                        updatedSyncRecord.setSynStatus("SUCCESS");
                        updatedSyncRecord.setLastSychedAt(new Timestamp(System.currentTimeMillis()));
                        syncRecordRepository.save(updatedSyncRecord);
                        LOGGER.info("After Data save to crm customer");
                    } catch (Exception e) {
                        LOGGER.error("Exception during save: ", e);
                    }
                }               
            }

            LOGGER.info("[ {} ] :: ENDED :: [ {} ]", currentMethodName, requestId);
            return marketingContactPostResponse;

        } catch (Exception e) {
            LOGGER.error("[ {} ] :: ERROR :: [ {} ]", currentMethodName, requestId);
        }
        return marketingContactPostResponse;
    }

}
