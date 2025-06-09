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


@Service
public class MarketingContactServiceImpl implements MarketingContactService{
    private static final Logger LOGGER = LogManager.getLogger(MarketingContactServiceImpl.class);
    
    @Autowired
    private MarketingContactRepository marketingContactRepository;
    
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

            LOGGER.info("[ {} ] :: ENDED :: [ {} ]", currentMethodName, requestId);
            return marketingContactPostResponse;

        } catch (Exception e) {
            LOGGER.error("[ {} ] :: ERROR :: [ {} ]", currentMethodName, requestId);
        }
        return marketingContactPostResponse;
    }

}
