package com.ril.SB_SAMPLE.filter;


import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;

import com.google.gson.Gson;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
@Order(2)
public class RequestCorelationFilter extends OncePerRequestFilter {

    @Autowired
    private Gson gson;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String requestId = getRequestId(request.getHeader("X-Request-Id"));
            MDC.put("requestId", UUID.randomUUID().toString().replace("-", ""));
            logRequestDetails(request);
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove("requestId");
        }
    }

    private void logRequestDetails(HttpServletRequest request) {
        if (logger.isDebugEnabled()) {
            HttpServletRequest cachedRequest = new ContentCachingRequestWrapper(request);
            Map<String, Object> requestLog = new HashMap<>();
            requestLog.put("url", cachedRequest.getRequestURL());
            requestLog.put("method", cachedRequest.getMethod());
            requestLog.put("headers", getHeaders(cachedRequest));
            requestLog.put("body", getRequestBody(cachedRequest));
            logger.debug(requestLog);
        }
    }

    private Object getRequestBody(HttpServletRequest cachedRequest) {
        try {
            if (cachedRequest.getContentLength() < 4000) {
                return gson.fromJson(cachedRequest.getReader(), Map.class);
            } else {
                return "Request body character count exceeds max limit of 4000";
            }
        } catch (IOException e) {
            return "Unable to read the request body";
        }
    }

    private Map<String, String> getHeaders(HttpServletRequest cachedRequest) {
        Map<String, String> headerMap = new HashMap<>();
        Enumeration<String> requestHeaderNames = cachedRequest.getHeaderNames();
        while (requestHeaderNames.hasMoreElements()) {
            String headerName = requestHeaderNames.nextElement();
            headerMap.put(headerName, cachedRequest.getHeader(headerName));
        }
        return headerMap;
    }

    private String getRequestId(String requestId) {
        if (StringUtils.isEmpty(requestId)) {
            return requestId;
        }
        return UUID.randomUUID().toString().replace("-", "");
    }
}
