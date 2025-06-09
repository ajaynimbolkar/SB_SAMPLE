package com.ril.SB_SAMPLE.service;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import com.ril.SB_SAMPLE.dto.request.MarketingContactPostRequest;
import com.ril.SB_SAMPLE.dto.response.MarketingContactPostResponse;


@Service
public interface MarketingContactService {
    public MarketingContactPostResponse createMarketingContact(HttpHeaders httpHeaders, MarketingContactPostRequest marketingContactPostRequest, String requestId);
}