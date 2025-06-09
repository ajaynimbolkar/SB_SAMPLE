package com.ril.SB_SAMPLE.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ril.SB_SAMPLE.constants.ApiConstant;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.RequestHeader;
import org.slf4j.MDC;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import com.ril.SB_SAMPLE.dto.response.MarketingContactPostResponse;
import com.ril.SB_SAMPLE.dto.request.MarketingContactPostRequest;   
import com.ril.SB_SAMPLE.dto.response.ApiResponse;
import com.ril.SB_SAMPLE.service.MarketingContactService;

@RestController 
@RequestMapping(ApiConstant.MarketRequestMapping)
public class SampleController {
    Logger LOGGER = LogManager.getLogger(SampleController.class);
    
    @Autowired
    private MarketingContactService marketingContactService;

    @PostMapping("")
    public MarketingContactPostResponse createSamplePost(@RequestHeader HttpHeaders httpHeaders,
        @RequestBody MarketingContactPostRequest marketingContactPostRequest
    ) {
        String requestId = MDC.get(ApiConstant.REQUEST_ID);
        String currentMethodName = new Throwable().getStackTrace()[0].getMethodName();
        LOGGER.info("[ {} ] :: STARTED :: [ {} ]", 
                                currentMethodName,
                                requestId);

        MarketingContactPostResponse  samplePostResponse = marketingContactService.createMarketingContact(httpHeaders, marketingContactPostRequest, requestId);

        LOGGER.info("[ {} ] :: ENDED :: [ {} ]", 
                                currentMethodName,
                                requestId);
        return samplePostResponse;
    }


}






