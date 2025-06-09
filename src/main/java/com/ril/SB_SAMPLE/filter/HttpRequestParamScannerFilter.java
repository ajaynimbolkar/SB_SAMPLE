package com.ril.SB_SAMPLE.filter;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URLDecoder;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.ril.SB_SAMPLE.beans.ErrorObject;
import com.ril.SB_SAMPLE.constants.ApiConstant;
import com.ril.SB_SAMPLE.constants.ErrorConstant;
import com.ril.SB_SAMPLE.dto.response.Response;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class HttpRequestParamScannerFilter implements Filter {
	private static final Logger LOGGER = LogManager.getLogger(HttpRequestParamScannerFilter.class);

	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
			throws IOException, ServletException {
		String currentMethodName = (new Throwable().getStackTrace()[0].getMethodName());
		LOGGER.info("HttpRequestParamScannerFilter :: doFilter  :: Started :: ");
		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) res;
		CachedBodyHttpServletRequestFilter wrappedRequest = new CachedBodyHttpServletRequestFilter(request);
		if (isRequestHarmful(wrappedRequest)) {
			LOGGER.info("[ {} ] :: Harmful request found {}", currentMethodName, request.getRequestURI());
			Enumeration<String> strE = request.getParameterNames();
			String s = null;
			String p = null;
			while (strE.hasMoreElements()) {
				p = strE.nextElement();
				s = request.getParameter(p);
				LOGGER.info(
						"[ {} ] :: In User Filter with Multipart Request Request param name: {} and Param value:{} ",
						currentMethodName, p, s);
			}
			req.setAttribute("message", "Request contains potentially harmful characters");
			LOGGER.info("[ {} ] :: Context path: {}", currentMethodName, request.getContextPath());

			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.setContentType("application/json");

			// You can use Gson or Jackson here; assuming Gson is used
			Gson gson = new Gson();

			ErrorObject errorObject = ErrorObject.builder()
					.errorCode(ErrorConstant.HARMFUL_REQUEST_ERROR.getErrorCode())
					.errorMessage(ErrorConstant.HARMFUL_REQUEST_ERROR.getMessage()).build();
			Response errorResponse = new Response(ApiConstant.BOOLEAN_FALSE,
					ErrorConstant.HARMFUL_REQUEST_ERROR.getHttpStatus().value(), Collections.singletonList(errorObject),
					Collections.emptyMap());

			response.getWriter().write(gson.toJson(errorResponse));
			return;
		} else
			chain.doFilter(wrappedRequest, response);

	}

	private boolean isRequestHarmful(ServletRequest req) {
		LOGGER.info("HttpRequestParamScannerFilter :: isRequestHarmful  :: Started ::");
		String currentMethodName = (new Throwable().getStackTrace()[0].getMethodName());
		HttpServletRequest request = (HttpServletRequest) req;
		Enumeration<String> enrtn = request.getParameterNames();
		LOGGER.error("[ {} ] :: {} ", currentMethodName, enrtn);
		String paraName = null;
		String paraVal[] = null;

		while (enrtn.hasMoreElements()) {
			paraName = enrtn.nextElement();
			paraVal = request.getParameterValues(paraName);
			LOGGER.info("[ {} ] :: Parameter Name: {} and Parameter value: {}", currentMethodName, paraName,
					request.getParameter(paraName));
			if (paraVal != null) {
				for (String str : paraVal) {
					if (!stripXSS(str))
						return true;
				}
			}
		}
		try {
			StringBuilder sb = new StringBuilder();
			BufferedReader reader = request.getReader();
			String line;
			while ((line = reader.readLine()) != null)
				sb.append(line);
			String body = sb.toString();

			if (!body.isEmpty()) {
				ObjectMapper mapper = new ObjectMapper();
				JsonNode json = mapper.readTree(body);
				if (containsXSSInJson(json))
					return true;
			}
		} catch (Exception e) {
			LOGGER.error("JSON body XSS check failed", e);
		}

		LOGGER.info("HttpRequestParamScannerFilter :: isRequestHarmful  :: Ended :: ");
		return false;
	}

	private boolean containsXSSInJson(JsonNode node) {
		if (node.isValueNode()) {
			String value = node.asText();
			if (!stripXSS(value)) {
				return true;
			}
		} else if (node.isObject()) {
			for (Iterator<Map.Entry<String, JsonNode>> it = node.fields(); it.hasNext();) {
				if (containsXSSInJson(it.next().getValue()))
					return true;
			}
		} else if (node.isArray()) {
			for (JsonNode item : node) {
				if (containsXSSInJson(item))
					return true;
			}
		}
		return false;
	}

	@SuppressWarnings("deprecation")
	private boolean stripXSS(String value) {
		LOGGER.info("HttpRequestParamScannerFilter :: stripXSS  :: Started :: ");
		String currentMethodName = (new Throwable().getStackTrace()[0].getMethodName());
		value = URLDecoder.decode(URLDecoder.decode(value));

		if (value != null) {
			value = Normalizer.normalize(value, Normalizer.Form.NFD);

			boolean isNullCharacter = value.contains("\0");
			if (isNullCharacter)
				LOGGER.warn("[ {} ] :: Null character detected in value: {}", currentMethodName, value);

			Pattern scriptPattern = Pattern.compile("<script>(.*?)</script>", Pattern.CASE_INSENSITIVE);
			boolean isScriptPattern = scriptPattern.matcher(value).find();
			if (isScriptPattern)
				LOGGER.warn("[ {} ] :: <script> tag detected in value: {}", currentMethodName, value);

			scriptPattern = Pattern.compile("src[\r\n]*=[\r\n]*\\\'(.*?)\\\'",
					Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
			boolean isSrcTag = scriptPattern.matcher(value).find();
			if (isSrcTag)
				LOGGER.warn("[ {} ] :: src='...' detected in value: {}", currentMethodName, value);

			scriptPattern = Pattern.compile("src[\r\n]*=[\r\n]*\\\"(.*?)\\\"",
					Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
			boolean isSrcTagWithDoubleQuote = scriptPattern.matcher(value).find();
			if (isSrcTagWithDoubleQuote)
				LOGGER.warn("[ {} ] :: src=\"...\" detected in value: {}", currentMethodName, value);

			scriptPattern = Pattern.compile("</script>", Pattern.CASE_INSENSITIVE);
			boolean isScriptCloseTag = scriptPattern.matcher(value).find();
			if (isScriptCloseTag)
				LOGGER.warn("[ {} ] :: </script> tag detected in value: {}", currentMethodName, value);

			scriptPattern = Pattern.compile("<script(.*?)>",
					Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
			boolean isScriptTagWithAttribute = scriptPattern.matcher(value).find();
			if (isScriptTagWithAttribute)
				LOGGER.warn("[ {} ] :: <script ...> tag with attribute detected in value: {}", currentMethodName,
						value);

			scriptPattern = Pattern.compile("eval\\((.*?)\\)",
					Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
			boolean isEval = scriptPattern.matcher(value).find();
			if (isEval)
				LOGGER.warn("[ {} ] :: eval(...) expression detected in value: {}", currentMethodName, value);

			scriptPattern = Pattern.compile("expression\\((.*?)\\)",
					Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
			boolean isExpression = scriptPattern.matcher(value).find();
			if (isExpression)
				LOGGER.warn("[ {} ] :: expression(...) detected in value: {}", currentMethodName, value);

			scriptPattern = Pattern.compile("javascript:", Pattern.CASE_INSENSITIVE);
			boolean isJavaScript = scriptPattern.matcher(value).find();
			if (isJavaScript)
				LOGGER.warn("[ {} ] :: javascript: detected in value: {}", currentMethodName, value);

			scriptPattern = Pattern.compile("vbscript:", Pattern.CASE_INSENSITIVE);
			boolean isVBScript = scriptPattern.matcher(value).find();
			if (isVBScript)
				LOGGER.warn("[ {} ] :: vbscript: detected in value: {}", currentMethodName, value);

			scriptPattern = Pattern.compile("onload(.*?)=",
					Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
			boolean isOnLoad = scriptPattern.matcher(value).find();
			if (isOnLoad)
				LOGGER.warn("[ {} ] :: onload= detected in value: {}", currentMethodName, value);

			LOGGER.info("[ {} ] :: Processed String : {} ", currentMethodName, value);

			boolean isAllowedTags = true;
			if (value.contains("<") || value.contains(">")) {
				Pattern p = Pattern.compile("<([^\\s>/]+)");
				Matcher m = p.matcher(value);
				List<String> stringList = new ArrayList<>();
				while (m.find()) {
					String tag = m.group(1);
					stringList.add(tag);
				}
				isAllowedTags = containsValidHtmlTag(stringList);
				if (!isAllowedTags)
					LOGGER.warn("[ {} ] :: Disallowed HTML tag(s) detected in value: {}", currentMethodName, value);
			}

			return (!isNullCharacter && !isScriptPattern && !isSrcTag && !isSrcTagWithDoubleQuote && !isScriptCloseTag
					&& !isScriptTagWithAttribute && !isEval && !isExpression && !isJavaScript && !isVBScript
					&& !isOnLoad) && isAllowedTags;
		}

		LOGGER.info("HttpRequestParamScannerFilter :: stripXSS  :: Ended :: ");
		LOGGER.warn("[ {} ] :: Detected potentially harmful value: {}", currentMethodName, value);
		return false;
	}

	public boolean containsValidHtmlTag(List<String> list) {
		LOGGER.info("HttpRequestParamScannerFilter :: containsValidHtmlTag  :: Started :: ");
		List<String> stringList = Arrays.asList("b", "i", "u", "a", "span", "sup", "sub", "hr", "br", "strong", "em",
				"p");
		LOGGER.info("HttpRequestParamScannerFilter :: containsValidHtmlTag  :: Ended :: ");
		return list.stream().allMatch(stringList::contains);

	}

}