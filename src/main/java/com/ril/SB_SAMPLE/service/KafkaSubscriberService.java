package com.ril.SB_SAMPLE.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ril.SB_SAMPLE.dto.request.MarketingContactPostRequest;
import com.ril.SB_SAMPLE.model.SyncRecordModel;
import java.sql.Timestamp;
import com.ril.SB_SAMPLE.repository.SyncRecordRepository;
import lombok.Data;
import reactor.core.publisher.Mono;
import com.ril.SB_SAMPLE.dto.response.ApiResponse;
import com.ril.SB_SAMPLE.constants.ErrorConstant;
import com.ril.SB_SAMPLE.constants.ApiConstant;
import com.ril.SB_SAMPLE.exception.SynDataNotFoundException;

/**
 * Service to consume data from the kafka topics.
 */
@Data
@Service
public class KafkaSubscriberService {
    private static final Logger LOGGER = LogManager.getLogger(KafkaSubscriberService.class);

    // Group id of topic.
    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    // Topic name.
    @Value("${app.topic.ResultTopic}")
    public String ResultTopic;

    @Autowired
    private SyncRecordRepository syncRecordRepository;

    @Autowired
    private ApiService apiService;

    // Kafka listener to consume data from the topic.
    @KafkaListener(topics = "#{__listener.ResultTopic}", groupId = "#{__listener.groupId}")
    public void listen(@Payload String publishMessage) {
        // Get the current method name.
        String currentMethodName = new Throwable().getStackTrace()[0].getMethodName();

        // Log the start of the method.
        LOGGER.info("[ {} ] ::  STARTED", currentMethodName);

        // Object mapper to convert the json to object.
        ObjectMapper mapper = new ObjectMapper();

        // Root node to store the json data.
        JsonNode rootNode = null;

        // Try to parse the json data.
        try {
            // Parse the json data.
            rootNode = mapper.readTree(publishMessage);

            // Get the payload node.
            JsonNode payloadNode = rootNode.path("payload");

            // Get the after node.
            JsonNode afterNode = payloadNode.path("after");

            // Log the after node.
            LOGGER.info("Record:: [ {} ]", afterNode);


            String crmCustomerId = afterNode.path("crm_customer_id").asText().replace("crm_", "");

            SyncRecordModel syncRecordModel1 = syncRecordRepository.findBySyncId("sync-" + crmCustomerId);
            LOGGER.info("kafka syncRecordModel1 :: [ {} ]", syncRecordModel1);
            // If sync record is not found then return.
            if(syncRecordModel1 != null) {
                String syncId = "sync-" + crmCustomerId;
                SyncRecordModel updatedSyncRecord = syncRecordRepository.findById(syncId).orElseThrow(() -> new SynDataNotFoundException(ErrorConstant.SYNC_DATA_NOT_FOUND));
                updatedSyncRecord.setCrmCustomerId("crm_" + crmCustomerId);
                updatedSyncRecord.setSynStatus("SUCCESS");
                updatedSyncRecord.setLastSychedAt(new Timestamp(System.currentTimeMillis()));
                syncRecordRepository.save(updatedSyncRecord);
                return;
            }

            LOGGER.info("CRM Customer ID :: [ {} ]", crmCustomerId);
            String firstName = afterNode.path("first_name").asText();
            String lastName = afterNode.path("last_name").asText();
            String email = afterNode.path("email").asText();
            String phoneNumber = afterNode.path("phone_number").asText();

            // Once record is created in crm then we need to update sync record with status success.
            SyncRecordModel syncRecordModel = SyncRecordModel.builder()
                .syncId("sync-" + crmCustomerId)
                .crmCustomerId("crm_" + crmCustomerId)
                .sourceSystem("CRM")
                .synDirection("CRM-MARKETING")
                .synStatus("PENDING")
                .lastUpdatedAt(new Timestamp(System.currentTimeMillis()))
                .lastSychedAt(null)
                .retryCount(0)
                .errorMessage(null)
                .build();

            // Save the sync record.
            try {
                LOGGER.info("Before saving sync record");
                syncRecordRepository.save(syncRecordModel);
                LOGGER.info("After saving sync record");
            } catch (Exception e) {
                LOGGER.error("Exception during save: ", e);
            }
            // Log the sync record.
            LOGGER.info("Sync Record :: [ {} ]", syncRecordModel);

            // Create the marketing contact Request.
            MarketingContactPostRequest marketingContactPostRequest = MarketingContactPostRequest.builder()
                .marketingContactId("mkt_" + crmCustomerId)
                .fullName(firstName + " " + lastName)
                .emailAddress(email)
                .mobileNumber(phoneNumber)
                .build();
            LOGGER.info("Marketing Contact Request :: [ {} ]", marketingContactPostRequest);

            // Convert the request to object for API call
            Object requestPayload = mapper.convertValue(marketingContactPostRequest, Object.class);
            LOGGER.info("Request Payload :: [ {} ]", requestPayload);
            // Log the marketing contact request.

            // Call the api to create the marketing contact.
            Mono<ApiResponse<String>> response = null;
            response = apiService.callPostApi(ApiConstant.MARKETING_CONTACT_POST_URL, requestPayload);
            LOGGER.info("Mono Response :: [ {} ]", response);
            // Subscribe to the response.
            if (response != null) {
                response.subscribe(apiResponse -> {
                    if(apiResponse.getStatusCode() == 200) {
                        try {
                            // Get the body.
                            String body = apiResponse.getBody();
                            // Parse the body.
                            JsonNode jsonNode = mapper.readTree(body);
                            // Get the id.      
                            String marketingContactId = jsonNode.path("id").asText();
                            // Log the response.
                            LOGGER.info("Response :: [ {} ]", marketingContactId);
                            // Update the sync record.
                            String syncId = "sync-" + crmCustomerId;
                            // Find the sync record and update the status.
                            SyncRecordModel updatedSyncRecord = syncRecordRepository.findById(syncId).orElseThrow(() -> new SynDataNotFoundException(ErrorConstant.SYNC_DATA_NOT_FOUND));
                            updatedSyncRecord.setMarketingContactId(marketingContactId);
                            updatedSyncRecord.setSynStatus("SUCCESS");
                            updatedSyncRecord.setLastSychedAt(new Timestamp(System.currentTimeMillis()));
                            syncRecordRepository.save(updatedSyncRecord);

                            // Log the updated sync record.
                            LOGGER.info("Updated Sync Record :: [ {} ]", updatedSyncRecord);
                            
                        } catch (JsonProcessingException e) {
                            // Log the exception.
                            LOGGER.error("[ {} ] :: Error parsing response body :: [ {} ]", currentMethodName, e.getMessage());
                        }
                    } else {
                        // Log the exception.
                        LOGGER.error("[ {} ] :: Response not found :: [ {} ]", currentMethodName, apiResponse.getBody());
                    }
                });
            }
        } catch (JsonProcessingException e) {
            // Log the exception.
            LOGGER.error("[ {} ] :: ObjectMapper Exception :: [ {} ]", currentMethodName, e.getMessage());
        }
        LOGGER.info("[ {} ] :: ENDED ", currentMethodName);
    } // listen
}
